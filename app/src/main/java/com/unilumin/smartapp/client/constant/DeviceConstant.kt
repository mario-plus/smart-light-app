package com.unilumin.smartapp.client.constant

import androidx.compose.material.icons.Icons
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
import com.unilumin.smartapp.client.data.ProductType
import com.unilumin.smartapp.mock.ServerConfig

object DeviceConstant {
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


    // 初始化完整数据列表
    val DEVICE_PRODUCT_TYPE_LIST = listOf(
        ProductType(1, "单灯控制器", getIconForName("单灯控制器"), true),
        ProductType(2, "摄像头", getIconForName("摄像头")),
        ProductType(3, "灯具", getIconForName("灯具")),
        ProductType(4, "AP", getIconForName("AP")),
        ProductType(5, "LED屏", getIconForName("LED屏")),
        ProductType(7, "环境传感器", getIconForName("环境传感器"),true),
        ProductType(11, "紧急呼叫终端", getIconForName("紧急呼叫终端")),
        ProductType(12, "播放盒", getIconForName("播放盒")),
        ProductType(14, "充电桩", getIconForName("充电桩")),
        ProductType(15, "智慧网关", getIconForName("智慧网关")),
        ProductType(23, "杆体", getIconForName("杆体")),
        ProductType(25, "集中控制器", getIconForName("集中控制器")),
        ProductType(26, "音柱", getIconForName("音柱")),
        ProductType(27, "井盖传感器", getIconForName("井盖传感器")),
        ProductType(28, "垃圾桶传感器", getIconForName("垃圾桶传感器")),
        ProductType(29, "RFID接收器", getIconForName("RFID接收器")),
        ProductType(31, "水浸传感器", getIconForName("水浸传感器")),
        ProductType(32, "AC", getIconForName("AC")),
        ProductType(34, "温湿度传感器", getIconForName("温湿度传感器")),
        ProductType(35, "倾角传感器", getIconForName("倾角传感器")),
        ProductType(37, "积水传感器", getIconForName("积水传感器")),
        ProductType(38, "视频录像机", getIconForName("视频录像机")),
        ProductType(39, "门磁探测器", getIconForName("门磁探测器")),
        ProductType(40, "线控开关", getIconForName("线控开关")),
        ProductType(41, "紧急呼叫中控台", getIconForName("紧急呼叫中控台")),
        ProductType(42, "烟雾传感器", getIconForName("烟雾传感器")),
        ProductType(56, "回路控制器", getIconForName("回路控制器")),
        ProductType(57, "亮度传感器", getIconForName("亮度传感器")),
        ProductType(58, "行人监测传感器", getIconForName("行人监测传感器")),
        ProductType(59, "电表", getIconForName("电表")),
        ProductType(60, "电子锁", getIconForName("电子锁")),
        ProductType(61, "灯控网关", getIconForName("灯控网关")),
        ProductType(62, "风光互补控制器", getIconForName("风光互补控制器")),
        ProductType(63, "无线传输设备", getIconForName("无线传输设备")),
        ProductType(64, "雷视一体机", getIconForName("雷视一体机")),
        ProductType(65, "AI-BOX", getIconForName("AI-BOX")),
        ProductType(66, "AI-501", getIconForName("AI-501")),
        ProductType(67, "屏体控制器", getIconForName("屏体控制器")),
        ProductType(68, "二合一拼接控制器", getIconForName("二合一拼接控制器")),
        ProductType(69, "多功能卡盒", getIconForName("多功能卡盒")),
        ProductType(70, "光感探头", getIconForName("光感探头")),
        ProductType(71, "配电柜", getIconForName("配电柜")),
        ProductType(72, "拼接控制器", getIconForName("拼接控制器")),
        ProductType(73, "播控屏", getIconForName("播控屏")),
        ProductType(74, "亮化主控", getIconForName("亮化主控")),
        ProductType(75, "电流检测模块", getIconForName("电流检测模块")),
        ProductType(76, "媒体控制器", getIconForName("媒体控制器")),
        ProductType(77, "智能窗帘", getIconForName("智能窗帘")),
        ProductType(78, "红外探测器", getIconForName("红外探测器")),
        ProductType(79, "滑轨屏", getIconForName("滑轨屏")),
        ProductType(80, "电子升降机", getIconForName("电子升降机")),
        ProductType(81, "电子拍照设备", getIconForName("电子拍照设备")),
        ProductType(82, "数字音频处理器", getIconForName("数字音频处理器")),
        ProductType(83, "智能防护设备", getIconForName("智能防护设备")),
        ProductType(84, "智能防护终端", getIconForName("智能防护终端")),
        ProductType(85, "洲明智慧会议", getIconForName("洲明智慧会议")),
        ProductType(87, "智慧教育终端", getIconForName("智慧教育终端")),
        ProductType(88, "蜂鸣报警器", getIconForName("蜂鸣报警器")),
        ProductType(89, "应用启动器", getIconForName("应用启动器")),
        ProductType(90, "空调", getIconForName("空调")),
        ProductType(91, "基站", getIconForName("基站")),
        ProductType(92, "手环", getIconForName("手环")),
        ProductType(93, "灯光控制器", getIconForName("灯光控制器")),
        ProductType(94, "投影仪", getIconForName("投影仪")),
        ProductType(95, "闸机", getIconForName("闸机")),
        ProductType(115, "通用指令执行器", getIconForName("通用指令执行器"))
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
    const val SYSTEM_CONFIG = "系统配置"
    const val SERVER_ADDRESS = "服务器地址"

    val menuItems = listOf(
        Triple(SYSTEM_INFO, Icons.Rounded.Settings, null),
        Triple(SYSTEM_CONFIG, Icons.Rounded.Settings, null),
        Triple(SERVER_ADDRESS, Icons.Rounded.Dns, ServerConfig.getBaseUrl())
    )

    fun getIconFromId(productType: Long): ImageVector {
        var name = DEVICE_PRODUCT_TYPE_LIST.stream().filter { e -> e.id == productType }.map { e -> e.name }.findFirst().get()
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
            else -> Icons.Outlined.DevicesOther // 默认图标
        }
    }



}



