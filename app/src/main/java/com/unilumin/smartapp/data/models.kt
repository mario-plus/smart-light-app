package com.unilumin.smartapp.data


data class Alarm(
    val id: Int,
    val msg: String,
    val time: String,
    val level: String // "high", "medium", "low"
)

