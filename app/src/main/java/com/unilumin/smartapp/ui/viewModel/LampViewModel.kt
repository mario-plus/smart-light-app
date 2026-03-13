package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService.parseDataNewSuspend
import com.unilumin.smartapp.client.constant.FILTER_NONE
import com.unilumin.smartapp.client.data.CreateGroupDTO
import com.unilumin.smartapp.client.data.DevSimpleInfo
import com.unilumin.smartapp.client.data.DeviceStatusSummary
import com.unilumin.smartapp.client.data.ForceDelGroupDev
import com.unilumin.smartapp.client.data.GroupDevParam
import com.unilumin.smartapp.client.data.GroupMemberReq
import com.unilumin.smartapp.client.data.GroupOptDevVO
import com.unilumin.smartapp.client.data.GroupRequestParam
import com.unilumin.smartapp.client.data.JobRequestParam
import com.unilumin.smartapp.client.data.JobSceneElement
import com.unilumin.smartapp.client.data.LampCtlReq
import com.unilumin.smartapp.client.data.LampGroupInfo
import com.unilumin.smartapp.client.data.LampGroupProduct
import com.unilumin.smartapp.client.data.LightDayEnergy
import com.unilumin.smartapp.client.data.LightEnergy
import com.unilumin.smartapp.client.data.LightYearEnergy
import com.unilumin.smartapp.client.data.LoopCtlReq
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.OptGroupDev
import com.unilumin.smartapp.client.data.Quadruple
import com.unilumin.smartapp.client.data.RequestParam
import com.unilumin.smartapp.client.data.StrategyRequestParam
import com.unilumin.smartapp.client.service.RoadService
import com.unilumin.smartapp.util.ToastUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import retrofit2.Call

class LampViewModel(
    retrofitClient: RetrofitClient, application: Application
) : BaseViewModel(application) {

    //数据源
    private val _sceneOptions = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val sceneOptions = _sceneOptions.asStateFlow()

    //被选中的数据
    private val _selectSceneIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectSceneIds = _selectSceneIds.asStateFlow()
    fun updateSceneIds(ids: Set<Int>) {
        _selectSceneIds.value = ids
    }

    private val _isSwitch = MutableStateFlow(false)
    val isSwitch = _isSwitch.asStateFlow()

    private val roadService = retrofitClient.getService(RoadService::class.java)

    //0离线，1在线
    //策略状态
    //任务状态
    //分组类型
    val state = MutableStateFlow(-1)
    fun updateState(s: Int) {
        state.value = s
    }

    //策略同步状态
    val syncState = MutableStateFlow(-1)
    fun updateSyncState(s: Int) {
        syncState.value = s
    }

    //单灯模式
    val lampModel = MutableStateFlow(-1)
    fun updateLampModel(s: Int) {
        lampModel.value = s
    }


    //0未确认，1 已确认
    val alarmConfirm = MutableStateFlow(0)
    fun updateAlarmConfirm(s: Int) {
        alarmConfirm.value = s
    }

    //绑定状态
    val bindState = MutableStateFlow(-1)
    fun updateBindState(s: Int) {
        bindState.value = s
    }


    //设备列表查询参数(关键词)
    val searchQuery = MutableStateFlow("")
    fun updateSearch(query: String) {
        searchQuery.value = query
    }

    //首页在线率和亮灯率
    private val _deviceStatusSummary = MutableStateFlow<DeviceStatusSummary?>(null)
    val deviceStatusSummary = _deviceStatusSummary.asStateFlow()

    private val _currentGroupInfo = MutableStateFlow<LampGroupInfo?>(null)
    val currentGroupInfo = _currentGroupInfo.asStateFlow()
    fun updateCurrentGroupInfo(e: LampGroupInfo?) {
        _currentGroupInfo.value = e
    }

    //月度能耗对比
    private val _monthEnergyList = MutableStateFlow<List<LightEnergy>>(emptyList())
    val monthEnergyList = _monthEnergyList.asStateFlow()


    private val _dayEnergyList = MutableStateFlow<List<LightDayEnergy>>(emptyList())
    val dayEnergyList = _dayEnergyList.asStateFlow()


    private val _yearEnergyList = MutableStateFlow(LightYearEnergy())
    val yearEnergyList = _yearEnergyList.asStateFlow()


    //分组产品
    private val _groupProductList = MutableStateFlow<List<LampGroupProduct>>(emptyList())
    val groupProductList = _groupProductList.asStateFlow()


    //集控信息
    private val _gatewayDevSimpleInfo = MutableStateFlow<List<DevSimpleInfo>>(emptyList())
    val gatewayDevSimpleInfo = _gatewayDevSimpleInfo.asStateFlow()

    /**
     * 首页在线率和亮灯率
     * */
    suspend fun getStatusSummary() {
        val parseDataNewSuspend = parseDataNewSuspend(
            roadService.deviceStatusSummary()
        )
        _deviceStatusSummary.value = parseDataNewSuspend
    }

    /**
     * 当月数据对比
     * */
    suspend fun monthEnergyData() {
        val parseDataNewSuspend = parseDataNewSuspend(
            roadService.contrastLightEnergy()
        )
        if (parseDataNewSuspend != null) {
            _monthEnergyList.value = parseDataNewSuspend
        }
    }

    /**
     * 7天能耗
     * */
    suspend fun dayEnergyData() {
        val parseDataNewSuspend = parseDataNewSuspend(
            roadService.homeLightEnergy()
        )
        if (parseDataNewSuspend != null) {
            _dayEnergyList.value = parseDataNewSuspend
        }
    }

    suspend fun yearEnergyData() {
        val parseDataNewSuspend = parseDataNewSuspend(
            roadService.annualPowerConsumptionTrend()
        )
        if (parseDataNewSuspend != null) {
            _yearEnergyList.value = parseDataNewSuspend
        }
    }

    val lampLoopCtlFlow =
        createPagingFlow(combine(state, searchQuery, ::Pair)) { (filter, query), page, size ->
            fetchPageData {
                roadService.getLoopCtlList(
                    keyword = query,
                    curPage = page,
                    pageSize = size,
                    networkState = filter.takeIf { it != FILTER_NONE }
                )
            }
        }

    val lampGateWayFlow =
        createPagingFlow(combine(state, searchQuery, ::Pair)) { (filter, query), page, size ->
            fetchPageData {
                roadService.getGwCtlList(
                    RequestParam(
                        keyword = query,
                        curPage = page,
                        pageSize = size,
                        state = filter.takeIf { it != FILTER_NONE })
                )
            }
        }

    val lampLightGwFlow =
        createPagingFlow(combine(state, searchQuery, ::Pair)) { (filter, query), page, size ->
            fetchPageData {
                roadService.getLightGwList(
                    RequestParam(
                        keyword = query,
                        curPage = page,
                        pageSize = size,
                        state = filter.takeIf { it != FILTER_NONE })
                )
            }
        }

    // 5. 分组列表
    val lampGroupFlow =
        createPagingFlow(combine(state, searchQuery, ::Pair)) { (filter, query), page, size ->
            fetchPageData {
                roadService.getGroupList(
                    GroupRequestParam(
                        keyword = query,
                        curPage = page,
                        pageSize = size,
                        groupType = filter.takeIf { it != FILTER_NONE })
                )
            }
        }

    val lampLightFlow = createPagingFlow(
        combine(state, searchQuery, lampModel, ::Triple)
    ) { (taskState, searchQuery, lampModel), page, size ->
        fetchPageData {
            roadService.getLightCtlList(
                RequestParam(
                    keyword = searchQuery,
                    curPage = page,
                    pageSize = size,
                    state = taskState.takeIf { it != FILTER_NONE },
                    workMode = lampModel.takeIf { it != FILTER_NONE })
            )
        }
    }

    val deviceAlarmFlow = createPagingFlow(
        combine(state, searchQuery, alarmConfirm, ::Triple)
    ) { (level, searchQuery, alarmConfirm), page, size ->
        fetchPageData {
            roadService.deviceAlarmList(
                curPage = page,
                pageSize = size,
                keyword = searchQuery,
                level = level.takeIf { it != FILTER_NONE },
                isConfirm = alarmConfirm.takeIf { it != FILTER_NONE })
        }
    }

    suspend fun getGroupDevToOpt(
        curPage: Int, pageSize: Int, searchQuery: String, groupId: Long
    ): List<GroupOptDevVO> {
        return fetchPageData {
            roadService.getGroupDevToAdd(
                GroupDevParam(
                    curPage = curPage, pageSize = pageSize, keyword = searchQuery, id = groupId
                )
            )
        }
    }

    /**
     * 注意此处bindState和state替换
     * 目的是为了按条件隐藏在线/离线状态，所以使用state字段作为已绑定/未绑定，bindState作为在线/离线
     * */
    val groupMemberFlow = createPagingFlow(
        combine(currentGroupInfo.map { it?.id }, bindState, searchQuery, state, ::Quadruple)
    ) { (groupId, state, searchQuery, bindState), page, size ->
        fetchPageData {
            roadService.getGroupMembers(
                GroupMemberReq(
                    keyword = searchQuery,
                    curPage = page,
                    pageSize = size,
                    netState = state.takeIf { it != FILTER_NONE },
                    bindState = bindState.takeIf { it != FILTER_NONE },
                    id = groupId
                )
            )
        }
    }

    val lampStrategyFlow = createPagingFlow(
        combine(state, searchQuery, syncState, ::Triple)
    ) { (taskState, searchQuery, syncState), page, size ->
        fetchPageData {
            roadService.getStrategyList(
                StrategyRequestParam(
                    keyword = searchQuery,
                    curPage = page,
                    pageSize = size,
                    taskState = taskState.takeIf { it != FILTER_NONE },
                    syncState = syncState.takeIf { it != FILTER_NONE })
            )
        }
    }

    val lampJobFlow = createPagingFlow(
        combine(state, searchQuery, _selectSceneIds, ::Triple)
    ) { (state, searchQuery, sceneOptions), page, size ->
        val s = state.takeIf { it != FILTER_NONE }
        fetchPageData {
            roadService.getJobList(
                JobRequestParam(
                    businessTypes = _selectSceneIds.value.toList(),
                    keyword = searchQuery,
                    curPage = page,
                    pageSize = size,
                    status = s
                )
            )
        }
    }

    //设备控制按钮
    fun lampCtl(deviceId: Long, cmdType: Int, cmdValue: Int) {
        launchWithLoading {
            val call: Call<NewResponseData<String?>?>? = roadService.lampCtl(
                LampCtlReq(
                    cmdType = cmdType,
                    cmdValue = cmdValue,
                    ids = listOf(deviceId),
                    subSystemType = 1
                )
            )
            parseDataNewSuspend(call)
            ToastUtil.showSuccess(context, "操作成功")
        }
    }

    fun groupCtl(groupId: Long, cmdType: Int, cmdValue: Int) {
        launchWithLoading {
            val call: Call<NewResponseData<String?>?>? = roadService.groupCtl(
                LampCtlReq(
                    cmdType = cmdType, cmdValue = cmdValue, ids = listOf(groupId), subSystemType = 1
                )
            )
            parseDataNewSuspend(call)
            ToastUtil.showSuccess(context, "操作成功")
        }
    }


    //设备控制按钮
    fun loopCtl(id: Long, numList: List<Int>, onOff: Int) {
        launchWithLoading {
            val call: Call<NewResponseData<String?>?>? = roadService.loopCtl(
                LoopCtlReq(listOf(id), numList, onOff)
            )
            parseDataNewSuspend(call)
            ToastUtil.showSuccess(context, "操作成功")
        }
    }

    fun getGroupProduct() {
        launchWithLoading {
            val call = roadService.getGroupProduct()
            val resultData = parseDataNewSuspend(call)
            if (resultData != null) {
                _groupProductList.value = resultData
            } else {
                _groupProductList.value = emptyList()
            }
        }

    }

    fun getGatewayList(productId: Long) {
        launchWithLoading {
            val call = roadService.getGatewayList(productId)
            val resultData = parseDataNewSuspend(call)
            if (resultData != null) {
                _gatewayDevSimpleInfo.value = resultData
            } else {
                _gatewayDevSimpleInfo.value = emptyList()
            }
        }
    }


    fun createGroup(groupInfo: CreateGroupDTO) {
        launchWithLoading {
            var createGroup = roadService.createGroup(groupInfo)
            parseDataNewSuspend(createGroup)
        }

    }

    //分作成员操作
    fun optGroupDev(optInfo: OptGroupDev) {
        launchWithLoading {
            var createGroup = roadService.optGroupDev(optInfo)
            parseDataNewSuspend(createGroup)
        }
    }

    fun forceDelGroupDev(optInfo: ForceDelGroupDev) {
        launchWithLoading {
            var createGroup = roadService.forceDelGroupDev(optInfo)
            parseDataNewSuspend(createGroup)
        }
    }


    fun getJobScene() {
        launchWithLoading {
            val call: Call<NewResponseData<List<JobSceneElement>?>?>? =
                roadService.getJobSceneList()
            val parseDataNewSuspend = parseDataNewSuspend(call)
            _sceneOptions.value = buildList {
                parseDataNewSuspend?.forEach { e ->
                    e.list.forEach { k ->
                        add(k.key to "${k.typeName}-${k.value}")
                    }
                }
            }
        }
    }
}