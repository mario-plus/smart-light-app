package com.unilumin.smartapp.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.mock.ServerConfig

@Composable
fun ServerSettingsDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    retrofitClient: RetrofitClient
) {

    var tempProtocol by remember { mutableStateOf(ServerConfig.protocol) }
    var tempIp by remember { mutableStateOf(ServerConfig.ipAddress) }
    var tempPort by remember { mutableStateOf(ServerConfig.port) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "服务器配置",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 1. 协议选择
                Text("协议", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    ProtocolChip(
                        text = "http",
                        selected = tempProtocol == "http",
                        onClick = { tempProtocol = "http" }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    ProtocolChip(
                        text = "https",
                        selected = tempProtocol == "https",
                        onClick = { tempProtocol = "https" }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. IP 地址
                Text("IP 地址", fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(
                    value = tempIp,
                    onValueChange = { tempIp = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. 端口
                Text("端口", fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(
                    value = tempPort,
                    onValueChange = { tempPort = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 按钮组
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            ServerConfig.protocol = tempProtocol
                            ServerConfig.ipAddress = tempIp
                            ServerConfig.port = tempPort
                            onSave()
                            retrofitClient.initRetrofit(ServerConfig.getBaseUrl())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("保存配置")
                    }
                }
            }
        }
    }
}

// 辅助组件：协议选择Chip
@Composable
fun RowScope.ProtocolChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) Color(0xFF1E88E5) else Color(0xFFF5F5F5)
    val textColor = if (selected) Color.White else Color.Gray

    Box(
        modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text.uppercase(), color = textColor, fontWeight = FontWeight.Medium)
        }
    }
}