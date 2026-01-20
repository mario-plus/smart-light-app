package com.unilumin.smartapp.client.data

import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImagePainter

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
data class IotDevice(
    var id: Long,
    var deviceName: String? = null,
    var serialNum: String? = null,
    var productId: String? = null,
    var productName: String? = null,
    //会话状态 1-在线，0-离线
    var state: Int?,
    //设备状态（0停用 1启用）
    var deviceState: Int?,
    //工作状态: 1告警 0正常
    var alarmType: Int? = null
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
    val state: Int? = null,
    val subSystemType: Int? = 1,
)

data class GroupRequestParam(
    val keyword: String,
    val curPage: Int,
    val pageSize: Int,
    val groupType: Int? = null,
    val subSystemType: Int? = 1,
    val tagCondition: String? = "or"
)

data class StrategyRequestParam(
    val keyword: String,
    val curPage: Int,
    val pageSize: Int,
    val syncState: Int? = null,
    val taskState: Int? = null,
    val subSystemType: Int? = 1,
    val tagCondition: String? = "or"
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
    val id: Long,
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
    //在线/离线
    val state: Int,
    //设备状态（0停用 1启用）
    val deviceState: Int,

    val deviceStateName: String,
    //工作状态: 1告警 0正常
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

data class DeviceStatusAnalysisResp(
    val sum: Int,
    val offlineSum: Int,
    val onlineSum: Int,
    val offlineRatio: Double,
    val onlineRatio: Double,
    val poleSum: Int,
    val deviceStatusAnalysis: List<DeviceStatusAnalysis>
)

data class DeviceStatusAnalysis(
    val primaryClass: Int,
    val name: String,
    val sum: Int,
    val offlineSum: Int,
    val onlineSum: Int,
    val offlineRatio: Double,
    val onlineRatio: Double
)


//设备列表数据
data class OfflineDevice(
    val id: Long,
    val deviceName: String,
    val serialNum: String,
    val productId: String,
    val productTypeName: String,
    val productName: String,
    //设备状态（0停用 1启用）
    val deviceState: Int,
    //工作状态: 1告警 0正常
    val alarmType: Int,
    val productFactoryName: String,
    val lastActiveTime: String
)

data class PoleMapPointRes(
    val lat: String,
    val lng: String,
    val count: Int,
    val siteId: String,
    val siteName: String,
    val onOff: Int
)

data class PoleMapPointReq(
    val precision: Int,
    val minLng: String,
    val maxLng: String,
    val minLat: String,
    val maxLat: String,
    val projectRoadId: Long?,
    val keyword: String?
)


data class SystemConfig(
    val id: String,
    val name: String,
    val icon: ImageVector,
    var isSelected: Boolean = false
)


data class LampLightInfo(
    var id: Long,

    var name: String? = null,

    var serialNum: String? = null,

    var softVersion: String? = null,

    var productId: Long? = null,

    var productName: String? = null,

    var gatewayId: Long? = null,

    var gatewayName: String? = null,

    // 工作状态: 1告警 0正常
    var alarmType: Int? = null,

    // 设备状态（0停用 1启用）
    val deviceState: Int,

    // 工作模式(0:手动，1:自动)
    var workMode: Int? = null,

    // 会话状态 1-在线，0-离线
    var state: Int,

    var voltage: Double? = null,

    var current: Double? = null,

    var bright1: Double? = null,

    var power: Double? = null,

    var factor: Double? = null,

    var lightfs: Double? = null,

    var onOff: Int? = null,

    var lastActiveTime: String? = null,

    var onlineTime: String? = null,

    var ip: String? = null,

    var bright2: Double? = null,

    var temperature: Double? = null,

    var projectId: Long? = null
)


data class LampGateWayInfo(
    // 设备id
    var id: Long,

    // 设备名称
    var name: String? = null,

    // 序列码
    var serialNum: String? = null,

    // 产品id
    var productId: Long? = null,

    // 产品名称
    var productName: String? = null,

    // 会话状态 1-在线，0-离线
    var state: Int? = null,

    // 工作状态 1告警 0正常
    var alarmType: Int? = null,

    // A相电压
    var voltage1: Double? = null,

    // B相电压
    var voltage2: Double? = null,

    // C相电压
    var voltage3: Double? = null,

    // A相电流
    var current1: Double? = null,

    // B相电流
    var current2: Double? = null,

    // C相电流
    var current3: Double? = null,

    // 已绑定的单灯数
    var boundDevCount: Int? = null,

    // 未绑定的单灯数
    var unboundDevCount: Int? = null,

    // 白名单状态 0未同步 1同步中 2已同步 3同步失败
    var whiteListState: Int? = null
)


data class LampLoopCtlInfo(
    // id
    var id: Long,

    // 产品id
    var productId: Long? = null,

    // 产品名称
    var productName: String? = null,

    // 序列码
    var serialNum: String? = null,

    // 回路控制器名称
    var loopControllerName: String? = null,

    // 通信设备名称
    var gatewayName: String? = null,

    // 回路控制器是否自动添加（1是，0否）
    var autoAdd: Int? = null,

    // 通信设备ID
    var gwId: Long? = null,

    // 集控的网络状态
    var networkState: Int? = null,

    // 回路列表
    var loops: List<LoopInfo>? = null,

    // 备注
    var description: String? = null,

    // 工作模式(0:手动，1:自动)
    var workMode: Int? = null
)

data class LampGroupInfo(

    // 分组id
    var id: Long,

    // 分组名称
    var groupName: String? = null,

    // 产品id
    var productId: Long? = null,

    // 产品名称
    var productName: String? = null,

    // 分组类型：1单灯，25集控，56回路
    var groupType: Int? = null,

    // 集控id
    var deviceId: Long? = null,

    // 集控名称
    var deviceName: String? = null,

    // 备注
    var description: String? = null,

    // 同步状态
    var syncState: Int? = null,

    // 任务状态
    var taskState: Int? = null,

    // 是否支持分组 (默认true)
    var groupBy: Boolean? = true,

    // 默认分组（0否，1是）
    var autoAdd: Int? = null,

    // 设备数量
    var deviceNum: Int? = null,

    // 类型，和第三方有关，和我们平台无关
    var type: String? = null
)

data class LampStrategyInfo(

    // 策略id
    var id: Long,

    // 策略名称
    var name: String? = null,

    // 产品id
    var productId: Long? = null,

    // 产品名称
    var productName: String? = null,

    // 策略类别（1经纬度策略,2时间策略）
    var strategyClass: Int? = null,

    // 街区名称
    var streetName: String? = null,

    // 道路名称
    var roadName: String? = null,

    // 策略内容
    var contents: List<Any>? = null,

    // 同步状态
    var syncState: Int? = null,

    // 任务状态(执行中2，成功3，失败：4)
    var taskState: Int? = null,

    // 触发方式：0手动触发（默认），1指定时间
    var executeType: Int? = null,

    // 策略执行时间
    var executeTime: String? = null,

    // 备注
    var description: String? = null,

    // 分组groups
    var groups: List<GroupDO>? = null,

    // 修改者id (注：原Java代码中creator注解写的是修改者id，通常应为创建者id)
    var creator: Long? = null,

    // 修改者id
    var updater: Long? = null,

    // 创建时间
    var createTime: String? = null,

    // 修改时间
    var updateTime: String? = null,

    // 创建者名称
    var createName: String? = null,

    // 修改者名称
    var updateName: String? = null,

    // 策略是否可编辑（0不可编辑，1可以编辑）
    var edit: Int? = null,

    // 分组数量
    var groupNum: Int? = null,

    // 策略类型：1 分组，2 广播策略，3 单灯-策略保存在单灯
    var strategyType: Int? = null
)

data class GroupDO(

    // 分组id
    var id: Long? = null,
    // 分组名称
    var name: String? = null,
    // 分组类型（1单灯，25集控，56回路控制器）
    var groupType: Int? = null
)


data class LampJobInfo(

    val id: Long,

    val name: String,

    val businessType: Int,

    val status: Int,

    val jobId: String? = null, // 建议给可空字段加上默认值

    val createDate: String,

    val tryNum: Int,

    val exeDate: String,

    val expiredDate: String,

    val businessId: String? = null,

    val businessName: String,

    val canCancel: Int,

    val failedStrategy: String,

    val maxTryNum: Int? = null
)


data class JobRequestParam(
    val keyword: String,
    val curPage: Int,
    val pageSize: Int,
    val businessTypes:List<Int>?=emptyList(),
    val createDate: String? = null,

    val status: Int? = null,
    val subSystemType: Int? = 1
)

data class JobScene(
    val key: Int,
    val value: String,
    val typeName: String
)

data class JobSceneElement(
    val groupName: String,
    val list: List<JobScene>
)

