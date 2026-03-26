package com.unilumin.smartapp.util

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.unilumin.smartapp.client.data.KeyValue
import com.unilumin.smartapp.client.data.LngLatStrategyContent
import com.unilumin.smartapp.client.data.PolicyConfig
import com.unilumin.smartapp.client.data.PriorityRange
import com.unilumin.smartapp.client.data.TimeStrategyContent

object StrategyContentUtil {
    private const val TAG = "StrategyContentUtil"

    // 👇 新增：一站式解析方法
    fun getPolicyConfig(
        productId: Long, jsonObject: JsonObject?, key: String, language: String? = "zh"
    ): PolicyConfig {
        if (jsonObject == null) return PolicyConfig()
        return PolicyConfig(
            periodTypes = getPolicyPeriodTypes(jsonObject, key, language),
            priorityRange = getPolicyPriorityRange(jsonObject, key),
            actionTypes = getPolicyActionTypes(productId, jsonObject, key, language),
            maxSize = getPolicyItemMaxSize(jsonObject, key)
        )
    }

    // --- 公共业务方法 ---

    fun getPolicyTypeOrMode(
        productId: Long, jsonObject: JsonObject, language: String? = "zh"
    ): List<Pair<Long, KeyValue>> = safeParse("getPolicyTypeOrMode", emptyList()) {
        parseSelectArray(
            jsonArray = jsonObject.getAsJsonArray("select"),
            language = language,
            productId = productId,
            exclusionKey = "filter"
        )
    }

    fun getPolicyPeriodTypes(
        jsonObject: JsonObject?, key: String, language: String? = "zh"
    ): List<Pair<Long, KeyValue>> = safeParse("getPolicyPeriodTypes", emptyList()) {
        val array = jsonObject?.getAsJsonObject(key)
            ?.getAsJsonObject("contents")
            ?.getAsJsonObject("require")
            ?.getAsJsonObject("timeType")
            ?.getAsJsonArray("select")
        parseSelectArray(array, language)
    }

    fun getPolicyPriorityRange(jsonObject: JsonObject?, key: String): PriorityRange? =
        safeParse("getPolicyPriorityRange", null) {
            val priorityObj = jsonObject?.getAsJsonObject(key)?.getAsJsonObject("priority")
            val min = priorityObj?.get("min")?.asInt
            val max = priorityObj?.get("max")?.asInt

            if (min != null && max != null && min <= max) {
                PriorityRange(min = min, max = max)
            } else null
        }

    fun getPolicyActionTypes(
        productId: Long, jsonObject: JsonObject, key: String, language: String? = "zh"
    ): List<Pair<Long, KeyValue>> = safeParse("getPolicyActionTypes", emptyList()) {
        val actionJsonObj = jsonObject.getAsJsonObject(key)
            ?.getAsJsonObject("contents")
            ?.getAsJsonObject("action")
        //经纬度的hide才有效
        if (key == "lngLatStrategies" && actionJsonObj?.get("hide")?.asInt == 1) {
            return@safeParse emptyList()
        }
        val array = actionJsonObj
            ?.getAsJsonObject("actionType")
            ?.getAsJsonArray("select")
        parseSelectArray(array, language, productId, "exclude")
    }

    fun getLngLatRequireType(
        jsonObject: JsonObject, key: String, language: String? = "zh"
    ): List<Pair<Long, KeyValue>> = safeParse("getLngLatRequireType", emptyList()) {
        val array = jsonObject.getAsJsonObject(key)
            ?.getAsJsonObject("contents")
            ?.getAsJsonObject("require")
            ?.getAsJsonObject("lngLatType")
            ?.getAsJsonArray("select")
        parseSelectArray(array, language)
    }

    fun getPolicyItemMaxSize(jsonObject: JsonObject?, key: String): Long {
        return try {
            val contentsObj = jsonObject?.getAsJsonObject(key)?.getAsJsonObject("contents")
            val maxNumElement = contentsObj?.get("maxNum")
            val maxNum =
                if (maxNumElement != null && !maxNumElement.isJsonNull) maxNumElement.asLong else 0L

            // 👇 强力排查日志：把解析路径和拿到的 JSON 片段全打印出来
            Log.d(
                "StrategyContentUtil",
                "🔍 Check maxSize -> key: $key, parsed maxNum: $maxNum. \nContents JSON snippet: ${
                    contentsObj?.toString()?.take(300)
                }"
            )

            maxNum
        } catch (e: Exception) {
            Log.e("StrategyContentUtil", "❌ getPolicyItemMaxSize 解析失败, key: $key", e)
            0L
        }
    }

    fun getPolicyItemCanBeDeleted(jsonObject: JsonObject, key: String): Boolean {
        return jsonObject.getAsJsonObject(key)
            ?.getAsJsonObject("contents")?.get("canBeDelete")?.asBoolean == true
    }

    /**
     * 通用的异常捕获与日志打印执行块
     */
    private inline fun <T> safeParse(methodName: String, defaultValue: T, block: () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            Log.e(TAG, "$methodName parse error", e)
            defaultValue
        }
    }

    /**
     * 通用的 "select" 数组解析器
     */
    private fun parseSelectArray(
        jsonArray: JsonArray?,
        language: String?,
        productId: Long? = null,
        exclusionKey: String? = null
    ): List<Pair<Long, KeyValue>> {
        if (jsonArray == null || jsonArray.isJsonNull) return emptyList()
        val valueField = if (language == "zh") "zhDesc" else "enDesc"
        val results = mutableListOf<Pair<Long, KeyValue>>()
        for (element in jsonArray) {
            val item = element.asJsonObject
            if (productId != null && exclusionKey != null) {
                val filterElement = item.get(exclusionKey)
                if (filterElement != null && filterElement.isJsonArray) {
                    val isExcluded = filterElement.asJsonArray.any { it.asLong == productId }
                    if (isExcluded) continue
                }
            }
            val itemVal = item.get("val").asLong
            val value = item.get(valueField).asString
            val k = item.get("key").asString
            results.add(itemVal to KeyValue(key = k, value = value))
        }

        return results
    }

    fun formatTimeStrategy(contents: List<Any>?): List<TimeStrategyContent> {
        if (contents.isNullOrEmpty()) return emptyList()
        return contents.mapNotNull { item ->
            try {
                when (item) {
                    is String -> if (item.isNotBlank()) JsonUtils.fromJson(
                        item,
                        TimeStrategyContent::class.java
                    ) else null

                    is Map<*, *> -> {
                        val jsonString = JsonUtils.gson.toJson(item)
                        JsonUtils.fromJson(jsonString, TimeStrategyContent::class.java)
                    }

                    is JsonObject -> {
                        JsonUtils.fromJson(item.toString(), TimeStrategyContent::class.java)
                    }

                    is TimeStrategyContent -> item
                    else -> null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun formatLngLatStrategy(contents: List<Any>?): List<LngLatStrategyContent> {
        if (contents.isNullOrEmpty()) return emptyList()
        return contents.mapNotNull { item ->
            try {
                when (item) {
                    is String -> if (item.isNotBlank()) JsonUtils.fromJson(
                        item,
                        LngLatStrategyContent::class.java
                    ) else null

                    is Map<*, *> -> {
                        val jsonString = JsonUtils.gson.toJson(item)
                        JsonUtils.fromJson(jsonString, LngLatStrategyContent::class.java)
                    }

                    is JsonObject -> {
                        JsonUtils.fromJson(item.toString(), LngLatStrategyContent::class.java)
                    }

                    is LngLatStrategyContent -> item
                    else -> null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}