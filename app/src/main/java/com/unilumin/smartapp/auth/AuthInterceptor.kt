package com.unilumin.smartapp.auth

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(context: Context) : Interceptor {
    private val context: Context? = context.applicationContext

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val accessToken: String? = TokenManagerFactory.getInstance(context).getAccessToken()

        //针对minio文件，不需要添加token
        if (originalRequest.header("No-Auth") != null || originalRequest.url.toString()
                .contains("unilumin-minio")
        ) {
            return chain.proceed(originalRequest)
        }
        val authRequest =
            originalRequest.newBuilder().header("Authorization", "Bearer $accessToken")
                .header("Accept-Language", "zh").build()

        return chain.proceed(authRequest)
    }
}
