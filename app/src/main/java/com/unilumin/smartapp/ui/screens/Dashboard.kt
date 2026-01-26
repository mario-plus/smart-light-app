package com.unilumin.smartapp.ui.screens

import android.app.Application
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.data.LightDayEnergy
import com.unilumin.smartapp.client.data.LightEnergy
import com.unilumin.smartapp.client.data.LightYearEnergy
import com.unilumin.smartapp.ui.components.AppCard
import com.unilumin.smartapp.ui.theme.*
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(retrofitClient: RetrofitClient) {

    val context = LocalContext.current
    val application = context.applicationContext as Application

    val deviceViewModel: DeviceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DeviceViewModel(retrofitClient, application) as T
        }
    })

    // 状态收集
    val deviceStatusSummary by deviceViewModel.deviceStatusSummary.collectAsState()
    val dayEnergyList by deviceViewModel.dayEnergyList.collectAsState()
    val monthEnergyList by deviceViewModel.monthEnergyList.collectAsState()
    val yearEnergyList by deviceViewModel.yearEnergyList.collectAsState()

    // 异步加载数据
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                deviceViewModel.getStatusSummary()
                deviceViewModel.dayEnergyData()
                deviceViewModel.monthEnergyData()
                deviceViewModel.yearEnergyData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // 浅灰底色
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 顶部 Header
        item { HeaderSection() }

        // 2. 状态概览
        item {
            StatusCardsSection(
                onlinePercent = deviceStatusSummary?.onlinePercent?.toString() ?: "0",
                onlineCount = deviceStatusSummary?.onlineCount ?: 0,
                lightUpPercent = deviceStatusSummary?.lightUpPercent?.toString() ?: "0",
                totalCount = deviceStatusSummary?.total ?: 0
            )
        }

        // 3. 统计标题
        item {
            Text(
                "能耗统计",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        // 4. 核心统计图表卡片
        item {
            EnergyStatsCard(
                monthData = monthEnergyList,
                dayData = dayEnergyList,
                yearData = yearEnergyList
            )
        }

        // 底部留白
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

// --- 顶部 Header ---
@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("运维概览", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gray900)
        }
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Notifications, null, tint = Gray500)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Red500, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = 10.dp)
                )
            }
        }
    }
}

// --- 状态卡片 ---
@Composable
fun StatusCardsSection(
    onlinePercent: String,
    onlineCount: Int,
    lightUpPercent: String,
    totalCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppCard(modifier = Modifier.weight(1f)) {
            StatusItem(
                title = "在线率", percent = onlinePercent, detailLabel = "在线数", detailValue = onlineCount.toString(),
                icon = Icons.Rounded.Wifi, iconBg = Emerald50, iconColor = Emerald600
            )
        }
        AppCard(modifier = Modifier.weight(1f)) {
            StatusItem(
                title = "亮灯率", percent = lightUpPercent, detailLabel = "设备总数", detailValue = totalCount.toString(),
                icon = Icons.Rounded.WbSunny, iconBg = Amber50, iconColor = Amber500
            )
        }
    }
}

@Composable
fun StatusItem(title: String, percent: String, detailLabel: String, detailValue: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconBg: Color, iconColor: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = iconBg, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontSize = 14.sp, color = Gray500)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(percent, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Gray900, lineHeight = 30.sp)
            Text("%", fontSize = 14.sp, color = Gray400, modifier = Modifier.padding(bottom = 4.dp, start = 2.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("$detailLabel: $detailValue", fontSize = 12.sp, color = Gray500)
    }
}

// --- 核心统计卡片 ---
@Composable
fun EnergyStatsCard(
    monthData: List<LightEnergy>,
    dayData: List<LightDayEnergy>,
    yearData: LightYearEnergy
) {
    AppCard {
        Column(modifier = Modifier.fillMaxWidth()) {

            // 1. 月度对比
            Text("月度能耗对比 (kW·h)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Spacer(modifier = Modifier.height(16.dp))

            if (monthData.isNotEmpty()) {
                val maxVal = monthData.maxOfOrNull { it.degree?.toFloatOrNull() ?: 1f } ?: 1f
                monthData.forEachIndexed { index, item ->
                    val valueFloat = item.degree?.toFloatOrNull() ?: 0f
                    val percentage = if (maxVal > 0) valueFloat / maxVal else 0f
                    val isCurrent = index == monthData.lastIndex
                    EnergyBarRow(
                        label = item.month ?: "",
                        value = valueFloat,
                        percentage = percentage,
                        color = if (isCurrent) Blue600 else Color(0xFF93C5FD)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                EmptyState("暂无月度数据")
            }

            DividerLine()

            // 2. 近7天趋势
            Text("近7天用电量 (kW·h)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                WeeklyEnergyChart(dayData)
            }

            DividerLine()

            // 3. 年度趋势
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("年度用电趋势(kW·h)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Gray900)
                // 顶部图例
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendItem(color = Color(0xFF9C27B0), label = "去年") // 紫色
                    Spacer(modifier = Modifier.width(12.dp))
                    LegendItem(color = Blue600, label = "今年") // 深蓝
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 增加高度以容纳 Y 轴标签
            Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                AnnualEnergyChart(yearData)
            }
        }
    }
}

// --- 组件：月度水平条形图 ---
@Composable
fun EnergyBarRow(label: String, value: Float, percentage: Float, color: Color) {
    val formattedValue = String.format(Locale.US, "%.2f", value)

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 12.sp, color = Gray500, modifier = Modifier.width(60.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(24.dp)
                    .background(Gray100, RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage.coerceIn(0f, 1f))
                    .height(24.dp)
                    .background(
                        brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.8f), color)),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Text(
                text = formattedValue,
                fontSize = 11.sp,
                color = if (percentage > 0.2f) Color.Black else Gray500,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
            )
        }
    }
}

// --- 组件：近7天柱状图 ---
@Composable
fun WeeklyEnergyChart(data: List<LightDayEnergy>) {
    if (data.isEmpty()) {
        EmptyState("暂无近7天数据")
        return
    }

    val maxVal = data.maxOfOrNull { it.value?.toFloatOrNull() ?: 0f } ?: 100f
    val safeMax = if (maxVal == 0f) 100f else maxVal
    val maxBarHeight = 110.dp

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { item ->
            val value = item.value?.toFloatOrNull() ?: 0f
            val heightFraction = (value / safeMax).coerceIn(0.05f, 1f)

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = String.format(Locale.US, "%.0f", value),
                    fontSize = 10.sp,
                    color = Gray500,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(maxBarHeight * heightFraction)
                        .background(
                            brush = Brush.verticalGradient(listOf(Blue600, Color(0xFF93C5FD))),
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val dateStr = item.date ?: ""
                val dateLabel = if (dateStr.length > 5) dateStr.takeLast(5) else dateStr
                Text(
                    text = dateLabel,
                    fontSize = 10.sp,
                    color = Gray400,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Visible,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


/**
 * 辅助函数：将 "10000" 格式化为 "10k"，小于1000则直接显示
 */
private fun formatYAxisValue(value: Float): String {
    return when {
        value >= 10000 -> String.format(Locale.US, "%.0fk", value / 1000)
        else -> String.format(Locale.US, "%.0f", value)
    }
}

/**
 * 辅助函数：解析后端数据
 */
private fun extractMonthlyValues(data: List<LightEnergy>?): List<Float> {
    val result = FloatArray(12)
    data?.forEach { item ->
        val monthInt = item.month?.toIntOrNull() ?: 0
        val index = monthInt - 1
        if (index in 0..11) {
            result[index] = item.degree?.toFloatOrNull() ?: 0f
        }
    }
    return result.toList()
}

@Composable
fun AnnualEnergyChart(yearData: LightYearEnergy) {
    // 1. 数据解析
    val thisYearValues = remember(yearData.thisYear) { extractMonthlyValues(yearData.thisYear) }
    val lastYearValues = remember(yearData.lastYear) { extractMonthlyValues(yearData.lastYear) }

    // 交互状态：记录当前选中的月份索引 (0-11)，null 表示未选中
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val hasData = thisYearValues.any { it > 0 } || lastYearValues.any { it > 0 }
    if (!hasData) {
        EmptyState("暂无年度趋势数据")
        return
    }

    val months = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
    val allValues = thisYearValues + lastYearValues
    val maxDataValue = (allValues.maxOrNull() ?: 100f).coerceAtLeast(10f) * 1.15f

    // 颜色定义
    val thisYearColor = Blue600
    val lastYearColor = Color(0xFF9C27B0)
    val gridColor = Color(0xFFE2E8F0)

    val density = LocalDensity.current

    // 画笔定义
    val textPaint = remember(density) {
        Paint().apply {
            color = "#94A3B8".toColorInt()
            textAlign = Paint.Align.CENTER
            textSize = density.run { 10.sp.toPx() }
        }
    }
    val yAxisPaint = remember(density) {
        Paint().apply {
            color = "#94A3B8".toColorInt()
            textAlign = Paint.Align.RIGHT
            textSize = density.run { 10.sp.toPx() }
        }
    }
    // 提示框文字画笔
    val tooltipTextPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.WHITE // 白色文字
            textAlign = Paint.Align.LEFT
            textSize = density.run { 11.sp.toPx() }
            typeface = Typeface.DEFAULT_BOLD
        }
    }
    // 提示框标题画笔
    val tooltipTitlePaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.parseColor("#E0E0E0") // 浅灰
            textAlign = Paint.Align.LEFT
            textSize = density.run { 10.sp.toPx() }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // 监听点击手势
                detectTapGestures(
                    onTap = { offset ->
                        // 预留的左侧宽度
                        val yAxisWidth = 80f // 必须与下方绘制时的 yAxisLabelWidth 一致
                        val chartWidth = size.width - yAxisWidth
                        val stepX = chartWidth / (months.size - 1)

                        // 只有点击图表区域才响应
                        if (offset.x >= yAxisWidth) {
                            val relativeX = offset.x - yAxisWidth
                            // 计算最近的索引
                            val rawIndex = (relativeX / stepX).roundToInt()
                            val clickedIndex = rawIndex.coerceIn(0, months.size - 1)

                            // 如果点击同一个，则取消选中；否则选中新的
                            selectedIndex = if (selectedIndex == clickedIndex) null else clickedIndex
                        } else {
                            selectedIndex = null // 点击Y轴区域取消选中
                        }
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val paddingBottom = 60f
        val yAxisLabelWidth = 80f // 必须与手势监听中的一致

        val chartHeight = height - paddingBottom
        val chartWidth = width - yAxisLabelWidth
        val stepX = chartWidth / (months.size - 1)

        // --- 1. 绘制网格线和 Y 轴 ---
        val gridLines = 4
        for (i in 0..gridLines) {
            val fraction = i.toFloat() / gridLines
            val y = chartHeight - (fraction * chartHeight)
            val value = fraction * maxDataValue

            // 虚线网格
            drawLine(
                color = gridColor,
                start = Offset(yAxisLabelWidth, y),
                end = Offset(width, y),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
            // Y轴数值
            drawContext.canvas.nativeCanvas.drawText(
                formatYAxisValue(value),
                yAxisLabelWidth - 15f,
                y + 10f,
                yAxisPaint
            )
        }

        // --- 2. 绘制曲线 ---
        fun drawSmoothCurve(values: List<Float>, lineColor: Color, isDashed: Boolean) {
            if (values.isEmpty()) return
            val path = Path()

            values.forEachIndexed { index, value ->
                val x = yAxisLabelWidth + (index * stepX)
                val safeValue = value.coerceAtLeast(0f)
                val y = chartHeight - (safeValue / maxDataValue * chartHeight)

                if (index == 0) path.moveTo(x, y)
                else {
                    val prevX = yAxisLabelWidth + ((index - 1) * stepX)
                    val prevY = chartHeight - (values[index - 1].coerceAtLeast(0f) / maxDataValue * chartHeight)
                    val conX1 = prevX + (stepX * 0.5f)
                    val conX2 = x - (stepX * 0.5f)
                    path.cubicTo(conX1, prevY, conX2, y, x, y)
                }
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 6f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f) else null
                )
            )

            // 填充 (仅实线)
            if (!isDashed) {
                val fillPath = Path()
                fillPath.addPath(path)
                fillPath.lineTo(yAxisLabelWidth + ((months.size - 1) * stepX), chartHeight)
                fillPath.lineTo(yAxisLabelWidth, chartHeight)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.15f), Color.Transparent),
                        startY = 0f,
                        endY = chartHeight
                    )
                )
            }
        }

        drawSmoothCurve(lastYearValues, lastYearColor, true)
        drawSmoothCurve(thisYearValues, thisYearColor, false)

        // --- 3. 绘制 X 轴文字 ---
        months.forEachIndexed { index, month ->
            val x = yAxisLabelWidth + (index * stepX)
            // 如果被选中，字体加深/变大，否则正常
            val isSelected = selectedIndex == index
            textPaint.color = if (isSelected) android.graphics.Color.BLACK else android.graphics.Color.parseColor("#94A3B8")
            textPaint.typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

            drawContext.canvas.nativeCanvas.drawText(
                month, x, height - 15f, textPaint
            )
        }

        // --- 4. 绘制选中状态 (交互层) ---
        selectedIndex?.let { index ->
            val x = yAxisLabelWidth + (index * stepX)

            // 4.1 绘制垂直指示线
            drawLine(
                color = Color.Gray,
                start = Offset(x, 0f),
                end = Offset(x, chartHeight),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // 4.2 绘制曲线上的交点圆圈
            fun drawPoint(values: List<Float>, color: Color) {
                val value = values[index].coerceAtLeast(0f)
                val y = chartHeight - (value / maxDataValue * chartHeight)

                // 外圈白色描边
                drawCircle(Color.White, radius = 14f, center = Offset(x, y))
                // 内圈颜色
                drawCircle(color, radius = 10f, center = Offset(x, y))
            }
            drawPoint(lastYearValues, lastYearColor)
            drawPoint(thisYearValues, thisYearColor)

            // 4.3 绘制 Tooltip (信息提示框)
            val thisVal = thisYearValues[index]
            val lastVal = lastYearValues[index]

            val monthText = "${months[index]}月"
            val thisText = "今年: ${String.format(Locale.US, "%.0f", thisVal)}" // 显示完整数值
            val lastText = "去年: ${String.format(Locale.US, "%.0f", lastVal)}"

            // 计算 Tooltip 尺寸
            val padding = 20f
            val lineHeight = 40f
            val boxWidth = 240f
            val boxHeight = 160f

            // 智能计算 Tooltip 位置 (如果在右侧，框显示在左边，反之亦然)
            val isRightSide = index > months.size / 2
            val boxLeft = if (isRightSide) x - boxWidth - 30f else x + 30f
            val boxTop = 20f // 顶部固定

            val rect = RectF(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight)

            // 绘制背景 (圆角矩形 + 阴影模拟)
            drawContext.canvas.nativeCanvas.apply {
                // 简单阴影
                val shadowPaint = Paint().apply { color = android.graphics.Color.parseColor("#20000000") }
                drawRoundRect(RectF(rect.left+4, rect.top+4, rect.right+4, rect.bottom+4), 20f, 20f, shadowPaint)

                // 主体背景 (深色背景，看起来更现代)
                val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#CC333333") }
                drawRoundRect(rect, 20f, 20f, bgPaint)

                // 绘制文字
                drawText(monthText, rect.left + padding, rect.top + 45f, tooltipTitlePaint)

                // 今年数值 (蓝色点 + 文字)
                val p1Y = rect.top + 90f
                drawCircle(rect.left + padding + 10f, p1Y - 10f, 8f, Paint().apply { color = thisYearColor.toArgb() })
                drawText(thisText, rect.left + padding + 30f, p1Y, tooltipTextPaint)

                // 去年数值 (紫色点 + 文字)
                val p2Y = rect.top + 130f
                drawCircle(rect.left + padding + 10f, p2Y - 10f, 8f, Paint().apply { color = lastYearColor.toArgb() })
                drawText(lastText, rect.left + padding + 30f, p2Y, tooltipTextPaint)
            }
        }
    }
}

// --- 辅助组件 ---
@Composable
fun DividerLine() {
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(color = Gray100, thickness = 1.dp)
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun EmptyState(msg: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
        Text(msg, color = Gray400, fontSize = 12.sp)
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // 小圆点
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = Gray500)
    }
}