package com.unilumin.smartapp.client.data

import com.google.gson.annotations.SerializedName

data class ResponseData<T>(
    var code: Int?, var message: String?, var data: T?
)

data class NewResponseData<T>(
    var code: Int?, var message: String?, var result: T?, var success: Boolean?
)

data class LoginRequest(
    var username: String?,
    var password: String?,
)

data class LoginResponse(
    var token: String?
)

class RsaPublicKeyRes(
    var rsaPublicKey: String
)

data class UserInfo(
    var username: String?,

    var nickname: String?,

    var remark: String?,

    var avatar: String?,

    var id: String?
)

//设备列表数据
data class LightDevice(
    var id: Long,
    @SerializedName(
        value = "name", alternate = ["loopControllerName", "deviceName"]
    ) var name: String,

    var serialNum: String,

    var productId: String,
    var productName: String? = null,

    var gatewayName: String,
    //会话状态 1-在线，0-离线
    @SerializedName(value = "state", alternate = ["networkState"]) var state: Int?,
    //设备状态（0停用 1启用）
    var deviceState: Int,
    //工作状态: 1告警 0正常
    var alarmType: Int,
    var voltage: Double?,
    var current: Double?,
    var power: Double?,
    var factor: Double?,
    //亮度
    var bright1: Int?,
    //色温
    var bright2: Int?,
    //开关
    var onOff: Int?,
    //回路信息
    var loops: List<LoopInfo>?,

    //亮度
    var brightness: String?,
    //运行状态
    var powerStatus: String?,
    //音量
    var volume: String?,
    //当前节目
    var playingProgramName: String?,
    //分辨率
    var widthHeighProgram: String?,

    //环境传感器数据
    var envData: EnvData?
)

data class LoopInfo(
    var id: Long, var loopName: String, var loopNum: Int, var state: Int, var updateTime: String
)

data class PageResponse<T>(
    val total: Int,
    val list: List<T>,
    val pageNum: Int,
    val pageSize: Int,
    val size: Int,
    val startRow: String,
    val endRow: String,
    val pages: Int,
    val prePage: Int,
    val nextPage: Int,
    val isFirstPage: Boolean,
    val isLastPage: Boolean,
    val hasPreviousPage: Boolean,
    val hasNextPage: Boolean,
    val navigatePage: Int,
    val navigateNums: List<Int>,
    val navigateFirstPage: Int,
    val navigateLastPage: Int
)


data class RequestParam(
    val keyword: String,
    val curPage: Int,
    val pageSize: Int,
    val subSystemType: Int? = null,
)

data class ProjectInfo(
    val id: Long, val name: String
)

data class MinioUrl(
    val url: String
)

//道路信息
data class SiteRoadInfo(
    val id: Long, val name: String, val count: Int, val description: String
)

data class SiteInfo(
    val id: String?,
    val name: String?,
    val number: String?,
    val longitude: Double?,
    val latitude: Double?,
    val projectId: String?,
    val isDefault: Int?,
    val createTime: String?,
    val lastModifyTime: String?, // Or Any?
    val domainId: String?, // Or Any?
    val description: String?,
    val createName: String?,
    val lastModifyName: String?,
    val configId: String?,
    val configName: String?,
    val lamppoleType: Int?,
    val lamppoleTypeName: String?,
    val projectRoadId: String?,
    val projectRoadName: String?,
    val typeId: String?, // Or Any?
    val typeName: String?, // Or Any?
    val sectionId: String?, // Or Any?
    val subSystemType: String?, // Or Any?
    val oderNum: Int?, // Note the spelling 'oderNum' from image
    val productNum: Int?,
    val deviceNum: Int?,
    val deviceList: List<SiteDevice>
)

data class SiteDevice(
    val id: String,
    val deviceName: String,
    val serialNum: String?,
    val productId: String,
    val productName: String,
    val orderId: Int,
    val productTypeId: Int,
    val productTypeName: String,
    val state: Int,
    val deviceState: Int,
    val deviceStateName: String,
    val alarmType: Int
)

data class LampCtlReq(
    val cmdType: Int, val cmdValue: Int, val ids: List<Long>, val subSystemType: Int
)

data class LoopCtlReq(
    val idList: List<Long>, val loopNumList: List<Int>,
    //0关1开
    val onOff: Int
)

data class EnvData(
    val pm2_5: String? = null,
    val pm10: String? = null,
    val lightIntensity: String? = null,
    val noise: String? = null,
    val pa: String? = null,
    val ua: String? = null,
    val ta: String? = null,
    val precipitation: String? = null,
    val windspeed: String? = null,
    val windDirection: String? = null,
    val dust: Any? = null,
    val so2: Any? = null,
    val co: Any? = null,
    val co2: Any? = null,
    val no2: Any? = null,
    val o3: Any? = null,
    val sm: Any? = null,
    val sn: Any? = null,
    val sx: Any? = null,
    val dm: Any? = null,
    val dn: Any? = null,
    val dx: Any? = null,
    val ch2o: Any? = null,
    val ns: Any? = null,
    val ni: Any? = null,
    val nx: Any? = null,
    val tvoc: Any? = null,
    val temperature: Any? = null,
    val humidity: Any? = null,
)

data class EnvDisplayInfo(
    val label: String, val value: String, val unit: String
)

//wbertc
data class WebRTCResponse(
    val code: Int, val id: String, val sdp: String, val type: String
)

data class EnvDataReq(
    val ids: List<Long>
)

//设备详情
data class DeviceDetail(
    //设备id
    val id: Long,
    //设备名称
    val deviceName: String,
    //序列码
    val serialNum: String,
    //产品类型
    val productTypeName: String,
    //产品名称
    val name: String,
    //产品厂商
    val productFactoryName: String,
    //传输协议类型
    val transportProtocol: String,
    //协议名称
    val messageProtocol: String,

    //物模型数据(设备能力)
    val metadata: String
)

data class DeviceConfig(
    val key: String,
    val keyDes: String,
    val value: String,
    val modify: Int,
    val type: Int,
    val order: Int,
    //val switchContent: String,
    val required: Boolean
)

data class DeviceRealTimeDataReq(
    val deviceId: Long, val keys: List<String>
)

data class HistoryData(
    val deviceId: String, val eventTs: String, val key: String, val name: String, var value: String
)

data class HistoryDataReq(
    val startTime: String? = null,
    val endTime: String? = null,
    val deviceIds: List<String>,
    val keys: List<String>,
    val curPage: Int,
    val pageSize: Int
)

data class DeviceModelData(
    //物模型key
    val key: String,
    //名称
    val name: String,
    //描述
    val keyDes: String,
    //值
    var value: String? = null,
    //单位
    val unit: String? = null,
    //类型(String,Long,double,float)
    val type: String
)

data class SequenceTsl(
    val value: String,
    val ts: Long
)

data class SystemCpu(
    val cpuNum: Int,
    val total: Double,
    val sys: Double,
    val used: Double,
    val wait: Double,
    val free: Double
)

data class SystemMemory(
    val total: Double,
    val used: Double,
    val free: Double,
    val usage: Double,
)

data class SystemJvm(
    val total: Double,
    val max: Long,
    val used: Double,
    val name: String,
    val free: Double,
    val usage: Double,
    val version: String,
    val home: String,
    val startTime: String,
    val runTime: String,
    val inputArgs: String,
)

data class SystemBaseInfo(
    val computerName: String,
    val computerIp: String,
    val userDir: String,
    val osName: String,
    val osArch: String
)

data class SystemFileInfo(
    val dirName: String,
    val sysTypeName: String,
    val typeName: String,
    val total: String,
    val free: String,
    val used: String,
    val usage: Double
)

data class SystemInfo(
    val cpu: SystemCpu? = null,
    val memory: SystemMemory? = null,
    val jvm: SystemJvm? = null,
    val system: SystemBaseInfo? = null,
    val sysFiles: List<SystemFileInfo>? = null
)

data class PagingState(
    var pageIndex: Int = 1,
    var hasMore: Boolean = true
)