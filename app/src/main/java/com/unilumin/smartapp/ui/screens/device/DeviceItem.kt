package com.unilumin.smartapp.ui.screens.device

import EnvFeatureContent
import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceType
import com.unilumin.smartapp.client.constant.DeviceType.getDeviceIcon
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.theme.Blue50
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray50
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.Orange50
import com.unilumin.smartapp.ui.theme.Orange500
import com.unilumin.smartapp.ui.theme.White

@SuppressLint("DefaultLocale")
@Composable
fun DeviceCardItem(retrofitClient: RetrofitClient, lightDevice: LightDevice, type: String) {
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

            DeviceHeader(lightDevice, type, iconBg, iconTint)
            when (type) {
                DeviceType.LAMP -> LampFeatureContent(lightDevice, retrofitClient )
               DeviceType .LOOP -> LoopFeatureContent(lightDevice, retrofitClient)
                DeviceType .PLAY_BOX -> PlayboxFeatureContent(lightDevice)
                DeviceType.ENV->EnvFeatureContent(lightDevice)
            }
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
    iconBg: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // 左侧内容
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // 图标
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBg,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        getDeviceIcon(type), // 假设此函数在外部定义
                        null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 文字信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lightDevice.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SN:${lightDevice.serialNum}",
                    fontSize = 12.sp,
                    color = Gray400,
                    lineHeight = 16.sp
                )
            }
        }
        // 右侧：状态
        Spacer(modifier = Modifier.width(8.dp))
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