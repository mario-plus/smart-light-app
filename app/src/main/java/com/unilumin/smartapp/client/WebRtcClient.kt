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

    // 单线程池，确保 WebRTC 操作线程安全
    private val rtcExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null

    // 缓存 Track 和 Sink，用于处理 UI 加载时序
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
                        // 关键：禁用网络监控，防止内网/VPN 环境下连接检查失败
                        disableNetworkMonitor = true
                    })
                    .createPeerConnectionFactory()
                Log.d(TAG, "WebRTC Factory Initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Init Failed", e)
            }
        }
    }

    /**
     * 绑定渲染器：将视频流输出到 SurfaceViewRenderer
     */
    fun setRemoteRender(sink: VideoSink) {
        rtcExecutor.execute {
            this.remoteSink = sink
            // 如果在 UI 加载前已经收到了视频流，立即绑定
            if (remoteVideoTrack != null) {
                try {
                    remoteVideoTrack?.addSink(sink)
                    Log.d(TAG, "Renderer attached immediately")
                } catch (e: Exception) {
                    Log.e(TAG, "Attach Error", e)
                }
            }
        }
    }

    /**
     * 解绑渲染器：停止向 SurfaceViewRenderer 发送数据
     * 必须在 View 销毁前调用，否则会导致 "Dropping frame" 日志刷屏
     */
    fun removeRemoteRender() {
        rtcExecutor.execute {
            remoteSink?.let { sink ->
                remoteVideoTrack?.removeSink(sink)
                Log.d(TAG, "Renderer detached from track")
            }
            remoteSink = null
        }
    }

    suspend fun createOffer(): String = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            rtcExecutor.execute {
                try {
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
                        override fun onConnectionChange(state: PeerConnection.PeerConnectionState?) {
                            Log.d(TAG, "Connection State: $state")
                        }

                        // 关键：当收到视频流时回调
                        override fun onTrack(transceiver: RtpTransceiver?) {
                            val track = transceiver?.receiver?.track()
                            if (track is VideoTrack) {
                                Log.d(TAG, "Received Video Track")
                                remoteVideoTrack = track
                                remoteSink?.let { sink ->
                                    track.addSink(sink)
                                    Log.d(TAG, "Renderer attached in onTrack")
                                }
                            }
                        }
                    })

                    // 拉流模式：只收不发
                    val recvOnly = RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
                    peerConnection?.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO, recvOnly)

                    // 禁用音频，防止 ZLM 报 pt:50 错误（如果摄像头音频格式不兼容）
                    val constraints = MediaConstraints().apply {
                        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
                        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                    }

                    peerConnection?.createOffer(object : SimpleSdpObserver() {
                        override fun onCreateSuccess(sdp: SessionDescription?) {
                            if (sdp == null) return
                            // 保持原始 SDP，不修改，交给 ViewModel 以 raw string 发送
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
            remoteSink?.let { remoteVideoTrack?.removeSink(it) }
            remoteVideoTrack = null
            remoteSink = null
            peerConnection?.close()
            peerConnection?.dispose()
            peerConnection = null
        }
    }
}

// 整个项目中只保留这一处 SimpleSdpObserver 定义
open class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription?) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(error: String?) {}
    override fun onSetFailure(error: String?) { Log.e("WebRtcClient", "SDP Error: $error") }
}