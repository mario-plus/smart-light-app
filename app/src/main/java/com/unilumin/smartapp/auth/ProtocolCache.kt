package com.unilumin.smartapp.auth

import android.content.Context
import androidx.core.content.edit

object ProtocolCache {
    private const val PREF_NAME = "app_protocol_cache"
    private const val KEY_HAS_AGREED = "has_agreed_protocol"

    // 检查是否已同意过协议
    fun hasAgreed(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_HAS_AGREED, false)
    }

    // 设置协议同意状态
    fun setAgreed(context: Context, isAgreed: Boolean) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit { putBoolean(KEY_HAS_AGREED, isAgreed) }
    }
}