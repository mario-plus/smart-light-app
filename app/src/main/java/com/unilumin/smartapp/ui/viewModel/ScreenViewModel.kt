package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.constant.DeviceConstant.fileTypeOptionsTransform
import com.unilumin.smartapp.client.data.LedDevGroupRes
import com.unilumin.smartapp.client.data.LedFileReq
import com.unilumin.smartapp.client.data.LedMaterialInfoVO
import com.unilumin.smartapp.client.data.LedPlanBO
import com.unilumin.smartapp.client.data.LedProgramRequest
import com.unilumin.smartapp.client.data.Quadruple
import com.unilumin.smartapp.client.service.ScreenService
import com.unilumin.smartapp.client.service.UserService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * 智慧屏幕
 * */
class ScreenViewModel(
    retrofitClient: RetrofitClient, application: Application
) : BaseViewModel(application) {


    private val screenService = retrofitClient.getService(ScreenService::class.java)
    private val userService = retrofitClient.getService(UserService::class.java)


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

    val ledProgramPagingFlow =
        createPagingFlow(
            combine(
                checkState,
                searchQuery,
                ::Pair
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


    val ledGroupPagingFlow =
        createPagingFlow(searchQuery) { searchQuery, page, size ->
            getLedGroup(
                searchQuery = searchQuery, page = page, pageSize = size
            )
        }

    /**
     * 方案管理分页数据
     * */
    val ledPlanPagingFlow =
        createPagingFlow(combine(planType, searchQuery, ::Pair)) {(planType, keywords), page, size ->
            getLedPlans(
                planType = planType,
                searchQuery = keywords,
                page = page,
                pageSize = size
            )
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
        val parseDataNewSuspend = UniCallbackService.parseDataNewSuspend(
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
                        val detailData = UniCallbackService.parseDataNewSuspend(
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
        val pageData = UniCallbackService.parseDataNewSuspend(
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
                        val detailData = UniCallbackService.parseDataNewSuspend(
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
        val pageData = UniCallbackService.parseDataNewSuspend(
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