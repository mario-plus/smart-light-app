package com.unilumin.smartapp.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// 全局服务器配置对象
object ServerConfig {
    var protocol by mutableStateOf("https") // 或 https
    var ipAddress by mutableStateOf("10.2.126.21")
   // var ipAddress by mutableStateOf("39.108.1.197")
    var port by mutableStateOf("443")

    // 获取完整的 Base URL
    fun getBaseUrl(): String {
        return "$protocol://$ipAddress:$port"
    }

}