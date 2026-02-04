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
 */
object EglManager {
    private var eglBase: EglBase? = null

    @Synchronized
    fun getEglBase(): EglBase {
        if (eglBase == null) {
            // 使用 CONFIG_PLAIN 兼容性更好，减少模拟器崩溃
            eglBase = EglBase.create(null, EglBase.CONFIG_PLAIN)
        }
        return eglBase!!
    }

    @Synchronized
    fun release() {
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

    private val rootEglBase = EglManager.getEglBase()
    private var webRtcClient: WebRtcClient? = null

    // 核心：带锁的代理渲染器，防止 Native 崩溃
    private val proxyVideoSink = ProxyVideoSink()

    private val rtcLock = Mutex()
    private var currentJob: Job? = null

    /**
     * 切换/播放设备
     */
    fun switchListPlayer(deviceId: Long, forceReconnect: Boolean = true) {
        val isSameDevice = (_currentPlayingId.value == deviceId)

        // 无缝切换逻辑
        if (isSameDevice && _webRtcSdp.value != null && !forceReconnect) {
            Log.d("CameraViewModel", "Seamless: Keeping connection for $deviceId")
            return
        }

        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _isSwitching.value = true // UI 进入 Loading 态

            withContext(Dispatchers.IO) {
                rtcLock.withLock {
                    try {
                        if (!isSameDevice || forceReconnect) {
                            Log.d("CameraViewModel", "Switching device: $deviceId")
                            // 1. 立即切断渲染，防止 Native 向 UI 写数据
                            proxyVideoSink.setTarget(null)
                            // 2. 清理状态
                            _webRtcSdp.value = null
                            // 注意：这里先置空 ID，等连接成功后再赋值？
                            // 为了 UI 显示 Loading，我们这里先保留 ID 或者赋值新 ID
                            // 但如果失败，必须在 catch 里回滚！
                            _currentPlayingId.value = deviceId
                            // 3. 释放旧 Client (Soft Release)
                            if (webRtcClient != null) {
                                try {
                                    webRtcClient?.release()
                                    webRtcClient = null
                                } catch (e: Exception) {
                                    Log.w("CameraViewModel", "Release warning: ${e.message}")
                                }
                            }
                            // 4. 强制 GC 并延时，等待 Native 线程退出 (解决 SIGSEGV 关键)
                            System.gc()
                            delay(800)
                            // 5. 初始化新 Client
                            initWebRtcClient()
                            // 6. 执行连接
                            executeWebRtcEstablish(deviceId)
                        }
                    } catch (e: Exception) {
                        Log.e("CameraViewModel", "Switch Fatal Error", e)
                        handleConnectionFailure()
                    } finally {
                        _isSwitching.value = false
                    }
                }
            }
        }
    }

    private fun handleConnectionFailure() {
        // 发生错误时，必须重置所有状态，否则 UI 会一直转圈
        _webRtcSdp.value = null
        _currentPlayingId.value = null
        // 释放可能初始化了一半的 Client
        try {
            webRtcClient?.release()
            webRtcClient = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initWebRtcClient() {
        if (webRtcClient == null) {
            try {
                webRtcClient = WebRtcClient(context, rootEglBase)
                webRtcClient?.setRemoteRender(proxyVideoSink)
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Init client failed", e)
                throw e // 抛出异常触发 handleConnectionFailure
            }
        }
    }

    private suspend fun executeWebRtcEstablish(deviceId: Long) {
        val client = webRtcClient ?: throw Exception("WebRTC Client is null")

        try {
            // 1. 获取推流地址
            // 关键修复：如果返回 null，抛出异常，进入 catch 块清理状态
            val pathResult = UniCallbackService<String>().parseDataNewSuspend(
                deviceService.getCameraLiveUrl(deviceId, 1, 1), context
            ) ?: throw Exception("Live URL response is empty")

            val uri = pathResult.toUri()
            val app = uri.getQueryParameter("app") ?: ""
            val stream = uri.getQueryParameter("stream") ?: ""
            val type = uri.getQueryParameter("type") ?: "play"

            // 2. 创建 SDP Offer
            val localOfferSdp = client.createOffer()
            val requestBody = localOfferSdp.toRequestBody("text/plain".toMediaTypeOrNull())

            // 3. 交换 SDP
            val sdpResponse = UniCallbackService<WebRTCResponse>().parseDirectSuspend(
                call = deviceService.getCameraLive(app, stream, type, requestBody),
                context = context,
                checkSuccess = { resp -> if (resp.code == 0) null else "Server Error: ${resp.id}" }
            )

            // 4. 设置远程 SDP
            if (sdpResponse?.sdp != null) {
                if (webRtcClient != null) { // 双重检查
                    client.setRemoteDescription(sdpResponse.sdp)
                    _webRtcSdp.value = sdpResponse
                    Log.d("CameraViewModel", "Connection established for $deviceId")
                }
            } else {
                throw Exception("SDP Response is empty")
            }

        } catch (e: Exception) {
            // 捕获所有业务异常（网络、解析、空数据）
            Log.e("CameraViewModel", "Establish Logic Failed: ${e.message}")
            throw e // 抛给 switchListPlayer 的 catch 块统一处理
        }
    }

    /**
     * UI 解绑渲染器 (列表滚动复用时调用)
     */
    fun detachRenderer(renderer: VideoSink?) {
        proxyVideoSink.detachIfMatches(renderer)
    }

    /**
     * UI 绑定渲染器
     */
    fun attachRenderer(renderer: VideoSink?) {
        proxyVideoSink.setTarget(renderer)
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

    override fun onCleared() {
        super.onCleared()
        stopListPlayer()
    }

    /**
     * 线程安全的代理渲染器
     */
    private class ProxyVideoSink : VideoSink {
        private var target: VideoSink? = null
        private val lock = Any()

        fun setTarget(sink: VideoSink?) {
            synchronized(lock) {
                this.target = sink
            }
        }

        fun detachIfMatches(sink: VideoSink?) {
            synchronized(lock) {
                if (this.target === sink) {
                    this.target = null
                }
            }
        }

        override fun onFrame(frame: VideoFrame) {
            synchronized(lock) {
                if (target != null) {
                    try {
                        target?.onFrame(frame)
                    } catch (e: Exception) {
                        Log.e("ProxyVideoSink", "Render error", e)
                    }
                }
                // WebRTC 内部会自动 release frame
            }
        }
    }
}