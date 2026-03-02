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
import androidx.compose.material3.HorizontalDivider
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
            // 动态内容区域 (比如设备特有的控制面板)
            content()
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        // --- 第一部分：上半区核心信息 (图标 + 文字 + 状态标签) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically // 整体垂直居中，视觉更平衡
        ) {
            // 1.1 设备图标 (带柔和背景)
            Surface(
                shape = RoundedCornerShape(14.dp), // 稍微减小圆角，更显精致
                color = iconBg,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = rememberVectorPainter(getIconFromId(productType)),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 1.2 中间文本与右上角标签
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 第一行：设备名称 和 在线状态标签
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = iotDevice.deviceName ?: "未知设备",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DeviceStatus(iotDevice.state)
                }

                // 第二行：SN码
                Text(
                    text = "SN: ${iotDevice.serialNum ?: "--"}",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1
                )

                // 第三行：产品类型
                Text(
                    text = "产品:${iotDevice.productName ?: "--"}",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 分割线：强化上下分层结构 ---
        HorizontalDivider(
            color = Color(0xFFF3F4F6),
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- 第二部分：下半区状态行 (横跨全宽，填补图标下方的空白) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeviceStatusRow(
                isDisable = iotDevice.deviceState == 0,
                hasAlarm = iotDevice.alarmType == 1,
                modifier = Modifier.fillMaxWidth() // 让状态均匀分布或左对齐铺开
            )
        }
    }
}