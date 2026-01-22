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
import com.unilumin.smartapp.client.data.GroupRequestParam
import com.unilumin.smartapp.client.data.JobRequestParam
import com.unilumin.smartapp.client.data.JobSceneElement
import com.unilumin.smartapp.client.data.LampJobInfo
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
    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DIST = 2
        private const val FILTER_NONE = -1
    }


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


    //设备列表查询参数(关键词)
    val searchQuery = MutableStateFlow("")
    fun updateSearch(query: String) {
        searchQuery.value = query
    }

    // 1. 单灯列表
    val lampLightFlow = createPagingFlow(state, searchQuery) { page, size, filter, query ->
        fetchPageData(page, size) {
            roadService.getLightCtlList(
                RequestParam(keyword = query, curPage = page, pageSize = size, state = filter)
            )
        }
    }

    // 2. 回路列表
    val lampLoopCtlFlow = createPagingFlow(state, searchQuery) { page, size, filter, query ->
        fetchPageData(page, size) {
            roadService.getLoopCtlList(
                keyword = query, curPage = page, pageSize = size, networkState = filter
            )
        }
    }

    // 3. 网关列表
    val lampGateWayFlow = createPagingFlow(state, searchQuery) { page, size, filter, query ->
        fetchPageData(page, size) {
            roadService.getGwCtlList(
                RequestParam(keyword = query, curPage = page, pageSize = size, state = filter)
            )
        }
    }

    // 4. 光控网关列表
    val lampLightGwFlow = createPagingFlow(state, searchQuery) { page, size, filter, query ->
        fetchPageData(page, size) {
            roadService.getLightGwList(
                RequestParam(keyword = query, curPage = page, pageSize = size, state = filter)
            )
        }
    }

    // 5. 分组列表
    val lampGroupFlow = createPagingFlow(state, searchQuery) { page, size, groupType, query ->
        fetchPageData(page, size) {
            roadService.getGroupList(
                GroupRequestParam(
                    keyword = query, curPage = page, pageSize = size, groupType = groupType
                )
            )
        }
    }

    val lampStrategyFlow = createPagingFlow(state, searchQuery) { page, size, filter, query ->
        fetchPageData(page, size) {
            roadService.getStrategyList(
                StrategyRequestParam(
                    keyword = query, curPage = page, pageSize = size
                )
            )
        }
    }


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
                            page,
                            pageSize,
                            state,
                            searchQuery,
                            _selectSceneIds.value.toList()
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T : Any> createPagingFlow(
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

    /**
     * 专业修复版：精准匹配 Retrofit 的 Nullable 返回类型
     *
     * 之前的错误是因为缺少了 '?'，现在完全匹配您的截图：
     * Call<NewResponseData<PageResponse<T>?>?>?
     */
    private suspend fun <T : Any> fetchPageData(
        page: Int, pageSize: Int,
        // 【关键修复】这里完全照抄报错提示中的类型结构，允许所有层级为空
        apiCall: suspend () -> Call<NewResponseData<PageResponse<T>?>?>?
    ): List<T> {
        // 1. 获取 Call 对象
        val rawCall = apiCall()
        // 2. 调用 Service 解析
        // UniCallbackService 也需要同样的 Nullable 泛型结构来匹配
        val callbackService = UniCallbackService<PageResponse<T>>()
        // 此时 rawCall 的类型与 parseDataNewSuspend 要求的类型完全一致
        val parsedResult = callbackService.parseDataNewSuspend(rawCall, context)
        // 3. 更新总数并返回
        if (parsedResult != null) {
            _totalCount.value = parsedResult.total
        }
        return parsedResult?.list ?: emptyList()
    }
}