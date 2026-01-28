package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.DeviceConfig
import com.unilumin.smartapp.client.data.DeviceDetail
import com.unilumin.smartapp.client.data.DeviceModelData
import com.unilumin.smartapp.client.data.DeviceRealTimeDataReq
import com.unilumin.smartapp.client.data.DeviceStatusAnalysisResp
import com.unilumin.smartapp.client.data.DeviceStatusSummary
import com.unilumin.smartapp.client.data.HistoryData
import com.unilumin.smartapp.client.data.HistoryDataReq
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.client.data.LampCtlReq
import com.unilumin.smartapp.client.data.LampLightInfo
import com.unilumin.smartapp.client.data.LightDayEnergy
import com.unilumin.smartapp.client.data.LightEnergy
import com.unilumin.smartapp.client.data.LightYearEnergy
import com.unilumin.smartapp.client.data.LoopCtlReq
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.OfflineDevice
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.PagingState
import com.unilumin.smartapp.client.data.SequenceTsl
import com.unilumin.smartapp.client.service.DeviceService
import com.unilumin.smartapp.ui.viewModel.pages.GenericPagingSource
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
    retrofitClient: RetrofitClient, application: Application
) : AndroidViewModel(application) {
    val context = getApplication<Application>()


    //分页数据总数
    private val _totalCount = MutableStateFlow<Int>(0)
    val totalCount = _totalCount.asStateFlow()


    private val deviceService = retrofitClient.getService(DeviceService::class.java)

    //设备列表查询参数(产品类型)
    val productType = MutableStateFlow("1")
    fun updateFilter(type: String) {
        productType.value = type
    }

    //0离线，1在线
    val state = MutableStateFlow(-1)
    fun updateState(s: Int) {
        state.value = s
    }

    //设备列表查询参数(关键词)
    val searchQuery = MutableStateFlow("")
    fun updateSearch(query: String) {
        searchQuery.value = query
    }


    //图表类型(最近活跃时间，最近七天，最近30天，最近90天)
    val chartType = MutableStateFlow(0)
    fun updateChartType(query: Int) {
        chartType.value = query
    }

    //产品类型
    val primaryClass = MutableStateFlow(0)
    fun updatePrimary(type: Int) {
        primaryClass.value = type
    }

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

    private val _pagingState = MutableStateFlow(PagingState())
    val pagingState = _pagingState.asStateFlow()

    //基础信息
    private val _baseInfoList = MutableStateFlow<Map<String, String>>(emptyMap())
    val baseInfoList = _baseInfoList.asStateFlow()


    //遥测，属性统计报表数据
    private val _chartDataList = MutableStateFlow<List<SequenceTsl>>(emptyList())
    val chartDataList = _chartDataList.asStateFlow()


    //设备认证配置
    private val _deviceConfigList = MutableStateFlow<Map<String, String>>(emptyMap())
    val deviceConfigList = _deviceConfigList.asStateFlow()


    //离线图表数据
    private val _deviceStatusAnalysis = MutableStateFlow<DeviceStatusAnalysisResp?>(null)
    val deviceStatusAnalysisData = _deviceStatusAnalysis.asStateFlow()


    //设备列表分页数据列表
    @OptIn(ExperimentalCoroutinesApi::class)
    val devicePagingFlow =
        combine(state, productType, searchQuery) { state, productType, keywords ->
            Triple(state, productType, keywords)
        }.flatMapLatest { (state, productType, keywords) ->
            Pager(
                config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
                pagingSourceFactory = {
                    GenericPagingSource { page, pageSize ->
                        getDeviceList(
                            state = state,
                            productType = productType.toLong(),
                            searchQuery = keywords,
                            page = page,
                            pageSize = pageSize,
                            context = context
                        )
                    }
                }).flow
        }.cachedIn(viewModelScope)


    @OptIn(ExperimentalCoroutinesApi::class)
    val offlineDeviceList = combine(chartType, primaryClass) { timeType, primaryClass ->
        Pair(timeType, primaryClass)
    }.flatMapLatest { (filter, query) ->
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
            pagingSourceFactory = {
                GenericPagingSource { page, pageSize ->
                    getOfflineDeviceList(
                        timeType = filter, curPage = page, pageSize = pageSize, primaryClass = query
                    )

                }
            }).flow
    }.cachedIn(viewModelScope)


    suspend fun getOfflineDeviceList(
        curPage: Int, pageSize: Int, timeType: Int, primaryClass: Int
    ): List<OfflineDevice> {
        val tType = timeType.takeIf { it != 0 }
        val pClass = primaryClass.takeIf { it != 0 }
        val parseDataNewSuspend =
            UniCallbackService<PageResponse<OfflineDevice>>().parseDataNewSuspend(
                deviceService.offlineDeviceList(curPage, pageSize, tType, pClass), context
            )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }




    //获取设备列表
    suspend fun getDeviceList(
        state: Int,
        productType: Long,
        searchQuery: String,
        page: Int,
        pageSize: Int,
        context: Context
    ): List<IotDevice> {
        return getIotDevices(
            state, productType, deviceService, searchQuery, page, pageSize, context
        )
//        if (type == DeviceType.LAMP) {
//            var parseDataNewSuspend =
//                UniCallbackService<PageResponse<LightDevice>>().parseDataNewSuspend(
//                    deviceService.getLightCtlList(
//                        RequestParam(searchQuery, page, pageSize)
//                    ), context
//                )
//            _totalCount.value = parseDataNewSuspend?.total!!
//            return parseDataNewSuspend.list
//        } else if (type == DeviceType.CONCENTRATOR) {
//            var parseDataNewSuspend =
//                UniCallbackService<PageResponse<LightDevice>>().parseDataNewSuspend(
//                    deviceService.getGwCtlList(
//                        RequestParam(searchQuery, page, pageSize, 1)
//                    ), context
//                )
//            _totalCount.value = parseDataNewSuspend?.total!!
//            return parseDataNewSuspend.list
//        } else if (type == DeviceType.LOOP) {
//            var parseDataNewSuspend =
//                UniCallbackService<PageResponse<LightDevice>>().parseDataNewSuspend(
//                    deviceService.getLoopCtlList(searchQuery, page, pageSize, 1), context
//                )
//            _totalCount.value = parseDataNewSuspend?.total!!
//            return parseDataNewSuspend.list
//        } else if (type == DeviceType.PLAY_BOX) {
//            var parseDataNewSuspend =
//                UniCallbackService<PageResponse<LightDevice>>().parseDataNewSuspend(
//                    deviceService.getLedList(searchQuery, page, pageSize, 12, 3), context
//                )
//            _totalCount.value = parseDataNewSuspend?.total!!
//            return parseDataNewSuspend.list
//        } else if (type == DeviceType.ENV) {
//            var iotDevices =
//                getIotDevices(type, deviceService, searchQuery, page, pageSize, context)
//            val deviceIds = iotDevices.map { it.id }
//            //填充环境传感器设备数据
//            val envDataMap = if (deviceIds.isNotEmpty()) {
//                UniCallbackService<Map<Long, EnvData>>().parseDataNewSuspend(
//                    deviceService.getEnvDataList(EnvDataReq(deviceIds)), context
//                ) ?: emptyMap()
//            } else {
//                emptyMap()
//            }
//            iotDevices.forEach { device ->
//                val envData = envDataMap[device.id]
//                device.envData = envData
//            }
//            return iotDevices
//        }
//        return emptyList()
    }

    suspend fun getIotDevices(
        state: Int,
        productType: Long,
        deviceService: DeviceService,
        searchQuery: String,
        page: Int,
        pageSize: Int,
        context: Context
    ): List<IotDevice> {
        val s = state.takeIf { it != -1 }
        val parseDataNewSuspend = UniCallbackService<PageResponse<IotDevice>>().parseDataNewSuspend(
            deviceService.getDeviceList(
                searchQuery, page, pageSize, productType, s
            ), context
        )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }


    fun launchWithLoading(consumer: suspend () -> Unit) {
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



    /**
     * 离线报表统计信息
     * */
    fun deviceStatusAnalysis() {
        launchWithLoading {
            try {
                var parseDataNewSuspend =
                    UniCallbackService<DeviceStatusAnalysisResp>().parseDataNewSuspend(
                        deviceService.deviceStatusAnalysis(), context
                    )
                _deviceStatusAnalysis.value = parseDataNewSuspend
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
     * 历史数据，暂不修改
     * */
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadHistoryData(
        deviceId: Long,
        startTime: String,
        endTime: String,
        isRefresh: Boolean = false,
        keys: List<String>
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
            val asJsonObject = element.asJsonObject

            val specsObject = asJsonObject.get("specs")?.asJsonObject
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