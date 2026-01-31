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
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel

@Composable
fun LivePlayerDialog(
    device: IotDevice,
    viewModel: DeviceViewModel,
    onDismiss: () -> Unit
) {
    val sdpData by viewModel.webRtcSdp.collectAsState()

    // 进入自动开始协商
    LaunchedEffect(device.id) {
        viewModel.getCameraWebRtcSdp(device.id)
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = {
            viewModel.clearWebRtcData()
            onDismiss()
        },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
            Column {
                // 1. 顶部栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.IconButton(onClick = onDismiss) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.ArrowBack,
                            null,
                            tint = Color.White
                        )
                    }
                    Text(device.deviceName ?: "监控直播", color = Color.White)
                }

                // 2. 视频预览区 (16:9)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.77f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (sdpData == null) {
                        androidx.compose.material3.CircularProgressIndicator(color = Color.White)
                    } else {
                        // 占位提示：实际需配合 SurfaceViewRenderer
                        Text("视频流已建立", color = Color.Green)
                    }
                }

                // 3. 云台控制区 (PTZ)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "云台控制",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    PTZJoystick { direction ->
                        // 调用后端接口控制摄像头
                        // viewModel.controlCamera(device.id, direction)
                    }
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
        modifier = Modifier
            .size(60.dp)
            .background(Color.White.copy(alpha = 0.1f), CircleShape)
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(36.dp))
    }
}