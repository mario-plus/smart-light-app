package com.unilumin.smartapp.auth

import android.content.Context

object TokenManagerFactory {
    private var tokenManagerService: TokenManagerService? = null

    //暂时返回sharedPref实现方式
    @Synchronized
    fun getInstance(context: Context?): TokenManagerService {
        if (tokenManagerService == null) {
            tokenManagerService = SharedPrefTokenManager(context)
        }
        return tokenManagerService!!
    }
}
