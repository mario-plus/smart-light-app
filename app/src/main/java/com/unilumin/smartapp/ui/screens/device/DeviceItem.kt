package com.unilumin.smartapp.ui.screens.device

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel

@SuppressLint("DefaultLocale")
@Composable
fun DeviceCardItem(
    deviceViewModel: DeviceViewModel,
    iotDevice: IotDevice,
    productType: Long,
    onDetailClick: (IotDevice) -> Unit
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DeviceHeader(iotDevice, productType, iconBg, iconTint, onClick = {
                onDetailClick(iotDevice)
            })
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

/**
 * 通用头部：图标、名称、SN、状态
 */
@Composable
fun DeviceHeader(
    iotDevice: IotDevice,
    productType: Long,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit = {} // 1. 新增点击回调，默认为空以防预览报错
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick) // 2. 点击事件：放在 padding 之前，让水波纹充满整个条目
            .padding(vertical = 8.dp, horizontal = 4.dp), // 稍微增加一点内边距，视觉更舒适
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // --- 左侧主要内容 ---
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 1. 设备图标容器
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = iconBg,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        getIconFromId(productType),
                        null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp) // 稍微调小行间距，让4行内容紧凑一些
            ) {
                Text(
                    text = iotDevice.deviceName.toString(),
                    fontSize = 16.sp, // 17sp 稍微有点大，16sp 更精致
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "序列码: ${iotDevice.serialNum}", // 增加空格
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1
                )
                Text(
                    text = "产品名称: ${iotDevice.productName}", // 增加空格
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1
                )
                Box(modifier = Modifier.padding(top = 4.dp)) {
                    DeviceStatusRow(
                        isDisable = iotDevice.deviceState == 0,
                        hasAlarm = iotDevice.alarmType == 1
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        DeviceStatus(iotDevice.state)
    }
}
