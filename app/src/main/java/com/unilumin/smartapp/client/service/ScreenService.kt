package com.unilumin.smartapp.client.service

import com.unilumin.smartapp.client.constant.RequestPathKey
import com.unilumin.smartapp.client.data.IdsBody
import com.unilumin.smartapp.client.data.LedCommandReq
import com.unilumin.smartapp.client.data.LedCtlPlanDetail
import com.unilumin.smartapp.client.data.LedDevGroupRes
import com.unilumin.smartapp.client.data.LedFileReq
import com.unilumin.smartapp.client.data.LedGroupLogBO
import com.unilumin.smartapp.client.data.LedGroupMemberUpdate
import com.unilumin.smartapp.client.data.LedMaterialInfoVO
import com.unilumin.smartapp.client.data.LedPageBO
import com.unilumin.smartapp.client.data.LedPlanBO
import com.unilumin.smartapp.client.data.LedProgramRequest
import com.unilumin.smartapp.client.data.LedProgramRes
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.PlayBoxDeviceBO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
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


    /**
     * 播放表
     * */
    @POST(RequestPathKey.KEY_PROGRAM_LIST)
    fun getLedProgramList(@Body request: LedProgramRequest): Call<NewResponseData<PageResponse<LedProgramRes>?>?>?

    /**
     * 播放盒分组信息
     * */
    @GET(RequestPathKey.KEY_LED_GROUP_LIST)
    fun getLedGroupList(
        @Query("keyword") keyword: String,
        @Query("curPage") curPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("primaryClass") primaryClass: Int? = 5,
        @Query("subSystemType") subSystemType: Int? = 3
    ): Call<NewResponseData<PageResponse<LedDevGroupRes>?>?>?

    /**
     * 播放盒分组成员
     * */
    @GET(RequestPathKey.KEY_LED_GROUP_MEMBER)
    fun getLedGroupMember(
        @Query("groupId") groupId: Long, @Query("subSystemType") subSystemType: Int? = 3
    ): Call<NewResponseData<List<PlayBoxDeviceBO>?>?>?

    /**
     * 可选设备
     * */
    @GET(RequestPathKey.KEY_LED_GROUP_DEV_OPTIONAL)
    fun getLedGroupDevOptional(
        @Query("groupId") groupId: Long,
        @Query("subSystemType") subSystemType: Int? = 3
    ): Call<NewResponseData<List<PlayBoxDeviceBO>?>?>?


    @POST(RequestPathKey.KEY_LED_GROUP_MEMBER_UPDATE)
    fun updateGroupMember(@Body request: LedGroupMemberUpdate): Call<NewResponseData<Void?>?>?


    /**
     * 方案列表
     * */
    @GET(RequestPathKey.KEY_LED_PLAN_LIST)
    fun getLedPlans(
        @Query("keyword") keyword: String,
        @Query("curPage") curPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("type") type: Int,   //1-控制方案，2播放方案
        @Query("subSystemType") subSystemType: Int? = 3
    ): Call<NewResponseData<PageResponse<LedPlanBO>?>?>?


    /**
     * 删除播放方案
     * */
    @HTTP(method = "DELETE", path = RequestPathKey.KEY_LED_PLAN_DEL, hasBody = true)
    fun delLedPlans(@Body requestBody: IdsBody): Call<NewResponseData<Void?>?>?

    /**
     * 新增播放方案
     * */
    @POST(RequestPathKey.KEY_LED_PLAN_DEL)
    fun addLedPlans(@Body requestBody: LedPlanBO): Call<NewResponseData<Long?>?>?

    /**
     * 编辑播放方案
     * */
    @PUT(RequestPathKey.KEY_LED_PLAN_DEL)
    fun editLedPlans(@Body requestBody: LedPlanBO): Call<NewResponseData<Long?>?>?


    /**
     * 控制方案详情
     * */
    @GET(RequestPathKey.KEY_LED_CTL_PLAN_DETAIL)
    fun getLedCtlPlanDetail(
        @Query("id") id: Long, @Query("subSystemType") subSystemType: Int? = 3
    ): Call<NewResponseData<List<LedCtlPlanDetail>?>?>?


    /**
     * 素材列表
     * */
    @POST(RequestPathKey.KEY_LED_FILE_LIST)
    fun getLedFileList(
        @Body request: LedFileReq
    ): Call<NewResponseData<PageResponse<LedMaterialInfoVO>?>?>?


    /**
     * 远程控制指令
     * */
    @PUT(RequestPathKey.KEY_LED_COMMAND)
    fun ledCommand(@Body request: LedCommandReq): Call<NewResponseData<Void?>?>?

    /**
     * 播放盒详情
     * */
    @GET(RequestPathKey.KEY_LED_DEVICE_DETAIL)
    fun getLedDevDetail(@Query("id") id: Long): Call<NewResponseData<LedPageBO?>?>?

    /**
     * 播放盒分组日志
     * */
    @GET(RequestPathKey.KEY_LED_GROUP_LOG)
    fun getLedGroupLog(
        @Query("curPage") curPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("groupId") groupId: Long,
        @Query("subSystemType") subSystemType: Int? = 3
    ): Call<NewResponseData<PageResponse<LedGroupLogBO>?>?>?
}