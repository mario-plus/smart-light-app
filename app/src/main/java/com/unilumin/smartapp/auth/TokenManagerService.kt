package com.unilumin.smartapp.auth

interface TokenManagerService {

    fun getAccessTokenTime(): Long

    fun setAccessToken(token: String)

    fun getAccessToken(): String

    fun clear()
}
