package com.unilumin.smartapp.ui.screens.site

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.data.SiteDevice
import com.unilumin.smartapp.client.data.SiteInfo
import com.unilumin.smartapp.client.data.SiteRoadInfo
import com.unilumin.smartapp.ui.components.EmptyDataView
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray50
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.viewModel.SiteViewModel
import com.unilumin.smartapp.ui.viewModel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SitesScreen(retrofitClient: RetrofitClient) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val siteViewModel: SiteViewModel = viewModel(
        factory = ViewModelFactory {
            SiteViewModel(retrofitClient, context)
        }
    )

    val totalCount = siteViewModel.totalCount.collectAsState()
    val pagingItems = siteViewModel.sitePagingFlow.collectAsLazyPagingItems()
    val siteRoadInfo by siteViewModel.siteRoadInfo.collectAsState()
    val searchKeyword by siteViewModel.searchKeyword.collectAsState()

    // --- 状态管理 ---
    // 列表/地图 模式切换
    var isMapView by remember { mutableStateOf(false) }

    // 导航深度状态 (替代原来的 string currentView)
    // Level 1: selectedSiteInfo == null (列表/地图)
    // Level 2: selectedSiteInfo != null (站点设备列表)
    // Level 3: selectedDevice != null (设备详情)
    var selectedSiteInfo by remember { mutableStateOf<SiteInfo?>(null) }
    var selectedDevice by remember { mutableStateOf<SiteDevice?>(null) }

    // 筛选下拉状态
    var selectedRoad by remember { mutableStateOf<SiteRoadInfo?>(null) }
    var isRoadDropdownExpanded by remember { mutableStateOf(false) }

    // --- 返回键逻辑 ---
    // 优先级：关闭设备详情 -> 关闭站点详情 -> 退出地图模式
    BackHandler(enabled = selectedDevice != null || selectedSiteInfo != null || isMapView) {
        when {
            selectedDevice != null -> selectedDevice = null
            selectedSiteInfo != null -> selectedSiteInfo = null
            isMapView -> isMapView = false
        }
    }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            // 根据当前状态计算标题
            val title = when {
                selectedDevice != null -> "设备详情"
                selectedSiteInfo != null -> selectedSiteInfo?.name ?: "站点详情"
                isMapView -> "站点地图"
                else -> "站点管理"
            }
            // 只要不是在最外层列表，都显示返回按钮
            val showBack = isMapView || selectedSiteInfo != null

            SitesTopBar(
                title = title,
                showBack = showBack,
                onBack = {
                    when {
                        selectedDevice != null -> selectedDevice = null
                        selectedSiteInfo != null -> selectedSiteInfo = null
                        isMapView -> isMapView = false
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ================== Layer 1: 底层内容 (地图 OR 列表) ==================
            // 核心逻辑：如果是地图模式，让 SiteMapView 始终处于 Composition 中
            // 即使被上面的 Detail 盖住，也不要移除它，这样才能保持状态

            if (isMapView) {
                // 这里获取数据快照给地图
                val currentItems = pagingItems.itemSnapshotList.items
                SiteMapView(
                    siteList = currentItems,
                    onSiteClick = { site ->
                        selectedSiteInfo = site // 触发 Layer 2 显示
                    }
                )
            }

            // 列表模式：只有在不是地图模式，且没有选中站点详情时才显示
            // (列表模式下进入详情，列表可以被销毁或隐藏，不影响状态，因为列表恢复成本低，且Paging库有缓存)
            if (!isMapView && selectedSiteInfo == null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 搜索栏放在这里，只在列表/地图主页显示
                    Box(
                        modifier = Modifier.padding(
                            top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp
                        )
                    ) {
                        SearchAndFilterSection(
                            selectedRoad = selectedRoad,
                            siteRoadInfo = siteRoadInfo,
                            searchKeyword = searchKeyword,
                            isDropdownExpanded = isRoadDropdownExpanded,
                            isMapView = isMapView,
                            onDropdownToggle = { isRoadDropdownExpanded = it },
                            onRoadSelected = {
                                selectedRoad = it
                                siteViewModel.updateRoadFilter(it?.id?.toString())
                                isRoadDropdownExpanded = false
                            },
                            onKeywordChanged = { siteViewModel.updateSearchKeyword(it) },
                            onClearKeyword = { siteViewModel.updateSearchKeyword("") },
                            onToggleViewMode = { isMapView = !isMapView },
                            focusManager = focusManager
                        )
                    }

                    PagingList(
                        totalCount = totalCount.value,
                        lazyPagingItems = pagingItems,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        itemKey = { site -> site.id },
                        emptyMessage = "未找到相关站点"
                    ) { siteInfo ->
                        SiteCardItem(
                            siteInfo = siteInfo,
                            onClick = { selectedSiteInfo = siteInfo }
                        )
                    }
                }
            } else if (isMapView && selectedSiteInfo == null) {
                // 地图模式下的搜索栏浮层
                // 必须重复写一下或者抽离出来，因为上面的Column在selectedSiteInfo!=null时会隐藏
                // 但在地图模式下，我们希望搜索栏也随着进入详情而隐藏，或者一直浮在上面？
                // 这里选择：进入详情后，搜索栏隐藏，让位给详情内容
                Box(
                    modifier = Modifier.padding(
                        top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp
                    )
                ) {
                    SearchAndFilterSection(
                        selectedRoad = selectedRoad,
                        siteRoadInfo = siteRoadInfo,
                        searchKeyword = searchKeyword,
                        isDropdownExpanded = isRoadDropdownExpanded,
                        isMapView = isMapView,
                        onDropdownToggle = { isRoadDropdownExpanded = it },
                        onRoadSelected = {
                            selectedRoad = it
                            siteViewModel.updateRoadFilter(it?.id?.toString())
                            isRoadDropdownExpanded = false
                        },
                        onKeywordChanged = { siteViewModel.updateSearchKeyword(it) },
                        onClearKeyword = { siteViewModel.updateSearchKeyword("") },
                        onToggleViewMode = { isMapView = !isMapView },
                        focusManager = focusManager
                    )
                }
            }

            // ================== Layer 2 & 3: 详情覆盖层 (Overlay) ==================
            // 使用 AnimatedVisibility 做进入退出动画
            // 当 selectedSiteInfo 不为空时，这个 Surface 会盖住底下的地图/列表

            AnimatedVisibility(
                visible = selectedSiteInfo != null,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.zIndex(2f) // 确保在最上层
            ) {
                // 全屏白色背景，阻挡点击穿透到地图
                Surface(
                    color = Gray50,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 这里处理二级导航：设备列表 -> 设备详情
                    // 使用 AnimatedContent 在这两个视图间切换
                    AnimatedContent(
                        targetState = selectedDevice,
                        transitionSpec = {
                            if (targetState != null) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut())
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut())
                            }
                        },
                        label = "DetailTransition"
                    ) { device ->
                        if (device == null) {
                            // --- 显示站点下的设备列表 (Poles View) ---
                            val devices = selectedSiteInfo?.deviceList ?: emptyList()
                            if (devices.isEmpty()) {
                                EmptyDataView(message = "该站点下暂无设备")
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
                                        val item = devices[index]
                                        SiteDeviceCardItem(
                                            device = item,
                                            onClick = { selectedDevice = item } // 进入三级详情
                                        )
                                    }
                                }
                            }
                        } else {
                            // --- 显示单个设备详情 (Detail View) ---
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = device.productName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("设备详情页开发中...", color = Gray400)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 简单的 TopBar 封装，处理返回按钮显隐
@Composable
fun SitesTopBar(title: String, showBack: Boolean, onBack: () -> Unit) {
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
            AnimatedVisibility(visible = showBack) {
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

// 其他辅助组件 (EmptyStateView, InfoLabelValue 等) 保持你原有的不变即可