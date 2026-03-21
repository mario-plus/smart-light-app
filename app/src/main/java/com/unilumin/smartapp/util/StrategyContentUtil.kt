package com.unilumin.smartapp.util

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.unilumin.smartapp.client.data.KeyValue

object StrategyContentUtil {

    //获取策略类型或策略模式
    fun getPolicyTypeOrMode(
        productId: Long,
        jsonObject: JsonObject,
        language: String? = "zh"
    ): List<Pair<Long, KeyValue>> {
        try {
            val results = mutableListOf<Pair<Long, KeyValue>>()
            val jsonArray = jsonObject.getAsJsonArray("select") ?: return emptyList()
            for (i in 0 until jsonArray.size()) {
                val item = jsonArray.get(i).asJsonObject
                val key = item.get("val").asLong
                val valueField = if (language == "zh") "zhDesc" else "enDesc"
                val value = item.get(valueField).asString
                val k = item.get("key").asString

                var filters = item.get("filter")
                if (filters?.isJsonArray == true) {
                    val productIdElement = JsonPrimitive(productId) // 封装成相同的类型
                    if (filters.asJsonArray.contains(productIdElement)) {
                        break
                    }
                }
                results.add(key to KeyValue(key = k, value = value))
            }
            return results
        } catch (ignore: Exception) {
            return emptyList()
        }
    }
}