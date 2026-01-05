package com.unilumin.smartapp.ui.screens.device

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceType.DETAIL
import com.unilumin.smartapp.client.constant.DeviceType.DeviceDetailTabs
import com.unilumin.smartapp.client.constant.DeviceType.EVENT
import com.unilumin.smartapp.client.constant.DeviceType.NETWORK
import com.unilumin.smartapp.client.constant.DeviceType.PROPERTY
import com.unilumin.smartapp.client.constant.DeviceType.TELEMETRY
import com.unilumin.smartapp.client.data.DeviceModelData
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.ui.components.DetailCard
import com.unilumin.smartapp.ui.components.DetailRow
import com.unilumin.smartapp.ui.components.DeviceDataGrid
import com.unilumin.smartapp.ui.components.DeviceTag
import com.unilumin.smartapp.ui.components.EmptyDataView
import com.unilumin.smartapp.ui.components.FilterChip
import com.unilumin.smartapp.ui.components.HistoryDataListView
import com.unilumin.smartapp.ui.components.LoadingContent
import com.unilumin.smartapp.ui.screens.dialog.ChartDataDialog
import com.unilumin.smartapp.ui.screens.dialog.DeviceHistoryDialog
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.theme.TextDark
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    lightDevice: LightDevice, retrofitClient: RetrofitClient, onBack: () -> Unit
) {

    val context = LocalContext.current

    val deviceViewModel: DeviceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceViewModel(retrofitClient, context) as T
        }
    })
    // 状态管理
    var selectedLabel by remember { mutableStateOf(DETAIL) }

    //卡片-历史数据弹窗
    var listDataDialog by remember { mutableStateOf(false) }
    //选中的卡片信息
    var selectedDeviceModelData by remember { mutableStateOf<DeviceModelData?>(null) }
    //卡片-图片数据
    var showChartDialog by remember { mutableStateOf(false) }

    //记录时间区间
    val tabDatesMap = remember { mutableStateMapOf<String, Pair<String, String>>() }
    val currentRange = tabDatesMap[selectedLabel] ?: ("" to "")
    val currentStart = currentRange.first
    val currentEnd = currentRange.second

    /**
     * 数据观察
     * */
    //服务
    val chartDataList by deviceViewModel.chartDataList.collectAsState()
    //基础信息
    val baseInfoList by deviceViewModel.baseInfoList.collectAsState()
    //设备认证配置
    val deviceConfigList by deviceViewModel.deviceConfigList.collectAsState()
    //服务
    val deviceServiceDataList by deviceViewModel.deviceServiceDataList.collectAsState()
    //属性
    val devicePropertiesDataList by deviceViewModel.devicePropertiesDataList.collectAsState()
    //事件
    val deviceEventsDataList by deviceViewModel.deviceEventsDataList.collectAsState()
    //遥测
    val deviceTelemetryDataList by deviceViewModel.deviceTelemetryDataList.collectAsState()
    //历史数据
    val historyDataList by deviceViewModel.historyDataList.collectAsState()
    //分页状态
    val pagingState by deviceViewModel.pagingState.collectAsState()
    // 观察 ViewModel 状态
    val isLoading by deviceViewModel.isLoading.collectAsState()


    // 自动触发数据加载
    LaunchedEffect(selectedLabel) {
        when (selectedLabel) {
            DETAIL -> deviceViewModel.getDeviceDetail(lightDevice.id)
            PROPERTY -> deviceViewModel.getDeviceRealData(lightDevice.id, false)
            TELEMETRY -> deviceViewModel.getDeviceRealData(lightDevice.id, true)
            EVENT -> deviceViewModel.loadHistoryData(
                lightDevice.id,
                currentRange.first,
                currentRange.second,
                true,
                deviceEventsDataList.map { it.key })

            NETWORK -> deviceViewModel.loadHistoryData(
                lightDevice.id,
                currentRange.first,
                currentRange.second,
                true,
                listOf("onLine", "offLine")
            )
        }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Column(modifier = Modifier.background(CardWhite)) {
                    CenterAlignedTopAppBar(
                        title = {
                        Text(
                            text = "${lightDevice.name}-详情", style = TextStyle(
                                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark
                            )
                        )
                    },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "返回",
                                    tint = TextDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                    // 4. 替换为 LazyRow 实现的滑动 Chip Tabs
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(DeviceDetailTabs) { (id, label) ->
                            val isSelected = selectedLabel == id
                            FilterChip(
                                label = label,
                                isActive = isSelected,
                                onClick = { selectedLabel = id })
                        }
                    }
                }
            }
        }, containerColor = PageBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LoadingContent(isLoading = isLoading) {
                when (selectedLabel) {
                    DETAIL, TELEMETRY, PROPERTY -> {
                        SelectionContainer {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                when (selectedLabel) {
                                    DETAIL -> {

                                        if (baseInfoList.isNotEmpty()) {
                                            item {
                                                DetailCard(title = "基础信息") {
                                                    baseInfoList.forEach { (key, value) ->
                                                        DetailRow(
                                                            key, value
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (deviceConfigList.isNotEmpty()) {
                                            item {
                                                DetailCard(title = "设备配置信息") {
                                                    deviceConfigList.forEach { (key, value) ->
                                                        DetailRow(
                                                            key, value
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (deviceServiceDataList.isNotEmpty()) {
                                            item {
                                                DetailCard(title = "设备功能") {
                                                    FlowRow(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 8.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            8.dp
                                                        ),
                                                        verticalArrangement = Arrangement.spacedBy(
                                                            10.dp
                                                        )
                                                    ) {
                                                        deviceServiceDataList.forEach { e ->
                                                            DeviceTag(
                                                                e.name
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    PROPERTY -> {
                                        if (devicePropertiesDataList.isNotEmpty()) {
                                            item {
                                                DeviceDataGrid(
                                                    dataList = devicePropertiesDataList,
                                                    onHistoryClick = { data ->
                                                        listDataDialog = true
                                                        selectedDeviceModelData = data.copy()
                                                    },
                                                    onAnalysisClick = { data ->
                                                        showChartDialog = true
                                                        selectedDeviceModelData = data.copy()
                                                    })
                                            }
                                        } else {
                                            item {
                                                EmptyDataView("暂无属性数据")
                                            }

                                        }
                                    }

                                    TELEMETRY -> {
                                        if (deviceTelemetryDataList.isNotEmpty()) {
                                            item {
                                                DeviceDataGrid(
                                                    dataList = deviceTelemetryDataList,
                                                    onHistoryClick = { data ->
                                                        listDataDialog = true
                                                        selectedDeviceModelData = data.copy()
                                                    },
                                                    onAnalysisClick = { data ->
                                                        showChartDialog = true
                                                        selectedDeviceModelData = data.copy()
                                                    })
                                            }
                                        } else {
                                            item {
                                                EmptyDataView("暂无遥测数据")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    NETWORK -> {
                        var keys = listOf("onLine", "offLine")
                            HistoryDataListView(
                                limitDays = 14,
                                startDate = currentStart,
                                endDate = currentEnd,
                                historyDataList,
                                hasMore = pagingState.hasMore,
                                onRangeSelected = { start, end ->
                                    tabDatesMap[selectedLabel] = start to end
                                    deviceViewModel.loadHistoryData(
                                        lightDevice.id, start, end, isRefresh = true, keys = keys
                                    )
                                },
                                onLoadMore = { start, end ->
                                    deviceViewModel.loadHistoryData(
                                        lightDevice.id, start, end, isRefresh = false, keys = keys
                                    )
                                })
                    }

                    EVENT -> {
                        var keys = deviceEventsDataList.map { it.key }
                            HistoryDataListView(
                                limitDays = 14,
                                startDate = currentStart,
                                endDate = currentEnd,
                                historyDataList,
                                hasMore = pagingState.hasMore,
                                onRangeSelected = { start, end ->
                                    tabDatesMap[selectedLabel] = start to end
                                    deviceViewModel.loadHistoryData(
                                        lightDevice.id, start, end, isRefresh = true, keys = keys
                                    )

                                },
                                onLoadMore = { start, end ->
                                    deviceViewModel.loadHistoryData(
                                        lightDevice.id, start, end, isRefresh = false, keys = keys
                                    )

                                })
                    }
                }

            }
        }
    }

    if (listDataDialog && selectedDeviceModelData != null) {
        DeviceHistoryDialog(
            selectedDeviceModelData = selectedDeviceModelData,
            historyDataList = historyDataList,
            hasMore = pagingState.hasMore,
            onLoadData = { start, end, refresh, keys ->
                deviceViewModel.loadHistoryData(lightDevice.id, start, end, refresh, keys)
            },
            onDismiss = {
                listDataDialog = false
            })
    }

    if (showChartDialog && selectedDeviceModelData != null) {
        ChartDataDialog(selectedDeviceModelData = selectedDeviceModelData, onDismiss = {
            showChartDialog = false
        }, limitDays = 14, chartDataList, onLoadData = { start, end ->
            deviceViewModel.loadChartData(
                lightDevice.id, start, end, selectedDeviceModelData!!
            )
        })
    }
}


