package com.unilumin.smartapp.client.constant

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CameraOutdoor
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.DevicesOther
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.ElectricMeter
import androidx.compose.material.icons.outlined.EvStation
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.SettingsRemote
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.unilumin.smartapp.client.data.SystemConfig
import com.unilumin.smartapp.mock.ServerConfig

object DeviceConstant {

    const val DETAIL = "detail"
    const val NETWORK = "network"
    const val PROPERTY = "property"
    const val TELEMETRY = "telemetry"
    const val EVENT = "event"


    const val OFFLINE_ANALYSIS = "offlineAnalysis"
    const val SMART_LAMP = "smartLamp"
    const val SMART_MONITOR = "smartMonitor"
    const val SMART_ENV = "smartEnv"
    const val SMART_BROAD = "smartBroad"
    const val SMART_PLAY_BOX = "smartPlayBox"

    //设备列表--应用列表
    val SMART_APP_LIST = listOf<SystemConfig>(
        SystemConfig(OFFLINE_ANALYSIS, "离线报表", getIconForName(OFFLINE_ANALYSIS), true),
        SystemConfig(SMART_LAMP, "智慧路灯", getIconForName(SMART_LAMP), true),
        SystemConfig(SMART_MONITOR, "安防监控", getIconForName(SMART_MONITOR), true),
        SystemConfig(SMART_ENV, "智能感知", getIconForName(SMART_ENV), true),
        SystemConfig(SMART_BROAD, "智慧广播", getIconForName(SMART_BROAD), true),
        SystemConfig(SMART_PLAY_BOX, "智慧屏幕", getIconForName(SMART_PLAY_BOX), true)
    )

    const val SMART_LAMP_LIGHT = "lampLight"
    const val SMART_LAMP_GATEWAY = "lightGateway"
    const val SMART_LIGHT_GATEWAY = "lampGw"
    const val SMART_LAMP_LOOP = "lightLoop"
    const val SMART_LAMP_GROUP = "lightGroup"
    const val SMART_LAMP_STRATEGY = "lightStrategy"
    const val SMART_LAMP_JOB = "lightJob"
    const val SMART_LAMP_STATISTIC = "lightStatistic"

    val SMART_LAMP_FUNC_LIST = listOf<SystemConfig>(
        SystemConfig(SMART_LAMP_LIGHT, "单灯管理", getIconForName(SMART_LAMP_LIGHT), true),
        SystemConfig(SMART_LIGHT_GATEWAY, "灯控网关", getIconForName(SMART_LIGHT_GATEWAY), true),
        SystemConfig(SMART_LAMP_GATEWAY, "集控管理", getIconForName(SMART_LAMP_GATEWAY), true),
        SystemConfig(SMART_LAMP_LOOP, "回路管理", getIconForName(SMART_LAMP_LOOP), true),
        SystemConfig(SMART_LAMP_GROUP, "分组管理", getIconForName(SMART_LAMP_GROUP), true),
        SystemConfig(SMART_LAMP_STRATEGY, "策略管理", getIconForName(SMART_LAMP_STRATEGY), true),
        SystemConfig(SMART_LAMP_JOB, "任务管理", getIconForName(SMART_LAMP_JOB), true),
        SystemConfig(SMART_LAMP_STATISTIC, "业务统计", getIconForName(SMART_LAMP_STATISTIC), true)
    )


    fun getSmartAppName(id: String): String {
        return SMART_APP_LIST.find { it.id == id }?.name ?: "未知应用"
    }


    // 设备列表--产品类型
    val DEVICE_PRODUCT_TYPE_LIST = listOf(
        SystemConfig("1", "单灯控制器", getIconForName("单灯控制器"), true),
        SystemConfig("2", "摄像头", getIconForName("摄像头")),
        SystemConfig("3", "灯具", getIconForName("灯具")),
        SystemConfig("4", "AP", getIconForName("AP")),
        SystemConfig("5", "LED屏", getIconForName("LED屏")),
        SystemConfig("7", "环境传感器", getIconForName("环境传感器"), true),
        SystemConfig("11", "紧急呼叫终端", getIconForName("紧急呼叫终端")),
        SystemConfig("12", "播放盒", getIconForName("播放盒")),
        SystemConfig("14", "充电桩", getIconForName("充电桩")),
        SystemConfig("15", "智慧网关", getIconForName("智慧网关")),
        SystemConfig("23", "杆体", getIconForName("杆体")),
        SystemConfig("25", "集中控制器", getIconForName("集中控制器")),
        SystemConfig("26", "音柱", getIconForName("音柱")),
        SystemConfig("27", "井盖传感器", getIconForName("井盖传感器")),
        SystemConfig("28", "垃圾桶传感器", getIconForName("垃圾桶传感器")),
        SystemConfig("29", "RFID接收器", getIconForName("RFID接收器")),
        SystemConfig("31", "水浸传感器", getIconForName("水浸传感器")),
        SystemConfig("32", "AC", getIconForName("AC")),
        SystemConfig("34", "温湿度传感器", getIconForName("温湿度传感器")),
        SystemConfig("35", "倾角传感器", getIconForName("倾角传感器")),
        SystemConfig("37", "积水传感器", getIconForName("积水传感器")),
        SystemConfig("38", "视频录像机", getIconForName("视频录像机")),
        SystemConfig("39", "门磁探测器", getIconForName("门磁探测器")),
        SystemConfig("40", "线控开关", getIconForName("线控开关")),
        SystemConfig("41", "紧急呼叫中控台", getIconForName("紧急呼叫中控台")),
        SystemConfig("42", "烟雾传感器", getIconForName("烟雾传感器")),
        SystemConfig("56", "回路控制器", getIconForName("回路控制器")),
        SystemConfig("57", "亮度传感器", getIconForName("亮度传感器")),
        SystemConfig("58", "行人监测传感器", getIconForName("行人监测传感器")),
        SystemConfig("59", "电表", getIconForName("电表")),
        SystemConfig("60", "电子锁", getIconForName("电子锁")),
        SystemConfig("61", "灯控网关", getIconForName("灯控网关")),
        SystemConfig("62", "风光互补控制器", getIconForName("风光互补控制器")),
        SystemConfig("63", "无线传输设备", getIconForName("无线传输设备")),
        SystemConfig("64", "雷视一体机", getIconForName("雷视一体机")),
        SystemConfig("65", "AI-BOX", getIconForName("AI-BOX")),
        SystemConfig("66", "AI-501", getIconForName("AI-501")),
        SystemConfig("67", "屏体控制器", getIconForName("屏体控制器")),
        SystemConfig("68", "二合一拼接控制器", getIconForName("二合一拼接控制器")),
        SystemConfig("69", "多功能卡盒", getIconForName("多功能卡盒")),
        SystemConfig("70", "光感探头", getIconForName("光感探头")),
        SystemConfig("71", "配电柜", getIconForName("配电柜")),
        SystemConfig("72", "拼接控制器", getIconForName("拼接控制器")),
        SystemConfig("73", "播控屏", getIconForName("播控屏")),
        SystemConfig("74", "亮化主控", getIconForName("亮化主控")),
        SystemConfig("75", "电流检测模块", getIconForName("电流检测模块")),
        SystemConfig("76", "媒体控制器", getIconForName("媒体控制器")),
        SystemConfig("77", "智能窗帘", getIconForName("智能窗帘")),
        SystemConfig("78", "红外探测器", getIconForName("红外探测器")),
        SystemConfig("79", "滑轨屏", getIconForName("滑轨屏")),
        SystemConfig("80", "电子升降机", getIconForName("电子升降机")),
        SystemConfig("81", "电子拍照设备", getIconForName("电子拍照设备")),
        SystemConfig("82", "数字音频处理器", getIconForName("数字音频处理器")),
        SystemConfig("83", "智能防护设备", getIconForName("智能防护设备")),
        SystemConfig("84", "智能防护终端", getIconForName("智能防护终端")),
        SystemConfig("85", "洲明智慧会议", getIconForName("洲明智慧会议")),
        SystemConfig("87", "智慧教育终端", getIconForName("智慧教育终端")),
        SystemConfig("88", "蜂鸣报警器", getIconForName("蜂鸣报警器")),
        SystemConfig("89", "应用启动器", getIconForName("应用启动器")),
        SystemConfig("90", "空调", getIconForName("空调")),
        SystemConfig("91", "基站", getIconForName("基站")),
        SystemConfig("92", "手环", getIconForName("手环")),
        SystemConfig("93", "灯光控制器", getIconForName("灯光控制器")),
        SystemConfig("94", "投影仪", getIconForName("投影仪")),
        SystemConfig("95", "闸机", getIconForName("闸机")),
        SystemConfig("115", "通用指令执行器", getIconForName("通用指令执行器"))
    )

    val DeviceDetailTabs = listOf(
        DETAIL to "详细信息",
        NETWORK to "网络状态",
        PROPERTY to "属性数据",
        TELEMETRY to "遥测数据",
        EVENT to "事件数据"
    )

    val statusOptions = listOf(-1 to "全部", 1 to "在线", 0 to "离线")
    val groupTypeOptions = listOf(-1 to "全部", 1 to "单灯分组", 25 to "集控分组", 56 to "回路分组")

    val jobOrStrategyStatusOptions = listOf(-1 to "全部", 1 to "待执行", 2 to "执行中", 3 to "成功", 4 to "失败")



    //双色温单灯产品id
    val colorTempSupportedList = listOf("107", "125")

    const val SYSTEM_INFO = "系统信息"
    const val SYSTEM_CONFIG = "系统配置"
    const val SERVER_ADDRESS = "服务器"

    val menuItems = listOf(
        Triple(SYSTEM_INFO, Icons.Rounded.Settings, null),
        Triple(SYSTEM_CONFIG, Icons.Rounded.Settings, null),
        Triple(SERVER_ADDRESS, Icons.Rounded.Dns, ServerConfig.getBaseUrl())
    )

    fun getIconFromId(productType: Long): ImageVector {
        var name = DEVICE_PRODUCT_TYPE_LIST.stream().filter { e -> e.id == productType.toString() }
            .map { e -> e.name }.findFirst().get()
        return getIconForName(name)
    }

    // 根据名称匹配图标的简单逻辑
    fun getIconForName(name: String): ImageVector {
        return when {
            name.contains("灯") || name.contains("亮化") -> Icons.Outlined.Lightbulb
            name.contains("摄像头") || name.contains("录像") || name.contains("监控") -> Icons.Outlined.Videocam
            name.contains("屏") || name.contains("播放") -> Icons.Outlined.Tv
            name.contains("AP") || name.contains("基站") || name.contains("无线") || name.contains("网关") -> Icons.Outlined.Router
            name.contains("传感器") || name.contains("探测") || name.contains("监测") -> Icons.Outlined.Sensors
            name.contains("充电") -> Icons.Outlined.EvStation
            name.contains("控制器") || name.contains("开关") -> Icons.Outlined.SettingsRemote
            name.contains("呼叫") || name.contains("音柱") || name.contains("音频") -> Icons.Outlined.Speaker
            name.contains("环境") || name.contains("温湿度") || name.contains("烟雾") -> Icons.Outlined.Thermostat
            name.contains("水") || name.contains("井盖") -> Icons.Outlined.WaterDrop
            name.contains("门") || name.contains("锁") -> Icons.Outlined.Lock
            name.contains("电表") || name.contains("电流") -> Icons.Outlined.ElectricMeter
            name.contains("车") || name.contains("闸") -> Icons.Outlined.DirectionsCar
            name.contains("人") -> Icons.Outlined.DirectionsWalk
            name == OFFLINE_ANALYSIS -> Icons.Filled.Assessment // 或 Icons.Filled.Analytics
            name == SMART_LAMP -> Icons.Filled.Lightbulb // 标准库没有很好的路灯，用灯泡代替
            name == SMART_MONITOR -> Icons.Filled.CameraOutdoor // 需要 material-icons-extended 依赖
            name == SMART_ENV -> Icons.Filled.Eco // 代表环境/生态
            name == SMART_BROAD -> Icons.Filled.Campaign // 扩音器代表广播
            name == SMART_PLAY_BOX -> Icons.Filled.Tv // 电视代表屏幕
            else -> Icons.Outlined.DevicesOther // 默认图标

        }
    }


}



