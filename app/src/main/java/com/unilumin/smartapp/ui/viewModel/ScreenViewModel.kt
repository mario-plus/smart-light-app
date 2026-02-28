package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.LedDevGroupRes
import com.unilumin.smartapp.client.data.LedPageBO
import com.unilumin.smartapp.client.data.LedPlanBO
import com.unilumin.smartapp.client.data.LedProgramRequest
import com.unilumin.smartapp.client.data.LedProgramRes
import com.unilumin.smartapp.client.service.ScreenService
import com.unilumin.smartapp.ui.viewModel.pages.GenericPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

/**
 * 智慧屏幕
 * */
class ScreenViewModel(
    retrofitClient: RetrofitClient, application: Application
) : AndroidViewModel(application) {


    private val screenService = retrofitClient.getService(ScreenService::class.java)

    //分页数据总数
    private val _totalCount = MutableStateFlow<Int>(0)
    val totalCount = _totalCount.asStateFlow()

    //设备网络状态
    val state = MutableStateFlow(-1)
    fun updateState(s: Int) {
        state.value = s
    }

    //审核状态  全部，3待提交，0待审核，1-2已审核
    val checkState = MutableStateFlow(-1)
    fun updateCheckState(s: Int) {
        checkState.value = s
    }

    //1-控制方案，2播放方案
    val planType = MutableStateFlow(-1)
    fun updateType(type: Int) {
        planType.value = type
    }


    //关键词
    val searchQuery = MutableStateFlow("")
    fun updateSearch(query: String) {
        searchQuery.value = query
    }


    /**
     * 播放盒分页数据
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    val ledDevPagingFlow =
        combine(state, searchQuery) { state, keywords ->
            Pair(state, keywords)
        }.flatMapLatest { (state, keywords) ->
            Pager(
                config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
                pagingSourceFactory = {
                    GenericPagingSource { page, pageSize ->
                        getLedInfo(
                            state = state,
                            searchQuery = keywords,
                            page = page,
                            pageSize = pageSize
                        )
                    }
                }).flow
        }.cachedIn(viewModelScope)

    /**
     * 播放表分页数据
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    val ledProgramPagingFlow =
        combine(checkState, searchQuery) { checkState, keywords ->
            Pair(checkState, keywords)
        }.flatMapLatest { (checkState, keywords) ->
            Pager(
                config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
                pagingSourceFactory = {
                    GenericPagingSource { page, pageSize ->
                        getLedProgram(
                            checkState = checkState,
                            searchQuery = keywords,
                            page = page,
                            pageSize = pageSize
                        )
                    }
                }).flow
        }.cachedIn(viewModelScope)

    /**
     * 分组管理分页数据
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    val ledGroupPagingFlow =
        searchQuery.flatMapLatest { keywords ->
            Pager(
                config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
                pagingSourceFactory = {
                    GenericPagingSource { page, pageSize ->
                        getLedGroup(
                            searchQuery = keywords,
                            page = page,
                            pageSize = pageSize
                        )
                    }
                }
            ).flow
        }.cachedIn(viewModelScope)

    /**
     * 方案管理分页数据
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    val ledPlanPagingFlow =
        combine(planType, searchQuery) { planType, keywords ->
            Pair(planType, keywords)
        }.flatMapLatest { (planType, keywords) ->
            Pager(
                config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
                pagingSourceFactory = {
                    GenericPagingSource { page, pageSize ->
                        getLedPlans(
                            planType = planType,
                            searchQuery = keywords,
                            page = page,
                            pageSize = pageSize
                        )
                    }
                }).flow
        }.cachedIn(viewModelScope)


    suspend fun getLedInfo(
        state: Int,
        searchQuery: String,
        page: Int,
        pageSize: Int,
    ): List<LedPageBO> {
        val s = state.takeIf { it != -1 }
        val parseDataNewSuspend = UniCallbackService.parseDataNewSuspend(
            screenService.getLedList(
                searchQuery, page, pageSize, s
            )
        )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }

    suspend fun getLedProgram(
        checkState: Int,
        searchQuery: String,
        page: Int,
        pageSize: Int,
    ): List<LedProgramRes> {


        val parseDataNewSuspend = UniCallbackService.parseDataNewSuspend(
            screenService.getLedProgramList(
                LedProgramRequest(
                    keyword = searchQuery,
                    pageSize = pageSize,
                    curPage = page,
                    reviewStatus = when (checkState) {
                        -1 -> null
                        0 -> listOf(0)
                        1 -> listOf(1, 2)
                        2 -> listOf(3)
                        else -> null
                    }
                )
            )
        )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }


    suspend fun getLedGroup(
        searchQuery: String,
        page: Int,
        pageSize: Int,
    ): List<LedDevGroupRes> {
        val parseDataNewSuspend = UniCallbackService.parseDataNewSuspend(
            screenService.getLedGroupList(
                keyword = searchQuery,
                pageSize = pageSize,
                curPage = page
            )
        )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }

    suspend fun getLedPlans(
        planType: Int,
        searchQuery: String,
        page: Int,
        pageSize: Int,
    ): List<LedPlanBO> {
        val parseDataNewSuspend = UniCallbackService.parseDataNewSuspend(
            screenService.getLedPlans(
                keyword = searchQuery,
                pageSize = pageSize,
                curPage = page,
                type = planType
            )
        )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }
}