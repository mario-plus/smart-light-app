package com.unilumin.smartapp.ui.viewModel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.constant.DeviceType
import com.unilumin.smartapp.client.data.LampCtlReq
import com.unilumin.smartapp.client.data.LoopCtlReq
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.service.DeviceService
import com.unilumin.smartapp.ui.viewModel.pages.DevicePagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import retrofit2.Call

class DeviceViewModel(
    retrofitClient: RetrofitClient, val context: Context
) : ViewModel() {
    private val deviceService = retrofitClient.getService(DeviceService::class.java)
    val currentFilter = MutableStateFlow(DeviceType.LAMP)
    val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val devicePagingFlow = combine(currentFilter, searchQuery) { filter, query ->
        Pair(filter, query)
    }.flatMapLatest { (filter, query) ->
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20), pagingSourceFactory = {
                DevicePagingSource(
                    filter, query, retrofitClient, context
                )
            }).flow
    }.cachedIn(viewModelScope)

    fun updateFilter(type: String) {
        currentFilter.value = type
    }

    fun updateSearch(query: String) {
        searchQuery.value = query
    }

    //设备控制按钮
    fun lampCtl(deviceId: Long, cmdType: Int, cmdValue: Int) {
        viewModelScope.launch {
            try {
                val call: Call<NewResponseData<String?>?>? = deviceService.lampCtl(
                    LampCtlReq(
                        cmdType = cmdType,
                        cmdValue = cmdValue,
                        ids = listOf(deviceId),
                        subSystemType = 1
                    )
                )
                UniCallbackService<String>().parseDataNewSuspend(call, context)
                Toast.makeText(context, "操作成功", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //设备控制按钮
    fun loopCtl(id: Long, numList: List<Int>, onOff: Int) {
        viewModelScope.launch {
            try {
                val call: Call<NewResponseData<String?>?>? = deviceService.loopCtl(
                    LoopCtlReq(listOf(id), numList, onOff)
                )
                UniCallbackService<String>().parseDataNewSuspend(call, context)
                Toast.makeText(context, "操作成功", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}