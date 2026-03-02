package com.unilumin.smartapp.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONObject

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

    fun parseJsonToKeyValue(jsonString: String): List<Pair<String, String>> {
        return try {
            val jsonObject = JSONObject(jsonString)
            val list = mutableListOf<Pair<String, String>>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonObject.optString(key, "")
                if (!value.isEmpty()) {
                    list.add(key to value)
                }
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}