package com.unilumin.smartapp.ui.screens.app.env

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.unilumin.smartapp.client.data.DeviceModelData
import com.unilumin.smartapp.client.data.EnvTelBO
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.DeviceStatusRow
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader
import com.unilumin.smartapp.ui.screens.dialog.ChartDataDialog
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel
import com.unilumin.smartapp.ui.viewModel.SystemViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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


    //服务
    val chartDataList by deviceViewModel.chartDataList.collectAsState()

    //卡片-图片数据
    var showChartDialog by remember { mutableStateOf(false) }


    var selectDeviceId by remember { mutableLongStateOf(0L) }

    //选中的卡片信息
    var selectedDeviceModelData by remember { mutableStateOf<DeviceModelData?>(null) }


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
                DeviceEnvItem(
                    device = device, onCardClick = { envData, deviceId ->
                        if ("long" == envData.type || "double" == envData.type) {
                            showChartDialog = true
                            selectDeviceId = deviceId
                            selectedDeviceModelData = DeviceModelData(
                                key = envData.key,
                                name = envData.name,
                                value = envData.value,
                                keyDes = envData.description,
                                unit = envData.unit,
                                type = envData.type
                            )
                        }
                    })
            }
        }
    }
    if (showChartDialog && selectedDeviceModelData != null) {
        ChartDataDialog(
            selectedDeviceModelData = selectedDeviceModelData,
            onDismiss = {
                showChartDialog = false
                deviceViewModel.clearChartData()
            },
            limitDays = 14,
            chartDataList,
            onLoadData = { start, end ->
                deviceViewModel.loadChartData(
                    selectDeviceId, start, end, selectedDeviceModelData!!.key, 2
                )
            })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceEnvItem(
    device: IotDevice, onCardClick: (EnvTelBO, Long) -> Unit
) {
    val dataList = device.telemetryList
    val maxTs = dataList.maxOfOrNull { it.ts }
    val updateTimeStr = if (maxTs != null && maxTs > 0) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(maxTs))
    } else {
        ""
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 1. 顶部设备信息
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (device.state == 1) Color(0xFFE8EFFF) else Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(14.dp)
                        ), contentAlignment = Alignment.Center
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
                        text = device.productName ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                DeviceStatus(device.state)
            }

            // 2. 更新时间显示
            if (updateTimeStr.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "最近更新时间: $updateTimeStr",
                    fontSize = 11.sp,
                    color = Color(0xFF999999)
                )
            }

            // 3. 实时数据网格 (FlowRow)
            if (dataList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
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
                            onClick = { onCardClick(data, device.id) },
                            modifier = Modifier.weight(1f, fill = true)
                        )
                    }
                    val fillCount = 3 - (dataList.size % 3)
                    if (fillCount < 3) {
                        repeat(fillCount) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

            // 4. 底部状态行
            DeviceStatusRow(
                isDisable = device.deviceState == 0,
                hasAlarm = device.alarmType == 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EnvSensorCard(
    label: String, value: String, unit: String?, onClick: () -> Unit, // 【新增】增加点击回调
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF8F9FB), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick) // 【新增】给数据卡片增加点击事件
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
            verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = value ?: "--",
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF3B7CFF)
            )
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