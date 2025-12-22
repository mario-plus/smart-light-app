package com.unilumin.smartapp.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceType
import com.unilumin.smartapp.ui.viewModel.pages.DevicePagingSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class DeviceViewModel(
    retrofitClient: RetrofitClient,
    context: Context
) : ViewModel() {

    val currentFilter = MutableStateFlow(DeviceType.LAMP)
    val searchQuery = MutableStateFlow("")

    val devicePagingFlow = combine(currentFilter, searchQuery) { filter, query ->
        Pair(filter, query)
    }.flatMapLatest { (filter, query) ->
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20),
            pagingSourceFactory = {
                DevicePagingSource(
                    filter,
                    query,
                    retrofitClient,
                    context
                )
            }).flow
    }.cachedIn(viewModelScope)

    fun updateFilter(type: String) {
        currentFilter.value = type
    }

    fun updateSearch(query: String) {
        searchQuery.value = query
    }
}