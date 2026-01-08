package com.unilumin.smartapp.ui.screens.site

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MarkerOptions
import com.unilumin.smartapp.R // 确保这里引入的是你的R文件
import com.unilumin.smartapp.client.data.SiteInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Compose 地图组件入口
 */
@Composable
fun SiteMapView(
    siteList: List<SiteInfo>,
    onSiteClick: (SiteInfo) -> Unit
) {
    val context = LocalContext.current
    // 1. 初始化 MapView 和 管理器
    val mapView = remember { MapView(context) }
    val clusterManager = remember { SiteClusterManager(context, onSiteClick) }

    // 2. 生命周期绑定
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> {
                    mapView.onDestroy()
                    clusterManager.destroy()
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

    // 3. 数据更新监听
    LaunchedEffect(siteList) {
        if (siteList.isNotEmpty()) {
            clusterManager.updateData(mapView.map, siteList)
        }
    }

    // 4. 渲染 AndroidView
    AndroidView(
        factory = {
            mapView.apply {
                map.uiSettings.isZoomControlsEnabled = false
                map.uiSettings.isScaleControlsEnabled = true
                clusterManager.attachToMap(map)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * 核心逻辑管理器：负责聚合计算、渲染和交互
 */
class SiteClusterManager(
    private val context: Context,
    private val onSiteClick: (SiteInfo) -> Unit
) {
    private var map: AMap? = null
    private var allSites: List<SiteInfo> = emptyList()
    private var calculateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    // 显示名称的缩放阈值 (当不需要聚合时，通常缩放级别已经很大了，可以考虑一直显示或大于17显示)
    private val showNameZoomLevel = 17.0f

    fun attachToMap(aMap: AMap) {
        this.map = aMap

        // 监听相机移动：移动结束后，根据新的可视范围决定是否聚合
        aMap.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(position: CameraPosition?) {}
            override fun onCameraChangeFinish(position: CameraPosition?) {
                reCalculateClusters(autoMoveToDensest = false)
            }
        })

        // Marker 点击逻辑
        aMap.setOnMarkerClickListener { marker ->
            val obj = marker.`object`
            when (obj) {
                is Cluster -> {
                    zoomToCluster(obj) // 点击聚合点 -> 放大
                    true
                }
                is SiteInfo -> {
                    onSiteClick(obj) // 点击单杆 -> 回调详情
                    true
                }
                else -> false
            }
        }
    }

    fun updateData(aMap: AMap, list: List<SiteInfo>) {
        this.map = aMap
        this.allSites = list
        // 数据更新时，自动计算并跳转到最密区域
        reCalculateClusters(autoMoveToDensest = true)
    }

    /**
     * 核心聚合算法
     * 策略：屏幕可视范围 > 5km -> 聚合 (半径60dp)
     * 屏幕可视范围 < 5km -> 不聚合 (半径0)
     */
    private fun reCalculateClusters(autoMoveToDensest: Boolean) {
        val currentMap = this.map ?: return
        if (allSites.isEmpty()) return

        // 1. 获取主线程参数
        val zoomLevel = currentMap.cameraPosition.zoom
        val scalePerPixel = currentMap.scalePerPixel // 米/像素
        val screenWidthPixels = context.resources.displayMetrics.widthPixels

        // 计算当前屏幕可视宽度的实际地理距离 (米)
        val visibleDistanceMeters = screenWidthPixels * scalePerPixel

        calculateJob?.cancel()
        calculateJob = scope.launch(Dispatchers.Default) {

            // =========================================================
            // 【策略配置】5公里阈值控制
            // =========================================================
            val clusterThresholdMeters = 5000f // 5公里

            val clusterRadiusDp = if (visibleDistanceMeters > clusterThresholdMeters) {
                60f // 视野宏大，开启聚合
            } else {
                0f  // 视野狭窄(街道级)，关闭聚合
            }

            val distanceThresholdPx = dp2px(context, clusterRadiusDp)
            // =========================================================

            val clusters = mutableListOf<Cluster>()
            val visited = BooleanArray(allSites.size)

            for (i in allSites.indices) {
                if (visited[i]) continue
                val seed = allSites[i]
                val seedLat = seed.latitude ?: 0.0
                val seedLng = seed.longitude ?: 0.0
                if (seedLat == 0.0 || seedLng == 0.0) continue

                val cluster = Cluster(seedLat, seedLng)
                cluster.items.add(seed)
                visited[i] = true

                // 只有当需要聚合时 (阈值 > 0)，才进行邻居查找
                // 性能优化：不聚合时复杂度由 O(N^2) 降为 O(N)
                if (distanceThresholdPx > 0) {
                    for (j in i + 1 until allSites.size) {
                        if (visited[j]) continue
                        val candidate = allSites[j]
                        val dist = com.amap.api.maps.AMapUtils.calculateLineDistance(
                            LatLng(seedLat, seedLng),
                            LatLng(candidate.latitude ?: 0.0, candidate.longitude ?: 0.0)
                        )

                        if (dist / scalePerPixel < distanceThresholdPx) {
                            cluster.items.add(candidate)
                            visited[j] = true
                        }
                    }
                }
                clusters.add(cluster)
            }

            // 回到主线程渲染
            withContext(Dispatchers.Main) {
                renderMarkers(clusters, zoomLevel)

                // 只有在开启聚合状态(大视野)时，才自动定位
                if (autoMoveToDensest && clusters.isNotEmpty() && distanceThresholdPx > 0) {
                    val densest = clusters.maxByOrNull { it.items.size }
                    densest?.let { zoomToCluster(it) }
                }
            }
        }
    }

    private fun renderMarkers(clusters: List<Cluster>, zoomLevel: Float) {
        val currentMap = this.map ?: return
        currentMap.clear()

        clusters.forEach { cluster ->
            val size = cluster.items.size
            val latLng = LatLng(cluster.centerLat, cluster.centerLng)
            val options = MarkerOptions().position(latLng)

            if (size == 1) {
                // === 渲染单个智慧杆 ===
                val site = cluster.items.first()
                // 当缩放很大(看得很清)时才显示文字，或者根据需求一直显示
                val showName = zoomLevel >= showNameZoomLevel

                val bitmap = createSiteIconWithText(
                    context,
                    site.name ?: "智慧杆",
                    showName
                )

                options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                options.anchor(0.5f, 1.0f) // 底部对齐
                options.zIndex(10f)        // 确保单杆在最上层

                val marker = currentMap.addMarker(options)
                marker.`object` = site
            } else {
                // === 渲染聚合圈 ===
                val bitmap = createClusterBitmap(context, size)

                options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                options.anchor(0.5f, 0.5f) // 中心对齐
                options.zIndex(5f)

                val marker = currentMap.addMarker(options)
                marker.`object` = cluster
            }
        }
    }

    private fun zoomToCluster(cluster: Cluster) {
        val currentMap = this.map ?: return
        if (cluster.items.size <= 1) return // 只有1个点无需缩放

        val builder = LatLngBounds.Builder()
        cluster.items.forEach {
            builder.include(LatLng(it.latitude ?: 0.0, it.longitude ?: 0.0))
        }

        try {
            // padding 200px 确保内容居中不贴边
            currentMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200), 600L, null)
        } catch (e: Exception) {
            currentMap.animateCamera(CameraUpdateFactory.zoomIn())
        }
    }

    fun destroy() {
        calculateJob?.cancel()
        map = null
    }
}

// ==========================================
// 辅助工具类 (绘图 & 数据)
// ==========================================

data class Cluster(
    val centerLat: Double,
    val centerLng: Double,
    val items: MutableList<SiteInfo> = mutableListOf()
)

/**
 * 绘制智慧杆图标 (含可选文字)
 */
fun createSiteIconWithText(context: Context, name: String, showName: Boolean): Bitmap {
    val iconWidth = dp2px(context, 36f) // 图标宽度
    val iconHeight = dp2px(context, 72f) // 图标高度
    val textSize = dp2px(context, 11f).toFloat()
    val textPadding = dp2px(context, 2f)

    // 获取 Vector Drawable
    val vectorBitmap = getVectorBitmap(context, R.drawable.ic_smart_lamp_pole, iconWidth, iconHeight)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        this.textSize = textSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(3f, 0f, 0f, Color.WHITE) // 文字白边
    }

    val bounds = Rect()
    if (showName) textPaint.getTextBounds(name, 0, name.length, bounds)

    val canvasWidth = if (showName) maxOf(iconWidth, bounds.width() + 40) else iconWidth
    val canvasHeight = if (showName) iconHeight + textPadding + bounds.height() + 10 else iconHeight

    val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 绘制图标 (居中)
    val iconX = (canvasWidth - iconWidth) / 2f
    if (vectorBitmap != null) {
        canvas.drawBitmap(vectorBitmap, iconX, 0f, null)
    }

    // 绘制文字
    if (showName) {
        val textX = canvasWidth / 2f
        val textY = iconHeight + textPadding + bounds.height()
        canvas.drawText(name, textX, textY.toFloat(), textPaint)
    }

    return bitmap
}

/**
 * 绘制聚合圆点
 */
fun createClusterBitmap(context: Context, count: Int): Bitmap {
    val radius = dp2px(context, 22f)
    val bitmap = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 颜色分级
    val (colorRes, _) = when {
        count < 20 -> Color.parseColor("#29B6F6") to Unit // 浅蓝
        count < 100 -> Color.parseColor("#0288D1") to Unit // 深蓝
        else -> Color.parseColor("#FF5722") to Unit        // 橙色
    }

    // 光晕
    paint.color = colorRes
    paint.alpha = 80
    canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius.toFloat(), paint)

    // 实心圆
    paint.alpha = 255
    canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius * 0.75f, paint)

    // 白边
    paint.style = Paint.Style.STROKE
    paint.color = Color.WHITE
    paint.strokeWidth = dp2px(context, 2f).toFloat()
    canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius * 0.75f, paint)

    // 数字
    paint.style = Paint.Style.FILL
    paint.textSize = dp2px(context, 12f).toFloat()
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = Typeface.DEFAULT_BOLD

    val text = if (count > 99) "99+" else count.toString()
    val baseline = radius - (paint.fontMetrics.bottom + paint.fontMetrics.top) / 2
    canvas.drawText(text, radius.toFloat(), baseline, paint)

    return bitmap
}

fun getVectorBitmap(context: Context, drawableId: Int, width: Int, height: Int): Bitmap? {
    val drawable = androidx.core.content.ContextCompat.getDrawable(context, drawableId) ?: return null
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun dp2px(context: Context, dpValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}