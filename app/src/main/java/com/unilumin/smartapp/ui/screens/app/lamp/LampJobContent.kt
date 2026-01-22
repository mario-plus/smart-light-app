package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.viewModel.LampViewModel
import com.unilumin.smartapp.ui.components.SearchStyleMultiSelectBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampJobContent(
    lampViewModel: LampViewModel
) {
    val sceneOptions = lampViewModel.sceneOptions.collectAsState()
    val sceneSelectIds = lampViewModel.selectSceneIds.collectAsState()


    val lampJobFlow = lampViewModel.lampJobFlow.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
        lampViewModel.getJobScene()
    }



    BaseLampListScreen(
        statusOptions = DeviceConstant.jobStatusOptions,
        viewModel = lampViewModel,
        pagingItems = lampJobFlow,
        keySelector = { it.id },
        searchTitle = "搜索分组名称或产品名称",
        middleContent = {
            SearchStyleMultiSelectBar(
                label = "场景",
                options = sceneOptions.value,          // 传入 Pair 列表
                selectedKeys = sceneSelectIds.value, // 传入 Key 集合
                onSelectionChanged = { newIds -> // 回调 Key 集合
                    lampViewModel.updateSceneIds(newIds)
                },
                placeholder = "请选择场景"
            )
        }
    ) { item ->
        Text("ssss")
    }


}







