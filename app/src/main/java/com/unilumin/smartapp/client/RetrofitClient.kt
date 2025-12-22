package com.unilumin.smartapp.client

import android.annotation.SuppressLint
import android.content.Context
import coil.ImageLoader
import com.unilumin.smartapp.auth.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class RetrofitClient {

    private lateinit var retrofit: Retrofit
    private val serviceCache = ConcurrentHashMap<Class<*>, Any>()
    private lateinit var client: OkHttpClient
    private val gsonConverterFactory: GsonConverterFactory by lazy {
        GsonConverterFactory.create()
    }



    fun initRetrofit(url: String) {
        retrofit =
            Retrofit.Builder().baseUrl(url).client(client).addConverterFactory(gsonConverterFactory)
                .build()
        serviceCache.clear()
    }

    fun initClient(context: Context) {
        // 1. 创建忽略 SSL 的 TrustManager
        // 1. 创建信任所有证书的 TrustManager
        val trustAllCerts = arrayOf<TrustManager>(
            @SuppressLint("CustomX509TrustManager") object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?, authType: String?
                ) {
                }
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?, authType: String?
                ) {
                }
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
        // 2. 配置 SSLContext
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        // 3. 构建 OkHttpClient
        client = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true } // 信任所有主机名
            .addInterceptor(AuthInterceptor(context)) // 你的 AuthInterceptor
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()
    }

    fun getImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context).okHttpClient(client).build()
    }

    fun <T> getService(serviceClass: Class<T>): T {
        if (!::retrofit.isInitialized) {
            throw IllegalStateException("RetrofitClient has not been initialized. Call init() first.")
        }
        return serviceCache.getOrPut(serviceClass) {
            retrofit.create(serviceClass)
        } as T
    }
}