package com.unilumin.smartapp.ui.viewModel.pages

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.constant.DeviceType
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.RequestParam
import com.unilumin.smartapp.client.service.DeviceService


class DevicePagingSource(
    private val filterType: String,
    private val searchQuery: String,
    private val retrofitClient: RetrofitClient,
    val context: Context
) : PagingSource<Int, LightDevice>() {

    private val deviceService: DeviceService by lazy {
        retrofitClient.getService(DeviceService::class.java)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LightDevice> {
        val page = params.key ?: 1
        val pageSize = params.loadSize
        return try {
            val deviceList =
                getDeviceList(filterType, deviceService, searchQuery, page, pageSize, context)
            LoadResult.Page(
                data = deviceList,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (deviceList.isEmpty() || deviceList.size < pageSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LightDevice>): Int? = null
}

// --- Data Fetching Helper ---
suspend fun getDeviceList(
    type: String,
    deviceService: DeviceService,
    searchQuery: String,
    page: Int,
    pageSize: Int,
    context: Context
): List<LightDevice> {
    if (type == DeviceType.LAMP) {
        return UniCallbackService<PageResponse<LightDevice>>().parseDataNewSuspend(
            deviceService.getLightCtlList(
                RequestParam(searchQuery, page, pageSize)
            ), context
        )?.list ?: emptyList()
    } else if (type == DeviceType.CONCENTRATOR) {
        return UniCallbackService<PageResponse<LightDevice>>().parseDataNewSuspend(
            deviceService.getGwCtlList(
                RequestParam(searchQuery, page, pageSize, 1)
            ), context
        )?.list ?: emptyList()
    } else if (type == DeviceType.LOOP) {
        var parseDataNewSuspend =
            UniCallbackService<PageResponse<LightDevice>>().parseDataNewSuspend(
                deviceService.getLoopCtlList(searchQuery, page, pageSize, 1), context
            )
        return parseDataNewSuspend?.list ?: emptyList()
    } else if (type == DeviceType.PLAY_BOX) {
        var parseDataNewSuspend =
            UniCallbackService<PageResponse<LightDevice>>().parseDataNewSuspend(
                deviceService.getLedList(searchQuery, page, pageSize, 12, 3), context
            )
        return parseDataNewSuspend?.list ?: emptyList()
    } else if (type == DeviceType.ENV || type == DeviceType.CAMERA) {
        var parseDataNewSuspend =
            UniCallbackService<PageResponse<LightDevice>>().parseDataNewSuspend(
                deviceService.getDeviceList(
                    searchQuery,
                    page,
                    pageSize,
                    DeviceType.getDeviceProductTypeId(type)
                ), context
            )
        return parseDataNewSuspend?.list ?: emptyList()
    }
    // TODO 可以扩展其他类型的设备获取逻辑
    return emptyList()
}