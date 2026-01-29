package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant.groupDeviceBindOptions
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.components.ModernStateSelector
import com.unilumin.smartapp.ui.viewModel.LampViewModel

/**
 * 分组成员列表
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampGroupMemberContent(
    lampViewModel: LampViewModel
) {
    // 分页数据
    val groupMemberFlow = lampViewModel.groupMemberFlow.collectAsLazyPagingItems()
    val bindState = lampViewModel.bindState.collectAsState()

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
        lampViewModel.updateBindState(-1)
    }

    BaseLampListScreen(
        viewModel = lampViewModel,
        pagingItems = groupMemberFlow,
        keySelector = { it.deviceId },
        searchTitle = "搜索设备名称或序列码",
        middleContent = {
            ModernStateSelector(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                options = groupDeviceBindOptions,
                selectedValue = bindState.value,
                onValueChange = { newValue ->
                    lampViewModel.updateBindState(newValue)
                })
        }) { item ->

    }

}