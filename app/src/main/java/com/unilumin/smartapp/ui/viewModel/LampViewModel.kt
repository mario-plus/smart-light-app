package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
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
import com.unilumin.smartapp.client.data.LampGateWayInfo
import com.unilumin.smartapp.client.data.LampGroupInfo
import com.unilumin.smartapp.client.data.LampJobInfo
import com.unilumin.smartapp.client.data.LampLightInfo
import com.unilumin.smartapp.client.data.LampLoopCtlInfo
import com.unilumin.smartapp.client.data.LoopCtlReq
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.RequestParam
import com.unilumin.smartapp.client.data.StrategyRequestParam
import com.unilumin.smartapp.client.service.RoadService
import com.unilumin.smartapp.ui.viewModel.pages.GenericPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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



    private val _sceneData = MutableStateFlow<List<JobSceneElement>>(emptyList())
    val sceneData: StateFlow<List<JobSceneElement>> = _sceneData.asStateFlow()

    val flatCheckboxOptions: StateFlow<List<Pair<String, String>>> = _sceneData.map { groups ->
        groups.flatMap { group ->
            group.list.map { scene ->
                val uniqueKey = "${scene.typeName}-${scene.key}"
                val displayName = scene.value
                displayName to uniqueKey
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. 选中的 ID 集合 (存储的是 "typeName-key")
    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    // 切换单个选中状态
    fun toggleSelection(uniqueId: String) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply {
            if (contains(uniqueId)) remove(uniqueId) else add(uniqueId)
        }
    }

    // 全选/反选逻辑
    fun toggleAllSelection() {
        val allIds = flatCheckboxOptions.value.map { it.second }
        val currentIds = _selectedIds.value
        if (currentIds.containsAll(allIds) && allIds.isNotEmpty()) {
            _selectedIds.value = emptySet() // 全取消
        } else {
            _selectedIds.value = allIds.toSet() // 全选
        }
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


    val lampJobFlow = createPagingFlow(state, searchQuery) { page, size, state, searchQuery ->
        fetchPageData(page, size) {
            roadService.getJobList(
                JobRequestParam(
                    keyword = searchQuery, curPage = page, pageSize = size, status = state
                )
            )
        }
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
                var parseDataNewSuspend =
                    UniCallbackService<List<JobSceneElement>>().parseDataNewSuspend(call, context)
                if (parseDataNewSuspend != null) {
                    _sceneData.value = parseDataNewSuspend
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
            filterFlow,
            queryFlow,
            ::Pair
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