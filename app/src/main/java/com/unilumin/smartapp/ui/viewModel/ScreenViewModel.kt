package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import com.google.gson.reflect.TypeToken
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.UniCallbackService.parseDataNewSuspend
import com.unilumin.smartapp.client.constant.DeviceConstant.fileTypeOptionsTransform
import com.unilumin.smartapp.client.data.IdsBody
import com.unilumin.smartapp.client.data.LedCommandReq
import com.unilumin.smartapp.client.data.LedDevFunc
import com.unilumin.smartapp.client.data.LedDevGroupRes
import com.unilumin.smartapp.client.data.LedFileReq
import com.unilumin.smartapp.client.data.LedGroupMemberUpdate
import com.unilumin.smartapp.client.data.LedMaterialInfoVO
import com.unilumin.smartapp.client.data.LedPageBO
import com.unilumin.smartapp.client.data.LedPlanBO
import com.unilumin.smartapp.client.data.LedProgramRequest
import com.unilumin.smartapp.client.data.PlayBoxDeviceBO
import com.unilumin.smartapp.client.data.Quadruple
import com.unilumin.smartapp.client.service.RoadService
import com.unilumin.smartapp.client.service.ScreenService
import com.unilumin.smartapp.client.service.UserService
import com.unilumin.smartapp.util.JsonUtils
import com.unilumin.smartapp.util.ToastUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

/**
 * 智慧屏幕
 * */
class ScreenViewModel(
    retrofitClient: RetrofitClient, application: Application
) : BaseViewModel(application) {


    private val screenService = retrofitClient.getService(ScreenService::class.java)

    private val userService = retrofitClient.getService(UserService::class.java)

    private val roadService = retrofitClient.getService(RoadService::class.java)

    //选中的播放盒
    val screenshot = MutableStateFlow("")
    private val _selectLedDevInfo = MutableStateFlow<LedPageBO?>(null)
    val selectLedDevInfo = _selectLedDevInfo.asStateFlow()

    //选中的播放盒分组
    private val _selectLedGroup = MutableStateFlow<LedDevGroupRes?>(null)
    val selectLedGroup = _selectLedGroup.asStateFlow()


    //播放盒设备功能
    private val _ledDevFuncMaps = MutableStateFlow<Map<String, LedDevFunc>>(emptyMap())
    val ledDevFuncMaps = _ledDevFuncMaps.asStateFlow()


    //播放盒分组设备
    private val _ledGroupDevMember = MutableStateFlow<List<PlayBoxDeviceBO>>(emptyList())
    val ledGroupDevMember = _ledGroupDevMember.asStateFlow()

    //播放盒分组可添加的设备
    private val _ledGroupDevOptional = MutableStateFlow<List<PlayBoxDeviceBO>>(emptyList())
    val ledGroupDevOptional = _ledGroupDevOptional.asStateFlow()


    //设备网络状态
    val state = MutableStateFlow(-1)
    fun updateState(s: Int) {
        state.value = s
    }

    //审核状态  全部，3待提交，0待审核，1-2已审核
    val checkState = MutableStateFlow(-1)
    fun updateCheckState(s: Int) {
        checkState.value = s
    }

    //1-控制方案，2播放方案
    val planType = MutableStateFlow(-1)
    fun updateType(type: Int) {
        planType.value = type
    }


    //video,audio,image,document,txt
    val fileType = MutableStateFlow(-1)
    fun updateFileType(type: Int) {
        fileType.value = type
    }


    val parentId = MutableStateFlow(0L)
    fun updateParentId(type: Long) {
        parentId.value = type
    }

    //素材审核状态
    val fileStatus = MutableStateFlow(-1)
    fun updateFileStatus(status: Int) {
        fileStatus.value = status
    }

    //关键词
    val searchQuery = MutableStateFlow("")
    fun updateSearch(query: String) {
        searchQuery.value = query
    }

    fun getLedDevFunc(ledDevInfo: LedPageBO, onSuccess: (() -> Unit)? = null) {
        launchWithLoading(onSuccess = onSuccess) {
            _selectLedDevInfo.value = ledDevInfo
            getMinioUrl(ledDevInfo.screenshot)?.let { url ->
                screenshot.value = url
            }
            _ledDevFuncMaps.value =
                fetchAndParseDevFuncMap(ledDevInfo.productId, "infoPublicControl")
        }
    }

    fun getLedGroupFunc(ledGroupRes: LedDevGroupRes, onSuccess: (() -> Unit)? = null) {
        launchWithLoading(onSuccess = onSuccess) {
            _selectLedGroup.value = ledGroupRes
            _ledDevFuncMaps.value =
                fetchAndParseDevFuncMap(ledGroupRes.productId, "infoPublicGroupControl")
        }
    }

    fun updateLedGroupMember(request: LedGroupMemberUpdate, onSuccess: (() -> Unit)? = null) {
        launchWithLoading(onSuccess = onSuccess) {
            parseDataNewSuspend(screenService.updateGroupMember(request))
        }
    }


    fun editGroupMember(ledGroupRes: LedDevGroupRes, onSuccess: (() -> Unit)? = null) {
        launchWithLoading(onSuccess = onSuccess) {
            _selectLedGroup.value = ledGroupRes
            val parseData = parseDataNewSuspend(screenService.getLedGroupMember(ledGroupRes.id))
            parseData?.let {
                _ledGroupDevMember.value = it
            }
            var parseDataNewSuspend =
                parseDataNewSuspend(screenService.getLedGroupDevOptional(ledGroupRes.id))
            parseDataNewSuspend?.let {
                _ledGroupDevOptional.value = it
            }
        }
    }

    private suspend fun fetchAndParseDevFuncMap(
        productId: Long, key: String
    ): Map<String, LedDevFunc> {
        return try {
            val parseData = parseDataNewSuspend(roadService.getProductRule(productId = productId))
            val arrayData = parseData?.getAsJsonArray(key)
            if (arrayData != null && arrayData.size() > 0) {
                val type = object : TypeToken<List<LedDevFunc>>() {}.type
                val funcList: List<LedDevFunc> = JsonUtils.gson.fromJson(arrayData, type)
                funcList.associateBy { it.key }
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }


    /**
     * 指令控制
     * */
    fun ledCommand(commandReq: LedCommandReq) {
        launchWithLoading {
            parseDataNewSuspend(screenService.ledCommand(commandReq))
            ToastUtil.showSuccess(context, "操作成功")
        }
    }

    fun delLedPlans(id: Long) {
        launchWithLoading {
            parseDataNewSuspend(screenService.delLedPlans(IdsBody(idList = listOf(id))))
            ToastUtil.showSuccess(context, "操作成功")
        }
    }


    fun ledDevDetail(deviceId: Long) {
        launchWithLoading {
            var parseDataNewSuspend = parseDataNewSuspend(screenService.getLedDevDetail(deviceId))
            if (parseDataNewSuspend != null) {
                //此处只更新截图，不更新整个页面数据，后端数据有问题，导致页面更新异常，亮度值0-->100
                //_selectLedDevInfo.value = parseDataNewSuspend
                var minioUrl = getMinioUrl(parseDataNewSuspend.screenshot)
                if (minioUrl != null) {
                    screenshot.value = minioUrl
                }
            }
        }
    }

    /**
     * 播放盒分页数据
     * */
    val ledDevPagingFlow =
        createPagingFlow(combine(state, searchQuery, ::Pair)) { (state, keywords), page, size ->
            fetchPageData {
                screenService.getLedList(
                    keywords, page, size, state.takeIf { it != -1 })
            }
        }


    /**
     * 播放表分页数据
     * */

    val ledProgramPagingFlow = createPagingFlow(
        combine(
            checkState, searchQuery, ::Pair
        )
    ) { (checkState, keywords), page, size ->
        fetchPageData {
            screenService.getLedProgramList(
                LedProgramRequest(
                    keyword = keywords,
                    pageSize = size,
                    curPage = page,
                    reviewStatus = when (checkState) {
                        -1 -> null
                        0 -> listOf(0)
                        1 -> listOf(1, 2)
                        2 -> listOf(3)
                        else -> null
                    }
                )
            )
        }
    }


    val ledGroupPagingFlow = createPagingFlow(searchQuery) { searchQuery, page, size ->
        getLedGroup(
            searchQuery = searchQuery, page = page, pageSize = size
        )
    }

    /**
     * 方案管理分页数据
     * */
    val ledPlanPagingFlow = createPagingFlow(
        combine(
            planType, searchQuery, ::Pair
        )
    ) { (planType, keywords), page, size ->
        getLedPlans(
            planType = planType, searchQuery = keywords, page = page, pageSize = size
        )
    }

    /**
     * led分组日志
     * */
    val ledGroupLogPagingFlow = createPagingFlow { page, size ->
        fetchPageData {
            screenService.getLedGroupLog(page, size, groupId = selectLedGroup.value?.id ?: 0L)
        }
    }


    // 素材列表
    val ledFilePagingFlow = createPagingFlow(
        combine(fileStatus, fileType, searchQuery, parentId, ::Quadruple)
    ) { (fileStatus, fileType, searchQuery, parentId), page, size ->
        getLedFiles(
            checkStatus = fileStatus,
            fileType = fileType,
            searchQuery = searchQuery,
            page = page,
            pageSize = size,
            parentId = parentId
        )
    }


    suspend fun getLedGroup(
        searchQuery: String,
        page: Int,
        pageSize: Int,
    ): List<LedDevGroupRes> {
        val parseDataNewSuspend = parseDataNewSuspend(
            screenService.getLedGroupList(
                keyword = searchQuery, pageSize = pageSize, curPage = page
            )
        )
        val groupList = parseDataNewSuspend?.list ?: emptyList()
        updateTotalCount(parseDataNewSuspend?.total ?: 0)
        if (groupList.isNotEmpty()) {
            coroutineScope {
                groupList.map { group ->
                    async {
                        val detailData = parseDataNewSuspend(
                            screenService.getLedGroupMember(group.id)
                        )
                        group.groupDevs = detailData
                    }
                }.awaitAll()
            }
        }
        return groupList
    }

    suspend fun getLedPlans(
        planType: Int,
        searchQuery: String,
        page: Int,
        pageSize: Int,
    ): List<LedPlanBO> {
        val pageData = parseDataNewSuspend(
            screenService.getLedPlans(
                keyword = searchQuery, pageSize = pageSize, curPage = page, type = planType
            )
        )
        val planList = pageData?.list ?: emptyList()
        updateTotalCount(pageData?.total ?: 0)
        if (planType == 1 && planList.isNotEmpty()) {
            coroutineScope {
                planList.map { plan ->
                    async {
                        val detailData = parseDataNewSuspend(
                            screenService.getLedCtlPlanDetail(plan.id)
                        )
                        plan.ctlPlanDetails = detailData
                    }
                }.awaitAll()
            }
        }
        return planList
    }

    suspend fun getLedFiles(
        checkStatus: Int,
        fileType: Int,
        searchQuery: String,
        page: Int,
        pageSize: Int,
        parentId: Long
    ): List<LedMaterialInfoVO> {
        val pageData = parseDataNewSuspend(
            screenService.getLedFileList(
                LedFileReq(
                    keyword = searchQuery,
                    pageSize = pageSize,
                    curPage = page,
                    materialType = fileType.takeIf { it != -1 }?.let { key ->
                        fileTypeOptionsTransform[key]
                    },
                    reviewStatus = checkStatus.takeIf { it != -1 },
                    parentId = parentId
                )
            )
        )
        val fileList = pageData?.list ?: emptyList()
        updateTotalCount(pageData?.total ?: 0)
        coroutineScope {
            fileList.map { file ->
                async {
                    //获取封面
                    val targetPath = file.videoCoverPath ?: file.relativePath
                    val realUrl = getMinioUrl(targetPath)
                    if (realUrl != null) {
                        file.pictureMinioUrl = realUrl
                    }
                }
            }.awaitAll()
        }
        return fileList
    }


    suspend fun getMinioUrl(targetPath: String?): String? {
        if (targetPath.isNullOrBlank()) return null
        return try {
            val minioResponse = UniCallbackService.parseDataSuspend(
                userService.getUserAvatarPath(targetPath)
            )
            minioResponse?.url
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}