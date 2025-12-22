package com.unilumin.smartapp.ui.screens.site

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.unilumin.smartapp.client.data.SiteInfo
import kotlinx.coroutines.*

@Composable
fun SiteMapView(
    siteList: List<SiteInfo>,
    onSiteClick: (SiteInfo) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    // 创建聚合管理器 (使用 remember 保持实例)
    val clusterManager = remember { SiteClusterManager(context, onSiteClick) }
    val scope = rememberCoroutineScope()

    // 生命周期管理
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> {
                    mapView.onDestroy()
                    clusterManager.destroy() // 清理资源
                }
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
            clusterManager.destroy()
        }
    }

    // 数据更新监听
    LaunchedEffect(siteList) {
        if (siteList.isNotEmpty()) {
            clusterManager.updateData(mapView.map, siteList)
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                map.uiSettings.isZoomControlsEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = true
                map.uiSettings.isScaleControlsEnabled = true
                clusterManager.attachToMap(map)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

// ==========================================
// 2. 聚合核心管理器 (逻辑核心)
// ==========================================

class SiteClusterManager(
    private val context: Context,
    private val onSiteClick: (SiteInfo) -> Unit
) {
    private var map: AMap? = null
    private var allSites: List<SiteInfo> = emptyList()

    // 缓存当前屏幕显示的 Markers，用于差异化更新，防止闪烁
    private val currentMarkers = mutableListOf<Marker>()

    // 协程任务，用于防抖和后台计算
    private var calculateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    // 聚合半径 (dp)，在这个半径内的点会聚合在一起
    private val clusterRadiusDp = 50f

    fun attachToMap(aMap: AMap) {
        this.map = aMap

        // 监听相机移动结束（缩放或拖拽停止后重新计算）
        aMap.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(position: CameraPosition?) {}
            override fun onCameraChangeFinish(position: CameraPosition?) {
                reCalculateClusters()
            }
        })

        // 监听 Marker 点击
        aMap.setOnMarkerClickListener { marker ->
            val obj = marker.`object`
            if (obj is Cluster) {
                // 点击聚合点 -> 放大地图
                val builder = LatLngBounds.Builder()
                obj.items.forEach { builder.include(LatLng(it.latitude ?: 0.0, it.longitude ?: 0.0)) }
                try {
                    map?.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
                } catch (e: Exception) {
                    // 只有1个点或异常时直接放大
                    map?.animateCamera(CameraUpdateFactory.zoomIn())
                }
                true
            } else if (obj is SiteInfo) {
                // 点击单个设备 -> 回调详情
                onSiteClick(obj)
                true // 消费事件，阻止默认行为
            } else {
                false
            }
        }
    }

    fun updateData(aMap: AMap, list: List<SiteInfo>) {
        this.map = aMap
        this.allSites = list
        reCalculateClusters()

        // 第一次加载数据时，自动缩放到包含所有点的范围
        if (list.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            // 只取前100个点计算范围，防止5万个点计算太慢
            list.take(100).forEach {
                if((it.latitude ?: 0.0) != 0.0) builder.include(LatLng(it.latitude!!, it.longitude!!))
            }
            try {
                aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 核心算法：重新计算聚合
    private fun reCalculateClusters() {
        val map = this.map ?: return
        if (allSites.isEmpty()) return

        // 取消上一次计算，防止频繁拖拽导致的计算堆积
        calculateJob?.cancel()

        calculateJob = scope.launch(Dispatchers.Default) { // 切换到后台线程计算

            val zoomLevel = map.cameraPosition.zoom
            val clusters = mutableListOf<Cluster>()

            // 1. 过滤：只计算当前屏幕可见区域内的点 (性能优化关键)
            // 注意：projection 需要在主线程获取，这里为了简单全量计算，
            // 几万数据量下，简单的距离聚类全量计算通常在 100-200ms 内，可以接受。
            // 如果数据量极大，建议先用 QuadTree 过滤范围。

            val visited = BooleanArray(allSites.size)
            val distanceThreshold = dp2px(context, clusterRadiusDp) // 聚合像素距离
            val scale = map.scalePerPixel // 当前缩放级别下，1像素代表多少米

            // 简单的贪婪算法聚合
            for (i in allSites.indices) {
                if (visited[i]) continue

                val seed = allSites[i]
                val seedLat = seed.latitude ?: 0.0
                val seedLng = seed.longitude ?: 0.0
                if (seedLat == 0.0 || seedLng == 0.0) continue

                val cluster = Cluster(seedLat, seedLng)
                cluster.items.add(seed)
                visited[i] = true

                // 寻找邻居
                for (j in i + 1 until allSites.size) {
                    if (visited[j]) continue

                    val candidate = allSites[j]
                    val lat = candidate.latitude ?: 0.0
                    val lng = candidate.longitude ?: 0.0

                    // 计算两点间的直线距离 (米)
                    val distanceMeters = com.amap.api.maps.AMapUtils.calculateLineDistance(
                        LatLng(seedLat, seedLng), LatLng(lat, lng)
                    )

                    // 转换成屏幕像素距离：距离(米) / (米/像素)
                    // 这种方式不需要依赖 Projection，可以在后台线程纯数学计算
                    val distancePixels = distanceMeters / scale

                    if (distancePixels < distanceThreshold) {
                        cluster.items.add(candidate)
                        visited[j] = true
                    }
                }
                clusters.add(cluster)
            }

            // 计算完成，切回主线程渲染
            withContext(Dispatchers.Main) {
                renderMarkers(clusters)
            }
        }
    }

    // 渲染 Marker
    private fun renderMarkers(clusters: List<Cluster>) {
        val map = this.map ?: return

        // 简单粗暴方案：清除所有重新添加
        // 优化方案（进阶）：对比 ID 做增量更新，这里为了代码简洁直接 Clear
        map.clear()
        currentMarkers.clear()

        clusters.forEach { cluster ->
            val size = cluster.items.size
            val latLng = LatLng(cluster.centerLat, cluster.centerLng)

            val markerOptions = MarkerOptions().position(latLng)

            if (size == 1) {
                // 单个点：显示普通杆体图标
                val site = cluster.items.first()
                markerOptions
                    .title(site.name)
                    // .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_lamp_pole)) // 你的图标
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                val marker = map.addMarker(markerOptions)
                marker.`object` = site // 绑定数据对象
            } else {
                // 聚合点：显示数字圆圈
                val bitmap = createClusterBitmap(context, size)
                markerOptions
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .anchor(0.5f, 0.5f) // 居中

                val marker = map.addMarker(markerOptions)
                marker.`object` = cluster // 绑定聚合对象
            }
        }
    }

    fun destroy() {
        calculateJob?.cancel()
        map = null
    }
}

// ==========================================
// 3. 辅助类与工具
// ==========================================

// 聚合数据实体
data class Cluster(
    val centerLat: Double,
    val centerLng: Double,
    val items: MutableList<SiteInfo> = mutableListOf()
)

// ==========================================
// 美化版聚合图标绘制工具
// ==========================================
fun createClusterBitmap(context: Context, count: Int): Bitmap {
    // 1. 配置参数
    val radiusDp = 22f // 整体半径加大一点
    val strokeWidthDp = 3f // 白色描边宽度
    val radius = dp2px(context, radiusDp)
    val strokeWidth = dp2px(context, strokeWidthDp).toFloat()

    // 预留阴影空间 (padding)
    val shadowPadding = dp2px(context, 3f)
    val totalSize = (radius + shadowPadding) * 2

    val bitmap = Bitmap.createBitmap(totalSize, totalSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 2. 准备画笔
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 3. 确定颜色 (洲明蓝配色体系)
    val circleColor = when {
        count < 10 -> Color.parseColor("#4B7BEC") // 亮蓝
        count < 100 -> Color.parseColor("#F7B731") // 橙黄
        else -> Color.parseColor("#EB3B5A") // 警示红
    }

    // 中心点坐标
    val centerX = totalSize / 2f
    val centerY = totalSize / 2f
    // 实际圆的半径 (减去阴影和描边的一半)
    val drawRadius = radius - shadowPadding

    // 4. 绘制阴影 (画在最底层)
    paint.color = circleColor
    paint.setShadowLayer(dp2px(context, 4f).toFloat(), 0f, dp2px(context, 2f).toFloat(), Color.parseColor("#60000000"))
    // 需要关闭硬件加速阴影才生效，或者绘制一个透明圆带阴影
    canvas.drawCircle(centerX, centerY, drawRadius.toFloat(), paint)
    paint.clearShadowLayer()

    // 5. 绘制白色描边 (外圈)
    paint.color = Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = strokeWidth
    canvas.drawCircle(centerX, centerY, drawRadius.toFloat(), paint)

    // 6. 绘制实心圆 (内圈)
    paint.style = Paint.Style.FILL
    paint.color = circleColor
    // 半径减去描边宽度的一半，防止重叠锯齿
    canvas.drawCircle(centerX, centerY, drawRadius - strokeWidth / 2, paint)

    // 7. 绘制文字
    textPaint.color = Color.WHITE
    textPaint.textSize = dp2px(context, 13f).toFloat()
    textPaint.typeface = android.graphics.Typeface.DEFAULT_BOLD
    textPaint.textAlign = Paint.Align.CENTER

    val fontMetrics = textPaint.fontMetrics
    // 文字垂直居中计算公式
    val baseline = centerY - (fontMetrics.bottom + fontMetrics.top) / 2

    val text = if (count > 999) "999+" else count.toString()
    canvas.drawText(text, centerX, baseline, textPaint)

    return bitmap
}

fun dp2px(context: Context, dpValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}