package com.unilumin.smartapp.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.ResponseData
import com.unilumin.smartapp.client.data.SystemInfo
import com.unilumin.smartapp.client.service.SystemService
import retrofit2.Call

class SystemViewModel(
    retrofitClient: RetrofitClient, val context: Context
) : ViewModel() {
    private val systemService = retrofitClient.getService(SystemService::class.java)

    //通过scope.launch调用
    suspend fun getSystemInfo(): SystemInfo? {
        try {
            val call: Call<ResponseData<SystemInfo?>?>? = systemService.getSystemInfo()
            var parseDataSuspend =
                UniCallbackService<SystemInfo>().parseDataSuspend(call, context)
            return parseDataSuspend
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}