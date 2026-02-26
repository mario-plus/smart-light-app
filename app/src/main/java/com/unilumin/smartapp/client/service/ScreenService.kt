package com.unilumin.smartapp.client.service

import com.unilumin.smartapp.client.constant.RequestPathKey
import com.unilumin.smartapp.client.data.LedPageBO
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.PageResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 智慧屏幕服务
 **/
interface ScreenService {
    /**
     * 获取led设备详情
     * */
    @GET(RequestPathKey.KEY_LED_LIST)
    fun getLedList(
        @Query("keyword") keyword: String,
        @Query("curPage") curPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("state") state: Int?, //设备会话状态1-在线，0-离线
        @Query("productTypeId") productTypeId: Long? = 12L,
        @Query("subSystemType") subSystemType: Int? = 3
    ): Call<NewResponseData<PageResponse<LedPageBO>?>?>?


}