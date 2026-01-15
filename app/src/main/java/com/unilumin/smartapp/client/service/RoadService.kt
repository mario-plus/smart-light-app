package com.unilumin.smartapp.client.service

import com.unilumin.smartapp.client.constant.RequestPathKey
import com.unilumin.smartapp.client.data.LampGateWayInfo
import com.unilumin.smartapp.client.data.LampGroupInfo
import com.unilumin.smartapp.client.data.LampLightInfo
import com.unilumin.smartapp.client.data.LampLoopCtlInfo
import com.unilumin.smartapp.client.data.LampStrategyInfo
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.RequestParam
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RoadService {

    //单灯列表
    @POST(RequestPathKey.KEY_GET_LIGHT_LIST)
    fun getLightCtlList(@Body param: RequestParam?): Call<NewResponseData<PageResponse<LampLightInfo>?>?>?

    //集控列表
    @POST(RequestPathKey.KEY_GET_GW_LIST)
    fun getGwCtlList(@Body param: RequestParam?): Call<NewResponseData<PageResponse<LampGateWayInfo>?>?>?

    //回路控制器列表
    @GET(RequestPathKey.KEY_GET_LOOP_LIST)
    fun getLoopCtlList(
        @Query("keyword") keyword: String,
        @Query("curPage") curPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("subSystemType") subSystemType: Int,
        @Query("networkState") networkState: Int? = null
    ): Call<NewResponseData<PageResponse<LampLoopCtlInfo>?>?>?

    //分组列表
    @POST(RequestPathKey.KEY_GET_GROUP_LIST)
    fun getGroupList(): Call<NewResponseData<PageResponse<LampGroupInfo>?>?>?

    //策略列表
    @POST(RequestPathKey.KEY_GET_STRATEGY_LIST)
    fun getStrategyList(): Call<NewResponseData<PageResponse<LampStrategyInfo>?>?>?

}