package com.unilumin.smartapp.client.data

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.JsonObject
import java.math.BigDecimal

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
    var productId: Long? = null,
    var productName: String? = null,
    //会话状态 1-在线，0-离线
    var state: Int?,
    //设备状态（0停用 1启用）
    var deviceState: Int?,
    //工作状态: 1告警 0正常
    var alarmType: Int? = null,
    //遥测实时信息
    var telemetryList: List<EnvTelBO>,
    var description: String
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
    val workMode: Int? = null,
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
    val cmdType: Int, val cmdValue: Int, val ids: List<Long>, val subSystemType: Int? = null
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

data class EnvReq(
    val deviceId: Long, val productId: Long?
)

data class EnvDataReq(
    val envRealData: List<EnvReq>
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
    val type: String,

    var updateTime: String? = null
)

data class SequenceTsl(
    val value: String, val ts: Long
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
    var pageIndex: Int = 1, var hasMore: Boolean = true
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
    val id: String, val name: String, val icon: ImageVector, var isSelected: Boolean = false
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

    var deviceState: Int? = null,

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
    val businessTypes: List<Int>? = emptyList(),
    val createDate: String? = null,

    val status: Int? = null,
    val subSystemType: Int? = 1
)

data class JobScene(
    val key: Int, val value: String, val typeName: String
)

data class JobSceneElement(
    val groupName: String, val list: List<JobScene>
)


data class DeviceAlarmInfo(
    val id: Long,
    val code: String?,
    val name: String?,
    val level: Int?,
    val levelName: String?,
    val primaryClass: Int?,
    val primaryClassName: String?,
    val firstAlarmTime: String?,
    val lastAlarmTime: String?,
    val msgType: Int?,
    val source: String?,
    val serialNum: String?,
    val dealUserName: String?,
    val confirmUserName: String?,
    val deviceId: String?,
    val extendFiled: String?,
    val address: String?,
    val poleId: String?,
    val poleName: String?,
    val alarmPicUrl: String?,
    val projectId: String?,
    val number: Int?,
    val newAlarm: Int?,
    val notificationState: Int?,
    val score: Double?,
    val riskLevel: Int?
)

data class AlarmRequestParam(
    val keyword: String, val curPage: Int, val pageSize: Int,
    //0未确认
    //1已确认
    val isConfirm: Int? = null, val level: Int? = null
)


data class DeviceStatusSummary(

    val total: Int?,
    val onlineCount: Int?,
    val onlinePercent: Double?,
    val lightUpCount: Int?,
    val lightUpPercent: Double?
)

data class LightEnergy(
    val month: String?, val degree: String?
)

data class LightYearEnergy(
    val thisYear: List<LightEnergy>? = null, val lastYear: List<LightEnergy>? = null
)

data class LightDayEnergy(

    val date: String?,

    val value: String?
)

data class GroupMemberReq(
    val id: Long?,
    val curPage: Int,
    val pageSize: Int,
    val netState: Int? = null,        // 网络状态 (例如: 1在线, 0离线, null全部)
    val bindState: Int? = null,       // 绑定状态
    val keyword: String? = "",        // 搜索关键字
    val subSystemType: Int? = 1       // 子系统类型 (JSON中是1，这里给个默认值或可空)
)

data class GroupMemberInfo(
    val deviceId: Long,          // 设备ID
    val deviceName: String?,       // 设备名称
    val serialNum: String?,        // 序列号 (SN)
    val createTime: String?,       // 创建时间
    val netState: Int?,            // 网络状态 (通常 1:在线, 0:离线)
    val syncState: Int?,           // 同步状态
    val bindState: Int?,           // 绑定状态
    val operateState: Int?,        // 运行/操作状态 (例如: 开/关/故障)
    val optType: Int?,             // 操作类型/模式
    val gwId: String?,             // 所属网关ID
    val gwName: String?,           // 所属网关名称
    val loopCtlId: String?,        // 所属回路控制器ID
    val loopCtlName: String?,      // 所属回路控制器名称
    // 回路具体信息
    val loopCode: String?,         // 回路编码
    val loopNum: String?           // 回路编号 (JSON中是null，可能是Int也可能是String，String更安全)
)

// 定义一个数据类来存放所有的筛选参数
data class GroupMemberFilter(
    val state: Int, val searchQuery: String, val bindState: Int, // 如果之前改为 Long 这里也要改
    val groupId: Long? // <--- 假设这是你的第4个参数
)

//产品详情
data class IotProductDetail(
    var id: Long? = null,
    var productTypeId: Int? = null,
    var productTypeName: String? = null,
    var productFactoryId: Long? = null,
    var productFactoryName: String? = null,
    var name: String? = null,
    var model: String? = null,
    var productProperties: String? = null,
    var installPosition: String? = null,
    var functionId: Int? = null,
    var accessType: Int? = null,
    var transportProtocol: String? = null,
    var protocolId: Int? = null,
    var messageProtocol: String? = null,
    var gwMetadata: String? = null,
    var metadata: String? = null,
    var photoUrl: String? = null,
    var power: String? = null,
    var warranty: String? = null,
    var description: String? = null,
    var cataloguePrice: BigDecimal? = null,
    var discountPrice: BigDecimal? = null,
    var specialPrice: BigDecimal? = null,
    var state: Int? = null,
    var isDeletable: Int? = null
)

data class EnvTelBO(
    val key: String,
    val value: String,
    val name: String,
    val unit: String?,
    val description: String,
    val ts: Long,
    val type: String
)
//data class TimeCondition(
//    //时间点：9：21
//    var timePoint: String? = null,
//    //1:日出,2：日落,3：时间点
//    var lngLatType: String? = null,
//    //日出偏移量
//    var sunrise: String? = null,
//    //日落偏移量
//    var sundown: String? = null,
//    //策略下发间隔(天)
//    var interval: Int? = null,
//    //是否自动执行
//    var isAutoExec: Int? = null,
//    //时间类型（1每天，2星期，3连续时间区间（7月28--8月28））
//    var timeType: Int? = null,
//    //对应时间类型：星期（星期一，星期二用1,2表示）
//    var week: String? = null,
//    // 对应时间类型：3 连续时间区间
//    var days: DayData? = null,
//    //自研灯控策略优先级1-16
//    var priority: Int? = null
//)

data class DayData(
    //对应时间类型：3连续时间区间的开始时间
    val startTime: String? = null,
    //对应时间类型：3连续时间区间的结束时间
    val endTime: String? = null
)

////时间策略
//data class TimeStrategy(
//    val id: Long, val require: TimeCondition, val action: StrategyAction
//)
//
////经纬度策略
//data class LngLatStrategy(
//    val id: Long, val require: LngLatCondition, val action: StrategyAction
//)
//
////经纬度条件
//data class LngLatCondition(
//    val syncLngLat: Int, val riseDown: RiseDown, val lngLatData: LngLatData
//)
//
data class RiseDown(
    //类型（日出：1 ，日落：2）
    val riseType: String,
    //日出偏移（正数表示延后，负数表示提前
    val sunrise: Int,
    //日落偏移（正数表示延后，负数表示提前）
    val sundown: Int
)

data class LngLatData(
    //是否同步经纬度(0否，1是)
    val isLngLat: Int,
    //经度 （前端没有设值，后端查数据库杆的经纬度）
    val lng: String,
    //纬度 （前端没有设值，后端查数据库杆的经纬度）
    val lat: String
)

data class LedPageBO(
    // 设备id
    val id: Long,
    // 设备名称
    val deviceName: String? = null,
    // 设备序列号
    val serialNum: String? = null,
    // 产品id
    val productId: Long,
    // 产品名称
    val productName: String? = null,
    // 产品名称
    val name: String? = null,
    // 网络状态，1-在线，0-离线
    val state: Int? = null,
    // 工作状态: 1告警 0正常
    val alarmType: Int? = null,
    // 多功能杆id
    val poleId: Long? = null,
    // 多功能杆名称
    val poleName: String? = null,
    // 创建时间
    val createTime: String? = null,
    // 子设备数量
    val childDevNum: Int? = null,
    // 亮度
    val brightness: String? = null,
    // 状态：1-唤醒，0-休眠
    val powerStatus: String? = null,
    // 音量
    val volume: String? = null,
    // 当前正在播放的节目
    val playingProgramName: String? = null,
    // 节目分辨率
    val widthHeighProgram: String? = null,
    // 最新截图
    val screenshot: String? = null,
    // 自动亮度调节
    val autoBrightness: Boolean? = null,
    // 编排类别 0LED播放盒 1媒体播放盒
    val arrangeType: Int? = null
)

data class LedProgramRequest(
    val keyword: String,
    val curPage: Int,
    val pageSize: Int,
    val reviewStatus: List<Int>? = null,//审核状态：0-待审核，1-审核通过，2-审核不通过,3-待提交
    val arrangeType: Int = 0,//编排类别 0LED播放盒 1媒体播放盒
    val subSystemType: Int = 3
)

data class LedProgramRes(
    val id: Long,
    val name: String?,
    val width: Int?,
    val height: Int?,
    val createTime: String?,
    val updateTime: String?,
    val createBy: String?,
    val createByName: String?,
    //审核状态：0待审核，1审核通过，2审核不通过
    val reviewStatus: Int?,
    //节目内容
    val json: String?,
    val reviewBy: String?,
    val reviewUser: String?,
    val reviewTime: String?,
    val remark: String?,
    val arrangeType: Int?,
    //收纳状态:0收纳中1收纳成功2收纳失败
    val storageStatus: Int?,
    //白名单状态：0未加入1加入
    val whiteStatus: Int?,
    //白名单任务状态：-1未执行0执行中1成功2失败
    val whiteTaskStatus: Int?
)

data class LedDevGroupRes(
    val id: Long,
    //分组名称
    val name: String?,
    //产品类型
    val primaryClass: Int?,
    //产品类型名称
    val primaryClassName: String?,
    //
    val updateTime: String?,
    //分组状态：0，正常 1.不可用
    val groupState: Int?,
    val gatewayName: String?,
    val gatewayId: String?,
    //0 软件分组，1硬件分组
    val type: Int?,
    val hardwareGroupId: String?,
    val createName: String?,
    val createTime: String?,
    val isModify: Int?,
    val strategyId: String?,
    val productId: Long,
    val productName: String?,
    //产品型号
    val productMode: String?,
    //产品型号备注
    val productDesc: String?,
    //编排类别 0LED播放盒 1媒体播放盒
    val arrangeType: Int?,
    val remark: String?,
    val brightSensorId: String?,
    //分组开关状态
    val onOff: Int?,
    //分组亮度
    val bright: Int?,
    //分组色温
    val colorTemperature: Int?,
    val exist: Int?,
    val commandParams: Any?,
    var groupDevs: List<PlayBoxDeviceBO>? = null
)

data class LedPlanBO(
    val id: Long? = null,
    var name: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    //周限值，中间用英文逗号分隔，7个值，分别表示星期一到星期天：1执行 0不执行
    var weekValue: String? = null, val createTime: String? = null, val updateTime: String? = null,
    //类型：1.指令(控制) 2.节目(播放)
    var type: Int? = null,
    //执行时间，HH:mm:ss
    val commandExecuteTime: String? = null,
    //指令类型:1.休眠 2.唤醒 3.重启 4.亮度(0-100)"
    val commandType: Int? = null,
    //指令值
    val commandValue: String? = null,
    //节目开始时间 格式18:00:00
    var programStartTime: String? = null,
    //节目结束时间 格式22:00:00
    var programEndTime: String? = null,
    //节目播放类型 100插播 200轮播
    var programPlayType: Int? = null,
    //节目优先级 0~100，同一个终端时，多个优先级必须不一样
    var programSort: Int? = null,
    var programId: String? = null,
    //节目名称
    var programName: String? = null,
    //是否有时间，0否1是
    var isTime: Int? = null,
    //是否有日期，0否1是
    var isDate: Int? = null,
    //是否有星期，0否1是
    var isWeek: Int? = null,
    //是否已发布，0否1是
    val isPublish: Int? = null,
    //同步状态，0 未同步，1 已同步
    val asyncStatus: Int? = null,
    val subSystemType: Int? = 3,
    //控制方案--执行计划
    var ctlPlanDetails: List<LedCtlPlanDetail>? = null
)

data class LedCtlPlanDetail(
    val id: Long? = null,
    //方案id
    val scheduleId: Long? = null,
    //指令类型:2.亮屏 3.重启 4.亮度(0-100)
    val commandType: Int? = null,
    //执行时间
    val time: String? = null,
    //指令值
    val commandValue: Int? = null,
    //开始时间：格式2020-10-22
    val startTime: String? = null,
    //结束时间：格式2020-10-22
    val endTime: String? = null,
    //创建时间
    val createTime: String? = null,
    //更新时间
    val updateTime: String? = null
)

//分组成员
data class PlayBoxDeviceBO(
    val id: Long,
    val name: String? = null,
    val uuid: String? = null,
    val longitude: String? = null,
    val latitude: String? = null,
    val primaryClass: String? = null,
    val primaryClassName: String? = null,
    val assetId: String? = null,
    val assetName: String? = null,
    val serialNum: String? = null,
    val description: String? = null,
    val workMode: String? = null,
    val boundLightGateway: String? = null // 同上
)

data class RealTimeDataTs(
    val key: String, val value: String, val ts: Long
)

data class LedMaterialInfoVO(

    //主键id
    var id: Long,

    //名称
    var name: String? = null,

    //1-目录 2-素材
    var type: Int? = null,

    //文件后缀，即文件格式
    var suffix: String? = null,

    //文件大小（字节）
    var size: Long? = null,

    //分辨率
    var resolution: String? = null,

    //文件相对路径
    var relativePath: String? = null,

    //视频封面路径
    var videoCoverPath: String? = null,

    var pictureMinioUrl: String? = null,

    //文本素材内容
    var txtContent: String? = null,

    //视频时长（单位秒）
    var videoTime: Long? = null,

    //审核人员
    var reviewBy: String? = null,

    //审核策略
    var reviewStrategy: String? = null,

    //审核状态：0-待审核，1-审核中，2-审核通过，3-审核不通过，4-申诉中
    var reviewStatus: Int? = null,

    //审核结果
    var reviewResult: String? = null,

    //审核时间
    var reviewTime: String? = null,

    //是否可以审核 0-不可以 1-可以
    var canReview: Int? = null,

    //是否可以申诉 0-不可以 1-可以
    var canAppeal: Int? = null,

    //是否可以申诉批复 0-不可以 1-可以
    var canAppealReply: Int? = null,

    //创建时间
    var createTime: String? = null,

    //创建者名称
    var creatorName: String? = null,

    //转码状态 0未转码  1转码中 2已转码
    var transCodingStatus: Int? = null,

    //审核流程日志
    var reviewProcessLog: String? = null,

    //AI审核违规详情
    var violationDetail: String? = null,

    //AI审核违规视频的截图
    var violationImages: String? = null,

    //审核流程 1-多级审核 2-AI+人工审核 3-人工审核 4-无审核
    var reviewProcessId: Long? = null,
)

data class LedFileReq(
    val keyword: String,
    val curPage: Int,
    val pageSize: Int,
    val materialType: String? = null,//素材类型 video-视频 image-图片 txt-文本 document-文档
    val reviewStatus: Int? = null,//审核状态 0-待审核，1-审核中，2-审核通过，3-审核不通过
    val queryType: Int? = 1,//素材管理(只查询上传者(超级管理员不限制)上传的素材，且可以查询素材和目录)
    val parentId: Long? = 0
)

/**
 * 4个参数
 * */
data class Quadruple<out A, out B, out C, out D>(
    val first: A, val second: B, val third: C, val fourth: D
)

// 用于记录路径层级的数据结构
data class FolderNode(val id: Long, val name: String)


//智慧路灯分组产品类型
data class LampGroupProduct(
    val id: Long, val productTypeId: Long, val name: String, val mode: String, val protocolId: Int
)

//创建分组
data class CreateGroupDTO(
    var groupName: String,
    var productId: Long? = null,
    //所属设备
    var deviceId: Long? = null,
    //所属设备集合，当productId=5603，即洲明回路控制器时，可以多选灯控网关设备
    var deviceIds: List<Long>? = emptyList<Long>(),
    var description: String? = null,
    var subSystemType: Int? = 1,
    var type: String? = null
)

//网关信息
data class DevSimpleInfo(
    val id: Long, val deviceName: String? = null
)

//操作分组设备
data class OptGroupDev(
    val groupId: Long, val deviceIds: List<Long>,
    //0删除，1新增
    val type: Int
)

//强制删除分组设备
data class ForceDelGroupDev(
    val groupId: Long, val deviceIds: List<Long>, val subSystemType: Int? = 1
)

//查询分组可添加的设备
data class GroupDevParam(
    val keyword: String, val curPage: Int, val pageSize: Int,
    //分组id
    val id: Long, val subSystemType: Int? = 1
)

data class GroupOptDevVO(
    // 设备id或回路id
    var id: Long,
    // 产品id
    var productId: Long? = null,
    // 产品名称
    var productName: String? = null,
    // 备注
    var description: String? = null,
    // 设备名称
    var deviceName: String? = null,
    // 设备序列号
    var serialNum: String? = null,
    // 集控id
    var gwId: Long? = null,
    // 集控名称
    var gwName: String? = null,
    // 回路控制器id
    var loopCtlId: Long? = null,
    // 回路控制器名称
    var loopCtlName: String? = null,
    // 回路名称
    var loopName: String? = null,
    // 回路编码
    var loopCode: String? = null,
    // 回路端口号
    var loopUuid: String? = null,
    // 回路序列号
    var loopNum: Int? = null,
    // 已绑定分组数
    var useGCount: Int = 0,
    // 可用分组数
    var unUseGCount: Int = 0
)

enum class GroupDevActionType {
    ADD,//分组添加设备
    REMOVE,//分组移除设备
    FORCE_REMOVE//分组强制移除设备
}

data class TaskIdRequest(
    var id: Long,
    /** 执行结果（1未执行或执行未成功 2执行成功 ） */
    var status: Int? = null,
    /** 设备名称 */
    var keyword: String? = null,
    /** 页数 */
    var curPage: Int? = null,
    /** 条数 */
    var pageSize: Int? = null
)

data class TaskInfo(
    var id: Long,
    /** 执行状态（1未执行或执行未成功 2执行成功 3.待确认） */
    var status: Int? = null,
    var updateDate: String? = null,
    var tryNum: Long? = null,
    /** 任务标识 */
    var transactionId: String? = null,
    /** 任务内容 */
    var context: String? = null,
    /** 相应数据 */
    var responseInfo: String? = null,
    var remark: String? = null,
    var parentId: Long? = null,
    var deviceName: String? = null,
    var businessId: Long? = null,
    /** 执行结果 */
    var cause: String? = null,
    /** 子任务 */
    var childJobs: List<TaskInfo>? = null
)

/**
 * 策略分组产品
 * */
data class StrategyProductVO(
    val productId: Long, val productName: String
)

data class StrategyGroupDTO(
    /** 策略id：编辑需要提供，新增不需要 */
    var strategyId: Long? = null,

    /** 产品id */
    var productId: Long? = null,

    /** 类型：0未选择，1已选择 */
    var type: Int? = null,

    /** 关键词：分组名称 */
    var keyword: String? = null,

    var projectId: Long? = null,

    /** 页数 */
    var curPage: Int? = null,
    /** 条数 */
    var pageSize: Int? = null,

    /** 子系统：1智慧路灯、2智慧景观、3智慧屏幕、4智慧光显、5光显控台、6智慧隧道 */
    var subSystemType: Int? = 1
)

data class StrategyGroupListVO(
    /** 分组id */
    var groupId: Long? = null,

    /** 分组名称 */
    var groupName: String? = null,

    /** 设备id */
    var deviceId: Long? = null,

    /** 分组类型（1.单灯，25集控，56回路） */
    var groupType: Long? = null,

    /** 设备名称 */
    var deviceName: String? = null,

    /** 同步状态 */
    var syncState: Int? = null,

    /** 组员数 */
    var count: Int? = null,

    /** 可用策略数量 */
    var availablePolicyNum: Int? = null,

    /** 最大策略数量 */
    var maxPolicyNum: Int? = null,

    /** 可用单灯策略数量 */
    var availablePolicyNumOfLight: Int? = null,

    /** 最大单灯策略数量 */
    var maxPolicyNumOfLight: Int? = null,

    /** 是否展示单灯策略数量：0或者null,不展示；1，展示 */
    var isShowPolicyNumOfLight: Int? = null
)

data class Tuple4<A, B, C, D>(val v1: A, val v2: B, val v3: C, val v4: D)


data class KeyValue(
    val key: String, val value: String
)

/**
 * 策略优先级范围
 * */
data class PriorityRange(
    val max: Int, val min: Int
)

data class TimeTaskConfig(
    val id: Int = 0,
    //时间点
    val time: String = "",
    //动作类型
    val actionType: Pair<Long, KeyValue>? = null,
    //动作值
    val actionValue: String = ""
)


//新增策略/更新策略
data class StrategyDTO(
    // 策略id
    var id: Long? = null,
    // 策略名称
    var name: String? = null,
    // 产品id
    var productId: Long? = null,
    // 分组id列表
    var groupId: List<Long>? = null,
    // 策略类型：1经纬度策略,2时间策略,5亮度传感器策略
    var strategyClass: Int? = null,
    // 策略类型：1 分组，2 广播策略，3 单灯-策略保存在单灯
    var strategyType: Int? = null,
    // 备注信息
    var description: String? = null,
    // 策略执行计划
    var content: List<JsonObject>? = null,
    // 执行类别：0手动触发，1指定时间
    var executeType: Int? = null,
    // 执行时间
    var executeTime: String? = null,
    // 子系统：1智慧路灯、2智慧景观、3智慧屏幕、4智慧光显、5光显控台、6智慧隧道
    var subSystemType: Int? = null
)

data class IdBody(
    val id: Long
)

data class IdsBody(
    val idList: List<Long>
)


data class TimeStrategyCondition(
    //时间点：9:21
    val timePoint: String?,
    //时间类型（1每天，2星期，3连续时间区间（7月28--8月28））
    val timeType: String?,
    //对应时间类型：星期（星期一，星期二用1,2表示）
    val week: String? = "",
    //对应时间类型：3 连续时间区间
    val days: DayData? = DayData(),
    //自研灯控策略优先级1-16
    val priority: Int?
)

data class StrategyAction(
    //执行动作类型（1调光，2开关,3调色温，5自定义指令，对应值为customize）
    val actionType: String? = null,
    //下发值（调光0-100，0关，1开）
    val actionValue: Int? = null,
    //色温值
    val temperature: Int? = 0,
    //自定义指令
    val customize: String? = ""
)

//时间策略内容
data class TimeStrategyContent(
    val id: Long,
    val require: TimeStrategyCondition,
    val action: StrategyAction
)

data class LngLatStrategyCondition(
    //时间类型（1每天，2星期，3连续时间区间（7月28--8月28））
    val timeType: String? = null,
    //对应时间类型：星期（星期一，星期二用1,2表示）
    val week: String? = null,
    //对应时间类型：3 连续时间区间
    val days: DayData? = null,
    //自研灯控策略优先级1-16
    val priority: Int? = null,
    val riseDown: RiseDown? = null,
    val lngLatData: LngLatData? = null,
)

data class LngLatStrategyContent(
    val require: LngLatStrategyCondition, val action: StrategyAction
)

data class PolicyConfig(
    //策略周期
    val periodTypes: List<Pair<Long, KeyValue>> = emptyList(),
    //策略优先级
    val priorityRange: PriorityRange? = null,
    //执行动作类型
    val actionTypes: List<Pair<Long, KeyValue>> = emptyList(),
    //最大size
    val maxSize: Long = 0L
)

data class SimpleProduct(
    val id: Long,
    val name: String,
    val productTypeId: Long,
    val functionId: Long,
    val model: String
)

data class AddDevice(
    val deviceName: String,
    val description: String,
    val serialNum: String,
    val productId: Long,
    val productTypeId: Long
)

data class UpdateDevice(
    val id: Long,
    val deviceName: String,
    val description: String,
    val serialNum: String,
    val productId: Long,
    val productTypeId: Long
)

data class LedCommandReq(
    //"指令类型:1.休眠 2.唤醒 3.重启 4.亮度(0-100) 5.截图 6 节目同步播放 7 NTP校时 8.发布节目 9.下发排程 10.同步分辨率 11.设置服务器地址 12.设置音量 13.获取终端日志 14 设置光感配置 15 清空排程和节目 16 节目收纳 17 场景切换"
    val type: Int,
    val value: Int,
    val deviceId: Long? = null,
    val groupId: Long? = null,
    // val val:
)

data class LedDevFunc(
    val key: String,
    val zhName: String,
    val enName: String,
    val esName: String,
    val zhDesc: String,
    val enDesc: String,
    val esDesc: String
)


data class LedGroupLogBO(
    val id: Long,
    val pushTransactionId: String?,
    //指令名称
    val instructName: String?,
    val operateTime: String?,
    val deviceId: String?,
    //设备名称
    val deviceName: String?,
    val groupId: String?,
    //1失败，2成功
    val state: Int?,
    //错误信息
    val errorName: String?,
    //执行时间
    val createTime: String?,
    val createBy: String?,
    val createName: String?
)

data class LedGroupMemberUpdate(
    val groupId: Long,
    val deviceIds: List<Long>,
    val subSystemType: Int? = 3,
)


data class LedScheduleAddDTO(
    // 名称
    var name: String,
    // 类型：1.指令 2.节目
    var type: Int,
    // 开始日期：格式2020-10-22
    var startDate: String? = null,
    // 结束日期：格式2020-10-22
    var endDate: String? = null,
    // 周限值，中间用英文逗号分隔，7个值，分别表示星期一到星期天：1执行 0不执行
    var weekValue: String? = null,
    // 指令执行时间：格式18:00:00
    var commandExecuteTime: String? = null,
    // 指令类型:1.休眠 2.唤醒 3.重启 4.亮度(0-100)
    var commandType: Int? = null,
    // 指令值
    var commandValue: Int? = null,
    // 节目开始时间 格式18:00:00
    var programStartTime: String? = null,
    // 节目结束时间 格式22:00:00
    var programEndTime: String? = null,
    // 节目播放类型 100插播 200轮播
    var programPlayType: Int? = null,
    // 节目优先级 0~100，同一个终端时，多个优先级必须不一样
    var programSort: Int? = null,
    // 节目ID
    var programId: Long? = null,
    // 是否有时间，0否1是
    var isTime: Int? = null,
    // 是否有日期，0否1是
    var isDate: Int? = null,
    // 是否有星期，0否1是
    var isWeek: Int? = null,
    // 子应用id
    var subSystemType: Int? = 3,
    // 执行计划
//    var executePlans: List<ScheduleExecutePlanDTO>? = null

)