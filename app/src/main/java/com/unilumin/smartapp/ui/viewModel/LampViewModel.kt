package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.constant.FILTER_NONE
import com.unilumin.smartapp.client.constant.PAGE_SIZE
import com.unilumin.smartapp.client.constant.PREFETCH_DIST
import com.unilumin.smartapp.client.data.GroupRequestParam
import com.unilumin.smartapp.client.data.JobRequestParam
import com.unilumin.smartapp.client.data.JobSceneElement
import com.unilumin.smartapp.client.data.LampJobInfo
import com.unilumin.smartapp.client.data.LampLightInfo
import com.unilumin.smartapp.client.data.LampStrategyInfo
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.RequestParam
import com.unilumin.smartapp.client.data.StrategyRequestParam
import com.unilumin.smartapp.client.service.RoadService
import com.unilumin.smartapp.ui.viewModel.pages.GenericPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import retrofit2.Call

class LampViewModel(
    retrofitClient: RetrofitClient, application: Application
) : AndroidViewModel(application) {

    val context = getApplication<Application>()

    //分页数据总数


    //数据源
    private val _sceneOptions = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val sceneOptions = _sceneOptions.asStateFlow()

    //被选中的数据
    private val _selectSceneIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectSceneIds = _selectSceneIds.asStateFlow()
    fun updateSceneIds(ids: Set<Int>) {
        _selectSceneIds.value = ids
    }

    private val _totalCount = MutableStateFlow(0)
    val totalCount = _totalCount.asStateFlow()

    private val _isSwitch = MutableStateFlow(false)
    val isSwitch = _isSwitch.asStateFlow()

    // --- 状态管理 ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

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

    val lampModel = MutableStateFlow(-1)
    fun updateLampModel(s: Int) {
        lampModel.value = s
    }


    //设备列表查询参数(关键词)
    val searchQuery = MutableStateFlow("")
    fun updateSearch(query: String) {
        searchQuery.value = query
    }


    // 2. 回路列表
    val lampLoopCtlFlow = createPagingFlow(state, searchQuery) { page, size, filter, query ->
        fetchPageData {
            roadService.getLoopCtlList(
                keyword = query, curPage = page, pageSize = size, networkState = filter
            )
        }
    }

    // 3. 网关列表
    val lampGateWayFlow = createPagingFlow(state, searchQuery) { page, size, filter, query ->
        fetchPageData {
            roadService.getGwCtlList(
                RequestParam(keyword = query, curPage = page, pageSize = size, state = filter)
            )
        }
    }

    // 4. 光控网关列表
    val lampLightGwFlow = createPagingFlow(state, searchQuery) { page, size, filter, query ->
        fetchPageData {
            roadService.getLightGwList(
                RequestParam(keyword = query, curPage = page, pageSize = size, state = filter)
            )
        }
    }

    // 5. 分组列表
    val lampGroupFlow = createPagingFlow(state, searchQuery) { page, size, groupType, query ->
        fetchPageData {
            roadService.getGroupList(
                GroupRequestParam(
                    keyword = query, curPage = page, pageSize = size, groupType = groupType
                )
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val lampLightFlow =
        combine(state, searchQuery, lampModel) { taskState, searchQuery, lampModel ->
            Triple(taskState, searchQuery, lampModel)
        }.flatMapLatest { (taskState, searchQuery, lampModel) ->
            Pager(
                config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
                pagingSourceFactory = {
                    GenericPagingSource { page, pageSize ->
                        getLampList(
                            page, pageSize, searchQuery, taskState, lampModel
                        )
                    }
                }).flow
        }.cachedIn(viewModelScope)

    suspend fun getLampList(
        curPage: Int,
        pageSize: Int,
        searchQuery: String,
        state: Int,
        workModel: Int,
    ): List<LampLightInfo> {
        val parseDataNewSuspend =
            UniCallbackService<PageResponse<LampLightInfo>>().parseDataNewSuspend(
                roadService.getLightCtlList(
                    RequestParam(
                        keyword = searchQuery,
                        curPage = curPage,
                        pageSize = pageSize,
                        state = state.takeIf { it != FILTER_NONE },
                        workMode = workModel.takeIf { it != FILTER_NONE })
                ), context
            )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }

    suspend fun getStrategyList(
        curPage: Int,
        pageSize: Int,
        searchQuery: String,
        taskState: Int,
        syncState: Int,
    ): List<LampStrategyInfo> {
        val parseDataNewSuspend =
            UniCallbackService<PageResponse<LampStrategyInfo>>().parseDataNewSuspend(
                roadService.getStrategyList(
                    StrategyRequestParam(
                        keyword = searchQuery,
                        curPage = curPage,
                        pageSize = pageSize,
                        taskState = taskState.takeIf { it != FILTER_NONE },
                        syncState = syncState.takeIf { it != FILTER_NONE })
                ), context
            )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val lampStrategyFlow =
        combine(state, searchQuery, syncState) { taskState, searchQuery, syncState ->
            Triple(taskState, searchQuery, syncState)
        }.flatMapLatest { (taskState, searchQuery, syncState) ->
            Pager(
                config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
                pagingSourceFactory = {
                    GenericPagingSource { page, pageSize ->
                        getStrategyList(
                            page, pageSize, searchQuery, taskState, syncState
                        )
                    }
                }).flow
        }.cachedIn(viewModelScope)


    @OptIn(ExperimentalCoroutinesApi::class)
    val lampJobFlow =
        combine(state, searchQuery, _selectSceneIds) { state, searchQuery, selectSceneIds ->
            Triple(state, searchQuery, selectSceneIds)
        }.flatMapLatest { (state, searchQuery, sceneOptions) ->
            Pager(
                config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 2),
                pagingSourceFactory = {
                    GenericPagingSource { page, pageSize ->
                        getLampJobList(
                            page, pageSize, state, searchQuery, _selectSceneIds.value.toList()
                        )
                    }
                }).flow
        }.cachedIn(viewModelScope)


    suspend fun getLampJobList(
        curPage: Int, pageSize: Int, state: Int, searchQuery: String, sceneIds: List<Int>
    ): List<LampJobInfo> {

        val s = state.takeIf { it != FILTER_NONE }
        val parseDataNewSuspend =
            UniCallbackService<PageResponse<LampJobInfo>>().parseDataNewSuspend(
                roadService.getJobList(
                    JobRequestParam(
                        businessTypes = sceneIds,
                        keyword = searchQuery,
                        curPage = curPage,
                        pageSize = pageSize,
                        status = s
                    )
                ), context
            )
        _totalCount.value = parseDataNewSuspend?.total!!
        return parseDataNewSuspend.list
    }


    fun launchWithLoading(consumer: suspend () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                consumer()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getJobScene() {
        launchWithLoading {
            try {
                val call: Call<NewResponseData<List<JobSceneElement>?>?>? =
                    roadService.getJobSceneList()
                val parseDataNewSuspend =
                    UniCallbackService<List<JobSceneElement>>().parseDataNewSuspend(call, context)
                _sceneOptions.value = buildList {
                    parseDataNewSuspend?.forEach { e ->
                        e.list.forEach { k ->
                            add(k.key to "${k.typeName}-${k.value}")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * 两参数封装
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T : Any> createPagingFlow(
        filterFlow: Flow<Int>,
        queryFlow: Flow<String>,
        fetcher: suspend (page: Int, pageSize: Int, filter: Int?, query: String) -> List<T>
    ): Flow<PagingData<T>> {
        return combine(
            filterFlow, queryFlow, ::Pair
        ).flatMapLatest { (currentFilter, currentQuery) ->
            val validFilter = currentFilter.takeIf { it != FILTER_NONE }
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    initialLoadSize = PAGE_SIZE,
                    prefetchDistance = PREFETCH_DIST
                ), pagingSourceFactory = {
                    GenericPagingSource { page, pageSize ->
                        fetcher(page, pageSize, validFilter, currentQuery)
                    }
                }).flow
        }.cachedIn(viewModelScope)
    }

    suspend fun <T : Any> fetchPageData(
        apiCall: suspend () -> Call<NewResponseData<PageResponse<T>?>?>?
    ): List<T> {
        val rawCall = apiCall()
        val callbackService = UniCallbackService<PageResponse<T>>()
        val parsedResult = callbackService.parseDataNewSuspend(rawCall, context)
        if (parsedResult != null) {
            _totalCount.value = parsedResult.total
        }
        return parsedResult?.list ?: emptyList()
    }


}