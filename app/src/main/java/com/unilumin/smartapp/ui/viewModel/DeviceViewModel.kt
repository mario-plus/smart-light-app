package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
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
import com.unilumin.smartapp.client.data.HistoryData
import com.unilumin.smartapp.client.data.HistoryDataReq
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.client.data.IotProductDetail
import com.unilumin.smartapp.client.data.OfflineDevice
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.PagingState
import com.unilumin.smartapp.client.data.SequenceTsl
import com.unilumin.smartapp.client.service.DeviceService
import com.unilumin.smartapp.ui.viewModel.pages.GenericPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    val context = getApplication<Application>()

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


    /**
     * 产品遥测信息
     * */
    private val _productTel = MutableStateFlow<Map<Long, List<DeviceModelData>>>(emptyMap())


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

    /**
     * 环境传感器数据，实现过程
     * 加载设备列表数据
     * 加载产品物模型，获取遥测数据
     * 获取实时遥测数据，直接显示，对于传感器，遥测数据，基本就是实时监控数据
     * 这种方式，新增的产品的，不用修改代码就能兼容
     * 下拉加载分页数据
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    val envDevicePagingFlow =
        combine(state, productType, searchQuery) { state, productType, keywords ->
            Triple(state, productType, keywords)
        }.flatMapLatest { (state, productType, keywords) ->
            Pager(
                config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
                pagingSourceFactory = {
                    GenericPagingSource { page, pageSize ->
                        var deviceList = getDeviceList(
                            state = state,
                            productType = productType.toLong(),
                            searchQuery = keywords,
                            page = page,
                            pageSize = pageSize,
                            context = context
                        )
                        coroutineScope {
                            deviceList.map { device ->
                                async {
                                    fillRealTimeData(device)
                                }
                            }.awaitAll()
                        }
                        deviceList
                    }
                }).flow
        }.cachedIn(viewModelScope)

    /**
     * 填充设备实时信息
     * */
    suspend fun fillRealTimeData(device: IotDevice) {
        val productId = device.productId?.toLong() ?: return
        val baseTelList = _productTel.value[productId]
        val sourceTemplateKeys = baseTelList?.mapNotNull { it.key }
        if (!sourceTemplateKeys.isNullOrEmpty()) {
            val deviceId = device.id
            try {
                val realTimeDataMap =
                    UniCallbackService<Map<String, Map<String, String>>>().parseDataNewSuspend(
                        deviceService.getDeviceRealTimeData(
                            DeviceRealTimeDataReq(deviceId, sourceTemplateKeys)
                        ), context
                    )
                val realTimeValues = realTimeDataMap?.get(deviceId.toString())
                val updatedList = baseTelList.map { modelData ->
                    val newValue = realTimeValues?.get(modelData.key)
                    modelData.copy(value = newValue)
                }
                device.telemetryList = updatedList
            } catch (e: Exception) {
                Log.e("Paging", "Fetch real-time data failed for ${device.id}", e)
            }
        }
    }

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


    /**
     * 产品遥测
     * */
    /**
     * 支持多类型初始化产品遥测数据
     * @param productTypeIds 产品类型 ID 列表
     * @return 聚合后的 Map<产品ID, 遥测数据列表>
     */
    suspend fun getProductTels(productTypeIds: List<Int>): Map<Long/**productId*/, List<DeviceModelData>> {
        return coroutineScope {
            val deferredResults = productTypeIds.map { typeId ->
                async {
                    UniCallbackService<List<IotProductDetail>>().parseDataNewSuspend(
                        deviceService.getProductList(typeId),
                        context
                    )
                }
            }
            val allProducts = deferredResults.awaitAll()
                .filterNotNull()
                .flatten()
            allProducts.mapNotNull { product ->
                val productId = product.id ?: return@mapNotNull null
                val deviceModelList = product.metadata?.let { metadataStr ->
                    try {
                        val jsonObject = JsonParser().parse(metadataStr).asJsonObject
                        getDeviceModelData(jsonObject, "telemetry")
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                productId to deviceModelList
            }.toMap()
        }
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