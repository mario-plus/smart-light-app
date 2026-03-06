package com.unilumin.smartapp.util

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

object TimeUtil {

    const val DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss"

    @SuppressLint("ConstantLocale")
    val defaultFormat = SimpleDateFormat(DEFAULT_PATTERN, Locale.getDefault())

    fun formatIsoTime(isoTime: String?): String {
        if (isoTime.isNullOrBlank()) return "--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            val parsedDate = parser.parse(isoTime) ?: return isoTime
            defaultFormat.timeZone = TimeZone.getDefault()
            defaultFormat.format(parsedDate)
        } catch (e: Exception) {
            // 兜底方案：如果解析异常，直接使用字符串裁剪去掉 T 和毫秒
            isoTime.substringBefore("T") + " " + isoTime.substringAfter("T").substringBefore(".")
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTs(ts: Long, pattern: String = "yyyy-MM-dd HH:mm"): String {
        return try {
            val instant = Instant.ofEpochMilli(ts)
            val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
            instant.atZone(ZoneId.systemDefault()).format(formatter)
        } catch (_: Exception) {
            ""
        }
    }
}