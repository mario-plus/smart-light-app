package com.unilumin.smartapp.ui.screens.site

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.LruCache
import androidx.core.content.ContextCompat
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.unilumin.smartapp.R
import com.unilumin.smartapp.client.data.PoleMapPointRes

/**
 * 全局 Bitmap 缓存
 */
object MarkerBitmapCache {
    private val cache = LruCache<String, Bitmap>(30)
    fun get(key: String): Bitmap? = cache.get(key)
    fun put(key: String, bitmap: Bitmap) = cache.put(key, bitmap)
}

class SiteMap(
    private val context: Context,
    private val onPoleClick: (PoleMapPointRes) -> Unit
) {
    private var map: AMap? = null

    fun attachToMap(aMap: AMap) {
        this.map = aMap
        aMap.setOnMarkerClickListener { marker ->
            val point = marker.`object` as? PoleMapPointRes ?: return@setOnMarkerClickListener false

            if (point.count > 1) {
                // 聚合点点击：平滑放大
                val newZoom = aMap.cameraPosition.zoom + 2.0f
                val lat = point.lat.toDoubleOrNull() ?: 0.0
                val lng = point.lng.toDoubleOrNull() ?: 0.0

                if (lat != 0.0 && lng != 0.0) {
                    val update = CameraUpdateFactory.newLatLngZoom(
                        LatLng(lat, lng),
                        newZoom.coerceAtMost(aMap.maxZoomLevel)
                    )
                    aMap.animateCamera(update)
                }
            } else {
                onPoleClick(point)
            }
            true
        }
    }

    fun render(points: List<PoleMapPointRes>) {
        val currentMap = map ?: return
        currentMap.clear()

        points.forEach { point ->
            val lat = point.lat.toDoubleOrNull() ?: 0.0
            val lng = point.lng.toDoubleOrNull() ?: 0.0
            if (lat == 0.0 || lng == 0.0) return@forEach

            val latLng = LatLng(lat, lng)
            val options = MarkerOptions().position(latLng)

            if (point.count > 1) {
                // === 聚合点渲染 (模拟热力光斑) ===
                val bitmap = getHeatmapClusterBitmap(point.count)
                options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                options.anchor(0.5f, 0.5f) // 中心对齐，方便光晕重叠
                options.zIndex(1f) // 层级最低，作为背景
            } else {
                // === 单杆渲染 (高亮实点) ===
                val isAlarm = false // 需根据业务字段调整
                val bitmap = getPoleBitmap(isAlarm)
                options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                options.anchor(0.5f, 1.0f)
                options.zIndex(10f) // 层级最高，浮在热力之上
            }

            val marker = currentMap.addMarker(options)
            marker.`object` = point
        }
    }

    /**
     * 获取热力图风格的聚合光斑
     * 特点：无边框、无阴影、大范围渐变透明，视觉上可融合
     */
    private fun getHeatmapClusterBitmap(count: Int): Bitmap {
        // 1. 热力图色阶分级
        val tier = when {
            count < 10 -> 1   // 绿色 (低密)
            count < 50 -> 2   // 黄色 (中密)
            count < 200 -> 3  // 橙色 (高密)
            else -> 4         // 红色 (爆表)
        }

        val key = "heatmap_blob_$tier"

        return MarkerBitmapCache.get(key) ?: run {
            // 定义热力颜色：核心色 & 边缘色
            val (coreColor, fadeColor, baseRadiusDp) = when (tier) {
                1 -> Triple(Color.parseColor("#00E676"), Color.parseColor("#00E676"), 16f) // 绿
                2 -> Triple(Color.parseColor("#FFEA00"), Color.parseColor("#FFEA00"), 20f) // 黄
                3 -> Triple(Color.parseColor("#FF9100"), Color.parseColor("#FF9100"), 24f) // 橙
                else -> Triple(Color.parseColor("#FF1744"), Color.parseColor("#FF1744"), 28f) // 红
            }

            // 尺寸计算：故意做得比较大，以便在地图上产生重叠效果
            val radius = dp2px(baseRadiusDp)
            val size = radius * 2

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // 绘制径向渐变：核心颜色(半透明) -> 完全透明
            // 这种渐变能让多个点叠加时产生“融合”的视觉错觉
            val gradient = RadialGradient(
                radius.toFloat(), radius.toFloat(),
                radius.toFloat(),
                intArrayOf(
                    setAlphaComponent(coreColor, 180), // 中心：70% 不透明度
                    setAlphaComponent(fadeColor, 50),  // 中间：20% 不透明度
                    Color.TRANSPARENT                  // 边缘：0%
                ),
                floatArrayOf(0.2f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )

            paint.shader = gradient
            paint.style = Paint.Style.FILL
            canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius.toFloat(), paint)

            // *关键*：移除所有 stroke (描边) 和 shadow (阴影)，去除“硬物感”
            // 也不绘制任何文字

            MarkerBitmapCache.put(key, bitmap)
            bitmap
        }
    }

    /**
     * 获取单杆图标 (保持清晰的定位针风格)
     */
    private fun getPoleBitmap(isAlarm: Boolean): Bitmap {
        val key = if (isAlarm) "pole_alarm" else "pole_normal"

        return MarkerBitmapCache.get(key) ?: run {
            val resId = R.drawable.ic_smart_lamp_pole
            val drawable = try { ContextCompat.getDrawable(context, resId) } catch (e: Exception) { null }

            val iconSize = dp2px(32f) // 略微缩小单点，显得更精致
            val w = iconSize
            val h = iconSize + dp2px(2f)

            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            if (drawable != null) {
                // 如果有UI切图，直接使用
                drawable.setBounds(0, 0, iconSize, iconSize)
                drawable.draw(canvas)
            } else {
                // 兜底绘制：简约圆点
                val cx = w / 2f
                val cy = iconSize / 2f
                val r = iconSize / 2.5f
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                // 只有单点才保留阴影，强调位置精准
                paint.color = if (isAlarm) Color.RED else Color.parseColor("#2979FF")
                paint.setShadowLayer(6f, 0f, 3f, Color.parseColor("#50000000"))
                canvas.drawCircle(cx, cy, r, paint)

                paint.clearShadowLayer()
                paint.color = Color.WHITE
                canvas.drawCircle(cx, cy, r * 0.4f, paint)
            }

            MarkerBitmapCache.put(key, bitmap)
            bitmap
        }
    }

    private fun dp2px(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    // 辅助函数：修改颜色透明度
    private fun setAlphaComponent(color: Int, alpha: Int): Int {
        return (color and 0x00FFFFFF) or (alpha shl 24)
    }
}