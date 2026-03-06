package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.constant.DeviceConstant.fileTypeOptionsTransform
import com.unilumin.smartapp.client.data.LedDevGroupRes
import com.unilumin.smartapp.client.data.LedFileReq
import com.unilumin.smartapp.client.data.LedMaterialInfoVO
import com.unilumin.smartapp.client.data.LedPageBO
import com.unilumin.smartapp.client.data.LedPlanBO
import com.unilumin.smartapp.client.data.LedProgramRequest
import com.unilumin.smartapp.client.data.LedProgramRes
import com.unilumin.smartapp.client.data.Quadruple
import com.unilumin.smartapp.client.service.ScreenService
import com.unilumin.smartapp.client.service.UserService
import com.unilumin.smartapp.ui.viewModel.pages.GenericPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

/**
 * 智慧屏幕
 * */
class ScreenViewModel(
    retrofitClient: RetrofitClient, application: Application
) : BaseViewModel(application) {


    private val screenService = retrofitClient.getService(ScreenService::class.java)
    private val userService = retrofitClient.getService(UserService::class.java)

    //分页数据总数
    private val _totalCount = MutableStateFlow<Int>(0)
    val totalCount = _totalCount.asStateFlow()

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
    @OptIn(ExperimentalCoroutinesApi::class)
    val ledDevPagingFlow = combine(state, searchQuery) { state, keywords ->
        Pair(state, keywords)
    }.flatMapLatest { (state, keywords) ->
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
            pagingSourceFactory = {
                GenericPagingSource { page, pageSize ->
                    getLedInfo(
                        state = state, searchQuery = keywords, page = page, pageSize = pageSize
                    )
                }
            }).flow
    }.cachedIn(viewModelScope)

    /**
     * 播放表分页数据
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    val ledProgramPagingFlow = combine(checkState, searchQuery) { checkState, keywords ->
        Pair(checkState, keywords)
    }.flatMapLatest { (checkState, keywords) ->
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
            pagingSourceFactory = {
                GenericPagingSource { page, pageSize ->
                    getLedProgram(
                        checkState = checkState,
                        searchQuery = keywords,
                        page = page,
                        pageSize = pageSize
                    )
                }
            }).flow
    }.cachedIn(viewModelScope)

    /**
     * 分组管理分页数据
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    val ledGroupPagingFlow = searchQuery.flatMapLatest { keywords ->
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
            pagingSourceFactory = {
                GenericPagingSource { page, pageSize ->
                    getLedGroup(
                        searchQuery = keywords, page = page, pageSize = pageSize
                    )
                }
            }).flow
    }.cachedIn(viewModelScope)

    /**
     * 方案管理分页数据
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    val ledPlanPagingFlow = combine(planType, searchQuery) { planType, keywords ->
        Pair(planType, keywords)
    }.flatMapLatest { (planType, keywords) ->
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
            pagingSourceFactory = {
                GenericPagingSource { page, pageSize ->
                    getLedPlans(
                        planType = planType,
                        searchQuery = keywords,
                        page = page,
                        pageSize = pageSize
                    )
                }
            }).flow
    }.cachedIn(viewModelScope)


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


    suspend fun getLedInfo(
        state: Int,
        searchQuery: String,
        page: Int,
        pageSize: Int,
    ): List<LedPageBO> {
        val s = state.takeIf { it != -1 }
        val parseDataNewSuspend = UniCallbackService.parseDataNewSuspend(
            screenService.getLedList(
                searchQuery, page, pageSize, s
            )
        )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }

    suspend fun getLedProgram(
        checkState: Int,
        searchQuery: String,
        page: Int,
        pageSize: Int,
    ): List<LedProgramRes> {


        val parseDataNewSuspend = UniCallbackService.parseDataNewSuspend(
            screenService.getLedProgramList(
                LedProgramRequest(
                    keyword = searchQuery,
                    pageSize = pageSize,
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
        )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
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
        _totalCount.value = parseDataNewSuspend?.total ?: 0
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
        _totalCount.value = pageData?.total ?: 0
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
        _totalCount.value = pageData?.total ?: 0
        coroutineScope {
            fileList.map { file ->
                async {
                    val targetPath = file.videoCoverPath ?: file.relativePath
                    if (!targetPath.isNullOrBlank()) {
                        try {
                            val minioResponse = UniCallbackService.parseDataSuspend(
                                userService.getUserAvatarPath(targetPath)
                            )
                            minioResponse?.url?.let { validUrl ->
                                file.videoCoverPath = validUrl
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }.awaitAll()
        }
        return fileList
    }

}