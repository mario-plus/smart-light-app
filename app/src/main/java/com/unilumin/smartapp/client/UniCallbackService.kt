package com.unilumin.smartapp.client

import android.content.Context
import android.widget.Toast
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.ResponseData
import com.unilumin.smartapp.client.service.UniCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UniCallbackService<T> {


//    suspend fun parseDataSuspend(call: Call<T>, context: Context?): T? {
//        return suspendCancellableCoroutine { continuation ->
//            {
//                continuation.invokeOnCancellation {
//                    call.cancel()
//                }
//                call.enqueue(object : Callback<T> {
//                    override fun onResponse(call: Call<T?>, response: Response<T?>) {
//
//                        if (response.body() == null) {
//                            resumeWithFailure(continuation, context, "接口响应为空")
//                            return
//                        }
//                        // 成功：恢复协程并返回数据
//                        if (continuation.isActive) {
//                            continuation.resume(response.body())
//                        }
//                    }
//
//                    override fun onFailure(call: Call<T?>, t: Throwable) {
//                        resumeWithFailure(continuation, context, "接口调用失败: ${t.message}")
//                    }
//
//                })
//            }
//        }
//    }

    suspend fun parseDataSuspend(call: Call<ResponseData<T?>?>?, context: Context?): T? {
        return suspendCancellableCoroutine { continuation ->
            // 1. 如果协程被取消（比如界面关闭），取消网络请求
            continuation.invokeOnCancellation {
                call?.cancel()
            }
            call?.enqueue(object : Callback<ResponseData<T?>?> {
                override fun onResponse(
                    call: Call<ResponseData<T?>?>?,
                    response: Response<ResponseData<T?>?>?
                ) {
                    val body = response?.body()
                    // 校验空数据
                    if (body == null || body.code == null) {
                        resumeWithFailure(continuation, context, "接口响应为空")
                        return
                    }
                    // 校验业务 Code
                    val code: Int? = body.code
                    if (code != 200) {
                        resumeWithFailure(continuation, context, body.message ?: "未知错误")
                        return
                    }
                    // 成功：恢复协程并返回数据
                    if (continuation.isActive) {
                        continuation.resume(body.data)
                    }
                }

                override fun onFailure(call: Call<ResponseData<T?>?>?, t: Throwable?) {
                    resumeWithFailure(continuation, context, "接口调用失败: ${t?.message}")
                }
            })
        }
    }

    suspend fun parseDataNewSuspend(call: Call<NewResponseData<T?>?>?, context: Context?): T? {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                call?.cancel()
            }

            call?.enqueue(object : Callback<NewResponseData<T?>?> {
                override fun onResponse(
                    call: Call<NewResponseData<T?>?>?,
                    response: Response<NewResponseData<T?>?>?
                ) {
                    val body = response?.body()

                    if (body == null || body.code == null) {
                        resumeWithFailure(continuation, context, "接口响应为空")
                        return
                    }

                    val code: Int? = body.code
                    if (code != 200) {
                        resumeWithFailure(continuation, context, body.message ?: "未知错误")
                        return
                    }

                    // 注意：NewResponseData 取的是 result
                    if (continuation.isActive) {
                        continuation.resume(body.result)
                    }
                }

                override fun onFailure(call: Call<NewResponseData<T?>?>?, t: Throwable?) {
                    resumeWithFailure(continuation, context, "接口调用失败: ${t?.message}")
                }
            })
        }
    }

    /**
     * 抽象泛型方法：处理直接返回实体类的接口
     */
    suspend fun <T> parseDirectSuspend(
        call: Call<T>?,
        context: Context?,
        checkSuccess: (T) -> String? = { null }
    ): T? {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                call?.cancel()
            }
            call?.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()

                    if (body == null) {
                        resumeWithFailure(continuation, context, "响应数据为空")
                        return
                    }

                    // 执行业务成功校验逻辑
                    val errorMessage = checkSuccess(body)
                    if (errorMessage != null) {
                        resumeWithFailure(continuation, context, errorMessage)
                        return
                    }

                    if (continuation.isActive) {
                        continuation.resume(body)
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    resumeWithFailure(continuation, context, "网络请求失败: ${t.message}")
                }
            })
        }
    }


    /**
     * 修复后的泛型失败处理函数
     */
    private fun <T> resumeWithFailure(
        continuation: kotlinx.coroutines.CancellableContinuation<T?>,
        context: Context?,
        message: String
    ) {
        if (continuation.isActive) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
            continuation.resumeWithException(Exception(message))
        }
    }


    fun parseData(call: Call<ResponseData<T?>?>?, context: Context?, doCallback: UniCallback<T?>) {
        call?.enqueue(object : Callback<ResponseData<T?>?> {
            override fun onResponse(
                call: Call<ResponseData<T?>?>?,
                response: Response<ResponseData<T?>?>?
            ) {
                val body = response!!.body()
                if (body == null || body.code == null) {
                    doOnFailure(context, doCallback, "接口响应为空")
                    return
                }
                val code: Int? = body.code
                if (code != 200) {
                    doOnFailure(context, doCallback, body.message)
                    return
                }
                doCallback.success(body.data)
            }

            override fun onFailure(call: Call<ResponseData<T?>?>?, t: Throwable?) {
                doOnFailure(context, doCallback, "接口调用失败")
            }
        })
    }

    fun parseDataNew(
        call: Call<NewResponseData<T?>?>,
        context: Context?,
        doCallback: UniCallback<T?>
    ) {
        call.enqueue(object : Callback<NewResponseData<T?>?> {
            override fun onResponse(
                call: Call<NewResponseData<T?>?>?,
                response: Response<NewResponseData<T?>?>?
            ) {
                val body = response!!.body()
                if (body == null || body.code == null) {
                    doOnFailure(context, doCallback, "接口响应为空")
                    return
                }
                val code: Int? = body.code
                if (code != 200) {
                    doOnFailure(context, doCallback, body.message)
                    return
                }
                doCallback.success(body.result)
            }

            override fun onFailure(call: Call<NewResponseData<T?>?>?, t: Throwable?) {
                doOnFailure(context, doCallback, "接口调用失败")
            }
        })
    }

    private fun <T> doOnFailure(context: Context?, doCallback: UniCallback<T?>, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        doCallback.failed()
    }
}