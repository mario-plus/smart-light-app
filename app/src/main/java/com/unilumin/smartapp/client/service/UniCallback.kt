package com.unilumin.smartapp.client.service

interface UniCallback<T> {

    fun success(data: T?)

    fun failed(errorMessage: String)

}