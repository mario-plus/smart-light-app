package com.unilumin.smartapp.client.service

import com.unilumin.smartapp.client.constant.RequestPathKey
import com.unilumin.smartapp.client.data.ResponseData
import com.unilumin.smartapp.client.data.SystemInfo
import retrofit2.Call
import retrofit2.http.GET

interface SystemService {


    @GET(RequestPathKey.KEY_GET_SYSTEM_INFO)
    fun getSystemInfo(): Call<ResponseData<SystemInfo?>?>?

}