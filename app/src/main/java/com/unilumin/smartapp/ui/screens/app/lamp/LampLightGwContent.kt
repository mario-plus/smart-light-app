package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.ui.components.BaseLampListScreen

import com.unilumin.smartapp.ui.viewModel.LampViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampLightGwContent(
    lampViewModel: LampViewModel
) {
    val lightGwFlow = lampViewModel.lampLightGwFlow.collectAsLazyPagingItems()
    BaseLampListScreen(
        viewModel = lampViewModel,
        pagingItems = lightGwFlow,
        keySelector = { it.id },
        searchTitle = "搜索设备名称或序列码"
    ) { item ->
        LampGatewayCard(item = item, onDetailClick = {})
    }
}






