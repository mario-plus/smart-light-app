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
import com.unilumin.smartapp.client.data.SiteInfo
import com.unilumin.smartapp.client.data.SiteRoadInfo
import com.unilumin.smartapp.client.service.SiteService
import com.unilumin.smartapp.ui.viewModel.pages.SitePagingSource
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
    //道路信息
    private val _siteRoadInfo = MutableStateFlow<List<SiteRoadInfo>?>(null)
    val siteRoadInfo = _siteRoadInfo.asStateFlow()
    private val _selectedRoadId = MutableStateFlow<String?>(null)
    private val _searchKeyword = MutableStateFlow<String>("")
    var searchKeyword = _searchKeyword.asStateFlow()

    init {
        getRoadList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val sitePagingFlow: Flow<PagingData<SiteInfo>> = combine(
        _selectedRoadId,
        _searchKeyword
    ) { roadId, keyword ->
        Pair(roadId, keyword)
    }.flatMapLatest { (roadId, keyword) ->
        Pager(
            config = PagingConfig(
                pageSize = 20, // 每页数量
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = {
                SitePagingSource(siteService, context, roadId, keyword)
            }
        ).flow
    }.cachedIn(viewModelScope)

    fun updateRoadFilter(roadId: String?) {
        _selectedRoadId.value = roadId
    }
    fun updateSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword
    }

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
}