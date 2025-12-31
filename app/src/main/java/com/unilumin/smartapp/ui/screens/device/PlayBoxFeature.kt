package com.unilumin.smartapp.ui.screens.device

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.ui.components.InfoColumn
import com.unilumin.smartapp.ui.components.RemoteControlButtonGroup
import com.unilumin.smartapp.ui.components.VerticalDivider

/**
 * 播放盒 (Playbox) 特有内容
 */
@Composable
fun PlayboxFeatureContent(lightDevice: LightDevice,onDetailClick: (LightDevice) -> Unit) {

    var showDialog by remember { mutableStateOf(false) }
    var showDeviceDataDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
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
        Spacer(modifier = Modifier.height(12.dp))
        RemoteControlButtonGroup(
            canClick = lightDevice.state == 1,
            showRemoteCtlBtn = true,
            onRemoteControlClick = { showDialog = true },
            onHistoryClick = { showDeviceDataDialog = true })
    }
    if (showDialog) {
        //远程控制
    }
    if (showDeviceDataDialog) {
        onDetailClick(lightDevice)
    }
}