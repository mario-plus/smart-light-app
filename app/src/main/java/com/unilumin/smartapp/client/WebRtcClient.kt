package com.unilumin.smartapp.client

import android.content.Context
import java.util.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.webrtc.PeerConnectionFactory
import org.webrtc.PeerConnection
import org.webrtc.EglBase
import org.webrtc.SessionDescription
import org.webrtc.SdpObserver
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.DataChannel
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.MediaConstraints
import org.webrtc.MediaStreamTrack
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.DefaultVideoDecoderFactory

class WebRtcClient(
    private val context: Context,
    private val rootEglBase: EglBase
) {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null

    init {
        initPeerConnectionFactory(context)
        peerConnectionFactory = createPeerConnectionFactory()
    }

    private fun initPeerConnectionFactory(context: Context) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .createPeerConnectionFactory()
    }

    /**
     * 第一步：创建本地 PeerConnection 并生成 Offer SDP
     */
    suspend fun createOffer(): String = suspendCancellableCoroutine { continuation ->
        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UnifiedPlan
        }

        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(p0: IceCandidate?) {} // 实际中需要 trickle ICE 逻辑，此处简化
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(p0: MediaStream?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
            override fun onTrack(transceiver: RtpTransceiver?) {
                // 监听远程轨道，在此处处理视频渲染
            }
        })

        // 添加音视频收发器 (Play 模式)
        peerConnection?.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO, RtpTransceiver.RtpTransceiverInit(RtpTransceiver.Direction.RECV_ONLY))
        peerConnection?.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO, RtpTransceiver.RtpTransceiverInit(RtpTransceiver.Direction.RECV_ONLY))

        val constraints = MediaConstraints()
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)
                if (continuation.isActive) continuation.resume(sdp.description)
            }
            override fun onCreateFailure(s: String?) {
                if (continuation.isActive) continuation.resumeWithException(Exception(s))
            }
            override fun onSetSuccess() {}
            override fun onSetFailure(s: String?) {}
        }, constraints)
    }

    /**
     * 第二步：收到服务器返回的 Answer 后设置远程描述
     */
    fun setRemoteDescription(answerSdp: String) {
        val sdp = SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
        peerConnection?.setRemoteDescription(SimpleSdpObserver(), sdp)
    }

    fun release() {
        peerConnection?.close()
        peerConnectionFactory?.dispose()
    }
}

// 辅助类：简化观察者回调
open class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(p0: SessionDescription?) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(p0: String?) {}
    override fun onSetFailure(p0: String?) {}
}