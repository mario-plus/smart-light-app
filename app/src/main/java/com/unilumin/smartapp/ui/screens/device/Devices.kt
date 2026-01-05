package com.unilumin.smartapp.ui.screens.device

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceType
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.ui.components.EmptyDataView
import com.unilumin.smartapp.ui.components.EndOfListView
import com.unilumin.smartapp.ui.components.ErrorRetryView
import com.unilumin.smartapp.ui.components.FilterChip
import com.unilumin.smartapp.ui.components.PageAppendLoadingView
import com.unilumin.smartapp.ui.components.PageLoadingView
import com.unilumin.smartapp.ui.components.SearchBar
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray200
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.White
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel

// --- Main Screen ---
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    retrofitClient: RetrofitClient,
    onDetailClick: (LightDevice) -> Unit
) {
    val context = LocalContext.current
    val deviceViewModel: DeviceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceViewModel(retrofitClient, context) as T
        }
    })
    val activeFilter by deviceViewModel.currentFilter.collectAsState()
    val searchQuery by deviceViewModel.searchQuery.collectAsState()
    val lazyPagingItems = deviceViewModel.devicePagingFlow.collectAsLazyPagingItems()
    val isRefreshing =
        lazyPagingItems.loadState.refresh is LoadState.Loading && lazyPagingItems.itemCount > 0
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
    ) {

        Surface(
            color = White, shadowElevation = 2.dp, modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
        ) {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                // 1. 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "设备列表", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gray900
                    )
                    Surface(
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Gray200),
                        color = White,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { }) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.FilterList,
                                null,
                                tint = Gray500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 2. 搜索框
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { deviceViewModel.updateSearch(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. 筛选 Tabs
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(DeviceType.DataList) { (id, label) ->
                        val isActive = activeFilter == id
                        FilterChip(
                            label = label,
                            isActive = isActive,
                            onClick = { deviceViewModel.updateFilter(id) })
                    }
                }
            }
        }

        // --- 列表内容 (使用 PullToRefreshBox 包裹) ---
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { lazyPagingItems.refresh() },
            modifier = Modifier.weight(1f) // 填满剩余高度
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(lazyPagingItems.itemCount) { index ->
                    val device = lazyPagingItems[index]
                    if (device != null) {
                        DeviceCardItem(
                            deviceViewModel,
                            device,
                            deviceViewModel.currentFilter.value,
                            onDetailClick
                        )
                    }
                }

                lazyPagingItems.apply {
                    when {
                        // 初始加载 (列表为空时显示中间的大 Loading)
                        loadState.refresh is LoadState.Loading && lazyPagingItems.itemCount == 0 -> {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PageLoadingView()
                                }
                            }
                        }

                        // 底部加载更多
                        loadState.append is LoadState.Loading -> item { PageAppendLoadingView() }

                        // 到底了
                        loadState.append.endOfPaginationReached && lazyPagingItems.itemCount > 0 -> item { EndOfListView() }

                        // 错误处理
                        loadState.refresh is LoadState.Error -> item { ErrorRetryView("加载失败") { retry() } }

                        // 空数据
                        loadState.refresh is LoadState.NotLoading && lazyPagingItems.itemCount == 0 -> item {
                            EmptyDataView(
                                "未找数据"
                            )
                        }
                    }
                }
            }
        }
    }
}
