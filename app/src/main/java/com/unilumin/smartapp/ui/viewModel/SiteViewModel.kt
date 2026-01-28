package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.amap.api.maps.CameraUpdate
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.PoleMapPointReq
import com.unilumin.smartapp.client.data.PoleMapPointRes
import com.unilumin.smartapp.client.data.SiteInfo
import com.unilumin.smartapp.client.data.SiteRoadInfo
import com.unilumin.smartapp.client.service.SiteService
import com.unilumin.smartapp.ui.viewModel.pages.GenericPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class SiteViewModel(
    val retrofitClient: RetrofitClient,
    application: Application
) : AndroidViewModel(application) {

    val context = getApplication<Application>()
    private val siteService = retrofitClient.getService(SiteService::class.java)

    // ================== 1. 地图相关状态 ==================
    // 地图聚合点数据
    private val _mapPoints = MutableStateFlow<List<PoleMapPointRes>>(emptyList())
    val mapPoints = _mapPoints.asStateFlow()

    // 【优化】移除了 isMapLoading 状态，实现静默加载

    // 地图请求防抖 Job
    private var mapFetchJob: Job? = null

    // 搜索跳转防抖 Job
    private var searchLocateJob: Job? = null

    // 相机移动指令流（用于搜索跳转）
    private val _cameraEffect = MutableSharedFlow<CameraUpdate>()
    val cameraEffect = _cameraEffect.asSharedFlow()

    // ================== 2. 详情与筛选状态 ==================
    private val _selectedSiteInfo = MutableStateFlow<SiteInfo?>(null)
    val selectedSiteInfo = _selectedSiteInfo.asStateFlow()

    // 详情加载 Loading (详情页可能还需要保留Loading，视需求而定，这里暂时保留详情的，只去除地图的)
    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading = _isDetailLoading.asStateFlow()

    // 列表总数
    private val _totalCount = MutableStateFlow(0)
    val totalCount = _totalCount.asStateFlow()

    // 筛选条件
    private val _selectedRoadId = MutableStateFlow<String?>(null)
    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword = _searchKeyword.asStateFlow()

    // 标记是否是首次加载地图
    private var isFirstLoad = true

    // 道路元数据
    private val _siteRoadInfo = MutableStateFlow<List<SiteRoadInfo>?>(null)
    val siteRoadInfo = _siteRoadInfo.asStateFlow()

    init {
        getRoadList()
    }

    // ================== 核心方法：地图视口变化加载数据 ==================
    fun onMapCameraChange(
        minLng: Double, maxLng: Double,
        minLat: Double, maxLat: Double,
        zoom: Float
    ) {
        // 1. 防抖
        mapFetchJob?.cancel()
        mapFetchJob = viewModelScope.launch {
            delay(300)
            // 【优化】不再设置 Loading = true，静默请求

            // 2. 动态精度计算
            val precision = when {
                zoom < 10 -> 1        // 宏观视图
                zoom < 12 -> 10       // 市级
                zoom < 14 -> 100      // 区级
                zoom < 16 -> 1000     // 街道级
                else -> 100000        // 设备级
            }
            try {
                val req = PoleMapPointReq(
                    precision = precision,
                    minLng = minLng.toString(),
                    maxLng = maxLng.toString(),
                    minLat = minLat.toString(),
                    maxLat = maxLat.toString(),
                    keyword = _searchKeyword.value.takeIf { it.isNotBlank() },
                    projectRoadId = _selectedRoadId.value?.toLongOrNull()
                )

                val response = UniCallbackService<List<PoleMapPointRes>>().parseDataNewSuspend(
                    siteService.getSiteAggPoint(req),
                    context
                )

                val resultList = response ?: emptyList()
                // 数据回来后直接更新，界面会自动刷新，无感知
                _mapPoints.value = resultList

                // 首次加载自动寻找最密集的点
                if (isFirstLoad && resultList.isNotEmpty()) {
                    isFirstLoad = false
                    findAndMoveToDensestPoint(resultList)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun findAndMoveToDensestPoint(points: List<PoleMapPointRes>) {
        val maxPoint = points.maxByOrNull { it.count }
        if (maxPoint != null) {
            val update = CameraUpdateFactory.newLatLngZoom(
                LatLng(maxPoint.lat.toDoubleOrNull() ?: 0.0, maxPoint.lng.toDoubleOrNull() ?: 0.0),
                16f
            )
            _cameraEffect.emit(update)
        }
    }

    // ================== 点击杆体获取详情 ==================
    fun fetchSiteDetail(siteId: Long) {
        // 详情获取逻辑
    }

    fun clearSelection() {
        _selectedSiteInfo.value = null
    }

    // ================== 列表分页数据流 ==================
    @OptIn(ExperimentalCoroutinesApi::class)
    val sitePagingFlow = combine(_selectedRoadId, _searchKeyword) { roadId, keyword ->
        Pair(roadId, keyword)
    }.flatMapLatest { (roadId, keyword) ->
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
            pagingSourceFactory = {
                GenericPagingSource { page, pageSize ->
                    getSitePages(roadId, keyword, page, pageSize)
                }
            }
        ).flow
    }.cachedIn(viewModelScope)

    private suspend fun getSitePages(roadId: String?, keyword: String, page: Int, pageSize: Int): List<SiteInfo> {
        val rawResponse = siteService.getSiteList(
            curPage = page,
            pageSize = pageSize,
            roadIdList = roadId?.toLongOrNull()?.let { listOf(it) },
            tagCondition = "or",
            keyword = keyword
        )
        val result = UniCallbackService<PageResponse<SiteInfo>>().parseDataNewSuspend(rawResponse, context)
        if (page == 1) _totalCount.value = result?.total ?: 0
        return result?.list ?: emptyList()
    }

    // ================== 筛选与定位 ==================

    fun updateRoadFilter(roadId: String?) {
        _selectedRoadId.value = roadId
        triggerSearchAndLocate(_searchKeyword.value, roadId)
    }

    fun updateSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword
        triggerSearchAndLocate(keyword, _selectedRoadId.value)
    }

    private fun triggerSearchAndLocate(keyword: String, roadId: String?) {
        searchLocateJob?.cancel()
        if (keyword.isBlank() && roadId == null) return

        searchLocateJob = viewModelScope.launch {
            delay(800)
            // 【优化】静默请求，不显示 Loading

            try {
                val rawResponse = siteService.getSiteList(
                    curPage = 1,
                    pageSize = 1,
                    roadIdList = roadId?.toLongOrNull()?.let { listOf(it) },
                    tagCondition = "or",
                    keyword = keyword
                )
                val result = UniCallbackService<PageResponse<SiteInfo>>().parseDataNewSuspend(rawResponse, context)

                val targetSite = result?.list?.firstOrNull()

                if (targetSite != null) {
                    val lat = targetSite.latitude
                    val lng = targetSite.longitude

                    if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                        _cameraEffect.emit(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 16f))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getRoadList() {
        viewModelScope.launch {
            try {
                val result = UniCallbackService<List<SiteRoadInfo>>().parseDataNewSuspend(
                    siteService.getRoadList(), context
                )
                _siteRoadInfo.value = result
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}