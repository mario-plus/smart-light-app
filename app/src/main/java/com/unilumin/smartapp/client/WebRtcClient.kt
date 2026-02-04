package com.unilumin.smartapp.client

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoSink
import org.webrtc.VideoTrack
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WebRtcClient(
    context: Context,
    private val rootEglBase: EglBase
) {
    private val appContext = context.applicationContext
    private val TAG = "WebRtcClient"

    // 使用单线程池确保所有 WebRTC 操作按顺序在同一线程执行，避免竞态
    private val rtcExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null

    // 缓存 Track 和 Sink
    private var remoteVideoTrack: VideoTrack? = null
    private var remoteSink: VideoSink? = null

    init {
        rtcExecutor.execute {
            try {
                PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(appContext)
                        .setEnableInternalTracer(false)
                        .createInitializationOptions()
                )

                val encoderFactory = DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true)
                val decoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)

                peerConnectionFactory = PeerConnectionFactory.builder()
                    .setVideoEncoderFactory(encoderFactory)
                    .setVideoDecoderFactory(decoderFactory)
                    .setOptions(PeerConnectionFactory.Options().apply {
                        disableNetworkMonitor = true
                    })
                    .createPeerConnectionFactory()
            } catch (e: Exception) {
                Log.e(TAG, "PeerConnectionFactory Init Error", e)
            }
        }
    }

    fun setRemoteRender(sink: VideoSink) {
        rtcExecutor.execute {
            this.remoteSink = sink
            if (remoteVideoTrack != null) {
                try {
                    remoteVideoTrack?.addSink(sink)
                } catch (e: Exception) {
                    Log.e(TAG, "Attach Sink Error", e)
                }
            }
        }
    }

    /**
     * 解绑渲染器，防止 "Dropping frame"
     */
    fun removeRemoteRender() {
        rtcExecutor.execute {
            remoteSink?.let { sink ->
                remoteVideoTrack?.removeSink(sink)
            }
            remoteSink = null
        }
    }

    suspend fun createOffer(): String = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            rtcExecutor.execute {
                try {
                    // 双重保险：如果之前的链接没释放，先强制释放
                    if (peerConnection != null) {
                        Log.w(TAG, "peerConnection was not null, disposing before new offer")
                        peerConnection?.dispose()
                        peerConnection = null
                    }

                    val factory = peerConnectionFactory
                    if (factory == null) {
                        if (continuation.isActive) continuation.resumeWithException(IllegalStateException("Factory is null"))
                        return@execute
                    }

                    val iceServers = listOf(
                        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
                    )

                    val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
                        sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
                        continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
                    }

                    peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
                        override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
                        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {}
                        override fun onIceConnectionReceivingChange(p0: Boolean) {}
                        override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
                        override fun onIceCandidate(p0: IceCandidate?) {}
                        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
                        override fun onAddStream(p0: MediaStream?) {}
                        override fun onRemoveStream(p0: MediaStream?) {}
                        override fun onDataChannel(p0: DataChannel?) {}
                        override fun onRenegotiationNeeded() {}
                        override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
                        override fun onConnectionChange(state: PeerConnection.PeerConnectionState?) {}

                        override fun onTrack(transceiver: RtpTransceiver?) {
                            val track = transceiver?.receiver?.track()
                            if (track is VideoTrack) {
                                remoteVideoTrack = track
                                remoteSink?.let { sink -> track.addSink(sink) }
                            }
                        }
                    })

                    val recvOnly = RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
                    peerConnection?.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO, recvOnly)

                    val constraints = MediaConstraints().apply {
                        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
                        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                    }

                    peerConnection?.createOffer(object : SimpleSdpObserver() {
                        override fun onCreateSuccess(sdp: SessionDescription?) {
                            if (sdp == null) return
                            val rawSdp = sdp.description

                            peerConnection?.setLocalDescription(object : SimpleSdpObserver() {
                                override fun onSetSuccess() {
                                    if (continuation.isActive) continuation.resume(rawSdp)
                                }
                                override fun onSetFailure(e: String?) {
                                    if (continuation.isActive) continuation.resumeWithException(Exception(e))
                                }
                            }, sdp)
                        }

                        override fun onCreateFailure(e: String?) {
                            if (continuation.isActive) continuation.resumeWithException(Exception(e))
                        }
                    }, constraints)

                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resumeWithException(e)
                }
            }
        }
    }

    fun setRemoteDescription(answerSdp: String) {
        rtcExecutor.execute {
            val sdp = SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
            peerConnection?.setRemoteDescription(SimpleSdpObserver(), sdp)
        }
    }

    fun release() {
        rtcExecutor.execute {
            try {
                Log.d(TAG, "Start releasing WebRTC resources...")
                // 1. 先解除渲染绑定，防止渲染线程在资源销毁时尝试访问
                remoteSink?.let { remoteVideoTrack?.removeSink(it) }
                remoteVideoTrack = null
                remoteSink = null

                // 2. 关闭 PeerConnection
                peerConnection?.close()
                peerConnection?.dispose()
                peerConnection = null
                Log.d(TAG, "WebRTC resources released successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Release Error", e)
            }
        }
    }
}

open class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription?) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(error: String?) {}
    override fun onSetFailure(error: String?) { Log.e("WebRtcClient", "SDP Error: $error") }
}