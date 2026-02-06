package com.unilumin.smartapp.client.exception

/**
 * 用于封装服务端返回的错误信息
 */
class ApiException(
    override val message: String?,
    val code: Int? = null
) : Exception(message)