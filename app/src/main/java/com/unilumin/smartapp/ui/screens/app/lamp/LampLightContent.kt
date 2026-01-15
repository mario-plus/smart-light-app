package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.screens.device.DeviceCardItem

import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.viewModel.LampViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampLightContent(
    retrofitClient: RetrofitClient
) {

    val context = LocalContext.current

    val lampViewModel: LampViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LampViewModel(retrofitClient, context) as T
        }
    })

    val totalCount = lampViewModel.totalCount.collectAsState()
    val isSwitching = lampViewModel.isSwitch.collectAsState()
    // 分页数据
    val lampLightFlow = lampViewModel.lampLightFlow.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
    ) {
        // 分页列表展示
        PagingList(
            totalCount = totalCount.value,
            lazyPagingItems = lampLightFlow,
            // 只有在切换查询条件时才强制显示 Loading，否则由 PagingList 内部根据 loadState 决定
            forceLoading = isSwitching.value,
            modifier = Modifier.weight(1f),
            itemKey = { lampLightInfo -> lampLightInfo.id },
            emptyMessage = "未找到相关设备",
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
        ) { IampLightInfo ->
            var iotDevice = IotDevice(
                IampLightInfo.id,
                IampLightInfo.state.toString(),
                serialNum = IampLightInfo.serialNum,
                productId = TODO(),
                productName = TODO(),
                state = TODO(),
                deviceState = TODO(),
                alarmType = TODO()
            )
            DeviceCardItem(
                iotDevice = iotDevice,
                productType = 1,
                onDetailClick = { })
        }

    }
}