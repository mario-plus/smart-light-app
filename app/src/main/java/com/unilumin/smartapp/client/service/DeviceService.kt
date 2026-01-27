package com.unilumin.smartapp.client.service

import com.unilumin.smartapp.client.constant.RequestPathKey
import com.unilumin.smartapp.client.data.AlarmRequestParam
import com.unilumin.smartapp.client.data.DeviceAlarmInfo
import com.unilumin.smartapp.client.data.DeviceConfig
import com.unilumin.smartapp.client.data.DeviceDetail
import com.unilumin.smartapp.client.data.DeviceRealTimeDataReq
import com.unilumin.smartapp.client.data.DeviceStatusAnalysisResp
import com.unilumin.smartapp.client.data.DeviceStatusSummary
import com.unilumin.smartapp.client.data.EnvData
import com.unilumin.smartapp.client.data.EnvDataReq
import com.unilumin.smartapp.client.data.HistoryData
import com.unilumin.smartapp.client.data.HistoryDataReq
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.client.data.LampCtlReq
import com.unilumin.smartapp.client.data.LightDayEnergy
import com.unilumin.smartapp.client.data.LightEnergy
import com.unilumin.smartapp.client.data.LightYearEnergy
import com.unilumin.smartapp.client.data.LoopCtlReq
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.OfflineDevice
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.SequenceTsl
import com.unilumin.smartapp.client.data.WebRTCResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query


interface DeviceService {


    @GET(RequestPathKey.KEY_LED_LIST)
    fun getLedList(
        @Query("keyword") keyword: String,
        @Query("curPage") curPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("productTypeId") productTypeId: Int,
        @Query("subSystemType") subSystemType: Int
    ): Call<NewResponseData<PageResponse<IotDevice>?>?>?


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
        @Query("productTypeIds") productType: Long,
        @Query("state") state: Int?
    ): Call<NewResponseData<PageResponse<IotDevice>?>?>?

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
        @Query("timeType") timeType: Int?,
        @Query("primaryClass") primaryClass: Int?
    ): Call<NewResponseData<PageResponse<OfflineDevice>?>?>?





}