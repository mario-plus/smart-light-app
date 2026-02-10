package com.unilumin.smartapp.ui.screens.app.env

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
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

    LaunchedEffect(Unit) {
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
                contentPadding = PaddingValues(bottom = 16.dp)
            ) { device ->
                DeviceEnvItem(device)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceEnvItem(device: IotDevice) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (device.state == 1) Color(0xFFE8EFFF) else Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = if (device.state == 1) Color(0xFF3B7CFF) else Color(0xFF999999),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.deviceName ?: "未知设备",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "SN: ${device.serialNum}",
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = " ${device.productName}",
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                DeviceStatus(device.state)
            }

            Spacer(modifier = Modifier.height(18.dp))

            val dataList = device.telemetryList
            if (dataList.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 3,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dataList.forEach { data ->
                        EnvSensorCard(
                            label = data.name,
                            value = data.value,
                            unit = data.unit,
                            modifier = Modifier.weight(1f, fill = true)
                        )
                    }
                    val fillCount = 3 - (dataList.size % 3)
                    if (fillCount < 3) {
                        repeat(fillCount) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // 3. 底部：业务状态指示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusIndicator(label = "可用状态", status = "启用", isNormal = true)
                StatusIndicator(
                    label = "工作状态",
                    status = if (device.state == 1) "正常" else "离线",
                    isNormal = device.state == 1
                )
            }
        }
    }
}

@Composable
fun EnvSensorCard(
    label: String,
    value: String,
    unit: String?, // 【关键修复】改为可空类型 String?
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF8F9FB), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF7A7A7A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = value ?: "--", // 防止 value 也为 null
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF3B7CFF)
            )
            // 【关键修复】只有当 unit 不为空且不为空白字符时才显示
            if (!unit.isNullOrBlank()) {
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    color = Color(0xFF3B7CFF),
                    modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                )
            }
        }
    }
}

@Composable
fun StatusIndicator(label: String, status: String, isNormal: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: ", fontSize = 12.sp, color = Color(0xFF999999))
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isNormal) Color(0xFF00C091) else Color(0xFFFF4D4F),
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status,
            fontSize = 12.sp,
            color = if (isNormal) Color(0xFF00C091) else Color(0xFFFF4D4F),
            fontWeight = FontWeight.Bold
        )
    }
}