package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.amap.api.maps.CameraUpdate
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.PoleMapPointReq
import com.unilumin.smartapp.client.data.PoleMapPointRes
import com.unilumin.smartapp.client.data.SiteInfo
import com.unilumin.smartapp.client.data.SiteRoadInfo
import com.unilumin.smartapp.client.service.SiteService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SiteViewModel(
    val retrofitClient: RetrofitClient, application: Application
) : BaseViewModel(application) {
    private val siteService = retrofitClient.getService(SiteService::class.java)

    private val _mapPoints = MutableStateFlow<List<PoleMapPointRes>>(emptyList())
    val mapPoints = _mapPoints.asStateFlow()
    private var mapFetchJob: Job? = null
    private var searchLocateJob: Job? = null
    private val _cameraEffect = MutableSharedFlow<CameraUpdate>()
    val cameraEffect = _cameraEffect.asSharedFlow()

    private val _selectedSiteInfo = MutableStateFlow<SiteInfo?>(null)
    val selectedSiteInfo = _selectedSiteInfo.asStateFlow()

    private val _selectedRoadId = MutableStateFlow<String?>(null)
    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword = _searchKeyword.asStateFlow()
    private var isFirstLoad = true
    private val _siteRoadInfo = MutableStateFlow<List<SiteRoadInfo>?>(null)
    val siteRoadInfo = _siteRoadInfo.asStateFlow()

    // 地图页面，需要通过站点ID查设备详情
    private val _siteDetail = MutableStateFlow<SiteInfo?>(null)
    val siteDetail = _siteDetail.asStateFlow()

    init {
        getRoadList()
    }

    fun onMapCameraChange(
        minLng: Double, maxLng: Double, minLat: Double, maxLat: Double, zoom: Float
    ) {
        mapFetchJob?.cancel()
        mapFetchJob = viewModelScope.launch {
            delay(300)
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

                val response = UniCallbackService.parseDataNewSuspend(
                    siteService.getSiteAggPoint(req)
                )

                val resultList = response ?: emptyList()
                _mapPoints.value = resultList

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

    // 站点详情，包含设备
    fun fetchSiteDetail(siteId: Long) {
        // [修复点1] 获取新详情前，先清空可能残留的旧状态
        _selectedSiteInfo.value = null
        _siteDetail.value = null

        launchWithLoading {
            val result = UniCallbackService.parseDataNewSuspend(siteService.getSiteDetail(siteId))
            _siteDetail.value = result
        }
    }

    // [修复点2] 返回时，彻底清空两种选中状态，关闭详情弹层
    fun clearSelection() {
        _selectedSiteInfo.value = null
        _siteDetail.value = null
    }

    // 站点信息(包含设备信息) - Paging3自带内存缓存，不会因为UI图层覆盖而丢失数据
    val sitePagingFlow =
        createPagingFlow(
            combine(
                _selectedRoadId,
                _searchKeyword,
                ::Pair
            )
        ) { (roadId, keyword), page, size ->
            fetchPageData {
                siteService.getSiteList(
                    curPage = page,
                    pageSize = size,
                    roadIdList = roadId?.toLongOrNull()?.let { listOf(it) },
                    tagCondition = "or",
                    keyword = keyword
                )
            }
        }

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
            try {
                val rawResponse = siteService.getSiteList(
                    curPage = 1,
                    pageSize = 1,
                    roadIdList = roadId?.toLongOrNull()?.let { listOf(it) },
                    tagCondition = "or",
                    keyword = keyword
                )
                val result = UniCallbackService.parseDataNewSuspend(rawResponse)

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
        launchDirect {
            val result = UniCallbackService.parseDataNewSuspend(
                siteService.getRoadList()
            )
            _siteRoadInfo.value = result
        }
    }
}