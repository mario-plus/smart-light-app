package com.unilumin.smartapp.ui.screens.device

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.DETAIL
import com.unilumin.smartapp.client.constant.DeviceConstant.DeviceDetailTabs
import com.unilumin.smartapp.client.constant.DeviceConstant.EVENT
import com.unilumin.smartapp.client.constant.DeviceConstant.NETWORK
import com.unilumin.smartapp.client.constant.DeviceConstant.PROPERTY
import com.unilumin.smartapp.client.constant.DeviceConstant.TELEMETRY
import com.unilumin.smartapp.client.data.DeviceModelData
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.DetailCard
import com.unilumin.smartapp.ui.components.DetailRow
import com.unilumin.smartapp.ui.components.DeviceDataGrid
import com.unilumin.smartapp.ui.components.EmptyDataView
import com.unilumin.smartapp.ui.components.FilterChip
import com.unilumin.smartapp.ui.components.HistoryDataListView
import com.unilumin.smartapp.ui.components.LoadingContent
import com.unilumin.smartapp.ui.screens.dialog.ChartDataDialog
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel

/**
 * 设备详情页面
 * */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    iotDevice: IotDevice, retrofitClient: RetrofitClient, onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val deviceViewModel: DeviceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceViewModel(retrofitClient, application) as T
        }
    })

    // 状态管理
    var selectedLabel by remember { mutableStateOf(DETAIL) }

    // 卡片-遥测，属性历史数据
    var selectedDeviceModelData by remember { mutableStateOf<DeviceModelData?>(null) }
    var showChartDialog by remember { mutableStateOf(false) }

    // 编辑配置弹窗控制状态
    var showEditConfigSheet by remember { mutableStateOf(false) }
    val configSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 记录时间区间
    val tabDatesMap = remember { mutableStateMapOf<String, Pair<String, String>>() }
    val currentRange = tabDatesMap[selectedLabel] ?: ("" to "")
    val currentStart = currentRange.first
    val currentEnd = currentRange.second

    // 服务状态收集
    val chartDataList by deviceViewModel.chartDataList.collectAsState()
    val baseInfoList by deviceViewModel.baseInfoList.collectAsState()
    val deviceConfigList by deviceViewModel.deviceConfigList.collectAsState()
    val deviceServiceDataList by deviceViewModel.deviceServiceDataList.collectAsState()
    val devicePropertiesDataList by deviceViewModel.devicePropertiesDataList.collectAsState()
    val deviceEventsDataList by deviceViewModel.deviceEventsDataList.collectAsState()
    val deviceTelemetryDataList by deviceViewModel.deviceTelemetryDataList.collectAsState()
    val historyDataList by deviceViewModel.historyDataList.collectAsState()
    val pagingState by deviceViewModel.pagingState.collectAsState()
    val isLoading by deviceViewModel.isLoading.collectAsState()

    // 自动触发数据加载
    LaunchedEffect(selectedLabel) {
        when (selectedLabel) {
            DETAIL -> deviceViewModel.getDeviceDetail(iotDevice.id)
            PROPERTY -> deviceViewModel.getDeviceRealData(iotDevice.id, false)
            TELEMETRY -> deviceViewModel.getDeviceRealData(iotDevice.id, true)
            EVENT -> deviceViewModel.loadHistoryData(
                iotDevice.id,
                currentRange.first,
                currentRange.second,
                true,
                deviceEventsDataList.map { it.key })

            NETWORK -> deviceViewModel.loadHistoryData(
                iotDevice.id,
                currentRange.first,
                currentRange.second,
                true,
                listOf("onLine", "offLine")
            )
        }
    }
    if (showEditConfigSheet) {
        val editableConfigs = remember(deviceConfigList) {
            deviceConfigList.map { (_, value) ->
                value.key to (value.value?.toString() ?: "")
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showEditConfigSheet = false },
            sheetState = configSheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            EditConfigBottomSheetContent(
                initialConfigs = editableConfigs,
                onDismiss = { showEditConfigSheet = false },
                onSubmit = { updatedConfigs ->
                    deviceViewModel.saveDeviceConfig(iotDevice.id, updatedConfigs, onSuccess = {
                        showEditConfigSheet = false
                    })
                }
            )
        }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Column(modifier = Modifier.background(CardWhite)) {
                    CommonTopAppBar(
                        title = "设备 [${iotDevice.deviceName}] 详情", onBack = { onBack() })
                    Spacer(modifier = Modifier.height(8.dp))
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
                                                        DetailRow(key, value)
                                                    }
                                                }
                                            }
                                        }
                                        if (deviceConfigList.isNotEmpty()) {
                                            item {
                                                DetailCard(
                                                    title = "设备配置信息",
                                                    titleAction = {
                                                        TextButton(
                                                            onClick = {
                                                                showEditConfigSheet = true
                                                            },
                                                            contentPadding = PaddingValues(
                                                                horizontal = 8.dp,
                                                                vertical = 0.dp
                                                            ),
                                                            modifier = Modifier.height(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = "编辑配置",
                                                                modifier = Modifier.size(16.dp),
                                                                tint = Blue600
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "编辑配置",
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = Blue600
                                                            )
                                                        }
                                                    }
                                                ) {
                                                    deviceConfigList.forEach { (key, value) ->
                                                        DetailRow(
                                                            label = key ?: "未知",
                                                            value = value.value?.toString() ?: "--"
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
                                                            DeviceTag(e.name)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    PROPERTY, TELEMETRY -> {
                                        val (dataList, emptyText) = if (selectedLabel == PROPERTY) {
                                            devicePropertiesDataList to "暂无属性数据"
                                        } else {
                                            deviceTelemetryDataList to "暂无遥测数据"
                                        }
                                        if (dataList.isNotEmpty()) {
                                            item {
                                                DeviceDataGrid(
                                                    dataList = dataList,
                                                    onAnalysisClick = { data ->
                                                        deviceViewModel.clearChartData()
                                                        selectedDeviceModelData = data.copy()
                                                        showChartDialog = true
                                                    })
                                            }
                                        } else {
                                            item {
                                                EmptyDataView(emptyText)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    NETWORK -> {
                        val keys = listOf("onLine", "offLine")
                        HistoryDataListView(
                            limitDays = 14,
                            startDate = currentStart,
                            endDate = currentEnd,
                            historyDataList,
                            hasMore = pagingState.hasMore,
                            onRangeSelected = { start, end ->
                                tabDatesMap[selectedLabel] = start to end
                                deviceViewModel.loadHistoryData(
                                    iotDevice.id, start, end, isRefresh = true, keys = keys
                                )
                            },
                            onLoadMore = { start, end ->
                                deviceViewModel.loadHistoryData(
                                    iotDevice.id, start, end, isRefresh = false, keys = keys
                                )
                            })
                    }

                    EVENT -> {
                        val keys = deviceEventsDataList.map { it.key }
                        HistoryDataListView(
                            limitDays = 14,
                            startDate = currentStart,
                            endDate = currentEnd,
                            historyDataList,
                            hasMore = pagingState.hasMore,
                            onRangeSelected = { start, end ->
                                tabDatesMap[selectedLabel] = start to end
                                deviceViewModel.loadHistoryData(
                                    iotDevice.id, start, end, isRefresh = true, keys = keys
                                )

                            },
                            onLoadMore = { start, end ->
                                deviceViewModel.loadHistoryData(
                                    iotDevice.id, start, end, isRefresh = false, keys = keys
                                )

                            })
                    }
                }
            }
        }
    }

    // 图表弹窗
    if (showChartDialog && selectedDeviceModelData != null) {
        ChartDataDialog(selectedDeviceModelData = selectedDeviceModelData, onDismiss = {
            showChartDialog = false
        }, limitDays = 14, chartDataList, isLoading = isLoading, onLoadData = { start, end ->
            deviceViewModel.loadChartData(
                iotDevice.id, start, end, selectedDeviceModelData!!.key, 1
            )
        })
    }
}

/**
 * 美观的标签组件
 */
@Composable
fun DeviceTag(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), // 浅色背景
        shape = RoundedCornerShape(4.dp), // 轻微圆角，显得更有工业/科技感
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp, color = MaterialTheme.colorScheme.primary // 字体颜色
            )
        )
    }
}

/**
 * 设备配置编辑 BottomSheet 动态表单组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditConfigBottomSheetContent(
    initialConfigs: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSubmit: (Map<String, String>) -> Unit
) {
    // 动态管理多个表单域的状态
    val formState = remember {
        mutableStateMapOf<String, String>().apply {
            initialConfigs.forEach { (key, value) -> put(key, value) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp, top = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 头部标题与关闭按钮 ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "编辑设备配置",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "关闭", tint = Gray500)
            }
        }

        // --- 动态渲染配置项输入框 ---
        if (initialConfigs.isEmpty()) {
            Text(
                text = "暂无可编辑的配置项",
                color = Gray400,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            initialConfigs.forEach { (key, _) ->
                OutlinedTextField(
                    value = formState[key] ?: "",
                    onValueChange = { newValue -> formState[key] = newValue },
                    label = { Text(key) },
                    placeholder = { Text("请输入 $key", color = Gray400) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue600,
                        unfocusedBorderColor = Gray100,
                        focusedLabelColor = Blue600
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- 提交按钮 ---
        Button(
            onClick = { onSubmit(formState.toMap()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue600,
                disabledContainerColor = Gray100,
                disabledContentColor = Gray400
            ),
            enabled = initialConfigs.isNotEmpty() // 无配置项时不可提交
        ) {
            Text(
                text = "保存配置",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}