package com.unilumin.smartapp.ui.screens.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.constant.DeviceType
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.theme.Blue50
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Orange50
import com.unilumin.smartapp.ui.theme.Orange500

@Composable
fun DeviceHeader(
    lightDevice: LightDevice,
    type: String
) {
    // 1. 状态色彩逻辑
    val (iconBg, iconTint) = when (lightDevice.state) {
        1 -> Blue50 to Blue600       // 在线
        0 -> Gray100 to Gray400     // 离线
        else -> Orange50 to Orange500 // 故障
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top // 顶部对齐，保证右侧状态栏位置稳定
    ) {
        // --- 左侧核心区域 ---
        Row(modifier = Modifier.weight(1f)) {

            // 1. 增大后的图标容器 (从 48dp 增加到 56dp)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = iconBg,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = DeviceType.getDeviceIcon(type),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(30.dp) // 图标本身调大
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. 文本信息列：各占一行
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // 第一行：设备名称 (大字体，加粗)
                Text(
                    text = lightDevice.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827), // 深黑色
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 第二行：序列号 (辅助信息，灰色)
                Text(
                    text = "SN: ${lightDevice.serialNum}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280), // 辅助灰
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 第三行：产品型号 (现代化标签设计)
                if (lightDevice.productName?.isNotEmpty() == true) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF3F4F6), // 浅灰色底
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = lightDevice.productName!!,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // --- 右侧区域：状态标签 ---
        Spacer(modifier = Modifier.width(12.dp))
        DeviceStatus(lightDevice.state)
    }
}