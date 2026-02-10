package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import android.os.Build
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
import com.unilumin.smartapp.client.data.DeviceModelData
import com.unilumin.smartapp.client.data.DeviceRealTimeDataReq
import com.unilumin.smartapp.client.data.DeviceStatusAnalysisResp
import com.unilumin.smartapp.client.data.EnvDataReq
import com.unilumin.smartapp.client.data.EnvReq
import com.unilumin.smartapp.client.data.HistoryData
import com.unilumin.smartapp.client.data.HistoryDataReq
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.client.data.OfflineDevice
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DeviceViewModel(
    retrofitClient: RetrofitClient, application: Application
) : AndroidViewModel(application) {

    private val deviceService = retrofitClient.getService(DeviceService::class.java)
    private val _totalCount = MutableStateFlow<Int>(0)
    val totalCount = _totalCount.asStateFlow()

    val productType = MutableStateFlow("1")
    fun updateFilter(type: String) {
        productType.value = type
    }


    val state = MutableStateFlow(-1)
    fun updateState(s: Int) {
        state.value = s
    }

    val searchQuery = MutableStateFlow("")
    fun updateSearch(query: String) {
        searchQuery.value = query
    }

    val chartType = MutableStateFlow(0)
    fun updateChartType(query: Int) {
        chartType.value = query
    }

    val primaryClass = MutableStateFlow(0)
    fun updatePrimary(type: Int) {
        primaryClass.value = type
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val timeFormat: DateTimeFormatter? = DateTimeFormatter.ofPattern("HH:mm:ss")

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _propertiesList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    private val _devicePropertiesDataList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    val devicePropertiesDataList = _devicePropertiesDataList.asStateFlow()

    private val _serviceList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    private val _deviceServiceDataList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    val deviceServiceDataList = _deviceServiceDataList.asStateFlow()

    private val _telemetryList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    private val _deviceTelemetryDataList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    val deviceTelemetryDataList = _deviceTelemetryDataList.asStateFlow()

    private val _eventList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    private val _deviceEventsDataList = MutableStateFlow<List<DeviceModelData>>(emptyList())
    val deviceEventsDataList = _deviceEventsDataList.asStateFlow()

    private val _historyDataList = MutableStateFlow<List<HistoryData>>(emptyList())
    val historyDataList = _historyDataList.asStateFlow()

    private val _pagingState = MutableStateFlow(PagingState())
    val pagingState = _pagingState.asStateFlow()

    private val _baseInfoList = MutableStateFlow<Map<String, String>>(emptyMap())
    val baseInfoList = _baseInfoList.asStateFlow()

    private val _chartDataList = MutableStateFlow<List<SequenceTsl>>(emptyList())
    val chartDataList = _chartDataList.asStateFlow()
    fun clearChartData() {
        _chartDataList.value = emptyList()
    }

    private val _deviceConfigList = MutableStateFlow<Map<String, String>>(emptyMap())
    val deviceConfigList = _deviceConfigList.asStateFlow()

    private val _deviceStatusAnalysis = MutableStateFlow<DeviceStatusAnalysisResp?>(null)
    val deviceStatusAnalysisData = _deviceStatusAnalysis.asStateFlow()


    // Paging Flows
    @OptIn(ExperimentalCoroutinesApi::class)
    val devicePagingFlow =
        combine(state, productType, searchQuery) { state, productType, keywords ->
            Triple(state, productType, keywords)
        }.flatMapLatest { (state, productType, keywords) ->
            Pager(
                config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
                pagingSourceFactory = {
                    GenericPagingSource { page, pageSize ->
                        getIotDevices(
                            state = state,
                            productType = productType.toLong(),
                            searchQuery = keywords,
                            page = page,
                            pageSize = pageSize
                        )
                    }
                }).flow
        }.cachedIn(viewModelScope)


    @OptIn(ExperimentalCoroutinesApi::class)
    val envDevicePagingFlow =
        combine(state, productType, searchQuery) { state, productType, keywords ->
            Triple(state, productType, keywords)
        }.flatMapLatest { (state, productType, keywords) ->
            Pager(
                config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
                pagingSourceFactory = {
                    GenericPagingSource { page, pageSize ->
                        var deviceList = getEnvDevices(
                            state = state,
                            productType = productType.toLong(),
                            searchQuery = keywords,
                            page = page,
                            pageSize = pageSize
                        )

                        deviceList
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
        val parseDataNewSuspend = UniCallbackService.parseDataNewSuspend(
            deviceService.offlineDeviceList(curPage, pageSize, tType, pClass)
        )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }

    /**
     * 获取环境传感器设备列表+实时数据
     * */
    suspend fun getEnvDevices(
        state: Int,
        productType: Long,
        searchQuery: String,
        page: Int,
        pageSize: Int,
    ): List<IotDevice> {
        // 1. 处理状态参数
        val s = state.takeIf { it != -1 }
        // 2. 获取设备基础列表
        val deviceResponse = UniCallbackService.parseDataNewSuspend(
            deviceService.getDeviceList(searchQuery, page, pageSize, productType, s)
        ) ?: return emptyList()
        val deviceList = deviceResponse.list ?: return emptyList()
        _totalCount.value = deviceResponse.total ?: 0
        if (deviceList.isEmpty()) return emptyList()
        val envReqList = deviceList.map {
            EnvReq(deviceId = it.id, productId = it.productId)
        }
        val envDataReq = EnvDataReq(envReqList)
        val envDataResponse = UniCallbackService.parseDataNewSuspend(
            deviceService.getEnvDataList(envDataReq)
        )
        envDataResponse?.let { dataMap ->
            deviceList.forEach { device ->
                device.telemetryList = dataMap[device.id] ?: emptyList()
            }
        }

        return deviceList
    }

    suspend fun getIotDevices(
        state: Int,
        productType: Long,
        searchQuery: String,
        page: Int,
        pageSize: Int,
    ): List<IotDevice> {
        val s = state.takeIf { it != -1 }
        val parseDataNewSuspend = UniCallbackService.parseDataNewSuspend(
            deviceService.getDeviceList(
                searchQuery, page, pageSize, productType, s
            )
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

    fun deviceStatusAnalysis() {
        launchWithLoading {
            try {
                var parseDataNewSuspend = UniCallbackService.parseDataNewSuspend(
                    deviceService.deviceStatusAnalysis()
                )
                _deviceStatusAnalysis.value = parseDataNewSuspend
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadChartData(
        deviceId: Long, startTime: String, endTime: String, key: String, type: Int
    ) {
        launchWithLoading {
            try {
                val startFormatted = if (startTime.isNotBlank()) "$startTime 00:00:00" else ""
                val endFormatted = if (endTime.isNotBlank()) "$endTime 23:59:59" else ""
                val response = UniCallbackService.parseDataNewSuspend(
                    deviceService.getSequenceTsl(
                        deviceId = deviceId,
                        id = key,
                        type = type,
                        startTime = startFormatted,
                        endTime = endFormatted,
                        isAggregation = false
                    )
                )
                _chartDataList.value = response ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                _chartDataList.value = emptyList()
            }
        }
    }

    fun getDeviceRealData(deviceId: Long, isTelemetry: Boolean) {
        launchWithLoading {
            val sourceTemplate = if (isTelemetry) {
                _telemetryList.value
            } else {
                _propertiesList.value
            }
            try {
                val result = UniCallbackService.parseDataNewSuspend(
                    deviceService.getDeviceRealTimeData(
                        DeviceRealTimeDataReq(deviceId, sourceTemplate.map { it.key })
                    )
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

    fun getDeviceDetail(deviceId: Long) {
        launchWithLoading {
            val deviceDetail = UniCallbackService.parseDataNewSuspend(
                deviceService.getDeviceDetail(deviceId)
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
                val deviceConfig = UniCallbackService.parseDataNewSuspend(
                    deviceService.getDeviceConfig(deviceId)
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
            val response = UniCallbackService.parseDataNewSuspend(
                deviceService.getDeviceHistoryData(
                    HistoryDataReq(
                        deviceIds = listOf(deviceId.toString()),
                        startTime = start,
                        endTime = end,
                        keys = keys,
                        curPage = _pagingState.value.pageIndex,
                        pageSize = 20
                    )
                )
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

    fun getDeviceModelData(jsonObject: JsonObject, type: String): List<DeviceModelData> {
        val list = mutableListOf<DeviceModelData>()
        jsonObject.getAsJsonArray(type)?.forEach { element ->
            val asJsonObject = element.asJsonObject
            val specsObject = asJsonObject.get("specs")?.asJsonObject
            val unit = specsObject?.get("unit")?.takeIf { it.isJsonPrimitive }?.asString ?: ""
            list.add(
                DeviceModelData(
                    key = asJsonObject.get("id").asString,
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