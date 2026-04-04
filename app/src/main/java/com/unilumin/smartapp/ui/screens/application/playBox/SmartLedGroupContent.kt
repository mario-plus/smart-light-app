package com.unilumin.smartapp.ui.screens.application.playBox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.SettingsRemote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unilumin.smartapp.client.data.LedCommandReq
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.ScreenViewModel

/**
 * 智慧屏幕控制页
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLedGroupContent(
    screenViewModel: ScreenViewModel, onBack: () -> Unit
) {
    // 播放盒分组信息
    val selectLedGroup by screenViewModel.selectLedGroup.collectAsState()
    val ledDevFuncMaps by screenViewModel.ledDevFuncMaps.collectAsState()
    // 滚动状态
    val scrollState = rememberScrollState()
    val publishMenuItems = remember(ledDevFuncMaps) {
        val items = mutableListOf<PublishMenuItem>()
        if (ledDevFuncMaps.containsKey("programPublic")) {
            items.add(PublishMenuItem("发布播放表", Icons.Rounded.ListAlt) {})
        }
        if (ledDevFuncMaps.containsKey("sendSchedule")) {
            items.add(PublishMenuItem("发布播放方案", Icons.Rounded.PlayCircleOutline) {})
            items.add(PublishMenuItem("发布控制方案", Icons.Rounded.SettingsRemote) {})
        }
        items.toList()
    }
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = selectLedGroup?.name ?: "分组功能",
                subTitle = "播放盒分组",
                onBack = { onBack() },
            )
        }, containerColor = PageBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            RemoteControlSection(
                initialVolume = 0,
                initialBrightness = 0,
                onVolumeChangeFinished = { newVolume ->
                    screenViewModel.ledCommand(
                        LedCommandReq(
                            groupId = selectLedGroup?.id, type = 12, value = newVolume
                        )
                    )
                },
                onBrightnessChangeFinished = { newBrightness ->
                    screenViewModel.ledCommand(
                        LedCommandReq(
                            groupId = selectLedGroup?.id, type = 4, value = newBrightness
                        )
                    )
                },
                onActionClick = { actionType ->
                    when (actionType) {
                        ActionType.SCREEN_ON -> screenViewModel.ledCommand(LedCommandReq(groupId = selectLedGroup?.id, type = 2, value = 0))
                        ActionType.SCREEN_OFF -> screenViewModel.ledCommand(LedCommandReq(groupId = selectLedGroup?.id, type = 1, value = 0))
                        ActionType.SCREENSHOT -> screenViewModel.ledCommand(LedCommandReq(groupId = selectLedGroup?.id, type = 5, value = 0))
                        ActionType.REBOOT -> screenViewModel.ledCommand(LedCommandReq(groupId = selectLedGroup?.id, type = 3, value = 0))
                    }
                })
            // 3. 动态发布管理区域
            PublishManagementSection(menuItems = publishMenuItems)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}



