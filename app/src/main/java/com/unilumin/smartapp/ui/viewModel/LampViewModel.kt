package com.unilumin.smartapp.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.LampGateWayInfo
import com.unilumin.smartapp.client.data.LampLightInfo
import com.unilumin.smartapp.client.data.LampLoopCtlInfo
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.RequestParam
import com.unilumin.smartapp.client.service.RoadService
import com.unilumin.smartapp.ui.viewModel.pages.GenericPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class LampViewModel(
    retrofitClient: RetrofitClient, val context: Context
) : ViewModel() {
    //分页数据总数
    private val _totalCount = MutableStateFlow<Int>(0)
    val totalCount = _totalCount.asStateFlow()


    private val _isSwitch = MutableStateFlow<Boolean>(false)
    val isSwitch = _isSwitch.asStateFlow()

    // --- 状态管理 ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val roadService = retrofitClient.getService(RoadService::class.java)

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

    @OptIn(ExperimentalCoroutinesApi::class)
    val lampLightFlow = combine(state, searchQuery) { state, keywords ->
        Pair(state, keywords)
    }.flatMapLatest { (state, keywords) ->
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
            pagingSourceFactory = {
                GenericPagingSource { page, pageSize ->
                    getLampLightInfoList(state, keywords, page, pageSize, context)
                }
            }).flow
    }.cachedIn(viewModelScope)


    @OptIn(ExperimentalCoroutinesApi::class)
    val lampLoopCtlFlow = combine(state, searchQuery) { state, keywords ->
        Pair(state, keywords)
    }.flatMapLatest { (state, keywords) ->
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
            pagingSourceFactory = {
                GenericPagingSource { page, pageSize ->
                    getLampLoopCtlInfoList(state, keywords, page, pageSize, context)
                }
            }).flow
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val lampGateWayFlow = combine(state, searchQuery) { state, keywords ->
        Pair(state, keywords)
    }.flatMapLatest { (state, keywords) ->
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
            pagingSourceFactory = {
                GenericPagingSource { page, pageSize ->
                    getLampGatewayInfoList(state, keywords, page, pageSize, context)
                }
            }).flow
    }.cachedIn(viewModelScope)


    /**
     * 核心优化 1: 通用的 Flow 创建器
     * @param fetcher 数据获取逻辑，传入 (page, pageSize, state, query)
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T : Any> createPagingFlow(
        fetcher: suspend (Int, Int, Int?, String) -> List<T>
    ): Flow<PagingData<T>> {
        return combine(state, searchQuery, ::Pair)
            .flatMapLatest { (currentState, currentQuery) ->
                val filterState = currentState.takeIf { it != -1 }
                Pager(
                    config = PagingConfig(
                        pageSize = 20,
                        initialLoadSize = 20,
                        prefetchDistance = 2
                    ),
                    pagingSourceFactory = {
                        GenericPagingSource { page, pageSize ->
                            fetcher(page, pageSize, filterState, currentQuery)
                        }
                    }
                ).flow
            }
            .cachedIn(viewModelScope)
    }




    suspend fun getLampLightInfoList(
        state: Int, searchQuery: String, page: Int, pageSize: Int, context: Context
    ): List<LampLightInfo> {
        val s = state.takeIf { it != -1 }
        var parseDataNewSuspend =
            UniCallbackService<PageResponse<LampLightInfo>>().parseDataNewSuspend(
                roadService.getLightCtlList(
                    RequestParam(
                        keyword = searchQuery, curPage = page, pageSize = pageSize, state = s, 1
                    )
                ), context
            )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }

    suspend fun getLampLoopCtlInfoList(
        state: Int, searchQuery: String, page: Int, pageSize: Int, context: Context
    ): List<LampLoopCtlInfo> {
        val s = state.takeIf { it != -1 }
        var parseDataNewSuspend =
            UniCallbackService<PageResponse<LampLoopCtlInfo>>().parseDataNewSuspend(
                roadService.getLoopCtlList(
                    keyword = searchQuery,
                    curPage = page,
                    pageSize = pageSize,
                    networkState = s,
                    subSystemType = 1
                ), context
            )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }

    suspend fun getLampGatewayInfoList(
        state: Int, searchQuery: String, page: Int, pageSize: Int, context: Context
    ): List<LampGateWayInfo> {
        val s = state.takeIf { it != -1 }
        var parseDataNewSuspend =
            UniCallbackService<PageResponse<LampGateWayInfo>>().parseDataNewSuspend(
                roadService.getGwCtlList(
                    RequestParam(
                        keyword = searchQuery, curPage = page, pageSize = pageSize, state = s, 1
                    )
                ), context
            )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }
}