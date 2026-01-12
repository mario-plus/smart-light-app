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

    // Data Sources
    val mapPoints by siteViewModel.mapPoints.collectAsState()
    val isMapLoading by siteViewModel.isMapLoading.collectAsState()
    val totalCount by siteViewModel.totalCount.collectAsState()
    val pagingItems = siteViewModel.sitePagingFlow.collectAsLazyPagingItems()
    val siteRoadInfo by siteViewModel.siteRoadInfo.collectAsState()
    val searchKeyword by siteViewModel.searchKeyword.collectAsState()

    // States
    var isMapView by remember { mutableStateOf(false) }
    val selectedSiteInfo by siteViewModel.selectedSiteInfo.collectAsState()
    var selectedDevice by remember { mutableStateOf<SiteDevice?>(null) }
    val isDetailLoading by siteViewModel.isDetailLoading.collectAsState()

    // Filters
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
                // 将 ViewModel 传入以便内部监听 cameraEffect
                SiteMapViewContainer(
                    mapPoints = mapPoints,
                    viewModel = siteViewModel,
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

            // ================== 2. 列表层 (代码保持不变) ==================
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
            } else if (isMapView && selectedSiteInfo == null) {
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

            // ================== 3. 详情弹窗 (代码保持不变) ==================
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

/**
 * 封装地图组件
 */
@Composable
fun SiteMapViewContainer(
    mapPoints: List<PoleMapPointRes>,
    viewModel: SiteViewModel, // 传入 VM 用于接收相机指令
    onCameraChange: (Double, Double, Double, Double, Float) -> Unit,
    onPoleClick: (PoleMapPointRes) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val renderer = remember { SiteMapRenderer(context, onPoleClick) }

    // 生命周期管理
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

    // 渲染数据
    LaunchedEffect(mapPoints) {
        renderer.render(mapPoints)
    }

    // 【新增】监听 ViewModel 的相机移动指令 (自动跳转)
    LaunchedEffect(Unit) {
        viewModel.cameraEffect.collectLatest { update ->
            // 使用 animateCamera 让跳转更丝滑
            mapView.map?.animateCamera(update)
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                map.uiSettings.isScaleControlsEnabled = true
                renderer.attachToMap(map)

                map.setOnMapLoadedListener {
                    Log.i("SiteMapView", "地图加载完成，初始请求")

                    // 1. 设置初始视角为【宏观视角】 (Zoom 10)
                    // 这里坐标设为项目大概位置，或者城市中心。
                    // 重要的是 Zoom 要小，让 precision 变大，从而拉取聚合数据。
                    val centerLat = 22.5428
                    val centerLng = 113.959
                    val startZoom = 10f

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(centerLat, centerLng), startZoom))

                    // 2. 手动触发请求
                    val projection = map.projection
                    val bounds = projection.visibleRegion.latLngBounds

                    onCameraChange(
                        bounds.southwest.longitude,
                        bounds.northeast.longitude,
                        bounds.southwest.latitude,
                        bounds.northeast.latitude,
                        startZoom
                    )
                }

                map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
                    override fun onCameraChange(p: CameraPosition?) {}
                    override fun onCameraChangeFinish(p: CameraPosition?) {
                        val currentZoom = p?.zoom ?: map.cameraPosition.zoom
                        val bounds = map.projection.visibleRegion.latLngBounds
                        onCameraChange(
                            bounds.southwest.longitude,
                            bounds.northeast.longitude,
                            bounds.southwest.latitude,
                            bounds.northeast.latitude,
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