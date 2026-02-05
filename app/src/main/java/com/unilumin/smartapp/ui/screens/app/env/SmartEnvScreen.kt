package com.unilumin.smartapp.ui.screens.app.env

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_ENV
import com.unilumin.smartapp.client.constant.DeviceConstant.getSmartAppName
import com.unilumin.smartapp.client.constant.DeviceConstant.statusOptions
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel
import com.unilumin.smartapp.ui.viewModel.SystemViewModel

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


    val systemViewModel: SystemViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SystemViewModel(retrofitClient, application) as T
        }
    })

    val envProductTypeList by systemViewModel.envProductTypeList.collectAsState()

    val envDevicePagingFlow = deviceViewModel.envDevicePagingFlow.collectAsLazyPagingItems()
    val deviceState by deviceViewModel.state.collectAsState()
    val searchQuery by deviceViewModel.searchQuery.collectAsState()
    val totalCount by deviceViewModel.totalCount.collectAsState()


    LaunchedEffect (Unit) {
        deviceViewModel.updateFilter("7")
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = getSmartAppName(SMART_ENV),
                onBack = { onBack() },
                menuItems = envProductTypeList,
                onMenuItemClick = { e ->
                    deviceViewModel.updateFilter(e.id)
                })
        }, containerColor = PageBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchHeader(
                statusOptions = statusOptions,
                currentStatus = deviceState,
                searchQuery = searchQuery,
                searchTitle = "",
                onStatusChanged = { deviceViewModel.updateState(it) },
                onSearchChanged = { deviceViewModel.updateSearch(it) })
            PagingList(
                totalCount = totalCount,
                lazyPagingItems = envDevicePagingFlow,
                itemKey = { it.id },
                modifier = Modifier.weight(1f),
                emptyMessage = "暂无设备",
                contentPadding = PaddingValues(16.dp)
            ) { device ->
                DeviceEnvItem(device)
            }
        }
    }
}

/**
 * 优化后的设备环境监控卡片
 * 参考图片 实现
 */
@Composable
fun DeviceEnvItem(device: IotDevice) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. 顶部栏：图标 + 设备基本信息 + 状态标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // 左侧蓝色灯泡图标背景
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE8EFFF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF3B7CFF),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 中间设备详细文字信息
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.deviceName.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "SN: ${device.serialNum}",
                        fontSize = 13.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Text(
                        text = device.productName.toString(),
                        fontSize = 13.sp,
                        color = Color(0xFF999999),
                        lineHeight = 16.sp
                    )
                }
                // 右侧状态角标
                DeviceStatus(device.state)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 中间实时数据面板：横向排列 + 浅灰色背景
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8F9FB),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val telemetryList = device.telemetryList ?: emptyList()
                    telemetryList.forEachIndexed { index, data ->
                        // 数据项内容
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = data.name, fontSize = 12.sp, color = Color(0xFF999999))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${data.value ?: "--"}${data.unit}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (data.key == "switch" || data.key == "brightness") Color(
                                    0xFF3B7CFF
                                ) else Color(0xFF333333)
                            )
                        }

                        // 项之间的垂直分割线
                        if (index < telemetryList.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(20.dp)
                                    .background(Color(0xFFE0E0E0))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 底部状态栏：可用状态 + 工作状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusIndicator(label = "可用状态", status = "启用", isNormal = true)
                StatusIndicator(label = "工作状态", status = "正常", isNormal = true)
            }
        }
    }
}


/**
 * 底部带勾选图标的状态指示器
 */
@Composable
fun StatusIndicator(label: String, status: String, isNormal: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: ", fontSize = 13.sp, color = Color(0xFF999999))
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isNormal) Color(0xFF00C091) else Color(0xFFFF4D4F),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status,
            fontSize = 13.sp,
            color = if (isNormal) Color(0xFF00C091) else Color(0xFFFF4D4F),
            fontWeight = FontWeight.Medium
        )
    }
}