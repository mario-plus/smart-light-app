package com.unilumin.smartapp.client.service

import com.unilumin.smartapp.client.constant.RequestPathKey

import com.unilumin.smartapp.client.data.ProjectInfo
import com.unilumin.smartapp.client.data.ResponseData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface ProjectService {

    @GET(RequestPathKey.KEY_PROJECT_LIST)
    fun getProjects(): Call<ResponseData<List<ProjectInfo>?>?>?

    @GET(RequestPathKey.KEY_SWITCH_PROJECT)
    fun switchProject(@Query("projectId") projectId: Long): Call<ResponseData<String?>?>?
}