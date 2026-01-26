package com.unilumin.smartapp.ui.screens.device

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.constant.DeviceConstant.getIconFromId
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.DeviceStatusRow
import com.unilumin.smartapp.ui.theme.Blue50
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Orange50
import com.unilumin.smartapp.ui.theme.Orange500
import com.unilumin.smartapp.ui.theme.White

@SuppressLint("DefaultLocale")
@Composable
fun DeviceCardItem(
    iotDevice: IotDevice,
    productType: Long,
    onDetailClick: (IotDevice) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val (iconBg, iconTint) = when (iotDevice.state) {
        1 -> Blue50 to Blue600
        0 -> Gray100 to Gray400
        else -> Orange50 to Orange500
    }
    Surface(
        color = White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 0.5.dp,
        border = BorderStroke(1.dp, Gray100),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DeviceHeader(
                iotDevice = iotDevice,
                productType = productType,
                iconBg = iconBg,
                iconTint = iconTint,
                onClick = { onDetailClick(iotDevice) }
            )
            content()


//            when (type) {
//                DeviceType.LAMP -> LampFeatureContent(
//                    deviceViewModel,
//                    lightDevice,
//                    onDetailClick
//                )
//
//                DeviceType.LOOP -> LoopFeatureContent(
//                    deviceViewModel,
//                    lightDevice,
//                    onDetailClick
//                )
//
//                DeviceType.PLAY_BOX -> PlayboxFeatureContent(
//                    deviceViewModel,
//                    lightDevice,
//                    onDetailClick
//                )
//
//                DeviceType.ENV -> EnvFeatureContent(deviceViewModel, lightDevice, onDetailClick)
//            }
        }
    }
}

@Composable
fun DeviceHeader(
    iotDevice: IotDevice,
    productType: Long,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit = {}
) {
    // 1. 外层改为 Column，以便竖向排列“基本信息”和“状态行”
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp) // 增加 padding 提升卡片质感
    ) {
        // --- 第一部分：顶部主要信息 (图标 + 文字 + 在线标签) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top // 顶部对齐，防止文字多时图标被拉伸
        ) {
            // 1.1 设备图标
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = iconBg,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = rememberVectorPainter(getIconFromId(productType)), // 假设 getIconFromId 返回 ImageVector
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 1.2 中间文本信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp) // 紧凑排列
            ) {
                Text(
                    text = iotDevice.deviceName ?: "未知设备",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "SN: ${iotDevice.serialNum ?: "--"}",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1
                )
                Text(
                    text = iotDevice.productName ?: "--",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 1.3 右上角：在线/离线 状态
            DeviceStatus(iotDevice.state)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 第二部分：底部状态行 (单独一行) ---
        // 关键优化：使用 padding(start) 让这一行与上方的“文本”对齐，而不是与图标对齐
        // 计算方式：图标大小(52dp) + 间距(14dp) = 66dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 66.dp)
        ) {
            DeviceStatusRow(
                isDisable = iotDevice.deviceState == 0,
                hasAlarm = iotDevice.alarmType == 1,
                modifier = Modifier.fillMaxWidth() // 让内部的状态两端对齐
            )
        }
    }
}
