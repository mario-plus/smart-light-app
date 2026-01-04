package com.unilumin.smartapp.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.constant.DeviceType.DETAIL
import com.unilumin.smartapp.client.constant.DeviceType.DeviceDetailTabs
import com.unilumin.smartapp.client.constant.DeviceType.EVENT
import com.unilumin.smartapp.client.constant.DeviceType.NETWORK
import com.unilumin.smartapp.client.constant.DeviceType.PROPERTY
import com.unilumin.smartapp.client.constant.DeviceType.TELEMETRY
import com.unilumin.smartapp.client.data.DeviceConfig
import com.unilumin.smartapp.client.data.DeviceDetail
import com.unilumin.smartapp.client.data.DeviceModelData
import com.unilumin.smartapp.client.data.DeviceRealTimeDataReq
import com.unilumin.smartapp.client.data.HistoryData
import com.unilumin.smartapp.client.data.HistoryDataReq
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.service.DeviceService
import com.unilumin.smartapp.ui.components.DetailCard
import com.unilumin.smartapp.ui.components.DetailRow
import com.unilumin.smartapp.ui.components.DeviceRealDataCardModern
import com.unilumin.smartapp.ui.components.DeviceTag
import com.unilumin.smartapp.ui.components.EmptyDataView
import com.unilumin.smartapp.ui.components.FilterChip
import com.unilumin.smartapp.ui.components.HistoryDataListView
import com.unilumin.smartapp.ui.screens.dialog.DeviceHistoryDialog
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.ControlBlue
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.theme.TextDark
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    lightDevice: LightDevice, retrofitClient: RetrofitClient, onBack: () -> Unit
) {

    var selectedDeviceModelData by remember { mutableStateOf<DeviceModelData?>(null) }

    val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")

    val context = LocalContext.current

    val deviceService = remember(retrofitClient) {
        retrofitClient.getService(DeviceService::class.java)
    }

    val scope = rememberCoroutineScope()
    // 状态管理
    var selectedLabel by remember { mutableStateOf(DETAIL) }

    var isLoading by remember { mutableStateOf(false) }

    var listDataDialog by remember { mutableStateOf(false) }
    //基础信息
    val baseInfoList = remember { mutableStateListOf<Pair<String, String>>() }
    //设备认证配置
    val deviceConfigList = remember { mutableStateListOf<Pair<String, String>>() }
    //服务
    val deviceServiceDataList = remember { mutableStateListOf<DeviceModelData>() }
    //属性
    val devicePropertiesDataList = remember { mutableStateListOf<DeviceModelData>() }
    //事件
    val deviceEventsDataList = remember { mutableStateListOf<DeviceModelData>() }
    //遥测
    val deviceTelemetryDataList = remember { mutableStateListOf<DeviceModelData>() }

    //历史数据
    val historyDataList = remember { mutableStateListOf<HistoryData>() }

    var pageIndex by remember { mutableIntStateOf(1) }

    var hasMore by remember { mutableStateOf(true) }

    // 用 Map 记录 NETWORK 和 EVENT 各自的日期
    val tabDatesMap = remember { mutableStateMapOf<String, Pair<String, String>>() }
    // 获取当前选中 Tab 的日期，如果没有则用默认值
    val currentRange = tabDatesMap[selectedLabel] ?: ("" to "")
    val currentStart = currentRange.first
    val currentEnd = currentRange.second

    suspend fun loadHistoryData(
        startTime: String,
        endTime: String,
        isRefresh: Boolean = false,
        keys: List<String>
    ) {
        if (isRefresh) {
            pageIndex = 1
            historyDataList.clear()
            hasMore = true
        }
        if (!hasMore) return
        isLoading = true
        try {
            var format = LocalDateTime.now().format(timeFormat)
            var start: String? = null
            var end: String? = null
            if (startTime.isNotBlank()) {
                start = "$startTime $format"
            }
            if (endTime.isNotBlank()) {
                end = "$endTime $format"
            }
            val response = UniCallbackService<PageResponse<HistoryData>>().parseDataNewSuspend(
                deviceService.getDeviceHistoryData(
                    HistoryDataReq(
                        deviceIds = listOf(lightDevice.id.toString()),
                        startTime = start,
                        endTime = end,
                        keys = keys,
                        curPage = pageIndex,
                        pageSize = 20
                    )
                ), context
            )
            val newList = response?.list ?: emptyList()
            val totalCount = response?.total ?: 0
            if (isRefresh) {
                historyDataList.clear()
            }
            historyDataList.addAll(newList)
            pageIndex++
            hasMore = newList.isNotEmpty() && historyDataList.size < totalCount
        } finally {
            isLoading = false
        }
    }

    //解析元数据
    fun getDeviceModelData(jsonObject: JsonObject, type: String): List<DeviceModelData> {
        val list = mutableListOf<DeviceModelData>()
        jsonObject.getAsJsonArray(type)?.forEach { element ->
            var asJsonObject = element.asJsonObject

            var specsObject = asJsonObject.get("specs")?.asJsonObject
            val unit = specsObject?.get("unit")?.takeIf { it.isJsonPrimitive }?.asString ?: ""
            list.add(
                DeviceModelData(
                    key = asJsonObject.get(
                        "id"
                    ).asString,
                    name = asJsonObject.get("name").asString,
                    keyDes = asJsonObject.get("description").asString,
                    unit = unit,
                    type = asJsonObject.get("type")?.asString ?: ""
                )
            )
        }
        return list
    }

    //获取实时数据
    suspend fun getDeviceRealData(data: List<DeviceModelData>) {
        isLoading = true
        try {
            val result = UniCallbackService<Map<String, Map<String, String>>>().parseDataNewSuspend(
                deviceService.getDeviceRealTimeData(
                    DeviceRealTimeDataReq(
                        lightDevice.id, data.map { it.key })
                ), context
            )
            var realData = result?.get(lightDevice.id.toString())
            if (realData?.isNotEmpty() == true) {
                data.forEach { e ->
                    if (realData.containsKey(e.key)) {
                        e.value = realData[e.key].toString()
                    }
                }
            }
        } finally {
            isLoading = false
        }
    }

    // 数据获取逻辑
    LaunchedEffect(selectedLabel) {
        when (selectedLabel) {
            DETAIL -> {
                isLoading = true
                try {
                    val deviceDetail = UniCallbackService<DeviceDetail>().parseDataNewSuspend(
                        deviceService.getDeviceDetail(lightDevice.id), context
                    )
                    val deviceConfig = UniCallbackService<List<DeviceConfig>>().parseDataNewSuspend(
                        deviceService.getDeviceConfig(lightDevice.id), context
                    )
                    deviceDetail?.let { detail ->
                        baseInfoList.clear()
                        detail.productFactoryName?.let { baseInfoList.add("产品厂商" to it) }
                        detail.deviceName?.let { baseInfoList.add("设备名称" to it) }
                        detail.serialNum?.let { baseInfoList.add("序列码" to it) }
                        detail.productTypeName?.let { baseInfoList.add("产品类型" to it) }
                        detail.name?.let { baseInfoList.add("产品名称" to it) }
                        detail.transportProtocol?.let { baseInfoList.add("传输协议" to it) }
                        detail.messageProtocol?.let { baseInfoList.add("协议名称" to it) }
                        detail.metadata?.let { metadataStr ->
                            try {
                                val jsonObject = JsonParser().parse(metadataStr).asJsonObject
                                val services = getDeviceModelData(jsonObject, "services")
                                val properties = getDeviceModelData(jsonObject, "properties")
                                val telemetry = getDeviceModelData(jsonObject, "telemetry")
                                val events = getDeviceModelData(jsonObject, "events")
                                deviceServiceDataList.apply { clear(); addAll(services) }
                                devicePropertiesDataList.apply { clear(); addAll(properties) }
                                deviceTelemetryDataList.apply { clear(); addAll(telemetry) }
                                deviceEventsDataList.apply { clear(); addAll(events) }
                            } catch (e: Exception) {
                                Log.e("DeviceDetail", "Metadata parse error", e)
                            }
                        }
                    }
                    deviceConfig?.let { configs ->
                        deviceConfigList.clear()
                        configs.forEach { deviceConfigList.add(it.keyDes to it.value) }
                    }
                } catch (e: Exception) {
                    Log.e("DeviceDetail", "Data fetch error", e)
                } finally {
                    isLoading = false
                }
            }

            PROPERTY -> {
                if (devicePropertiesDataList.isNotEmpty()) {
                    getDeviceRealData(devicePropertiesDataList)
                }
            }

            TELEMETRY -> {
                if (deviceTelemetryDataList.isNotEmpty()) {
                    getDeviceRealData(deviceTelemetryDataList)
                }
            }

            EVENT -> {
                loadHistoryData(
                    currentStart,
                    currentEnd,
                    isRefresh = true,
                    keys = deviceEventsDataList.map { it.key })
            }

            NETWORK -> {
                loadHistoryData(
                    currentStart,
                    currentEnd,
                    isRefresh = true,
                    keys = listOf("onLine", "offLine")
                )
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Column(modifier = Modifier.background(CardWhite)) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "${lightDevice.name}-详情",
                                style = TextStyle(
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center), color = ControlBlue
                )
            } else {
                if (selectedLabel == DETAIL || selectedLabel == TELEMETRY || selectedLabel == PROPERTY) {
                    SelectionContainer {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when (selectedLabel) {
                                DETAIL -> {
                                    item {
                                        DetailCard(title = "基础信息") {
                                            baseInfoList.forEach { (key, value) ->
                                                DetailRow(
                                                    key, value
                                                )
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
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    deviceServiceDataList.forEach { e -> DeviceTag(e.name) }
                                                }
                                            }
                                        }
                                    }
                                }

                                PROPERTY, TELEMETRY -> {
                                    val currentList = when (selectedLabel) {
                                        PROPERTY -> devicePropertiesDataList
                                        TELEMETRY -> deviceTelemetryDataList
                                        else -> emptyList()
                                    }
                                    if (currentList.isNotEmpty()) {
                                        item {
                                            FlowRow(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                maxItemsInEachRow = 3,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                currentList.forEach { data ->
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .fillMaxHeight()
                                                    ) {
                                                        DeviceRealDataCardModern(
                                                            data = data,
                                                            onHistoryClick = {
                                                                listDataDialog = true
                                                                selectedDeviceModelData =
                                                                    data.copy()
                                                            },
                                                            onAnalysisClick = {
                                                                //图表数据展示
                                                            })
                                                    }
                                                }
                                                val itemFillCount = 3 - (currentList.size % 3)
                                                if (itemFillCount < 3) {
                                                    repeat(itemFillCount) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        item {
                                            EmptyDataView("暂无数据")
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedLabel == NETWORK) {
                    var keys = listOf("onLine", "offLine")
                    HistoryDataListView(
                        limitDays = 14,
                        startDate = currentStart,
                        endDate = currentEnd,
                        historyDataList,
                        hasMore = hasMore,
                        onRangeSelected = { start, end ->
                            tabDatesMap[selectedLabel] = start to end
                            scope.launch {
                                loadHistoryData(
                                    start,
                                    end,
                                    isRefresh = true,
                                    keys = keys
                                )
                            }
                        },
                        onLoadMore = { start, end ->
                            scope.launch {
                                loadHistoryData(
                                    start,
                                    end,
                                    isRefresh = false,
                                    keys = keys
                                )
                            }
                        }
                    )
                } else if (selectedLabel == EVENT) {
                    var keys = deviceEventsDataList.map { it.key }
                    HistoryDataListView(
                        limitDays = 14,
                        startDate = currentStart,
                        endDate = currentEnd,
                        historyDataList,
                        hasMore = hasMore,
                        onRangeSelected = { start, end ->
                            tabDatesMap[selectedLabel] = start to end
                            scope.launch {
                                loadHistoryData(
                                    start,
                                    end,
                                    isRefresh = true,
                                    keys = keys
                                )
                            }
                        },
                        onLoadMore = { start, end ->
                            scope.launch {
                                loadHistoryData(
                                    start,
                                    end,
                                    isRefresh = false,
                                    keys = keys
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    if (listDataDialog && selectedDeviceModelData != null) {
        DeviceHistoryDialog(
            selectedDeviceModelData = selectedDeviceModelData,
            historyDataList = historyDataList, // 传入弹窗专用列表
            hasMore = hasMore,
            isLoading = isLoading,
            onLoadData = { start, end, refresh, keys ->
                scope.launch {
                    loadHistoryData(start, end, refresh, keys)
                }
            },
            onDismiss = {
                listDataDialog = false
                historyDataList.clear() // 关闭弹窗时重置弹窗数据池
            }
        )
    }
}

