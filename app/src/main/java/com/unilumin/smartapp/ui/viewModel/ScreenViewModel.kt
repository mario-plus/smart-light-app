package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.LedPageBO
import com.unilumin.smartapp.client.service.ScreenService
import com.unilumin.smartapp.ui.viewModel.pages.GenericPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

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
    //关键词
    val searchQuery = MutableStateFlow("")
    fun updateSearch(query: String) {
        searchQuery.value = query
    }


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
}