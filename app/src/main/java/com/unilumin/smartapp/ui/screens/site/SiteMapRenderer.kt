package com.unilumin.smartapp.ui.screens.site

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.LruCache
import androidx.core.content.ContextCompat
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.unilumin.smartapp.R
import com.unilumin.smartapp.client.data.PoleMapPointRes

object MarkerBitmapCache {
    private val cache = LruCache<String, Bitmap>(100)
    fun get(key: String): Bitmap? = cache.get(key)
    fun put(key: String, bitmap: Bitmap) = cache.put(key, bitmap)
}

class SiteMapRenderer(
    private val context: Context,
    private val onPoleClick: (PoleMapPointRes) -> Unit
) {
    private var map: AMap? = null

    fun attachToMap(aMap: AMap) {
        this.map = aMap
        aMap.setOnMarkerClickListener { marker ->
            val point = marker.`object` as? PoleMapPointRes ?: return@setOnMarkerClickListener false
            if (point.count > 1) {
                val newZoom = aMap.cameraPosition.zoom + 2f
                val update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(

                        point.lat.toDoubleOrNull() ?: 0.0, // 转换失败则默认为 0.0

                        point.lng.toDoubleOrNull() ?: 0.0

                    )
                 ,
                    newZoom.coerceAtMost(aMap.maxZoomLevel)
                )
                aMap.animateCamera(update)
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
            val latLng = LatLng(

                point.lat.toDoubleOrNull() ?: 0.0, // 转换失败则默认为 0.0

                point.lng.toDoubleOrNull() ?: 0.0

            )
            val options = MarkerOptions().position(latLng)

            if (point.count > 1) {
                val bitmap = getClusterBitmap(point.count)
                options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                options.anchor(0.5f, 0.5f)
                options.zIndex(1f)
            } else {
                val isAlarm = false // 根据实际字段调整
                val bitmap = getPoleBitmap(isAlarm)
                options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                options.anchor(0.5f, 1.0f)
                options.zIndex(10f)
            }
            val marker = currentMap.addMarker(options)
            marker.`object` = point
        }
    }

    private fun getClusterBitmap(count: Int): Bitmap {
        val displayCount = if (count > 99) "99+" else count.toString()
        val key = "cluster_$displayCount"
        return MarkerBitmapCache.get(key) ?: run {
            val radius = dp2px(22f)
            val bitmap = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.parseColor("#4D2196F3")
            canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius.toFloat(), paint)
            paint.color = Color.parseColor("#2196F3")
            canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius * 0.75f, paint)
            paint.color = Color.WHITE
            paint.textSize = dp2px(13f).toFloat()
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textAlign = Paint.Align.CENTER
            val fontMetrics = paint.fontMetrics
            val baseline = radius - (fontMetrics.bottom + fontMetrics.top) / 2
            canvas.drawText(displayCount, radius.toFloat(), baseline, paint)
            MarkerBitmapCache.put(key, bitmap)
            bitmap
        }
    }

    private fun getPoleBitmap(isAlarm: Boolean): Bitmap {
        val key = if (isAlarm) "pole_alarm" else "pole_normal"
        return MarkerBitmapCache.get(key) ?: run {
            val resId = if (isAlarm) R.drawable.ic_smart_lamp_pole else R.drawable.ic_smart_lamp_pole
            val drawable = try { ContextCompat.getDrawable(context, resId) } catch (e: Exception) { null }
            val w = dp2px(36f)
            val h = dp2px(36f)
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            if (drawable != null) {
                drawable.setBounds(0, 0, w, h)
                drawable.draw(canvas)
            } else {
                val p = Paint().apply { color = if(isAlarm) Color.RED else Color.BLUE }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
            }
            MarkerBitmapCache.put(key, bitmap)
            bitmap
        }
    }

    private fun dp2px(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}