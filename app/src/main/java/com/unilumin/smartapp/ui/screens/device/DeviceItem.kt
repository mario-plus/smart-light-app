package com.unilumin.smartapp.ui.screens.device

import EnvFeatureContent
import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
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
import com.unilumin.smartapp.client.constant.DeviceType
import com.unilumin.smartapp.client.constant.DeviceType.getDeviceIcon
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.DeviceStatusRow
import com.unilumin.smartapp.ui.theme.Blue50
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray50
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.Orange50
import com.unilumin.smartapp.ui.theme.Orange500
import com.unilumin.smartapp.ui.theme.White
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel

@SuppressLint("DefaultLocale")
@Composable
fun DeviceCardItem(
    deviceViewModel: DeviceViewModel,
    lightDevice: LightDevice,
    type: String,
    onDetailClick: (LightDevice) -> Unit
) {


    // 状态颜色逻辑
    val (iconBg, iconTint) = when (lightDevice.state) {
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

            DeviceHeader(lightDevice, type, iconBg, iconTint, onClick = {
                onDetailClick(lightDevice)
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
    lightDevice: LightDevice,
    type: String,
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
                        getDeviceIcon(type),
                        null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 2. 文字信息区域
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp) // 稍微调小行间距，让4行内容紧凑一些
            ) {
                // 第一行：设备名称
                Text(
                    text = lightDevice.name,
                    fontSize = 16.sp, // 17sp 稍微有点大，16sp 更精致
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // 第二行：序列码
                Text(
                    text = "序列码: ${lightDevice.serialNum}", // 增加空格
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1
                )
                // 第三行：产品名称
                Text(
                    text = "产品名称: ${lightDevice.productName}", // 增加空格
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1
                )

                // 第四行：告警/状态组件
                Box(modifier = Modifier.padding(top = 4.dp)) {
                    DeviceStatusRow(
                        isDisable = lightDevice.deviceState == 0,
                        hasAlarm = lightDevice.alarmType == 1
                    )
                }
            }
        }

        // --- 右侧：在线/离线 大状态指示 ---
        Spacer(modifier = Modifier.width(8.dp))

        // 注意：如果 DeviceStatus 内部也有点击事件，可能会产生冲突
        // 如果 DeviceStatus 只是展示，没有问题。
        DeviceStatus(lightDevice.state)
    }
}


/**
 * 特性内容容器：包含上方的间距、内容主体、以及底部的分割线和间距
 * 保持原有布局结构一致
 */
@Composable
fun FeatureContentContainer(content: @Composable () -> Unit) {
    Spacer(modifier = Modifier.height(16.dp))
    content()
    Spacer(modifier = Modifier.height(16.dp))
    Divider(color = Gray100, thickness = 1.dp)
    Spacer(modifier = Modifier.height(12.dp))
}

/**
 * 灰色背景、可横向滚动的详情行容器
 * 用于 Lamp 和 Playbox 的参数展示
 */
@Composable
fun DetailInfoScrollRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray50, RoundedCornerShape(12.dp))
            .border(1.dp, Gray100, RoundedCornerShape(12.dp))
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun ProductTypeTag(typeName: String) {
    Surface(
        color = Color(0xFFF3F4F6), // 浅灰色背景 (Gray100)
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.height(20.dp) // 固定高度，整齐
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            Text(
                text = typeName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4B5563) // Gray600
            )
        }
    }
}