package com.unilumin.smartapp.ui.screens.site

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SitesScreen(retrofitClient: RetrofitClient) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val siteViewModel: SiteViewModel = viewModel(
        factory = ViewModelFactory { SiteViewModel(retrofitClient, application) }
    )
    val focusManager = LocalFocusManager.current

    val mapPoints by siteViewModel.mapPoints.collectAsState()
    val totalCount by siteViewModel.totalCount.collectAsState()
    val pagingItems = siteViewModel.sitePagingFlow.collectAsLazyPagingItems()
    val siteRoadInfo by siteViewModel.siteRoadInfo.collectAsState()
    val searchKeyword by siteViewModel.searchKeyword.collectAsState()
    val selectedSiteInfo by siteViewModel.selectedSiteInfo.collectAsState()
    val siteDetail by siteViewModel.siteDetail.collectAsState()
    val isLoading by siteViewModel.isLoading.collectAsState()

    var isMapView by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<SiteDevice?>(null) }
    var selectedRoad by remember { mutableStateOf<SiteRoadInfo?>(null) }
    var isRoadDropdownExpanded by remember { mutableStateOf(false) }

    // 控制是否展示站点详情/设备列表的全局标志位
    val displaySiteInfo = siteDetail ?: selectedSiteInfo

    // [核心返回逻辑] 按下系统返回键或手势返回
    BackHandler(enabled = selectedDevice != null || displaySiteInfo != null || isMapView) {
        when {
            selectedDevice != null -> selectedDevice = null
            displaySiteInfo != null -> siteViewModel.clearSelection() // 触发弹层隐藏
            isMapView -> isMapView = false
        }
    }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            val title = when {
                selectedDevice != null -> "设备详情"
                displaySiteInfo != null -> displaySiteInfo.name ?: "站点详情"
                isMapView -> "站点地图"
                else -> "站点管理"
            }
            val showBack = isMapView || displaySiteInfo != null || selectedDevice != null
            SitesTopBar(title = title, showBack = showBack, onBack = {
                when {
                    selectedDevice != null -> selectedDevice = null
                    displaySiteInfo != null -> siteViewModel.clearSelection()
                    isMapView -> isMapView = false
                }
            })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // ================== 第 1 层：底层视图（地图 或 列表） ==================
            AnimatedContent(
                targetState = isMapView,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "MapListToggle"
            ) { showMap ->
                if (showMap) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        SiteMapViewContainer(
                            mapPoints = mapPoints,
                            viewModel = siteViewModel,
                            searchKeyword = searchKeyword,
                            selectedRoadId = selectedRoad?.id?.toString(),
                            onCameraChange = { minLng, maxLng, minLat, maxLat, zoom ->
                                siteViewModel.onMapCameraChange(minLng, maxLng, minLat, maxLat, zoom)
                            },
                            onPoleClick = { point ->
                                point.siteId?.let { siteViewModel.fetchSiteDetail(it.toLong()) }
                            }
                        )
                        // 移除这里的 if(displaySiteInfo == null) 判断，让底层组件保持稳定
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
                } else {
                    // 🚨 核心修复：移除了 if(displaySiteInfo == null) 结构
                    // 这样即使展示了详情页，列表仍在后台驻留，不会丢失 Paging3 状态和滚动位置
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
                                onClick = {
                                    siteViewModel.fetchSiteDetail(siteInfo.id)
                                }
                            )
                        }
                    }
                }
            }

            // ================== 第 2 层：站点详情与设备列表弹层（覆盖在顶部） ==================
            AnimatedVisibility(
                visible = displaySiteInfo != null,
                // 增加 tween(300) 让滑出/滑入更加平滑，符合现代 App 调性
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(tween(300)),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(tween(300)),
                modifier = Modifier.zIndex(2f)
            ) {
                Surface(color = Gray50, modifier = Modifier.fillMaxSize()) {
                    if (isLoading && displaySiteInfo == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Blue600)
                        }
                    } else if (displaySiteInfo != null) {
                        AnimatedContent(
                            targetState = selectedDevice,
                            label = "DetailNav",
                            transitionSpec = {
                                fadeIn(tween(250)) togetherWith fadeOut(tween(250))
                            }
                        ) { device ->
                            if (device == null) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    val devices = displaySiteInfo.deviceList ?: emptyList()
                                    if (devices.isEmpty()) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            EmptyDataView(message = "该站点下暂无设备")
                                        }
                                    } else {
                                        LazyColumn(
                                            contentPadding = PaddingValues(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            items(devices.size) { index ->
                                                val item = devices[index]
                                                SiteDeviceCardItem(
                                                    device = item,
                                                    onClick = {
                                                        selectedDevice = item
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Rounded.Memory, contentDescription = null, modifier = Modifier.size(64.dp), tint = Blue600)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(text = device.productName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray900)
                                        Text(text = "序列号: ${device.serialNum ?: "-"}", fontSize = 14.sp, color = Gray500)
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(onClick = { selectedDevice = null }) {
                                            Text("返回设备列表")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SitesTopBar(title: String, showBack: Boolean, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "返回", tint = Gray900)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            scrolledContainerColor = Color.White
        ),
        modifier = Modifier.zIndex(1f)
    )
}

@Composable
fun SiteMapViewContainer(
    mapPoints: List<PoleMapPointRes>,
    viewModel: SiteViewModel,
    searchKeyword: String,
    selectedRoadId: String?,
    onCameraChange: (Double, Double, Double, Double, Float) -> Unit,
    onPoleClick: (PoleMapPointRes) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val renderer = remember { SiteMap(context, onPoleClick) }
    var isMapLoaded by remember { mutableStateOf(false) }
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

    LaunchedEffect(mapPoints) { renderer.render(mapPoints) }

    LaunchedEffect(Unit) {
        viewModel.cameraEffect.collectLatest { update ->
            mapView.map?.animateCamera(update)
        }
    }

    LaunchedEffect(searchKeyword, selectedRoadId, isMapLoaded) {
        if (!isMapLoaded) return@LaunchedEffect
        val map = mapView.map ?: return@LaunchedEffect
        if (map.projection != null && map.cameraPosition != null) {
            val visibleRegion = map.projection.visibleRegion
            if (visibleRegion != null) {
                val bounds = visibleRegion.latLngBounds
                val zoom = map.cameraPosition.zoom
                onCameraChange(
                    bounds.southwest.longitude, bounds.northeast.longitude,
                    bounds.southwest.latitude, bounds.northeast.latitude, zoom
                )
            }
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                map.uiSettings.isScaleControlsEnabled = true
                renderer.attachToMap(map)
                map.setOnMapLoadedListener {
                    isMapLoaded = true
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.0, 105.0), 4f))
                    val bounds = map.projection.visibleRegion.latLngBounds
                    onCameraChange(
                        bounds.southwest.longitude, bounds.northeast.longitude,
                        bounds.southwest.latitude, bounds.northeast.latitude, 4f
                    )
                }
                map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
                    override fun onCameraChange(p: CameraPosition?) {}
                    override fun onCameraChangeFinish(p: CameraPosition?) {
                        val currentZoom = p?.zoom ?: map.cameraPosition.zoom
                        val bounds = map.projection.visibleRegion.latLngBounds
                        onCameraChange(
                            bounds.southwest.longitude, bounds.northeast.longitude,
                            bounds.southwest.latitude, bounds.northeast.latitude, currentZoom
                        )
                    }
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun FilterSectionWrapper(padding: PaddingValues, content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(padding)) { content() }
}