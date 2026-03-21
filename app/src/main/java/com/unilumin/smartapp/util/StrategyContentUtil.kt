package com.unilumin.smartapp.util

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.unilumin.smartapp.client.data.KeyValue
import com.unilumin.smartapp.client.data.PriorityRange

object StrategyContentUtil {

    //获取策略类型或策略模式
    fun getPolicyTypeOrMode(
        productId: Long, jsonObject: JsonObject, language: String? = "zh"
    ): List<Pair<Long, KeyValue>> {
        try {
            val jsonArray = jsonObject.getAsJsonArray("select") ?: return emptyList()
            val results = mutableListOf<Pair<Long, KeyValue>>()
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

    //策略生效周期类型
    fun getPolicyPeriodTypes(
        jsonObject: JsonObject, key: String, language: String? = "zh"
    ): List<Pair<Long, KeyValue>> {
        try {
            val results = mutableListOf<Pair<Long, KeyValue>>()
            var jsonArray = jsonObject.getAsJsonObject(key).getAsJsonObject("contents")
                .getAsJsonObject("require").getAsJsonObject("timeType").getAsJsonArray("select")
                ?: return emptyList()
            for (i in 0 until jsonArray.size()) {
                val item = jsonArray.get(i).asJsonObject
                val key = item.get("val").asLong
                val valueField = if (language == "zh") "zhDesc" else "enDesc"
                val value = item.get(valueField).asString
                val k = item.get("key").asString
                results.add(key to KeyValue(key = k, value = value))
            }
            return results
        } catch (ignore: Exception) {
            return emptyList()
        }
    }

    //优先级
    fun getPolicyPriorityRange(
        jsonObject: JsonObject, key: String
    ): PriorityRange? {
        try {
            val priorityObj = jsonObject.getAsJsonObject(key).getAsJsonObject("priority")
            val min = priorityObj.get("min")
            val max = priorityObj.get("max")
            if (min == null || max == null || min.asInt > max.asInt) {
                return null
            }
            return PriorityRange(min = min.asInt, max = max.asInt)
        } catch (e: Exception) {
            return null
        }
    }


}

