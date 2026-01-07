package com.unilumin.smartapp.ui.screens.site

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.data.SiteInfo
import com.unilumin.smartapp.client.data.SiteRoadInfo
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray200
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray50
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray700
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.viewModel.SiteViewModel
import com.unilumin.smartapp.ui.viewModel.ViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SitesScreen(retrofitClient: RetrofitClient) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val siteViewModel: SiteViewModel = viewModel(
        factory = ViewModelFactory {
            SiteViewModel(retrofitClient, context)
        }
    )

    var totalCount = siteViewModel.totalCount.collectAsState()

    /**
     * 站点数据
     * */
    val pagingItems = siteViewModel.sitePagingFlow.collectAsLazyPagingItems()

    val siteRoadInfo by siteViewModel.siteRoadInfo.collectAsState()
    val searchKeyword by siteViewModel.searchKeyword.collectAsState()

    // 视图状态管理：list -> poles -> detail
    var currentView by remember { mutableStateOf("list") }
    // 新增：列表/地图 模式切换 (默认为列表)
    var isMapView by remember { mutableStateOf(false) }

    // 选中的数据
    var selectedSiteInfo by remember { mutableStateOf<SiteInfo?>(null) }
    var selectedRoad by remember { mutableStateOf<SiteRoadInfo?>(null) }
    var isRoadDropdownExpanded by remember { mutableStateOf(false) }


    // 返回键处理逻辑
    BackHandler(enabled = currentView != "list" || isMapView) {
        when {
            currentView == "detail" -> currentView = "poles"
            currentView == "poles" -> currentView = "list"
            // 如果在地图模式下按返回，切回列表模式
            currentView == "list" && isMapView -> isMapView = false
        }
    }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            SitesTopBar(
                currentView = currentView,
                title = when (currentView) {
                    "list" -> if (isMapView) "站点地图" else "站点管理"
                    "poles" -> selectedSiteInfo?.name ?: "站点详情"
                    else -> "设备详情"
                },
                onBack = {
                    if (currentView == "detail") currentView = "poles"
                    else if (currentView == "poles") currentView = "list"
                    else if (currentView == "list" && isMapView) isMapView = false
                }
            )
        }
    ) { paddingValues ->

        Crossfade(
            targetState = currentView,
            label = "SitesView",
            modifier = Modifier.padding(paddingValues)
        ) { view ->
            when (view) {
                // ================== 1. 站点列表/地图视图 ==================
                "list" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // 搜索栏始终显示 (根据需要，地图模式也可以隐藏部分筛选)
                        Box(
                            modifier = Modifier.padding(
                                top = 16.dp,
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 8.dp
                            )
                        ) {
                            SearchAndFilterSection(
                                selectedRoad = selectedRoad,
                                siteRoadInfo = siteRoadInfo,
                                searchKeyword = searchKeyword,
                                isDropdownExpanded = isRoadDropdownExpanded,
                                isMapView = isMapView, // 传入当前模式
                                onDropdownToggle = { isRoadDropdownExpanded = it },
                                onRoadSelected = {
                                    selectedRoad = it
                                    siteViewModel.updateRoadFilter(it?.id?.toString())
                                    isRoadDropdownExpanded = false
                                },
                                onKeywordChanged = { siteViewModel.updateSearchKeyword(it) },
                                onClearKeyword = { siteViewModel.updateSearchKeyword("") },
                                onToggleViewMode = { isMapView = !isMapView }, // 切换回调
                                focusManager = focusManager
                            )
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            if (isMapView) {
                                val currentItems = pagingItems.itemSnapshotList.items
                                SiteMapView(
                                    siteList = currentItems,
                                    onSiteClick = { site ->
                                        selectedSiteInfo = site
                                        currentView = "poles" // 点击地图Marker进入详情
                                    }
                                )
                            } else {
                                // 列表模式
                                PagingList(
                                    totalCount = totalCount.value,
                                    lazyPagingItems = pagingItems,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        bottom = 80.dp,
                                        start = 16.dp,
                                        end = 16.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    itemKey = { site -> site.id },
                                    emptyMessage = "未找到相关站点"
                                ) { siteInfo ->
                                    SiteCardItem(
                                        siteInfo = siteInfo,
                                        onClick = {
                                            selectedSiteInfo = siteInfo
                                            currentView = "poles"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // ================== 2. 杆体/设备列表视图 (保持不变) ==================
                "poles" -> {
                    // ... 保持原有代码不变 ...
                    // 为了篇幅，此处省略，直接使用你提供的原始代码块内容即可
                    val devices = selectedSiteInfo?.deviceList ?: emptyList()
                    if (devices.isEmpty()) {
                        EmptyStateView(message = "该站点下暂无设备")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = "共关联 ${devices.size} 个设备",
                                    fontSize = 14.sp,
                                    color = Gray500,
                                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                                )
                            }
                            items(
                                count = devices.size,
                                key = { index -> devices[index].id }
                            ) { index ->
                                val device = devices[index]
                                SiteDeviceCardItem(
                                    device = device,
                                    onClick = {}
                                )
                            }
                        }
                    }
                }

                // ================== 3. 详情视图 (保持不变) ==================
                "detail" -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("设备详情页开发中...", color = Gray400)
                    }
                }
            }
        }
    }
}


@Composable
fun SitesTopBar(currentView: String, title: String, onBack: () -> Unit) {
    Surface(
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = currentView != "list") {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Rounded.ArrowBack, "Back", tint = Gray900)
                }
            }
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
fun InfoLabelValue(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: ", fontSize = 12.sp, color = Gray400)
        Text(
            text = value,
            fontSize = 12.sp,
            color = Gray700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EmptyStateView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.SearchOff, null, tint = Gray200, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, color = Gray500)
    }
}

@Composable
fun AppendLoadState(loadState: LoadState, onRetry: () -> Unit) {
    when (loadState) {
        is LoadState.Loading -> {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = Blue600
                )
            }
        }

        is LoadState.Error -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("加载更多失败", fontSize = 12.sp, color = Gray500)
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onRetry) { Text("重试", color = Blue600, fontSize = 12.sp) }
            }
        }

        else -> {}
    }
}