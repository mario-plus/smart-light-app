package com.unilumin.smartapp.ui.screens.device

import androidx.compose.runtime.Composable
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.ui.components.InfoColumn
import com.unilumin.smartapp.ui.components.VerticalDivider

/**
 * 播放盒 (Playbox) 特有内容
 */
@Composable
fun PlayboxFeatureContent(lightDevice: LightDevice,onDetailClick: (LightDevice) -> Unit) {
    FeatureContentContainer {
        DetailInfoScrollRow {
            InfoColumn(
                "运行状态",
                if (lightDevice.powerStatus == "1") "唤醒" else "休眠",
                isHighlight = true
            )
            VerticalDivider()
            InfoColumn("亮度", "${lightDevice.brightness ?: "--"}")
            VerticalDivider()
            InfoColumn("音量", "${lightDevice.volume ?: "--"}")
            VerticalDivider()
            InfoColumn(
                "当前节目",
                lightDevice.playingProgramName.let { if (it.isNullOrEmpty()) "--" else it }
            )
            VerticalDivider()
            InfoColumn("节目分辨率", lightDevice.widthHeighProgram ?: "--")
        }
    }
}