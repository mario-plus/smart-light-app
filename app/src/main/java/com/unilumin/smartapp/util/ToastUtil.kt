package com.unilumin.smartapp.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt

object ToastUtil {

    // --- 现代柔和的配色方案 ---
    // 错误配色：浅红背景 + 红色字/图标 + 极浅的红边框
    private val COLOR_ERR_BG = "#FEF0F0".toColorInt()
    private val COLOR_ERR_TXT = "#F56C6C".toColorInt()
    private val COLOR_ERR_STROKE = "#FBC4C4".toColorInt()

    // 成功配色：浅绿背景 + 绿色字/图标 + 极浅的绿边框
    private val COLOR_SUC_BG = "#F0F9EB".toColorInt()
    private val COLOR_SUC_TXT = "#67C23A".toColorInt()
    private val COLOR_SUC_STROKE = "#E1F3D8".toColorInt()

    // 加载配色：浅灰背景 + 灰色字/动画 + 极浅的灰边框
    private val COLOR_LOAD_BG = "#F4F4F5".toColorInt()
    private val COLOR_LOAD_TXT = "#909399".toColorInt()
    private val COLOR_LOAD_STROKE = "#E9E9EB".toColorInt()

    /**
     * 显示红色的错误提示
     */
    fun showError(context: Context, message: String) {
        showCustomToast(
            context, message,
            COLOR_ERR_BG, COLOR_ERR_TXT, COLOR_ERR_STROKE,
            android.R.drawable.ic_dialog_alert, false
        )
    }

    /**
     * 显示绿色的成功提示
     */
    fun showSuccess(context: Context, message: String) {
        showCustomToast(
            context, message,
            COLOR_SUC_BG, COLOR_SUC_TXT, COLOR_SUC_STROKE,
            android.R.drawable.ic_dialog_info, false
        )
    }

    /**
     * 显示带旋转动画的加载提示
     */
    fun showLoading(context: Context, message: String = "加载中...") {
        showCustomToast(
            context, message,
            COLOR_LOAD_BG, COLOR_LOAD_TXT, COLOR_LOAD_STROKE,
            0, true
        )
    }

    private fun showCustomToast(
        context: Context,
        message: String,
        bgColor: Int,
        contentColor: Int,
        strokeColor: Int,
        iconResId: Int,
        isLoading: Boolean
    ) {
        val toast = Toast(context)
        // 错误提示停留稍长，其他提示短促一点体验更好
        toast.duration = if (contentColor == COLOR_ERR_TXT) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        val density = context.resources.displayMetrics.density

        // 1. 创建外层容器 (横向 LinearLayout)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            // 增加左右内边距，让胶囊看起来更修长
            val paddingH = (20 * density).toInt()
            val paddingV = (12 * density).toInt()
            setPadding(paddingH, paddingV, paddingH, paddingV)

            // 设置现代风圆角背景
            background = GradientDrawable().apply {
                setColor(bgColor)
                cornerRadius = 50 * density // 50dp 大圆角，形成胶囊形状
                setStroke((1 * density).toInt(), strokeColor) // 细腻的边框
            }
            // 增加微小的悬浮阴影 (API 21+)
            elevation = 4 * density
        }

        // 2. 添加图标或加载动画
        if (isLoading) {
            // 添加旋转的 ProgressBar
            val progressBar = ProgressBar(context).apply {
                val size = (18 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = (10 * density).toInt()
                }
                // 修改 Loading 圈圈的颜色与文字颜色一致
                indeterminateTintList = ColorStateList.valueOf(contentColor)
            }
            layout.addView(progressBar)
        } else {
            // 添加静态图标
            val imageView = ImageView(context).apply {
                setImageResource(iconResId)
                // 关键：把系统原生图标染成我们想要的主题色
                setColorFilter(contentColor)
                val size = (18 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = (10 * density).toInt()
                }
            }
            layout.addView(imageView)
        }

        // 3. 添加文字
        val textView = TextView(context).apply {
            text = message
            setTextColor(contentColor)
            textSize = 14f // 字体调小一点点，显得更精致
            paint.isFakeBoldText = true // 微微加粗，提升可读性
        }
        layout.addView(textView)

        // 4. 配置并显示 Toast
        toast.view = layout

        // --- 调整位置 ---
        // Gravity.BOTTOM 默认在底部。我们把 yOffset 设为 120dp，它就会明显被“提上去”。
        // 如果你觉得还是低，可以把 120 改成 150 或 200。
        val yOffset = (120 * density).toInt()
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, yOffset)

        toast.show()
    }
}