package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.client.constant.DeviceConstant.groupTypeOptions
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.viewModel.LampViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampJobContent(
    lampViewModel: LampViewModel
) {

    val lampJobFlow = lampViewModel.lampJobFlow.collectAsLazyPagingItems()
    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
    }

    BaseLampListScreen(
        statusOptions = DeviceConstant.jobStatusOptions,
        viewModel = lampViewModel,
        pagingItems = lampJobFlow,
        keySelector = { it.id },
        searchTitle = "搜索分组名称或产品名称",
        middleContent = {
        }
    ) { item ->
        Text("ssss")
    }



}
