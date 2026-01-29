package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant.groupDeviceBindOptions
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.ModernStateSelector
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.LampViewModel

/**
 * 分组成员列表
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampGroupMemberContent(
    lampViewModel: LampViewModel,
    onBack: () -> Unit  // <--- 新增这个参数
) {
    // 分页数据
    val groupMemberFlow = lampViewModel.groupMemberFlow.collectAsLazyPagingItems()
    val bindState = lampViewModel.bindState.collectAsState()
    val currentGroupInfo = lampViewModel.currentGroupInfo.collectAsState()

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
        lampViewModel.updateBindState(-1)
    }


    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Column(modifier = Modifier.background(CardWhite)) {
                    CommonTopAppBar(
                        title = "${currentGroupInfo.value?.groupName}",
                        onBack = { onBack() })
                }
            }
        }, containerColor = PageBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
    }


}