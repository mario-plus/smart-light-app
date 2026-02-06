package com.unilumin.smartapp.client

import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.ResponseData
import com.unilumin.smartapp.client.exception.ApiException
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 优化后的网络请求服务工具类
 * 1. 使用 object 单例，无需 new
 * 2. 移除了 Context 和 Toast，纯净的逻辑层
 * 3. 统一抛出 ApiException
 */
object UniCallbackService {

    // ==========================================
    // 协程版本 (推荐使用)
    // ==========================================

    /**
     * 处理标准 ResponseData 结构
     */
    suspend fun <T> parseDataSuspend(call: Call<ResponseData<T?>?>?): T? {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { call?.cancel() }

            call?.enqueue(object : Callback<ResponseData<T?>?> {
                override fun onResponse(
                    call: Call<ResponseData<T?>?>?,
                    response: Response<ResponseData<T?>?>?
                ) {
                    val body = response?.body()

                    // 1. HTTP 协议层面的失败 (404, 500 等)
                    if (!response?.isSuccessful!! || body == null) {
                        val errorMsg = "网络请求失败: ${response.code()}"
                        if (continuation.isActive) {
                            continuation.resumeWithException(ApiException(errorMsg, response.code()))
                        }
                        return
                    }

                    // 2. 业务层面的失败 (code != 200)
                    if (body.code != 200) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(ApiException(body.message ?: "未知业务错误", body.code))
                        }
                        return
                    }

                    // 3. 成功
                    if (continuation.isActive) {
                        continuation.resume(body.data)
                    }
                }

                override fun onFailure(call: Call<ResponseData<T?>?>?, t: Throwable?) {
                    // 网络断开、超时等物理错误
                    if (continuation.isActive) {
                        continuation.resumeWithException(ApiException("网络异常: ${t?.message}"))
                    }
                }
            })
        }
    }

    /**
     * 处理 NewResponseData 结构
     */
    suspend fun <T> parseDataNewSuspend(call: Call<NewResponseData<T?>?>?): T? {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { call?.cancel() }

            call?.enqueue(object : Callback<NewResponseData<T?>?> {
                override fun onResponse(
                    call: Call<NewResponseData<T?>?>?,
                    response: Response<NewResponseData<T?>?>?
                ) {
                    val body = response?.body()
                    if (!response?.isSuccessful!! || body == null) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(ApiException("网络请求失败: ${response.code()}",
                                response.code()
                            ))
                        }
                        return
                    }

                    if (body.code != 200) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(ApiException(body.message ?: "未知错误", body.code))
                        }
                        return
                    }

                    if (continuation.isActive) {
                        continuation.resume(body.result) // 注意这里取的是 result
                    }
                }

                override fun onFailure(call: Call<NewResponseData<T?>?>?, t: Throwable?) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(ApiException("网络异常: ${t?.message}"))
                    }
                }
            })
        }
    }

    /**
     * 处理直接返回实体类的情况
     */
    suspend fun <T> parseDirectSuspend(
        call: Call<T>?,
        checkSuccess: (T) -> String? = { null }
    ): T? {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { call?.cancel() }

            call?.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (!response.isSuccessful || body == null) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(ApiException("请求无数据: ${response.code()}"))
                        }
                        return
                    }

                    // 自定义校验逻辑
                    val errorMsg = checkSuccess(body)
                    if (errorMsg != null) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(ApiException(errorMsg))
                        }
                        return
                    }

                    if (continuation.isActive) {
                        continuation.resume(body)
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(ApiException("网络异常: ${t.message}"))
                    }
                }
            })
        }
    }
}