package com.unilumin.smartapp.client.service

interface UniCallback<T> {

    fun success(responseData: T?)

    fun failed() {
    }

}