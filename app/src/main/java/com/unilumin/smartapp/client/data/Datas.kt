package com.unilumin.smartapp.client.data

import androidx.compose.ui.graphics.vector.ImageVector
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
    var telemetryList: List<EnvTelBO>
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
    val type: String
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

data class StrategyAction(
    //执行动作类型（1调光，2开关,3调色温，5自定义指令，对应值为customize）
    var actionType: Int? = null,
    //下发值（调光0-100，0关，1开）
    var actionValue: Int? = null,
    //色温值
    var temperature: Int? = null,
    //自定义指令
    var customize: String? = null
)

data class TimeCondition(
    //时间点：9：21
    var timePoint: String? = null,
    //1:日出,2：日落,3：时间点
    var lngLatType: String? = null,
    //日出偏移量
    var sunrise: String? = null,
    //日落偏移量
    var sundown: String? = null,
    //策略下发间隔(天)
    var interval: Int? = null,
    //是否自动执行
    var isAutoExec: Int? = null,
    //时间类型（1每天，2星期，3连续时间区间（7月28--8月28））
    var timeType: Int? = null,
    //对应时间类型：星期（星期一，星期二用1,2表示）
    var week: String? = null,
    // 对应时间类型：3 连续时间区间
    var days: DayData? = null,
    //自研灯控策略优先级1-16
    var priority: Int? = null
)

data class DayData(
    //对应时间类型：3连续时间区间的开始时间
    val startTime: String,
    //对应时间类型：3连续时间区间的结束时间
    val endTime: String
)

//时间策略
data class TimeStrategy(
    val id: Long, val require: TimeCondition, val action: StrategyAction
)

//经纬度策略
data class LngLatStrategy(
    val id: Long, val require: LngLatCondition, val action: StrategyAction
)

//经纬度条件
data class LngLatCondition(
    val syncLngLat: Int, val riseDown: RiseDown, val lngLatData: LngLatData
)

data class RiseDown(
    //类型（日出：1 ，日落：2）
    val riseType: Integer,
    //日出偏移（正数表示延后，负数表示提前
    val sunrise: Integer,
    //日落偏移（正数表示延后，负数表示提前）
    val sundown: Integer
)

data class LngLatData(
    //是否同步经纬度(0否，1是)
    val isLngLat: Integer,
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
    val productId: Long? = null,
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
    val productId: String?,
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
    val commandParams: Any?
)

data class LedPlanBO(
    val id: Long,
    val name: String?,
    val startDate: String?,
    val endDate: String?,
    //周限值，中间用英文逗号分隔，7个值，分别表示星期一到星期天：1执行 0不执行
    val weekValue: String?,
    val createTime: String?,
    val updateTime: String?,
    //类型：1.指令(控制) 2.节目(播放)
    val type: Int?,
    //执行时间，HH:mm:ss
    val commandExecuteTime: String?,
    //指令类型:1.休眠 2.唤醒 3.重启 4.亮度(0-100)"
    val commandType: Int?,
    //指令值
    val commandValue: String?,
    //节目开始时间 格式18:00:00
    val programStartTime: String?,
    //节目结束时间 格式22:00:00
    val programEndTime: String?,
    //节目播放类型 100插播 200轮播
    val programPlayType: Int?,
    //节目优先级 0~100，同一个终端时，多个优先级必须不一样
    val programSort: Int?,
    val programId: String?,
    //节目名称
    val programName: String?,
    //是否有时间，0否1是
    val isTime: Int?,
    //是否有日期，0否1是
    val isDate: Int?,
    //是否有星期，0否1是
    val isWeek: Int?,
    //是否已发布，0否1是
    val isPublish: Int?,
    //同步状态，0 未同步，1 已同步
    val asyncStatus: Int?
)