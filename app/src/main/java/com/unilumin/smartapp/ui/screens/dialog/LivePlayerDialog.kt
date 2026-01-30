package com.unilumin.smartapp.ui.screens.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
    val sdpData = viewModel.webRtcSdp.collectAsState()

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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.IconButton(onClick = onDismiss) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.ArrowBack,
                            "Close",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = device.deviceName ?: "监控直播",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // 播放器占位
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (sdpData == null) {
                        androidx.compose.material3.CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(
                            "正在接收流: ${sdpData.value?.id}\nSDP 已就绪",
                            color = Color.Green,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }

                // 底部操作区
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp, top = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MonitorActionBtn(androidx.compose.material.icons.Icons.Default.Camera, "截图")
                    MonitorActionBtn(androidx.compose.material.icons.Icons.Default.Mic, "对讲")
                    MonitorActionBtn(
                        androidx.compose.material.icons.Icons.Default.FiberManualRecord,
                        "录制",
                        Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun MonitorActionBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = Color.White
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(50.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.padding(12.dp))
        }
        Text(label, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}