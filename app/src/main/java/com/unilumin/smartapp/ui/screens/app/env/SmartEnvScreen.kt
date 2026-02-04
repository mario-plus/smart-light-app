package com.unilumin.smartapp.ui.screens.app.env

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_ENV
import com.unilumin.smartapp.client.constant.DeviceConstant.getSmartAppName
import com.unilumin.smartapp.client.constant.DeviceConstant.statusOptions
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartEnvScreen(
    retrofitClient: RetrofitClient, onBack: () -> Unit
) {

    val context = LocalContext.current
    val application = context.applicationContext as Application

    val deviceViewModel: DeviceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceViewModel(retrofitClient, application) as T
        }
    })
    val devicePagingFlow = deviceViewModel.devicePagingFlow.collectAsLazyPagingItems()
    val deviceState by deviceViewModel.state.collectAsState()
    val searchQuery by deviceViewModel.searchQuery.collectAsState()
    val totalCount by deviceViewModel.totalCount.collectAsState()


    LaunchedEffect(Unit) {
        //环境传感器
        deviceViewModel.updateFilter("7")
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(title = getSmartAppName(SMART_ENV), onBack = { onBack() })
        }, containerColor = PageBackground
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            SearchHeader(
                statusOptions = statusOptions,
                currentStatus = deviceState,
                searchQuery = searchQuery,
                searchTitle = "",
                onStatusChanged = { deviceViewModel.updateState(it) },
                onSearchChanged = { deviceViewModel.updateSearch(it) }
            )
            PagingList(
                totalCount = totalCount,
                lazyPagingItems = devicePagingFlow,
                itemKey = { it.id },
                modifier = Modifier.weight(1f),
                emptyMessage = "暂无设备",
                contentPadding = PaddingValues(16.dp)
            ) { device ->
                //加载设备后，获取物模型数据
            }
        }
    }
}