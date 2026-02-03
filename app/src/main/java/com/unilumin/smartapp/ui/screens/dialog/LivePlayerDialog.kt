package com.unilumin.smartapp.ui.screens.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

@Composable
fun LivePlayerDialog(
    device: IotDevice,
    viewModel: DeviceViewModel,
    onDismiss: () -> Unit
) {
    val sdpData by viewModel.webRtcSdp.collectAsState()

    LaunchedEffect(device.id) {
        viewModel.getCameraWebRtcSdp(device.id)
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = {
            // 在关闭 Dialog 前清理数据
            viewModel.clearWebRtcData()
            onDismiss()
        },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
            Column {
                // 1. 顶部栏
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.IconButton(onClick = {
                        viewModel.clearWebRtcData()
                        onDismiss()
                    }) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.ArrowBack,
                            null,
                            tint = Color.White
                        )
                    }
                    Text(device.deviceName ?: "监控直播", color = Color.White)
                }

                // 2. 视频预览区
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.77f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (sdpData == null) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        // 使用 AndroidView 嵌入 WebRTC 的 SurfaceViewRenderer
                        AndroidView(
                            factory = { ctx ->
                                SurfaceViewRenderer(ctx).apply {
                                    init(viewModel.getEglBaseContext(), null)
                                    setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                                    setMirror(false)
                                    setEnableHardwareScaler(true)

                                    // 绑定渲染器
                                    viewModel.attachRenderer(this)
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            // 【关键修复】确保先解绑后销毁，解决 Dropping frame 日志
                            onRelease = { renderer ->
                                viewModel.detachRenderer()
                                renderer.release()
                            }
                        )
                    }
                }

                // 3. 云台控制区
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("云台控制", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
                    PTZJoystick { direction -> /* ... */ }
                }
            }
        }
    }
}

@Composable
fun PTZJoystick(onMove: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PTZIconBtn(Icons.Default.KeyboardArrowUp) { onMove("UP") }
        Row {
            PTZIconBtn(Icons.AutoMirrored.Filled.KeyboardArrowLeft) { onMove("LEFT") }
            Box(Modifier.size(60.dp))
            PTZIconBtn(Icons.AutoMirrored.Filled.KeyboardArrowRight) { onMove("RIGHT") }
        }
        PTZIconBtn(Icons.Default.KeyboardArrowDown) { onMove("DOWN") }
    }
}

@Composable
fun PTZIconBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = Modifier.size(60.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(36.dp))
    }
}