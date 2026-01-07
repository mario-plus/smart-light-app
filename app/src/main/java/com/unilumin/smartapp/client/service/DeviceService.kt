package com.unilumin.smartapp.client.service

import com.unilumin.smartapp.client.constant.RequestPathKey
import com.unilumin.smartapp.client.data.DeviceConfig
import com.unilumin.smartapp.client.data.DeviceDetail
import com.unilumin.smartapp.client.data.DeviceRealTimeDataReq
import com.unilumin.smartapp.client.data.DeviceStatusAnalysisResp
import com.unilumin.smartapp.client.data.EnvData
import com.unilumin.smartapp.client.data.EnvDataReq
import com.unilumin.smartapp.client.data.HistoryData
import com.unilumin.smartapp.client.data.HistoryDataReq
import com.unilumin.smartapp.client.data.LampCtlReq
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.client.data.LoopCtlReq
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.RequestParam
import com.unilumin.smartapp.client.data.SequenceTsl
import com.unilumin.smartapp.client.data.WebRTCResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query


interface DeviceService {
    //单灯列表
    @POST(RequestPathKey.KEY_GET_LIGHT_LIST)
    fun getLightCtlList(@Body param: RequestParam?): Call<NewResponseData<PageResponse<LightDevice>?>?>?

    //集控列表
    @POST(RequestPathKey.KEY_GET_GW_LIST)
    fun getGwCtlList(@Body param: RequestParam?): Call<NewResponseData<PageResponse<LightDevice>?>?>?

    //回路控制器列表
    @GET(RequestPathKey.KEY_GET_LOOP_LIST)
    fun getLoopCtlList(
        @Query("keyword") keyword: String,
        @Query("curPage") curPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("subSystemType") subSystemType: Int
    ): Call<NewResponseData<PageResponse<LightDevice>?>?>?

    @GET(RequestPathKey.KEY_LED_LIST)
    fun getLedList(
        @Query("keyword") keyword: String,
        @Query("curPage") curPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("productTypeId") productTypeId: Int,
        @Query("subSystemType") subSystemType: Int
    ): Call<NewResponseData<PageResponse<LightDevice>?>?>?


    //分组列表
    @POST(RequestPathKey.KEY_GET_GROUP_LIST)
    fun getGroupList()

    //策略列表
    @POST(RequestPathKey.KEY_GET_STRATEGY_LIST)
    fun getStrategyList()


    @POST(RequestPathKey.KEY_LAMP_CTL)
    fun lampCtl(@Body lampCtlReq: LampCtlReq): Call<NewResponseData<String?>?>?

    @PUT(RequestPathKey.KEY_LOOP_CTL)
    fun loopCtl(@Body loopCtlReq: LoopCtlReq): Call<NewResponseData<String?>?>?

    @GET(RequestPathKey.KEY_ENV_DATA)
    fun getEnvData(@Query("id") id: Long): Call<NewResponseData<EnvData?>?>?

    @POST(RequestPathKey.KEY_ENV_DATA_LIST)
    fun getEnvDataList(@Body req: EnvDataReq): Call<NewResponseData<Map<Long, EnvData>?>?>?


    //获取iot设备列表
    @GET(RequestPathKey.KEY_GET_DEVICE)
    fun getDeviceList(
        @Query("keyword") keyword: String,
        @Query("curPage") curPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("productTypeIds") productTypeIds: Int
    ): Call<NewResponseData<PageResponse<LightDevice>?>?>?

    @GET(RequestPathKey.KEY_CAMERA_LIVE_URL)
    fun getCameraLiveUrl(
        @Query("id") id: Long,
        @Query("isPushStream") isPushStream: Int,
        @Query("streamType") streamType: Int
    ): Call<NewResponseData<String?>?>?

    @GET(RequestPathKey.KEY_CAMERA_LIVE)
    fun getCameraLive(
        @Query("app") app: String, @Query("stream") stream: String, @Query("type") type: String
    ): Call<WebRTCResponse>

    @GET(RequestPathKey.KEY_GET_DEVICE_DETAIL)
    fun getDeviceDetail(@Query("id") id: Long): Call<NewResponseData<DeviceDetail?>?>?

    @GET(RequestPathKey.KEY_GET_DEVICE_CONFIG)
    fun getDeviceConfig(@Query("deviceId") deviceId: Long): Call<NewResponseData<List<DeviceConfig>?>?>?

    @POST(RequestPathKey.KEY_GET_DEVICE_REAL_DATA)
    fun getDeviceRealTimeData(@Body req: DeviceRealTimeDataReq): Call<NewResponseData<Map<String, Map<String, String>>?>?>?

    @POST(RequestPathKey.KEY_GET_DEVICE_HISTORY_DATA)
    fun getDeviceHistoryData(@Body req: HistoryDataReq): Call<NewResponseData<PageResponse<HistoryData>?>?>?

    /**
     * @param id 服务模型key
     * @param type long:1,double:2
     * @param isAggregation 是否聚合，默认为true
     * @param startTime 格式yyyy-MM-dd HH:mm:ss
     * */
    @GET(RequestPathKey.KEY_GET_DEVICE_SEQUENCE_TSL)
    fun getSequenceTsl(
        @Query("deviceId") deviceId: Long,
        @Query("id") id: String,
        @Query("type") type: Int,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String,
        @Query("isAggregation") isAggregation: Boolean,
    ): Call<NewResponseData<List<SequenceTsl>?>?>?


    /**
     * 获取离线报表数据
     * */
    @GET(RequestPathKey.KEY_GET_DEVICE_STATUS_ANALYSIS)
    fun deviceStatusAnalysis(): Call<NewResponseData<DeviceStatusAnalysisResp?>?>?

    /**
     * 离线设备详情
     * */
    @GET(RequestPathKey.KEY_GET_OFFLINE_DEVICE_LIST)
    fun offlineDeviceList(
        @Query("curPage") curPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("timeType") timeType: Int,
        @Query("primaryClass") primaryClass: Int
    ): Call<NewResponseData<PageResponse<LightDevice>?>?>?

}