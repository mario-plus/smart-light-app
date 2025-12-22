package com.unilumin.smartapp.client.service

import com.unilumin.smartapp.client.constant.RequestPathKey
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.SiteInfo

import com.unilumin.smartapp.client.data.SiteRoadInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 站点服务
 * */
interface SiteService {

    @GET(RequestPathKey.KEY_ROAD_LIST)
    fun getRoadList(): Call<NewResponseData<List<SiteRoadInfo>?>?>?


    //获取站点列表
    @GET(RequestPathKey.LEY_LAMP_LIST)
    fun getSiteList(
        @Query("keyword") keyword: String,
        @Query("curPage") curPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("tagCondition") tagCondition: String,
        @Query("roadIdList") roadIdList: List<Long>? = null
    ): Call<NewResponseData<PageResponse<SiteInfo>?>?>?
}