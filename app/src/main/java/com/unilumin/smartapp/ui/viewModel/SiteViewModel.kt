package com.unilumin.smartapp.ui.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
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
    val context: Context
) : ViewModel() {

    private val siteService = retrofitClient.getService(SiteService::class.java)

    // ================== 1. 地图相关状态 ==================
    // 地图聚合点数据
    private val _mapPoints = MutableStateFlow<List<PoleMapPointRes>>(emptyList())
    val mapPoints = _mapPoints.asStateFlow()

    // 地图 Loading 状态
    private val _isMapLoading = MutableStateFlow(false)
    val isMapLoading = _isMapLoading.asStateFlow()

    // 地图请求防抖 Job
    private var mapFetchJob: Job? = null

    // ================== 2. 详情与筛选状态 ==================
    // 当前选中的杆体详情
    private val _selectedSiteInfo = MutableStateFlow<SiteInfo?>(null)
    val selectedSiteInfo = _selectedSiteInfo.asStateFlow()
    private val _cameraEffect = MutableSharedFlow<CameraUpdate>()
    val cameraEffect = _cameraEffect.asSharedFlow()
    // 详情加载 Loading
    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading = _isDetailLoading.asStateFlow()

    // 列表总数
    private val _totalCount = MutableStateFlow(0)
    val totalCount = _totalCount.asStateFlow()

    // 筛选条件
    private val _selectedRoadId = MutableStateFlow<String?>(null)
    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword = _searchKeyword.asStateFlow()
    private var isFirstLoad = true
    // 道路元数据
    private val _siteRoadInfo = MutableStateFlow<List<SiteRoadInfo>?>(null)
    val siteRoadInfo = _siteRoadInfo.asStateFlow()

    init {
        getRoadList()
    }

    // ================== 核心方法：地图视口变化加载数据 ==================
    /**
     * @param zoom 当前地图缩放级别
     */
    fun onMapCameraChange(
        minLng: Double, maxLng: Double,
        minLat: Double, maxLat: Double,
        zoom: Float
    ) {
        // Log 调试：观察传入的 Zoom 是否正确
        Log.i("SiteViewModel", "地图视角更新 -> Zoom: $zoom, Bounds: [$minLng, $minLat] - [$maxLng, $maxLat]")

        // 1. 防抖：取消上一次未完成的请求
        mapFetchJob?.cancel()

        // 2. 启动新协程
        mapFetchJob = viewModelScope.launch {
            // 3. 延迟 350ms，防止用户滑动过程中频繁请求接口
            delay(350)

            _isMapLoading.value = true

            // 4. 根据 Zoom 计算聚合精度 (Backend Precision Strategy)
            // 阈值说明：
            // < 10: 省/市级视角，精度10 (大网格)
            // < 13: 区县级视角，精度100
            // < 15: 街道级视角，精度1000
            // >= 15: 设备级视角，精度100000 (极小网格，相当于不聚合)
            val precision = when {
                zoom < 10 -> 1       // 之前是10，现在改成1，让它在大范围时聚合得更狠
                zoom < 12 -> 10
                zoom < 14 -> 100
                zoom < 16 -> 1000
                else -> 100000
            }

            Log.i("SiteViewModel", "发起聚合请求 -> Precision: $precision")

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
                _mapPoints.value = resultList
                Log.i("SiteViewModel", "数据加载成功，点位数: ${_mapPoints.value.size}")

                if (isFirstLoad && resultList.isNotEmpty()) {
                    isFirstLoad = false // 标记已处理
                    moveToDensestCluster(resultList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SiteViewModel", "数据加载失败: ${e.message}")
            } finally {
                _isMapLoading.value = false
            }
        }
    }
    private suspend fun moveToDensestCluster(points: List<PoleMapPointRes>) {
        // 找到 count 最大的点
        val maxPoint = points.maxByOrNull { it.count }

        if (maxPoint != null) {
            Log.i("SiteViewModel", "自动定位到最密区域: [${maxPoint.lat}, ${maxPoint.lng}] 数量:${maxPoint.count}")

            // 构造相机移动指令：移动到中心点，并放大到 16 级 (街道视角)
            // 注意：这里只是发出指令，不直接操作 View
            val update = CameraUpdateFactory.newLatLngZoom(

                LatLng(
                    maxPoint.lat.toDoubleOrNull() ?: 0.0, // 转换失败则默认为 0.0
                    maxPoint.lng.toDoubleOrNull() ?: 0.0
                ),
                16f
            )
            _cameraEffect.emit(update)
        }
    }

    // ================== 点击杆体获取详情 ==================
    fun fetchSiteDetail(siteId: Long) {
//        viewModelScope.launch {
//            _isDetailLoading.value = true
//            try {
//                val result = UniCallbackService<SiteInfo>().parseDataNewSuspend(
//                    siteService.getSiteDetail(siteId),
//                    context
//                )
//                _selectedSiteInfo.value = result
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                _isDetailLoading.value = false
//            }
//        }
    }

    // 清除选中状态（关闭弹窗）
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

    // ================== 筛选条件更新 ==================
    fun updateRoadFilter(roadId: String?) {
        _selectedRoadId.value = roadId
        // 注意：UI层监听到筛选变化后，如果处于地图模式，应当触发一次刷新
    }

    fun updateSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword
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