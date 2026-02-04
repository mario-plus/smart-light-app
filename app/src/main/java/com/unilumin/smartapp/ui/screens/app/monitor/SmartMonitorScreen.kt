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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

enum class PtzDirection {
    UP, DOWN, LEFT, RIGHT
}

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

        if (isSwitching) {
            Box(Modifier.fillMaxSize().background(Color.Transparent).clickable(enabled = true) { })
        }

        if (fullscreenDevice != null) {
            Dialog(
                onDismissRequest = { fullscreenDevice = null },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                FullScreenPlayer(
                    deviceName = fullscreenDevice!!.deviceName,
                    isConnecting = isSwitching || sdpData == null,
                    viewModel = cameraViewModel,
                    onClose = {
                        fullscreenDevice = null
                    },
                    onPtzControl = { direction ->
                        // 实际控制逻辑
                        Toast.makeText(context, "云台: $direction", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun FullScreenPlayer(
    deviceName: String?,
    isConnecting: Boolean,
    viewModel: CameraViewModel,
    onClose: () -> Unit,
    onPtzControl: (PtzDirection) -> Unit
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
            viewModel.attachRenderer(null)
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
                        init(viewModel.getEglBaseContext(), null)
                        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                        setEnableHardwareScaler(true)
                        viewModel.attachRenderer(this)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                onRelease = { renderer ->
                    viewModel.attachRenderer(null)
                    try { renderer.release() } catch (e: Exception) { e.printStackTrace() }
                }
            )
        }

        // 1. 左上角标题栏
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
                    .size(40.dp)
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

        // 2. 右下角云台控制 (优化位置与样式)
        if (!isConnecting) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd) // 放在右下角
                    .padding(end = 48.dp, bottom = 48.dp) // 留出大拇指操作的边距
            ) {
                PtzController(onControl = onPtzControl)
            }
        }
    }
}

/**
 * 优化的半透明云台控制盘
 */
@Composable
fun PtzController(onControl: (PtzDirection) -> Unit) {
    val buttonSize = 52.dp // 增大触控面积
    val spacing = 4.dp // 缩短间距，看起来更整体
    val baseColor = Color.Black.copy(alpha = 0.3f) // 半透明底
    val iconColor = Color.White.copy(alpha = 0.9f) // 高亮图标

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 上
        PtzButton(
            icon = Icons.Default.KeyboardArrowUp,
            backgroundColor = baseColor,
            iconColor = iconColor,
            size = buttonSize,
            onClick = { onControl(PtzDirection.UP) }
        )

        Spacer(modifier = Modifier.height(spacing))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // 左
            PtzButton(
                icon = Icons.Default.KeyboardArrowLeft,
                backgroundColor = baseColor,
                iconColor = iconColor,
                size = buttonSize,
                onClick = { onControl(PtzDirection.LEFT) }
            )

            // 中心装饰圆点 (或者复位键)
            Box(
                modifier = Modifier
                    .padding(horizontal = spacing)
                    .size(buttonSize * 0.4f)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            )

            // 右
            PtzButton(
                icon = Icons.Default.KeyboardArrowRight,
                backgroundColor = baseColor,
                iconColor = iconColor,
                size = buttonSize,
                onClick = { onControl(PtzDirection.RIGHT) }
            )
        }

        Spacer(modifier = Modifier.height(spacing))

        // 下
        PtzButton(
            icon = Icons.Default.KeyboardArrowDown,
            backgroundColor = baseColor,
            iconColor = iconColor,
            size = buttonSize,
            onClick = { onControl(PtzDirection.DOWN) }
        )
    }
}

/**
 * 自定义云台按钮
 */
@Composable
fun PtzButton(
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 按下时变亮，提供反馈
    val currentColor = if (isPressed) backgroundColor.copy(alpha = 0.6f) else backgroundColor

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(30)) // 稍微圆润一点，但不完全是圆
            .background(currentColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(size * 0.6f)
        )
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
                                    viewModel.attachRenderer(this)
                                }
                            },
                            update = { renderer ->
                                viewModel.attachRenderer(renderer)
                            },
                            modifier = Modifier.fillMaxSize(),
                            onRelease = { renderer ->
                                viewModel.detachRenderer(renderer)
                                try { renderer.release() } catch (e: Exception) { e.printStackTrace() }
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