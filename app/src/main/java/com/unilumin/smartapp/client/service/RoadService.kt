package com.unilumin.smartapp.client.service

import com.unilumin.smartapp.client.constant.RequestPathKey
import com.unilumin.smartapp.client.data.DeviceAlarmInfo
import com.unilumin.smartapp.client.data.DeviceStatusSummary
import com.unilumin.smartapp.client.data.GroupMemberInfo
import com.unilumin.smartapp.client.data.GroupMemberReq
import com.unilumin.smartapp.client.data.GroupRequestParam
import com.unilumin.smartapp.client.data.JobRequestParam
import com.unilumin.smartapp.client.data.JobSceneElement
import com.unilumin.smartapp.client.data.LampCtlReq
import com.unilumin.smartapp.client.data.LampGateWayInfo
import com.unilumin.smartapp.client.data.LampGroupInfo
import com.unilumin.smartapp.client.data.LampJobInfo
import com.unilumin.smartapp.client.data.LampLightInfo
import com.unilumin.smartapp.client.data.LampLoopCtlInfo
import com.unilumin.smartapp.client.data.LampStrategyInfo
import com.unilumin.smartapp.client.data.LightDayEnergy
import com.unilumin.smartapp.client.data.LightEnergy
import com.unilumin.smartapp.client.data.LightYearEnergy
import com.unilumin.smartapp.client.data.LoopCtlReq
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.RequestParam
import com.unilumin.smartapp.client.data.StrategyRequestParam
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface RoadService {

    //单灯列表
    @POST(RequestPathKey.KEY_GET_LIGHT_LIST)
    fun getLightCtlList(@Body param: RequestParam?): Call<NewResponseData<PageResponse<LampLightInfo>?>?>?

    //集控列表
    @POST(RequestPathKey.KEY_GET_GW_LIST)
    fun getGwCtlList(@Body param: RequestParam?): Call<NewResponseData<PageResponse<LampGateWayInfo>?>?>?

    //灯控网关
    @POST(RequestPathKey.KEY_GET_LIGHT_GW_LIST)
    fun getLightGwList(@Body param: RequestParam?): Call<NewResponseData<PageResponse<LampGateWayInfo>?>?>?

    //回路控制器列表
    @GET(RequestPathKey.KEY_GET_LOOP_LIST)
    fun getLoopCtlList(
        @Query("keyword") keyword: String,
        @Query("curPage") curPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("subSystemType") subSystemType: Int? = 1,
        @Query("networkState") networkState: Int? = null
    ): Call<NewResponseData<PageResponse<LampLoopCtlInfo>?>?>?

    //分组列表
    @POST(RequestPathKey.KEY_GET_GROUP_LIST)
    fun getGroupList(
        @Body param: GroupRequestParam?
    ): Call<NewResponseData<PageResponse<LampGroupInfo>?>?>?

    //策略列表
    @POST(RequestPathKey.KEY_GET_STRATEGY_LIST)
    fun getStrategyList(@Body strategyParam: StrategyRequestParam): Call<NewResponseData<PageResponse<LampStrategyInfo>?>?>?


    /**
     * 接口数据有问题，下拉加载过程中，会出现相同数据
     * */
    @POST(RequestPathKey.KEY_JOB_LIST)
    fun getJobList(@Body param: JobRequestParam): Call<NewResponseData<PageResponse<LampJobInfo>?>?>?


    //获取任务场景类型
    @GET(RequestPathKey.KEY_JOB_SCENE_LIST)
    fun getJobSceneList(@Query("subSystemType") subSystemType: Int? = 1): Call<NewResponseData<List<JobSceneElement>?>?>?


    /**
     * 告警统计
     * */
    @GET(RequestPathKey.KEY_GET_DEVICE_ALARM_LIST)
    fun deviceAlarmList(
        @Query("keyword") keyword: String,
        @Query("curPage") curPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("isConfirm") isConfirm: Int? = null,
        @Query("level") level: Int? = null,
        @Query("isDeal") isDeal: Int? = 0
    ): Call<NewResponseData<PageResponse<DeviceAlarmInfo>?>?>?


    @GET(RequestPathKey.KEY_REAL_TIME_COUNT)
    fun deviceStatusSummary(
    ): Call<NewResponseData<DeviceStatusSummary?>?>?

    //当月能耗对比
    @GET(RequestPathKey.KEY_GET_LIGHT_ENERGY)
    fun contrastLightEnergy(): Call<NewResponseData<List<LightEnergy>?>?>?

    //年度用电趋势
    @GET(RequestPathKey.KEY_GET_ANNUAL_TREND)
    fun annualPowerConsumptionTrend(): Call<NewResponseData<LightYearEnergy?>?>?

    //近七天用电量
    @GET(RequestPathKey.KEY_GET_HOME_LIGHT_ENERGY)
    fun homeLightEnergy(): Call<NewResponseData<List<LightDayEnergy>?>?>?


    @POST(RequestPathKey.KEY_LAMP_CTL)
    fun lampCtl(@Body lampCtlReq: LampCtlReq): Call<NewResponseData<String?>?>?

    //uni_light_groupCtl
    @POST(RequestPathKey.KEY_GROUP_CTL)
    fun groupCtl(@Body lampCtlReq: LampCtlReq): Call<NewResponseData<String?>?>?

    @PUT(RequestPathKey.KEY_LOOP_CTL)
    fun loopCtl(@Body loopCtlReq: LoopCtlReq): Call<NewResponseData<String?>?>?

    @POST(RequestPathKey.KEY_GET_GROUP_MEMBER)
    fun getGroupMembers(groupReq: GroupMemberReq): Call<NewResponseData<PageResponse<GroupMemberInfo>?>?>?



}