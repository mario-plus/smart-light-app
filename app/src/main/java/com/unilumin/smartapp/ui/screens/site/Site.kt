package com.unilumin.smartapp.ui.screens.site

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.data.PoleMapPointRes
import com.unilumin.smartapp.client.data.SiteDevice
import com.unilumin.smartapp.client.data.SiteRoadInfo
import com.unilumin.smartapp.ui.components.EmptyDataView
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray50
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.viewModel.SiteViewModel
import com.unilumin.smartapp.ui.viewModel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SitesScreen(retrofitClient: RetrofitClient) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val siteViewModel: SiteViewModel = viewModel(
        factory = ViewModelFactory { SiteViewModel(retrofitClient, context) }
    )

    // Data & States
    val mapPoints by siteViewModel.mapPoints.collectAsState()
    val isMapLoading by siteViewModel.isMapLoading.collectAsState()
    val totalCount by siteViewModel.totalCount.collectAsState()
    val pagingItems = siteViewModel.sitePagingFlow.collectAsLazyPagingItems()
    val siteRoadInfo by siteViewModel.siteRoadInfo.collectAsState()
    val searchKeyword by siteViewModel.searchKeyword.collectAsState()
    val selectedSiteInfo by siteViewModel.selectedSiteInfo.collectAsState()
    val isDetailLoading by siteViewModel.isDetailLoading.collectAsState()

    var isMapView by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<SiteDevice?>(null) }
    var selectedRoad by remember { mutableStateOf<SiteRoadInfo?>(null) }
    var isRoadDropdownExpanded by remember { mutableStateOf(false) }

    // --- Back Handler ---
    BackHandler(enabled = selectedDevice != null || selectedSiteInfo != null || isMapView) {
        when {
            selectedDevice != null -> selectedDevice = null
            selectedSiteInfo != null -> siteViewModel.clearSelection()
            isMapView -> isMapView = false
        }
    }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            val title = when {
                selectedDevice != null -> "设备详情"
                selectedSiteInfo != null -> selectedSiteInfo?.name ?: "站点详情"
                isMapView -> "站点地图"
                else -> "站点管理"
            }
            val showBack = isMapView || selectedSiteInfo != null
            SitesTopBar(title = title, showBack = showBack, onBack = {
                when {
                    selectedDevice != null -> selectedDevice = null
                    selectedSiteInfo != null -> siteViewModel.clearSelection()
                    isMapView -> isMapView = false
                }
            })
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // ================== 1. 地图层 ==================
            if (isMapView) {
                // 监听 ViewModel 发出的相机移动指令 (用于自动跳转最密区域)
                // 注意：这里需要把事件传递给 Container 内部的 MapView，
                // 由于 Compose 不方便直接持有 View 引用，我们通过 SiteMapViewContainer 内部处理

                SiteMapViewContainer(
                    mapPoints = mapPoints,
                    viewModel = siteViewModel, // 传入 VM 以便内部监听事件
                    onCameraChange = { minLng, maxLng, minLat, maxLat, zoom ->
                        siteViewModel.onMapCameraChange(minLng, maxLng, minLat, maxLat, zoom)
                    },
                    onPoleClick = { point ->
                        point.siteId?.let { siteViewModel.fetchSiteDetail(it.toLong()) }
                    }
                )

                if (isMapLoading) {
                    Surface(
                        modifier = Modifier.align(Alignment.Center),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        shadowElevation = 4.dp
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(16.dp).size(24.dp),
                            strokeWidth = 2.dp, color = Blue600
                        )
                    }
                }
            }

            // ================== 2. 列表层 ==================
            if (!isMapView && selectedSiteInfo == null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)) {
                        SearchAndFilterSection(
                            selectedRoad = selectedRoad,
                            siteRoadInfo = siteRoadInfo,
                            searchKeyword = searchKeyword,
                            isDropdownExpanded = isRoadDropdownExpanded,
                            isMapView = false,
                            onDropdownToggle = { isRoadDropdownExpanded = it },
                            onRoadSelected = {
                                selectedRoad = it
                                siteViewModel.updateRoadFilter(it?.id?.toString())
                                isRoadDropdownExpanded = false
                            },
                            onKeywordChanged = { siteViewModel.updateSearchKeyword(it) },
                            onClearKeyword = { siteViewModel.updateSearchKeyword("") },
                            onToggleViewMode = { isMapView = true },
                            focusManager = focusManager
                        )
                    }
                    PagingList(
                        totalCount = totalCount,
                        lazyPagingItems = pagingItems,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        itemKey = { site -> site.id },
                        emptyMessage = "未找到相关站点"
                    ) { siteInfo ->
                        SiteCardItem(
                            siteInfo = siteInfo,
                            onClick = { siteViewModel.fetchSiteDetail(siteInfo.id) }
                        )
                    }
                }
            }
            else if (isMapView && selectedSiteInfo == null) {
                FilterSectionWrapper(padding = PaddingValues(16.dp)) {
                    SearchAndFilterSection(
                        selectedRoad = selectedRoad,
                        siteRoadInfo = siteRoadInfo,
                        searchKeyword = searchKeyword,
                        isDropdownExpanded = isRoadDropdownExpanded,
                        isMapView = true,
                        onDropdownToggle = { isRoadDropdownExpanded = it },
                        onRoadSelected = {
                            selectedRoad = it
                            siteViewModel.updateRoadFilter(it?.id?.toString())
                            isRoadDropdownExpanded = false
                        },
                        onKeywordChanged = { siteViewModel.updateSearchKeyword(it) },
                        onClearKeyword = { siteViewModel.updateSearchKeyword("") },
                        onToggleViewMode = { isMapView = false },
                        focusManager = focusManager
                    )
                }
            }

            // ================== 3. 详情弹窗 ==================
            AnimatedVisibility(
                visible = selectedSiteInfo != null,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.zIndex(2f)
            ) {
                Surface(color = Gray50, modifier = Modifier.fillMaxSize()) {
                    if (isDetailLoading && selectedSiteInfo == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Blue600)
                        }
                    } else if (selectedSiteInfo != null) {
                        AnimatedContent(targetState = selectedDevice, label = "DetailNav") { device ->
                            if (device == null) {
                                // 复用之前的列表代码
                                val devices = selectedSiteInfo?.deviceList ?: emptyList()
                                if (devices.isEmpty()) {
                                    EmptyDataView(message = "该站点下暂无设备")
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        item { Text(text = "共关联 ${devices.size} 个设备", fontSize = 14.sp, color = Gray500, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)) }
                                        items(devices.size) { index ->
                                            val item = devices[index]
                                            SiteDeviceCardItem(device = item, onClick = { selectedDevice = item })
                                        }
                                    }
                                }
                            } else {
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    Text(text = device.productName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray900)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 封装地图组件
@Composable
fun SiteMapViewContainer(
    mapPoints: List<PoleMapPointRes>,
    viewModel: SiteViewModel, // 传入 VM 监听事件
    onCameraChange: (Double, Double, Double, Double, Float) -> Unit,
    onPoleClick: (PoleMapPointRes) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val renderer = remember { SiteMapRenderer(context, onPoleClick) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    // 监听数据变化渲染
    LaunchedEffect(mapPoints) {
        renderer.render(mapPoints)
    }

    // 监听 ViewModel 的相机移动指令 (如：自动跳转到最密区域)
    LaunchedEffect(Unit) {
        viewModel.cameraEffect.collectLatest { update ->
            mapView.map?.animateCamera(update)
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                map.uiSettings.isScaleControlsEnabled = true
                renderer.attachToMap(map)

                map.setOnMapLoadedListener {
                    Log.d("SiteMapView", "地图加载完成，执行初始化逻辑")

                    // ========================================================
                    // 修复 1：初始视角设为宏观视角 (Zoom 8)，不设死具体坐标
                    // 这样 precision = 1，后端会返回大区域的聚合点
                    // ViewModel 会算出哪里点最多，然后发指令跳转
                    // ========================================================
                    val initialZoom = 8f
                    // 默认中心点可以设为项目所在的省份或城市中心，或者中国中心
                    val centerChina = LatLng(35.8617, 104.1954)

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(centerChina, initialZoom))

                    // 手动触发第一次请求 (此时 Zoom=8)
                    val projection = map.projection
                    val bounds = projection.visibleRegion.latLngBounds
                    onCameraChange(
                        bounds.southwest.longitude, bounds.northeast.longitude,
                        bounds.southwest.latitude, bounds.northeast.latitude,
                        initialZoom
                    )
                }

                map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
                    override fun onCameraChange(p: CameraPosition?) {}
                    override fun onCameraChangeFinish(p: CameraPosition?) {
                        val currentZoom = p?.zoom ?: map.cameraPosition.zoom
                        val bounds = map.projection.visibleRegion.latLngBounds
                        onCameraChange(
                            bounds.southwest.longitude, bounds.northeast.longitude,
                            bounds.southwest.latitude, bounds.northeast.latitude,
                            currentZoom
                        )
                    }
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun SitesTopBar(title: String, showBack: Boolean, onBack: () -> Unit) {
    Surface(color = Color.White, shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth().zIndex(1f)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            AnimatedVisibility(visible = showBack) {
                IconButton(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(Icons.Rounded.ArrowBack, "Back", tint = Gray900)
                }
            }
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray900, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun FilterSectionWrapper(padding: PaddingValues, content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(padding)) { content() }
}