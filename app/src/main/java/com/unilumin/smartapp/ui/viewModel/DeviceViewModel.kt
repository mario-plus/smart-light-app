package com.unilumin.smartapp.ui.viewModel

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.constant.DeviceType
import com.unilumin.smartapp.client.data.DeviceConfig
import com.unilumin.smartapp.client.data.DeviceDetail
import com.unilumin.smartapp.client.data.DeviceModelData
import com.unilumin.smartapp.client.data.DeviceRealTimeDataReq
import com.unilumin.smartapp.client.data.HistoryData
import com.unilumin.smartapp.client.data.HistoryDataReq
import com.unilumin.smartapp.client.data.LampCtlReq
import com.unilumin.smartapp.client.data.LoopCtlReq
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.PagingState
import com.unilumin.smartapp.client.data.SequenceTsl
import com.unilumin.smartapp.client.service.DeviceService
import com.unilumin.smartapp.ui.viewModel.pages.DevicePagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import retrofit2.Call
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DeviceViewModel(
    retrofitClient: RetrofitClient, val context: Context
) : ViewModel() {
    private val deviceService = retrofitClient.getService(DeviceService::class.java)
    val currentFilter = MutableStateFlow(DeviceType.LAMP)
    val searchQuery = MutableStateFlow("")

    @RequiresApi(Build.VERSION_CODES.O)
    val timeFormat: DateTimeFormatter? = DateTimeFormatter.ofPattern("HH:mm:ss")

    // --- 状态管理 ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    //属性
    private val _propertiesList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    private val _devicePropertiesDataList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    val devicePropertiesDataList = _devicePropertiesDataList.asStateFlow()

    //服务
    private val _serviceList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    private val _deviceServiceDataList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    val deviceServiceDataList = _deviceServiceDataList.asStateFlow()

    //遥测
    private val _telemetryList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    private val _deviceTelemetryDataList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    val deviceTelemetryDataList = _deviceTelemetryDataList.asStateFlow()

    //事件
    private val _eventList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    private val _deviceEventsDataList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    val deviceEventsDataList = _deviceEventsDataList.asStateFlow()

    //历史数据
    private val _historyDataList = MutableStateFlow<List<HistoryData>>(emptyList())
    val historyDataList = _historyDataList.asStateFlow()

    private val _pagingState = MutableStateFlow<PagingState>(PagingState())
    val pagingState = _pagingState.asStateFlow()

    //基础信息
    private val _baseInfoList = MutableStateFlow<Map<String, String>>(emptyMap())
    val baseInfoList = _baseInfoList.asStateFlow()


    private val _chartDataList = MutableStateFlow<List<SequenceTsl>>(emptyList())
    val chartDataList = _chartDataList.asStateFlow()


    //设备认证配置
    private val _deviceConfigList = MutableStateFlow<Map<String, String>>(emptyMap())
    val deviceConfigList = _deviceConfigList.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val devicePagingFlow = combine(currentFilter, searchQuery) { filter, query ->
        Pair(filter, query)
    }.flatMapLatest { (filter, query) ->
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20), pagingSourceFactory = {
                DevicePagingSource(
                    filter, query, retrofitClient, context
                )
            }).flow
    }.cachedIn(viewModelScope)

    fun updateFilter(type: String) {
        currentFilter.value = type
    }

    fun updateSearch(query: String) {
        searchQuery.value = query
    }


    private fun launchWithLoading(consumer: suspend () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                consumer()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    //设备控制按钮
    suspend fun lampCtl(deviceId: Long, cmdType: Int, cmdValue: Int) {
        try {
            val call: Call<NewResponseData<String?>?>? = deviceService.lampCtl(
                LampCtlReq(
                    cmdType = cmdType,
                    cmdValue = cmdValue,
                    ids = listOf(deviceId),
                    subSystemType = 1
                )
            )
            UniCallbackService<String>().parseDataNewSuspend(call, context)
            Toast.makeText(context, "操作成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //设备控制按钮
    suspend fun loopCtl(id: Long, numList: List<Int>, onOff: Int) {
        try {
            val call: Call<NewResponseData<String?>?>? = deviceService.loopCtl(
                LoopCtlReq(listOf(id), numList, onOff)
            )
            UniCallbackService<String>().parseDataNewSuspend(call, context)
            Toast.makeText(context, "操作成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //图表数据
    fun loadChartData(
        deviceId: Long, startTime: String, endTime: String, currentData: DeviceModelData
    ) {
        launchWithLoading {
            try {
                val startFormatted = if (startTime.isNotBlank()) "$startTime 00:00:00" else ""
                val endFormatted = if (endTime.isNotBlank()) "$endTime 23:59:59" else ""
                val response = UniCallbackService<List<SequenceTsl>>().parseDataNewSuspend(
                    deviceService.getSequenceTsl(
                        deviceId = deviceId,
                        id = currentData.key,
                        type = 1,
                        startTime = startFormatted,
                        endTime = endFormatted,
                        isAggregation = false
                    ), context
                )
                _chartDataList.value = response ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                _chartDataList.value = emptyList()
            }
        }
    }


    /**
     * 获取实时数据（通用方法）
     * @param deviceId 设备ID
     * @param isTelemetry 是否为遥测数据（true: 遥测, false: 属性）
     */
    fun getDeviceRealData(deviceId: Long, isTelemetry: Boolean) {
        launchWithLoading {
            val sourceTemplate = if (isTelemetry) {
                _telemetryList.value
            } else {
                _propertiesList.value
            }
            try {
                val result =
                    UniCallbackService<Map<String, Map<String, String>>>().parseDataNewSuspend(
                        deviceService.getDeviceRealTimeData(
                            DeviceRealTimeDataReq(deviceId, sourceTemplate.map { it.key })
                        ), context
                    )
                val realDataMap = result?.get(deviceId.toString()) ?: emptyMap()
                val updatedList = sourceTemplate.map { templateItem ->
                    val newValue = realDataMap[templateItem.key] ?: ""
                    templateItem.copy(value = newValue.toString())
                }
                if (isTelemetry) {
                    _deviceTelemetryDataList.value = updatedList
                } else {
                    _devicePropertiesDataList.value = updatedList
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 设备详情
     * */
    fun getDeviceDetail(deviceId: Long) {
        launchWithLoading {
            val deviceDetail = UniCallbackService<DeviceDetail>().parseDataNewSuspend(
                deviceService.getDeviceDetail(deviceId), context
            )
            deviceDetail?.let { detail ->
                _baseInfoList.value = buildMap {
                    detail.productFactoryName?.let { put("产品厂商", it) }
                    detail.deviceName?.let { put("设备名称", it) }
                    detail.serialNum?.let { put("序列码", it) }
                    detail.productTypeName?.let { put("产品类型", it) }
                    detail.name?.let { put("产品名称", it) }
                    detail.transportProtocol?.let { put("传输协议", it) }
                    detail.messageProtocol?.let { put("协议名称", it) }
                }

                detail.metadata?.let { metadataStr ->
                    val jsonObject = JsonParser().parse(metadataStr).asJsonObject
                    _serviceList.value = getDeviceModelData(jsonObject, "services")
                    _deviceServiceDataList.value = _serviceList.value
                    _propertiesList.value = getDeviceModelData(jsonObject, "properties")
                    _telemetryList.value = getDeviceModelData(jsonObject, "telemetry")
                    _eventList.value = getDeviceModelData(jsonObject, "events")
                }
                val deviceConfig = UniCallbackService<List<DeviceConfig>>().parseDataNewSuspend(
                    deviceService.getDeviceConfig(deviceId), context
                )
                deviceConfig?.let { configs ->
                    val newConfigMap = mutableMapOf<String, String>()
                    configs.forEach { config ->
                        newConfigMap[config.keyDes] = config.value
                    }
                    _deviceConfigList.value = newConfigMap
                }

            }
        }
    }

    /**
     * 历史数据
     * */
    @RequiresApi(Build.VERSION_CODES.O)
     fun loadHistoryData(
        deviceId: Long,
        startTime: String, endTime: String, isRefresh: Boolean = false, keys: List<String>
    ) {
        launchWithLoading {
            if (isRefresh) {
                _pagingState.value.pageIndex = 1
                _historyDataList.value = emptyList()
                _pagingState.value.hasMore = true
            }
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
                        deviceIds = listOf(deviceId.toString()),
                        startTime = start,
                        endTime = end,
                        keys = keys,
                        curPage = _pagingState.value.pageIndex,
                        pageSize = 20
                    )
                ), context
            )
            val newList = response?.list ?: emptyList()
            val totalCount = response?.total ?: 0
            if (isRefresh) {
                _historyDataList.value = emptyList()
            }
            _historyDataList.value = newList
            _pagingState.value.pageIndex = _pagingState.value.pageIndex + 1
            _pagingState.value.hasMore =
                newList.isNotEmpty() && _historyDataList.value.size < totalCount
        }
    }


    /**
     * 解析元数据
     * */
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
}