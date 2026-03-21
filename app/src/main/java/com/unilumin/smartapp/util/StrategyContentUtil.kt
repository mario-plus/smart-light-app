package com.unilumin.smartapp.util

import com.google.gson.JsonObject

object StrategyContentUtil {

    fun getPolicyType(jsonObject: JsonObject, language: String? = "zh"): List<Pair<Long, String>> {
        try {
            val results = mutableListOf<Pair<Long, String>>()
            val jsonArray = jsonObject.getAsJsonArray("select") ?: return emptyList()
            for (i in 0 until jsonArray.size()) {
                val item = jsonArray.get(i).asJsonObject
                val key = item.get("val").asLong
                val valueField = if (language == "zh") "zhDesc" else "enDesc"
                val value = item.get(valueField).asString
                results.add(key to value)
            }
            return results
        } catch (ignore: Exception) {
            return emptyList()
        }
    }

    fun getStrategyType(
        jsonObject: JsonObject,
        language: String? = "zh"
    ): List<Pair<Long, String>> {
        return getPolicyType(jsonObject, language)
    }

}