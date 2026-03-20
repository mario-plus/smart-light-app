package com.unilumin.smartapp.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
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

    fun isJsonValid(json: String?): Boolean {
        if (json.isNullOrBlank()) return false
        return try {
            val element = JsonParser().parse(json)
            // 只有当它是 JSON 对象或 JSON 数组时，才认为是我们需要的 "结构化 JSON"
            element.isJsonObject || element.isJsonArray
        } catch (e: Exception) {
            false
        }
    }
}