package com.unilumin.smartapp.client.constant

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.DeviceUnknown
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.ui.graphics.vector.ImageVector

object DeviceType {
    //单灯
    const val LAMP = "lamp"

    //集中控制器
    const val CONCENTRATOR = "concentrator"

    //回路控制器
    const val LOOP = "loop"

    //播放盒
    const val PLAY_BOX = "playbox"

    //环境传感器
    const val ENV = "env"

    const val CAMERA = "camera"


    //设备列表选项
    val DataList = listOf(
        LAMP to "单灯控制器",
        CONCENTRATOR to "集中控制器",
        LOOP to "回路控制器",
        PLAY_BOX to "播放盒",
        ENV to "环境传感器",
        CAMERA to "摄像头"
    )

    fun getDeviceProductTypeId(type: String): Int {
        return when (type) {
            ENV -> 7
            CAMERA -> 2
            else -> 0
        }
    }

    //设备图标
    fun getDeviceIcon(type: String): ImageVector {
        return when (type) {
            LAMP -> Icons.Rounded.Lightbulb
            CAMERA -> Icons.Rounded.Videocam
            CONCENTRATOR -> Icons.Rounded.Hub
            PLAY_BOX -> Icons.Rounded.Tv
            LOOP -> Icons.Rounded.Compress
            ENV -> Icons.Rounded.Thermostat
            else -> Icons.Rounded.DeviceUnknown
        }
    }
}