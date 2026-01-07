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
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.SiteInfo
import com.unilumin.smartapp.client.data.SiteRoadInfo
import com.unilumin.smartapp.client.service.SiteService
import com.unilumin.smartapp.ui.viewModel.pages.GenericPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class SiteViewModel(
    val retrofitClient: RetrofitClient, val context: Context
) : ViewModel() {

    private val siteService = retrofitClient.getService(SiteService::class.java)

    //分页数据总数
    private val _totalCount = MutableStateFlow<Int>(0)
    val totalCount = _totalCount.asStateFlow()

    //道路信息
    private val _siteRoadInfo = MutableStateFlow<List<SiteRoadInfo>?>(null)
    val siteRoadInfo = _siteRoadInfo.asStateFlow()
    private val _selectedRoadId = MutableStateFlow<String?>(null)
    private val _searchKeyword = MutableStateFlow<String>("")
    var searchKeyword = _searchKeyword.asStateFlow()

    init {
        getRoadList()
    }

    /**
     * 分页数据
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    val sitePagingFlow: Flow<PagingData<SiteInfo>> = combine(
        _selectedRoadId,
        _searchKeyword
    ) { roadId, keyword ->
        Pair(roadId, keyword)
    }.flatMapLatest { (roadId, keyword) ->
        Pager(
            config = PagingConfig(
                pageSize = 20, initialLoadSize = 20, prefetchDistance = 2
            ),
            pagingSourceFactory = {
                GenericPagingSource { page, pageSize ->
                    getSitePages(roadId.toString(), keyword, page, pageSize, context)
                }
            }
        ).flow
    }.cachedIn(viewModelScope)

    fun updateRoadFilter(roadId: String?) {
        _selectedRoadId.value = roadId
    }

    fun updateSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword
    }


    /**
     * 获取道路列表
     * */
    fun getRoadList() {
        viewModelScope.launch {
            try {
                val result = UniCallbackService<List<SiteRoadInfo>>().parseDataNewSuspend(
                    siteService.getRoadList(),
                    context
                )
                if (result != null) {
                    _siteRoadInfo.value = result
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getSitePages(
        roadId: String,
        keyword: String,
        page: Int,
        pageSize: Int,
        context: Context
    ): List<SiteInfo> {
        val rawResponse = siteService.getSiteList(
            curPage = page,
            pageSize = pageSize,
            roadIdList = roadId.toLongOrNull()?.let { listOf(it) },
            tagCondition = "or",
            keyword = keyword.toString()
        )
        var parseDataNewSuspend =
            UniCallbackService<PageResponse<SiteInfo>>().parseDataNewSuspend(
                rawResponse,
                context
            )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }
}