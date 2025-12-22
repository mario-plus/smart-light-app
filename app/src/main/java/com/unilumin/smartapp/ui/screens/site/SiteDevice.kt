package com.unilumin.smartapp.ui.screens.site

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.data.SiteDevice
import com.unilumin.smartapp.ui.theme.*


@Composable
fun SiteDeviceCardItem(device: SiteDevice, onClick: () -> Unit) {
    // 逻辑：灯具用琥珀色，其他用蓝色
    val isLight = device.productTypeName.contains("灯") || device.productName.contains("灯")
    val icon = if (isLight) Icons.Rounded.Lightbulb else Icons.Rounded.Sensors
    val iconColor = if (isLight) Amber600 else Blue600

    // 逻辑：状态颜色
    val isEnabled = device.deviceStateName == "启用"
    val stateColor = if (isEnabled) Green500 else Gray500
    val stateBg = if (isEnabled) Green50 else Gray100

    // 逻辑：告警
    val hasAlarm = device.alarmType != 0

    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon + Name + State
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.productName.takeIf { it.isNotBlank() } ?: "未知设备",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = device.deviceName,
                        fontSize = 12.sp,
                        color = Gray500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(color = stateBg, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = device.deviceStateName,
                        color = stateColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Gray100, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Detail Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    InfoLabelValue(label = "设备 ID", value = device.id)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoLabelValue(label = "产品类型", value = device.productTypeName)
                }
                Column(modifier = Modifier.weight(1f)) {
                    InfoLabelValue(label = "产品 ID", value = device.productId)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("状态告警: ", fontSize = 12.sp, color = Gray400)
                        Spacer(modifier = Modifier.width(4.dp))
                        if (hasAlarm) {
                            Icon(Icons.Rounded.Warning, null, tint = Red500, modifier = Modifier.size(14.dp))
                            Text("异常(${device.alarmType})", fontSize = 12.sp, color = Red500, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Rounded.CheckCircle, null, tint = Green500, modifier = Modifier.size(14.dp))
                            Text("正常", fontSize = 12.sp, color = Green500)
                        }
                    }
                }
            }

            if (!device.serialNum.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Gray50,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SN: ${device.serialNum}",
                        fontSize = 11.sp,
                        color = Gray600,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}