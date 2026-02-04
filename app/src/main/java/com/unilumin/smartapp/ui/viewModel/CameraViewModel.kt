package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.WebRtcClient
import com.unilumin.smartapp.client.data.WebRTCResponse
import com.unilumin.smartapp.client.service.DeviceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.webrtc.EglBase
import org.webrtc.VideoFrame
import org.webrtc.VideoSink

/**
 * 全局单例 EGL 管理器
 * 防止频繁创建销毁 EGLContext 导致 Native 层崩溃
 */
object EglManager {
    private var eglBase: EglBase? = null

    @Synchronized
    fun getEglBase(): EglBase {
        if (eglBase == null) {
            eglBase = EglBase.create()
        }
        return eglBase!!
    }

    @Synchronized
    fun release() {
        // 通常不需要手动释放，随进程死亡即可。
        // 如果必须释放，确保所有 Renderer 都已销毁
        if (eglBase != null) {
            try {
                eglBase?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            eglBase = null
        }
    }
}

class CameraViewModel(
    retrofitClient: RetrofitClient, application: Application
) : AndroidViewModel(application) {

    private val deviceService = retrofitClient.getService(DeviceService::class.java)
    val context = getApplication<Application>()

    private val _webRtcSdp = MutableStateFlow<WebRTCResponse?>(null)
    val webRtcSdp = _webRtcSdp.asStateFlow()

    private val _currentPlayingId = MutableStateFlow<Long?>(null)
    val currentPlayingId = _currentPlayingId.asStateFlow()

    private val _isSwitching = MutableStateFlow(false)
    val isSwitching = _isSwitching.asStateFlow()

    // 使用全局单例 EglBase
    private val rootEglBase = EglManager.getEglBase()

    // WebRtcClient 懒加载，注意不要频繁置空它
    private var webRtcClient: WebRtcClient? = null

    // 代理渲染器：核心防崩溃机制，隔离 Native 回调
    private val proxyVideoSink = ProxyVideoSink()

    private val rtcLock = Mutex()
    private var currentJob: Job? = null

    /**
     * 播放或切换设备
     */
    fun switchListPlayer(deviceId: Long, forceReconnect: Boolean = true) {
        val isSameDevice = (_currentPlayingId.value == deviceId)

        // 无缝切换逻辑：如果是同一设备且不强制重连，直接复用
        if (isSameDevice && _webRtcSdp.value != null && !forceReconnect) {
            Log.d("CameraViewModel", "Seamless: Keeping connection for $deviceId")
            return
        }

        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _isSwitching.value = true

            // 切换到 IO 线程执行，避免阻塞主线程导致掉帧
            withContext(Dispatchers.IO) {
                rtcLock.withLock {
                    try {
                        if (!isSameDevice || forceReconnect) {
                            Log.d("CameraViewModel", "Switching to device: $deviceId")

                            // 1. 切断 UI 渲染，防止 Native 向旧 Surface 写入数据
                            proxyVideoSink.setTarget(null)
                            _webRtcSdp.value = null
                            _currentPlayingId.value = null

                            // 2. 释放旧连接 (如果存在)
                            if (webRtcClient != null) {
                                try {
                                    Log.d("CameraViewModel", "Releasing old WebRTC client...")
                                    webRtcClient?.release()
                                    webRtcClient = null
                                } catch (e: Exception) {
                                    Log.e("CameraViewModel", "Release error (ignored)", e)
                                }
                            }

                            // 3. 关键延时：给予 Native 线程完全退出的时间
                            // 之前的 600ms 可能不够，特别是主线程繁忙时
                            delay(800)

                            // 4. 初始化新 Client
                            initWebRtcClient()

                            Log.d("CameraViewModel", "Connecting to $deviceId")
                            _currentPlayingId.value = deviceId

                            // 5. 执行建立连接
                            executeWebRtcEstablish(deviceId)
                        }
                    } catch (e: Exception) {
                        Log.e("CameraViewModel", "Switch Error", e)
                        _webRtcSdp.value = null
                    } finally {
                        _isSwitching.value = false
                    }
                }
            }
        }
    }

    private fun initWebRtcClient() {
        if (webRtcClient == null) {
            try {
                webRtcClient = WebRtcClient(context, rootEglBase)
                // 立即绑定代理 Sink
                webRtcClient?.setRemoteRender(proxyVideoSink)
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Init client failed", e)
            }
        }
    }

    private suspend fun executeWebRtcEstablish(deviceId: Long) {
        val client = webRtcClient ?: return
        try {
            val pathResult = UniCallbackService<String>().parseDataNewSuspend(
                deviceService.getCameraLiveUrl(deviceId, 1, 1), context
            ) ?: return

            val uri = pathResult.toUri()
            val app = uri.getQueryParameter("app") ?: ""
            val stream = uri.getQueryParameter("stream") ?: ""
            val type = uri.getQueryParameter("type") ?: "play"

            // 创建 Offer (耗时操作)
            val localOfferSdp = client.createOffer()
            val requestBody = localOfferSdp.toRequestBody("text/plain".toMediaTypeOrNull())

            val sdpResponse = UniCallbackService<WebRTCResponse>().parseDirectSuspend(
                call = deviceService.getCameraLive(app, stream, type, requestBody),
                context = context,
                checkSuccess = { resp -> if (resp.code == 0) null else "Error: ${resp.id}" }
            )

            sdpResponse?.sdp?.let { remoteAnswerSdp ->
                client.setRemoteDescription(remoteAnswerSdp)
                _webRtcSdp.value = sdpResponse
            }
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Establish failed: ${e.message}")
            // 发生错误时不要立即清理，保持当前状态，等待用户重试
            _webRtcSdp.value = null
        }
    }

    fun stopListPlayer() {
        currentJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            rtcLock.withLock {
                try {
                    proxyVideoSink.setTarget(null)
                    _currentPlayingId.value = null
                    _webRtcSdp.value = null

                    webRtcClient?.release()
                    webRtcClient = null
                } catch (e: Exception) {
                    Log.e("CameraViewModel", "Stop error", e)
                }
            }
        }
    }

    fun getEglBaseContext(): EglBase.Context = rootEglBase.eglBaseContext

    /**
     * UI 层调用：绑定/解绑 SurfaceView
     * 这是纯 Java 层引用操作，非常安全，不会导致 Native 崩溃
     */
    fun attachSurfaceView(renderer: VideoSink?) {
        proxyVideoSink.setTarget(renderer)
    }

    override fun onCleared() {
        super.onCleared()
        stopListPlayer()
        // 注意：这里我们不调用 rootEglBase.release()，因为它现在是全局管理的
        // 如果确定 APP 完全退出，可以在 Application 层管理释放，或者让它随进程销毁
    }

    /**
     * 代理渲染器
     */
    private class ProxyVideoSink : VideoSink {
        @Volatile
        private var target: VideoSink? = null

        fun setTarget(sink: VideoSink?) {
            this.target = sink
        }

        override fun onFrame(frame: VideoFrame) {
            // 如果 target 为空，直接丢弃帧，避免 Native 崩溃
            target?.onFrame(frame)
        }
    }
}