package com.unilumin.smartapp.ui.screens.site

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
        aMap.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(position: CameraPosition?) {}
            override fun onCameraChangeFinish(position: CameraPosition?) {
                reCalculateClusters()
            }
        })

        aMap.setOnMarkerClickListener { marker ->
            val obj = marker.`object`
            if (obj is Cluster) {
                val builder = LatLngBounds.Builder()
                obj.items.forEach { builder.include(LatLng(it.latitude ?: 0.0, it.longitude ?: 0.0)) }
                try {
                    map?.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150))
                } catch (e: Exception) {
                    map?.animateCamera(CameraUpdateFactory.zoomIn())
                }
                true
            } else if (obj is SiteInfo) {
                onSiteClick(obj)
                true
            } else false
        }
    }

    fun updateData(aMap: AMap, list: List<SiteInfo>) {
        this.map = aMap
        this.allSites = list
        reCalculateClusters()

        if (list.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            list.take(50).forEach {
                if ((it.latitude ?: 0.0) != 0.0) builder.include(LatLng(it.latitude!!, it.longitude!!))
            }
            try {
                aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
            } catch (e: Exception) {}
        }
    }

    private fun reCalculateClusters() {
        val map = this.map ?: return
        if (allSites.isEmpty()) return

        calculateJob?.cancel()
        calculateJob = scope.launch(Dispatchers.Default) {
            val zoomLevel = map.cameraPosition.zoom
            val clusters = mutableListOf<Cluster>()
            val dynamicRadius = if (zoomLevel > showNameZoomLevel) 25f else baseClusterRadiusDp

            val visited = BooleanArray(allSites.size)
            val distanceThreshold = dp2px(context, dynamicRadius)
            val scale = map.scalePerPixel

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
                    val distanceMeters = com.amap.api.maps.AMapUtils.calculateLineDistance(
                        LatLng(seedLat, seedLng), LatLng(candidate.latitude ?: 0.0, candidate.longitude ?: 0.0)
                    )
                    if (distanceMeters / scale < distanceThreshold) {
                        cluster.items.add(candidate)
                        visited[j] = true
                    }
                }
                clusters.add(cluster)
            }

            withContext(Dispatchers.Main) {
                renderMarkers(clusters, zoomLevel)
            }
        }
    }

    private fun renderMarkers(clusters: List<Cluster>, zoomLevel: Float) {
        val map = this.map ?: return
        map.clear()

        clusters.forEach { cluster ->
            val size = cluster.items.size
            val latLng = LatLng(cluster.centerLat, cluster.centerLng)
            val markerOptions = MarkerOptions().position(latLng)

            if (size == 1) {
                val site = cluster.items.first()
                // 1. 生成图标 (缩放级别高时带名字)
                val bitmap = createSiteIconWithText(
                    context,
                    site.name ?: "路灯",
                    showName = zoomLevel >= showNameZoomLevel
                )

                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                // 2. 关键：设置锚点在底部中心，这样灯杆的底座才会对准坐标点
                markerOptions.anchor(0.5f, 1.0f)

                val marker = map.addMarker(markerOptions)
                marker.`object` = site
            } else {
                val bitmap = createClusterBitmap(context, size)
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap)).anchor(0.5f, 0.5f)
                val marker = map.addMarker(markerOptions)
                marker.`object` = cluster
            }
        }
    }

    fun destroy() {
        calculateJob?.cancel()
        map = null
    }
}

// ================= 辅助绘图工具 =================

/**
 * 绘制灯杆图标并在下方显示文字
 */
fun createSiteIconWithText(context: Context, name: String, showName: Boolean): Bitmap {
    // 1. 参数定义
    val iconWidth = dp2px(context, 30f) // 灯杆宽度
    val iconHeight = dp2px(context, 60f) // 灯杆高度
    val textSize = dp2px(context, 11f).toFloat()
    val padding = dp2px(context, 2f)

    // 2. 加载图片并缩放
    val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_smart_lamp_pole)
    val scaledIcon = Bitmap.createScaledBitmap(originalBitmap, iconWidth, iconHeight, true)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333") // 文字深灰
        this.textSize = textSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        // 白色发光边，防止文字遮挡地图线条
        setShadowLayer(3f, 0f, 0f, Color.WHITE)
    }

    // 3. 计算画布大小
    val bounds = Rect()
    if (showName) textPaint.getTextBounds(name, 0, name.length, bounds)

    val canvasWidth = if (showName) maxOf(iconWidth, bounds.width() + 20) else iconWidth
    val canvasHeight = if (showName) iconHeight + padding + bounds.height() + 10 else iconHeight

    val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 4. 绘制图片 (水平居中)
    val iconX = (canvasWidth - iconWidth) / 2f
    canvas.drawBitmap(scaledIcon, iconX, 0f, null)

    // 5. 绘制文字 (在图片下方)
    if (showName) {
        val textX = canvasWidth / 2f
        val textY = iconHeight + padding + bounds.height()
        canvas.drawText(name, textX, textY.toFloat(), textPaint)
    }

    return bitmap
}

/**
 * 绘制聚合圆圈
 */
fun createClusterBitmap(context: Context, count: Int): Bitmap {
    val radius = dp2px(context, 22f)
    val bitmap = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 渐变色
    val color = when {
        count < 10 -> Color.parseColor("#4B7BEC")
        count < 100 -> Color.parseColor("#F7B731")
        else -> Color.parseColor("#EB3B5A")
    }

    paint.color = color
    canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius.toFloat(), paint)

    paint.color = Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = dp2px(context, 2f).toFloat()
    canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius - 2f, paint)

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

data class Cluster(
    val centerLat: Double,
    val centerLng: Double,
    val items: MutableList<SiteInfo> = mutableListOf()
)