package com.unilumin.smartapp.client.service

import com.unilumin.smartapp.client.data.LoginRequest
import com.unilumin.smartapp.client.data.LoginResponse
import com.unilumin.smartapp.client.data.ResponseData
import com.unilumin.smartapp.client.data.RsaPublicKeyRes
import com.unilumin.smartapp.client.constant.RequestPathKey
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthService {

    @Headers("No-Auth: true")
    @POST(RequestPathKey.KEY_LOGIN)
    fun login(@Body request: LoginRequest): Call<ResponseData<LoginResponse?>?>?

    @Headers("No-Auth: true")
    @GET(RequestPathKey.KEY_GET_PUBLIC_KEY)
    fun getPublicKey(): Call<ResponseData<RsaPublicKeyRes?>?>?

}