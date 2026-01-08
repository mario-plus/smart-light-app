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
import com.unilumin.smartapp.R
import com.unilumin.smartapp.client.data.SiteInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Compose 地图组件
 */
@Composable
fun SiteMapView(
    siteList: List<SiteInfo>,
    onSiteClick: (SiteInfo) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val clusterManager = remember { SiteClusterManager(context, onSiteClick) }

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

    // 当列表更新时，执行自动展开逻辑
    LaunchedEffect(siteList) {
        if (siteList.isNotEmpty()) {
            clusterManager.updateData(mapView.map, siteList)
        }
    }

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
 * 核心逻辑类：负责聚合计算、渲染、以及自动展开至最密区域
 */
class SiteClusterManager(
    private val context: Context,
    private val onSiteClick: (SiteInfo) -> Unit
) {
    private var map: AMap? = null
    private var allSites: List<SiteInfo> = emptyList()
    private var calculateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val baseClusterRadiusDp = 50f
    private val showNameZoomLevel = 15.5f

    fun attachToMap(aMap: AMap) {
        this.map = aMap

        // 监听地图移动停止
        aMap.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(position: CameraPosition?) {}
            override fun onCameraChangeFinish(position: CameraPosition?) {
                // 用户手动操作后重新计算，但不触发自动跳转
                reCalculateClusters(autoMoveToDensest = false)
            }
        })

        // 点击事件处理
        aMap.setOnMarkerClickListener { marker ->
            val obj = marker.`object`
            if (obj is Cluster) {
                zoomToCluster(obj) // 点击聚合点平滑展开
                true
            } else if (obj is SiteInfo) {
                onSiteClick(obj)
                true
            } else false
        }
    }

    /**
     * 更新数据源并自动定位到最密区域
     */
    fun updateData(aMap: AMap, list: List<SiteInfo>) {
        this.map = aMap
        this.allSites = list
        reCalculateClusters(autoMoveToDensest = true)
    }

    /**
     * 计算聚合逻辑
     * @param autoMoveToDensest 是否自动寻找最密区域并跳转
     */
    private fun reCalculateClusters(autoMoveToDensest: Boolean) {
        val currentMap = this.map ?: return
        if (allSites.isEmpty()) return

        calculateJob?.cancel()
        calculateJob = scope.launch(Dispatchers.Default) {
            val zoomLevel = currentMap.cameraPosition.zoom
            val scale = currentMap.scalePerPixel
            val distanceThreshold = dp2px(context, baseClusterRadiusDp)

            val clusters = mutableListOf<Cluster>()
            val visited = BooleanArray(allSites.size)

            // 1. 执行聚合算法
            for (i in allSites.indices) {
                if (visited[i]) continue
                val seed = allSites[i]
                val seedLat = seed.latitude ?: 0.0
                val seedLng = seed.longitude ?: 0.0
                if (seedLat == 0.0 || seedLng == 0.0) continue

                val cluster = Cluster(seedLat, seedLng)
                cluster.items.add(seed)
                visited[i] = true

                for (j in i + 1 until allSites.size) {
                    if (visited[j]) continue
                    val candidate = allSites[j]
                    val dist = com.amap.api.maps.AMapUtils.calculateLineDistance(
                        LatLng(seedLat, seedLng), LatLng(candidate.latitude ?: 0.0, candidate.longitude ?: 0.0)
                    )
                    // 根据当前地图比例尺判断是否需要聚合
                    if (dist / scale < distanceThreshold) {
                        cluster.items.add(candidate)
                        visited[j] = true
                    }
                }
                clusters.add(cluster)
            }

            // 2. 主线程渲染与跳转
            withContext(Dispatchers.Main) {
                renderMarkers(clusters, zoomLevel)

                if (autoMoveToDensest && clusters.isNotEmpty()) {
                    // 找到路灯最多的聚合簇
                    val densest = clusters.maxByOrNull { it.items.size }
                    densest?.let { zoomToCluster(it) }
                }
            }
        }
    }

    /**
     * 平滑缩放至指定区域
     */
    private fun zoomToCluster(cluster: Cluster) {
        val currentMap = this.map ?: return
        if (cluster.items.size <= 1) {
            currentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(cluster.centerLat, cluster.centerLng), 18f))
            return
        }

        val builder = LatLngBounds.Builder()
        cluster.items.forEach {
            builder.include(LatLng(it.latitude ?: 0.0, it.longitude ?: 0.0))
        }

        try {
            // padding 为 200，确保图标不贴边
            currentMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200), 600L, null)
        } catch (e: Exception) {
            currentMap.animateCamera(CameraUpdateFactory.zoomIn())
        }
    }

    /**
     * 将聚合结果绘制到地图上
     */
    private fun renderMarkers(clusters: List<Cluster>, zoomLevel: Float) {
        val currentMap = this.map ?: return
        currentMap.clear()

        clusters.forEach { cluster ->
            val size = cluster.items.size
            val latLng = LatLng(cluster.centerLat, cluster.centerLng)
            val options = MarkerOptions().position(latLng)

            if (size == 1) {
                // 单个路灯展示智慧路灯图标
                val site = cluster.items.first()
                val bitmap = createSiteIconWithText(
                    context,
                    site.name ?: "路灯",
                    showName = zoomLevel >= showNameZoomLevel
                )
                options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                options.anchor(0.5f, 1.0f) // 锚点在底部中心
                currentMap.addMarker(options).`object` = site
            } else {
                // 聚合展示圆圈
                val bitmap = createClusterBitmap(context, size)
                options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                options.anchor(0.5f, 0.5f) // 聚合圆圈中心对齐
                currentMap.addMarker(options).`object` = cluster
            }
        }
    }

    fun destroy() {
        calculateJob?.cancel()
        map = null
    }
}

// ================= 辅助绘图工具 (绘图逻辑) =================

fun createSiteIconWithText(context: Context, name: String, showName: Boolean): Bitmap {
    val iconWidth = dp2px(context, 30f)
    val iconHeight = dp2px(context, 60f)
    val textSize = dp2px(context, 11f).toFloat()
    val padding = dp2px(context, 4f)

    // --- 修改点：使用自定义方法获取矢量图 Bitmap，替代 BitmapFactory ---
    val scaledIcon = getVectorBitmap(context, R.drawable.ic_smart_lamp_pole, iconWidth, iconHeight)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        this.textSize = textSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(4f, 0f, 0f, Color.WHITE)
    }

    val bounds = Rect()
    if (showName) textPaint.getTextBounds(name, 0, name.length, bounds)

    val canvasWidth = if (showName) maxOf(iconWidth, bounds.width() + 40) else iconWidth
    val canvasHeight = if (showName) iconHeight + padding + bounds.height() + 10 else iconHeight

    val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 绘制图标居中
    val iconX = (canvasWidth - iconWidth) / 2f
    if (scaledIcon != null) {
        canvas.drawBitmap(scaledIcon, iconX, 0f, null)
    }

    // 绘制文字
    if (showName) {
        val textX = canvasWidth / 2f
        val textY = iconHeight + padding + bounds.height()
        canvas.drawText(name, textX, textY.toFloat(), textPaint)
    }

    return bitmap
}

fun createClusterBitmap(context: Context, count: Int): Bitmap {
    val radius = dp2px(context, 24f)
    val bitmap = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 根据数量选择科技感配色
    val mainColor = when {
        count < 20 -> Color.parseColor("#00D2FF") // 天蓝色
        count < 100 -> Color.parseColor("#3A7BD5") // 深蓝色
        else -> Color.parseColor("#1CB5E0")
    }

    // 绘制外圈光晕
    paint.color = mainColor
    paint.alpha = 100
    canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius.toFloat(), paint)

    // 绘制实心内圆
    paint.alpha = 255
    canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius * 0.8f, paint)

    // 绘制白边
    paint.color = Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = dp2px(context, 2f).toFloat()
    canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius * 0.8f, paint)

    // 绘制文字
    paint.style = Paint.Style.FILL
    paint.textSize = dp2px(context, 12f).toFloat()
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = Typeface.DEFAULT_BOLD
    val text = if (count > 999) "999+" else count.toString()
    val baseline = radius - (paint.fontMetrics.bottom + paint.fontMetrics.top) / 2
    canvas.drawText(text, radius.toFloat(), baseline, paint)

    return bitmap
}

fun dp2px(context: Context, dpValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}
/**
 * 将 VectorDrawable (XML) 转换为指定大小的 Bitmap
 */
fun getVectorBitmap(context: Context, drawableId: Int, width: Int, height: Int): Bitmap? {
    val drawable = androidx.core.content.ContextCompat.getDrawable(context, drawableId) ?: return null
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
/**
 * 聚合簇模型类
 */
data class Cluster(
    val centerLat: Double,
    val centerLng: Double,
    val items: MutableList<SiteInfo> = mutableListOf()
)