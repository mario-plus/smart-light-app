package com.unilumin.smartapp.util

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.unilumin.smartapp.client.data.KeyValue
import com.unilumin.smartapp.client.data.PriorityRange

object StrategyContentUtil {

    private const val TAG = "StrategyContentUtil"

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
        if (actionJsonObj?.get("hide")?.asInt == 1) {
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
        return jsonObject?.getAsJsonObject(key)
            ?.getAsJsonObject("contents")?.get("maxNum")?.asLong ?: 0L
    }

    fun getPolicyItemCanBeDeleted(jsonObject: JsonObject, key: String): Boolean {
        return jsonObject.getAsJsonObject(key)
            ?.getAsJsonObject("contents")?.get("canBeDelete")?.asBoolean == true
    }

    // 日出和日落选项
//    fun getPolicyLngLatTypes(
//        jsonObject: JsonObject, key: String, language: String? = "zh"
//    ): List<Pair<Long, KeyValue>> = safeParse("getPolicyLngLatTypes", emptyList()) {
//        val array = jsonObject.getAsJsonObject(key)
//            ?.getAsJsonObject("contents")
//            ?.getAsJsonObject("require")
//            ?.getAsJsonObject("riseDown")
//            ?.getAsJsonObject("riseType")
//            ?.getAsJsonArray("select")
//        parseSelectArray(array, language)
//    }


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
     *
     * @param jsonArray    需要解析的 select 数组
     * @param language     语言标识 (zh / en)
     * @param productId    产品ID（用于过滤逻辑，可空）
     * @param exclusionKey 触发过滤的字段名（如 "filter", "exclude"）
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
}