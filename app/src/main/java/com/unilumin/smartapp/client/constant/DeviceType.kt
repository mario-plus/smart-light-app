package com.unilumin.smartapp.client.constant

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.DeviceUnknown
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import com.unilumin.smartapp.mock.ServerConfig

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

    const val DETAIL = "detail"
    const val NETWORK = "network"
    const val PROPERTY = "property"
    const val TELEMETRY = "telemetry"
    const val EVENT = "event"

    //设备列表选项
    val DataList = listOf(
        LAMP to "单灯控制器",
        CONCENTRATOR to "集中控制器",
        LOOP to "回路控制器",
        PLAY_BOX to "播放盒",
        ENV to "环境传感器",
        CAMERA to "摄像头"
    )

    const val OFFLINE_ANALYSIS = "offlineAnalysis"
    const val MENU2 = "menu2"
    val DeviceMenus = listOf(
        OFFLINE_ANALYSIS to "离线报表",
        MENU2 to "待开发"
    )


    val DeviceDetailTabs = listOf(
        DETAIL to "详细信息",
        NETWORK to "网络状态",
        PROPERTY to "属性数据",
        TELEMETRY to "遥测数据",
        EVENT to "事件数据"
    )

    //双色温单灯产品id
    val colorTempSupportedList = listOf("107", "125")


    const val SYSTEM_INFO = "系统信息"
    const val SERVER_ADDRESS = "服务器地址"

    val menuItems = listOf(
        Triple(SYSTEM_INFO, Icons.Rounded.Settings, null),
        Triple(SERVER_ADDRESS, Icons.Rounded.Dns, ServerConfig.getBaseUrl())
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