package com.unilumin.smartapp.util

import android.util.Log
import com.google.gson.JsonObject
import com.unilumin.smartapp.client.data.KeyValue
import com.unilumin.smartapp.client.data.PriorityRange

object StrategyContentUtil {

    private const val TAG = "StrategyContentUtil"

    // 获取策略类型或策略模式
    fun getPolicyTypeOrMode(
        productId: Long, jsonObject: JsonObject, language: String? = "zh"
    ): List<Pair<Long, KeyValue>> {
        try {
            val jsonArray = jsonObject.getAsJsonArray("select") ?: return emptyList()
            val results = mutableListOf<Pair<Long, KeyValue>>()
            for (i in 0 until jsonArray.size()) {
                val item = jsonArray.get(i).asJsonObject
                val itemVal = item.get("val").asLong // 优化：重命名避免覆盖外层变量
                val valueField = if (language == "zh") "zhDesc" else "enDesc"
                val value = item.get(valueField).asString
                val k = item.get("key").asString

                val filters = item.get("filter")
                var isFiltered = false

                // 修复：使用数值直接比较，避免 Gson 的 contains 类型坑
                if (filters != null && filters.isJsonArray) {
                    for (element in filters.asJsonArray) {
                        if (element.asLong == productId) {
                            isFiltered = true
                            break
                        }
                    }
                }

                // 修复：将 break 替换为 continue，避免中断整个循环
                if (isFiltered) {
                    continue
                }
                results.add(itemVal to KeyValue(key = k, value = value))
            }
            return results
        } catch (e: Exception) {
            Log.e(TAG, "getPolicyTypeOrMode parse error", e)
            return emptyList()
        }
    }

    // 策略生效周期类型
    fun getPolicyPeriodTypes(
        jsonObject: JsonObject, key: String, language: String? = "zh"
    ): List<Pair<Long, KeyValue>> {
        try {
            val results = mutableListOf<Pair<Long, KeyValue>>()
            // 优化：使用 ?. 安全调用符，防止中间某一层级缺失导致空指针异常
            val jsonArray = jsonObject.getAsJsonObject(key)
                ?.getAsJsonObject("contents")
                ?.getAsJsonObject("require")
                ?.getAsJsonObject("timeType")
                ?.getAsJsonArray("select") ?: return emptyList()

            for (i in 0 until jsonArray.size()) {
                val item = jsonArray.get(i).asJsonObject
                val itemVal = item.get("val").asLong // 优化：重命名避免与参数 key 冲突
                val valueField = if (language == "zh") "zhDesc" else "enDesc"
                val value = item.get(valueField).asString
                val k = item.get("key").asString
                results.add(itemVal to KeyValue(key = k, value = value))
            }
            return results
        } catch (e: Exception) {
            Log.e(TAG, "getPolicyPeriodTypes parse error", e)
            return emptyList()
        }
    }

    // 优先级
    fun getPolicyPriorityRange(
        jsonObject: JsonObject, key: String
    ): PriorityRange? {
        try {
            // 优化：使用 ?. 安全调用符
            val priorityObj = jsonObject.getAsJsonObject(key)?.getAsJsonObject("priority") ?: return null
            val min = priorityObj.get("min")
            val max = priorityObj.get("max")

            if (min == null || max == null || min.asInt > max.asInt) {
                return null
            }
            return PriorityRange(min = min.asInt, max = max.asInt)
        } catch (e: Exception) {
            Log.e(TAG, "getPolicyPriorityRange parse error", e)
            return null
        }
    }

    // 获取策略执行动作类型
    fun getPolicyActionTypes(
        productId: Long,
        jsonObject: JsonObject,
        key: String,
        language: String? = "zh"
    ): List<Pair<Long, KeyValue>> {
        try {
            val results = mutableListOf<Pair<Long, KeyValue>>()

            // 优化：使用 ?. 安全调用符
            val jsonArray = jsonObject.getAsJsonObject(key)
                ?.getAsJsonObject("contents")
                ?.getAsJsonObject("action")
                ?.getAsJsonObject("actionType")
                ?.getAsJsonArray("select") ?: return emptyList()

            for (i in 0 until jsonArray.size()) {
                val item = jsonArray.get(i).asJsonObject
                val itemVal = item.get("val").asLong // 优化：重命名
                val valueField = if (language == "zh") "zhDesc" else "enDesc"
                val value = item.get(valueField).asString
                val k = item.get("key").asString
                val exclude = item.get("exclude")

                var isExcluded = false

                // 修复：使用 asLong 精确数值比对
                if (exclude != null && exclude.isJsonArray) {
                    for (element in exclude.asJsonArray) {
                        if (element.asLong == productId) {
                            isExcluded = true
                            break
                        }
                    }
                }

                // 修复：将被排除项的逻辑改为 continue
                if (isExcluded) {
                    continue
                }
                results.add(itemVal to KeyValue(key = k, value = value))
            }
            return results
        } catch (e: Exception) {
            Log.e(TAG, "getPolicyActionTypes parse error", e)
            return emptyList()
        }
    }

    // 获取经纬度操作类型(日出，日落)
    fun getLngLatRequireType(
        jsonObject: JsonObject, key: String, language: String? = "zh"
    ): List<Pair<Long, KeyValue>> {
        try {
            val results = mutableListOf<Pair<Long, KeyValue>>()
            // 优化：使用 ?. 安全调用符
            val jsonArray = jsonObject.getAsJsonObject(key)
                ?.getAsJsonObject("contents")
                ?.getAsJsonObject("require")
                ?.getAsJsonObject("lngLatType")
                ?.getAsJsonArray("select") ?: return emptyList()

            for (i in 0 until jsonArray.size()) {
                val item = jsonArray.get(i).asJsonObject
                val itemVal = item.get("val").asLong // 优化：重命名
                val valueField = if (language == "zh") "zhDesc" else "enDesc"
                val value = item.get(valueField).asString
                val k = item.get("key").asString
                results.add(itemVal to KeyValue(key = k, value = value))
            }
            return results
        } catch (e: Exception) {
            Log.e(TAG, "getLngLatRequireType parse error", e)
            return emptyList()
        }
    }
}