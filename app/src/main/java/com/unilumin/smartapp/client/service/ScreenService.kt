package com.unilumin.smartapp.client.service

import com.unilumin.smartapp.client.constant.RequestPathKey
import com.unilumin.smartapp.client.data.LedDevGroupRes
import com.unilumin.smartapp.client.data.LedPageBO
import com.unilumin.smartapp.client.data.LedPlanBO
import com.unilumin.smartapp.client.data.LedProgramRequest
import com.unilumin.smartapp.client.data.LedProgramRes
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.PageResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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


//    /**
//     *  素材用于制作节目，过于复杂，建议在web端制作，所以就没必要
//     * */
//    fun getLedFileList(
//        @Query("keyword") keyword: String,
//        @Query("curPage") curPage: Int,
//        @Query("pageSize") pageSize: Int,
//        @Query("materialType") materialType: String,//素材类型 video-视频 image-图片 txt-文本 document-文档
//        @Query("reviewStatus") reviewStatus: Int,//审核状态 0-待审核，1-审核中，2-审核通过，3-审核不通过
//        @Query("queryType") queryType: Int? = 1//素材管理(只查询上传者(超级管理员不限制)上传的素材，且可以查询素材和目录)
//    ): Call<NewResponseData<PageResponse<LedPageBO>?>?>?


}