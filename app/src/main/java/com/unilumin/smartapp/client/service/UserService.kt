package com.unilumin.smartapp.client.service

import com.unilumin.smartapp.client.data.ResponseData
import com.unilumin.smartapp.client.data.UserInfo
import com.unilumin.smartapp.client.constant.RequestPathKey
import com.unilumin.smartapp.client.data.MinioUrl
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UserService {

    @GET(RequestPathKey.KEY_GET_USER)
    fun getUserInfo(): Call<ResponseData<UserInfo?>?>?

    @GET(RequestPathKey.KEY_GET_MINIO_PATH)
    fun getUserAvatarPath(@Query("objectName") objectName: String): Call<ResponseData<MinioUrl?>?>?

}