package com.unilumin.smartapp.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * 统一的 JSON 工具类
 */
object JsonUtils {

    val gson: Gson = GsonBuilder()
        .serializeNulls()
        .create()

    fun <T> fromJson(json: String, classOfT: Class<T>): T? {
        return try {
            gson.fromJson(json, classOfT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}