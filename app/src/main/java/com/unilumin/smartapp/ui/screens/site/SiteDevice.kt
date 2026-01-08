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
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Sensors
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.data.SiteDevice
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.DeviceStatusRow
import com.unilumin.smartapp.ui.components.InfoLabelValue
import com.unilumin.smartapp.ui.theme.Amber600
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900


@Composable
fun SiteDeviceCardItem(device: SiteDevice, onClick: () -> Unit) {
    val isLight = device.productTypeName.contains("灯") || device.productName.contains("灯")
    val icon = if (isLight) Icons.Rounded.Lightbulb else Icons.Rounded.Sensors
    val iconColor = if (isLight) Amber600 else Blue600



    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

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
                        text = device.deviceName.takeIf { it.isNotBlank() } ?: "未知设备",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = (device.serialNum ?: ""),
                        fontSize = 12.sp,
                        color = Gray500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface( shape = RoundedCornerShape(6.dp)) {
                    DeviceStatus(device.state)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Gray100, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))
            // 产品分类+产品型号
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    InfoLabelValue(label = "产品分类", value = device.productTypeName)
                }
                Column(modifier = Modifier.weight(1f)) {
                    InfoLabelValue(label = "产品型号", value = device.productName)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            //禁用，告警状态
            DeviceStatusRow(device.deviceState == 0, device.alarmType == 1)
        }
    }
}