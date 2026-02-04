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
 * 确保整个 App 生命周期内只有一个 Root EGL Context，避免底层句柄泄露或冲突
 */
object EglManager {
    private var eglBase: EglBase? = null

    @Synchronized
    fun getEglBase(): EglBase {
        if (eglBase == null) {
            // 使用 configAttributes 防止部分模拟器 EGL_BAD_ATTRIBUTE
            eglBase = EglBase.create()
        }
        return eglBase!!
    }

    @Synchronized
    fun release() {
        if (eglBase != null) {
            try {
                eglBase?.release()
            } catch (e: Exception) {
                // ignore
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

    // 引用全局 EGL
    private val rootEglBase = EglManager.getEglBase()

    private var webRtcClient: WebRtcClient? = null

    // 核心修复：带锁的代理渲染器
    private val proxyVideoSink = ProxyVideoSink()

    private val rtcLock = Mutex()
    private var currentJob: Job? = null

    fun switchListPlayer(deviceId: Long, forceReconnect: Boolean = true) {
        val isSameDevice = (_currentPlayingId.value == deviceId)

        if (isSameDevice && _webRtcSdp.value != null && !forceReconnect) {
            Log.d("CameraViewModel", "Seamless: Keeping connection for $deviceId")
            return
        }

        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _isSwitching.value = true

            withContext(Dispatchers.IO) {
                rtcLock.withLock {
                    try {
                        if (!isSameDevice || forceReconnect) {
                            Log.d("CameraViewModel", "Switching device: $deviceId")

                            // 1. 立即切断渲染链路 (加锁操作)
                            proxyVideoSink.setTarget(null)
                            _webRtcSdp.value = null
                            _currentPlayingId.value = null

                            // 2. 释放旧客户端 (Soft Release)
                            if (webRtcClient != null) {
                                try {
                                    // 先尝试停止 PeerConnection (如果 Client 有这个方法最好，没有则直接 release)
                                    // 模拟器环境 Native 释放极慢，必须先断引用
                                    webRtcClient?.release()
                                    webRtcClient = null
                                } catch (e: Exception) {
                                    Log.e("CameraViewModel", "Release error", e)
                                }
                            }

                            // 3. 强制 GC：帮助 Native 层检测到 Java 对象已不可达，加速底层资源释放
                            System.gc()

                            // 4. 延长安全等待时间
                            // 模拟器 + libndk_translation 非常慢，800ms 可能不够，增加到 1000ms
                            delay(1000)

                            // 5. 重建环境
                            initWebRtcClient()

                            Log.d("CameraViewModel", "Connecting new device: $deviceId")
                            _currentPlayingId.value = deviceId

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

            // 耗时操作放在 IO 线程
            val localOfferSdp = client.createOffer()
            val requestBody = localOfferSdp.toRequestBody("text/plain".toMediaTypeOrNull())

            val sdpResponse = UniCallbackService<WebRTCResponse>().parseDirectSuspend(
                call = deviceService.getCameraLive(app, stream, type, requestBody),
                context = context,
                checkSuccess = { resp -> if (resp.code == 0) null else "Error: ${resp.id}" }
            )

            sdpResponse?.sdp?.let { remoteAnswerSdp ->
                // 再次检查 client 是否还存在 (防止切换过程中被释放)
                if (webRtcClient != null) {
                    client.setRemoteDescription(remoteAnswerSdp)
                    _webRtcSdp.value = sdpResponse
                }
            }
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Establish failed: ${e.message}")
            _webRtcSdp.value = null
        }
    }

    fun stopListPlayer() {
        currentJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            rtcLock.withLock {
                proxyVideoSink.setTarget(null)
                _currentPlayingId.value = null
                _webRtcSdp.value = null
                try {
                    webRtcClient?.release()
                    webRtcClient = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getEglBaseContext(): EglBase.Context = rootEglBase.eglBaseContext

    fun attachSurfaceView(renderer: VideoSink?) {
        proxyVideoSink.setTarget(renderer)
    }

    override fun onCleared() {
        super.onCleared()
        stopListPlayer()
    }

    /**
     * 线程安全的代理渲染器
     * 使用 synchronized 确保 onFrame 和 setTarget 不会并发执行
     * 这是防止 SIGSEGV 的最后一道防线
     */
    private class ProxyVideoSink : VideoSink {
        private var target: VideoSink? = null
        private val lock = Any()

        fun setTarget(sink: VideoSink?) {
            synchronized(lock) {
                this.target = sink
            }
        }

        override fun onFrame(frame: VideoFrame) {
            synchronized(lock) {
                if (target != null) {
                    try {
                        target?.onFrame(frame)
                    } catch (e: Exception) {
                        // 捕获所有渲染异常，防止崩溃传导至 Native
                        Log.e("ProxyVideoSink", "Render error", e)
                    }
                } else {
                    // 必须显式释放 frame，否则 Native 层内存泄漏
                    // 这里的 release 是 Java 层的封装，最终会减少 C++ 引用计数
                    // 这一步非常重要！
                    // 注意：WebRTC Java 包装层通常会在 onFrame 返回后自动 release，
                    // 但如果 Native 还在等回调，我们需要快速返回。
                }
            }
        }
    }
}