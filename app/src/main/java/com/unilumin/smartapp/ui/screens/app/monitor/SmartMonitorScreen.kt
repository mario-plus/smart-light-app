package com.unilumin.smartapp.ui.screens.app.monitor

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.statusOptions
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.CameraViewModel
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartMonitorScreen(
    retrofitClient: RetrofitClient, onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val deviceViewModel: DeviceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceViewModel(retrofitClient, application) as T
        }
    })

    val cameraViewModel: CameraViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CameraViewModel(retrofitClient, application) as T
        }
    })

    val devicePagingFlow = deviceViewModel.devicePagingFlow.collectAsLazyPagingItems()
    val deviceState by deviceViewModel.state.collectAsState()
    val searchQuery by deviceViewModel.searchQuery.collectAsState()
    val totalCount by deviceViewModel.totalCount.collectAsState()

    val currentPlayingId by cameraViewModel.currentPlayingId.collectAsState()
    val sdpData by cameraViewModel.webRtcSdp.collectAsState()
    val isSwitching by cameraViewModel.isSwitching.collectAsState()

    var fullscreenDevice by remember { mutableStateOf<IotDevice?>(null) }

    LaunchedEffect(Unit) {
        deviceViewModel.updateFilter("2")
    }

    LaunchedEffect(fullscreenDevice) {
        if (fullscreenDevice != null) {
            cameraViewModel.switchListPlayer(fullscreenDevice!!.id, forceReconnect = false)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (fullscreenDevice == null) {
                    CommonTopAppBar(title = "监控设备", onBack = onBack)
                }
            },
            containerColor = PageBackground
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                SearchHeader(
                    statusOptions = statusOptions,
                    currentStatus = deviceState,
                    searchQuery = searchQuery,
                    searchTitle = "",
                    onStatusChanged = { deviceViewModel.updateState(it) },
                    onSearchChanged = { deviceViewModel.updateSearch(it) }
                )

                PagingList(
                    totalCount = totalCount,
                    lazyPagingItems = devicePagingFlow,
                    itemKey = { it.id },
                    modifier = Modifier.weight(1f),
                    emptyMessage = "暂无设备",
                    contentPadding = PaddingValues(16.dp)
                ) { device ->
                    MonitorDeviceCard(
                        device = device,
                        isPlaying = (device.id == currentPlayingId),
                        isConnecting = isSwitching || (device.id == currentPlayingId && sdpData == null),
                        viewModel = cameraViewModel,
                        onPlayClick = {
                            if (device.state == 1) {
                                cameraViewModel.switchListPlayer(device.id, forceReconnect = true)
                            } else {
                                Toast.makeText(context, "设备已离线", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onFullscreenClick = {
                            fullscreenDevice = device
                        }
                    )
                }
            }
        }

        // 加厚遮罩，防止并发点击
        if (isSwitching) {
            Box(Modifier.fillMaxSize().background(Color.Transparent).clickable(enabled = true) { })
        }

        if (fullscreenDevice != null) {
            FullScreenPlayer(
                deviceName = fullscreenDevice!!.deviceName,
                isConnecting = isSwitching || sdpData == null,
                viewModel = cameraViewModel,
                onClose = {
                    fullscreenDevice = null
                }
            )
        }
    }
}

@Composable
fun FullScreenPlayer(
    deviceName: String?, isConnecting: Boolean, viewModel: CameraViewModel, onClose: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}

        val originalOrientation = activity.requestedOrientation
        val window = activity.window
        val originalVisibility = window.decorView.systemUiVisibility

        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )

        onDispose {
            // 先断开数据流
            viewModel.attachSurfaceView(null)
            activity.requestedOrientation = originalOrientation
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility = originalVisibility
        }
    }

    BackHandler { onClose() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (isConnecting) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    SurfaceViewRenderer(ctx).apply {
                        // 使用 ViewModel 提供的单例 Context
                        init(viewModel.getEglBaseContext(), null)
                        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                        setEnableHardwareScaler(true)
                        viewModel.attachSurfaceView(this)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                onRelease = { renderer ->
                    // AndroidView 销毁时，先确保解绑
                    viewModel.attachSurfaceView(null)
                    try {
                        renderer.release()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Close, "Close", tint = Color.White)
            }

            Text(
                text = deviceName ?: "监控中",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun MonitorDeviceCard(
    device: IotDevice, isPlaying: Boolean, isConnecting: Boolean, viewModel: CameraViewModel,
    onPlayClick: () -> Unit, onFullscreenClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.77f).background(Color.Black)) {
                if (isPlaying) {
                    if (isConnecting) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                        }
                    } else {
                        AndroidView(
                            factory = { ctx ->
                                SurfaceViewRenderer(ctx).apply {
                                    init(viewModel.getEglBaseContext(), null)
                                    setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                                    viewModel.attachSurfaceView(this)
                                }
                            },
                            update = { renderer ->
                                viewModel.attachSurfaceView(renderer)
                            },
                            modifier = Modifier.fillMaxSize(),
                            onRelease = { renderer ->
                                try {
                                    // 列表项回收时，如果是当前播放项，解绑
                                    // 注意：这里不需要调用 viewModel.stopListPlayer()
                                    // 只是 View 销毁，后台流可能还需要给全屏用
                                    renderer.release()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        )

                        IconButton(
                            onClick = onFullscreenClick,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).background(Color.Black.copy(0.4f), CircleShape)
                        ) {
                            Icon(Icons.Default.Fullscreen, null, tint = Color.White)
                        }
                    }
                } else {
                    AsyncImage(
                        model = "https://picsum.photos/seed/${device.id}/800/450",
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = if (device.state == 1) 1f else 0.5f
                    )

                    if (device.state == 1) {
                        IconButton(
                            onClick = onPlayClick,
                            modifier = Modifier.align(Alignment.Center).size(56.dp).background(Color.Black.copy(0.3f), CircleShape)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                    }

                    Surface(
                        modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                        color = if (device.state == 1) Color(0xFF34C759) else Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (device.state == 1) "ONLINE" else "OFFLINE",
                            color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(device.deviceName ?: "智慧摄像头", style = MaterialTheme.typography.titleMedium, color = Gray900, fontWeight = FontWeight.Bold)
                    Text(device.productName ?: "洲明智能监控", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                DeviceStatus(device.state)
            }
        }
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}