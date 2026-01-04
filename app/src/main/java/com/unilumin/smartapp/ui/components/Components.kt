package com.unilumin.smartapp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.unilumin.smartapp.client.data.DeviceModelData
import com.unilumin.smartapp.client.data.HistoryData
import com.unilumin.smartapp.client.data.LoopInfo
import com.unilumin.smartapp.ui.theme.AccentBlue
import com.unilumin.smartapp.ui.theme.Amber50
import com.unilumin.smartapp.ui.theme.Amber500
import com.unilumin.smartapp.ui.theme.BackgroundGray
import com.unilumin.smartapp.ui.theme.BgLightGray
import com.unilumin.smartapp.ui.theme.Blue50
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.ControlBlue
import com.unilumin.smartapp.ui.theme.Emerald50
import com.unilumin.smartapp.ui.theme.Emerald600
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray200
import com.unilumin.smartapp.ui.theme.Gray300
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray50
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.Green50
import com.unilumin.smartapp.ui.theme.Green500
import com.unilumin.smartapp.ui.theme.LineColor
import com.unilumin.smartapp.ui.theme.Orange50
import com.unilumin.smartapp.ui.theme.Orange500
import com.unilumin.smartapp.ui.theme.Red50
import com.unilumin.smartapp.ui.theme.Red500
import com.unilumin.smartapp.ui.theme.TextDark
import com.unilumin.smartapp.ui.theme.TextGray
import com.unilumin.smartapp.ui.theme.TextPrimary
import com.unilumin.smartapp.ui.theme.TextSecondary
import com.unilumin.smartapp.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Gray100)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "online" -> Triple(Emerald50, Emerald600, "在线")
        "offline" -> Triple(Gray100, Gray500, "离线")
        "high" -> Triple(Red50, Red500, "严重")
        "medium" -> Triple(Amber50, Amber500, "警告")
        "low" -> Triple(Blue50, Blue600, "提示")
        else -> Triple(Gray100, Gray500, status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.border(0.5.dp, textColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SearchBar(
    query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = TextStyle(fontSize = 14.sp, color = Gray900),
        singleLine = true,
        cursorBrush = SolidColor(Blue600),
        decorationBox = { innerTextField ->
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Gray100, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text("搜索设备名称或地址...", color = Gray400, fontSize = 14.sp)
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Clear",
                        tint = Gray400,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onQueryChange("") })
                }
            }
        })
}

@Composable
fun FilterChip(label: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isActive) Gray900 else White,
        border = if (!isActive) BorderStroke(1.dp, Gray200) else null,
        shadowElevation = if (isActive) 2.dp else 0.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            label,
            color = if (isActive) White else Gray500,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.9f), tonalElevation = 8.dp
    ) {
        val items = listOf(
            "dashboard" to Triple("概览", Icons.Rounded.Dashboard, Icons.Outlined.Dashboard),
            "devices" to Triple("设备", Icons.Rounded.List, Icons.Outlined.List),
            "sites" to Triple("站点", Icons.Rounded.LocationOn, Icons.Outlined.LocationOn),
            "profile" to Triple("我的", Icons.Rounded.Person, Icons.Outlined.Person)
        )

        items.forEach { (route, info) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) info.second else info.third,
                        contentDescription = info.first
                    )
                },
                label = { Text(info.first, fontSize = 10.sp) },
                selected = isSelected,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Blue600,
                    selectedTextColor = Blue600,
                    indicatorColor = Blue50,
                    unselectedIconColor = Gray400,
                    unselectedTextColor = Gray400
                )
            )
        }
    }
}


@Composable
fun PageLoadingView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Blue600)
    }
}

@Composable
fun PageAppendLoadingView() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp), color = Gray400, strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("正在加载更多...", color = Gray400, fontSize = 12.sp)
    }
}

@Composable
fun EndOfListView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("— 到底啦 —", color = Gray300, fontSize = 12.sp)
    }
}

@Composable
fun EmptyDataView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = Gray100, modifier = Modifier.size(80.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.SearchOff, null, tint = Gray400, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = Gray400, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ErrorRetryView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = Gray500, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Gray100, contentColor = Gray900),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("重试")
        }
    }
}


@Composable
fun InfoColumn(label: String, value: String, isHighlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Gray400, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight && value != "0%") Amber500 else Gray900
        )
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(Gray200)
    )
}

/**
 * 设备状态(按钮:在线/离线)
 * */
@Composable
fun DeviceStatus(status: Int?) {
    // 这里使用 Green 代表在线，和左侧图标区分一点层次，或者你也改成 Blue 都可以
    val (bgColor, fgColor, text) = when (status) {
        1 -> Triple(Green50, Green500, "在线")       // 在线：绿
        0 -> Triple(Gray100, Gray500, "离线")       // 离线：灰
        else -> Triple(Orange50, Orange500, "未知") // 其他：橙
    }

    Surface(
        color = bgColor, shape = RoundedCornerShape(percent = 50), modifier = Modifier.height(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(fgColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = fgColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 1.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoopCircleItem(loop: LoopInfo) {
    val (baseColor, contentColor) = when (loop.state) {
        1 -> Color(0xFFE8F5E9) to Color(0xFF2E7D32) // 柔和绿 (通电)
        0 -> Color(0xFFFFEBEE) to Color(0xFFC62828) // 柔和红 (断电)
        else -> Color(0xFFF5F5F5) to Color(0xFF757575) // 浅灰 (未知)
    }
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(), tooltip = {
            PlainTooltip(
                containerColor = Color(0xFF333333).copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    Text("状态: ${if (loop.state == 1) "通电" else "断电"}", color = Color.White)
                    Text(
                        "回路: 第 ${loop.loopNum} 路",
                        fontSize = 10.sp,
                        color = Color.White.copy(0.7f)
                    )
                }
            }
        }, state = tooltipState
    ) {
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
                .size(36.dp) // 稍微加大尺寸，更易点击
                .background(color = baseColor, shape = CircleShape)
                .border(1.dp, contentColor.copy(alpha = 0.3f), CircleShape) // 添加同色系的浅色边框
        ) {
            Text(
                text = "${loop.loopNum}", color = contentColor, // 文字颜色与边框/状态保持一致
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// 我的--页面子组件
@Composable
fun ProfileMenuItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBg: Color,
    trailingText: String? = null,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = trailingText == null, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 18.dp), // 加大一点内边距
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(14.dp), // 更圆润
                color = iconBg, modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon, null, tint = iconColor, modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Gray900
            )
        }
        if (trailingText != null) {
            Surface(
                color = Gray50,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = trailingText,
                    fontSize = 13.sp,
                    color = Gray500,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

        } else if (showArrow) {
            Icon(
                Icons.Rounded.ChevronRight, null, tint = Gray200, modifier = Modifier.size(22.dp)
            )
        }
    }
}

//拖动条
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrightnessControlCard(
    title: String,
    initValue: Int,
    onValueChange: (Int) -> Unit,
    onValueChangeFinished: (Int) -> Unit
) {
    // 外层容器：使用 Surface 自带的阴影和圆角，代码更简洁
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF2F2F2)) // 极淡的边框增加质感
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp), // 垂直方向压扁一点，更像一个列表项
            verticalAlignment = Alignment.CenterVertically, // 关键：垂直居中
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- 1. 左侧标题区域 ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 12.dp) // 给滑块留点呼吸空间
            ) {
                // 装饰性小竖条
                Box(
                    modifier = Modifier
                        .size(4.dp, 14.dp)
                        .background(ControlBlue, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title, fontSize = 14.sp, // 稍微改小一点，显得精致
                    color = Color(0xFF333333), fontWeight = FontWeight.Bold
                )
            }

            // --- 2. 中间滑块区域 (自适应宽度) ---
            // 使用 weight(1f) 让它填满标题和数值中间的所有空间
            Slider(
                value = initValue.toFloat(), onValueChange = { newValue ->
                    onValueChange(newValue.toInt())
                }, onValueChangeFinished = {
                    onValueChangeFinished(initValue)
                }, valueRange = 0f..100f, colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = ControlBlue,
                    inactiveTrackColor = BgLightGray.copy(alpha = 0.8f) // 轨道稍微深一点点
                ), modifier = Modifier
                    .weight(1f) // 关键：占据剩余空间
                    .height(24.dp), // 限制滑块组件的高度，防止默认的触摸区域撑太高
                thumb = {
                    // 自定义小滑块，比之前那个版本要做得更小一点，适配单行
                    Surface(
                        modifier = Modifier
                            .size(20.dp) // 缩小尺寸 (之前是 28dp)
                            .shadow(2.dp, CircleShape),
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(0.5.dp, Color(0xFFEEEEEE))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(ControlBlue, CircleShape)
                            )
                        }
                    }
                })

            // --- 3. 右侧数值区域 ---
            // 使用 Box 给定最小宽度，防止数字 9 -> 10 时宽度变化导致滑块抖动
            Box(
                modifier = Modifier
                    .width(46.dp) // 给定一个固定宽度，足以容纳 "100%"
                    .padding(start = 8.dp), contentAlignment = Alignment.CenterEnd // 文字靠右对齐
            ) {
                Text(
                    text = "${initValue}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold, // 数字加粗
                    color = ControlBlue,
                    style = TextStyle(fontFeatureSettings = "tnum") // 可选：等宽数字特性
                )
            }
        }
    }
}


/**
 * @param canClick 远程控制是否可以点击
 * @param onHistoryClick 历史数据事件
 * @param onRemoteControlClick 远程控制事件
 * */
@Composable
fun RemoteControlButtonGroup(
    canClick: Boolean,
    showRemoteCtlBtn: Boolean,
    onRemoteControlClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 历史数据
        ControlButton(
            text = "设备详情",
            icon = Icons.Default.History,
            canClick = true,
            onClick = onHistoryClick,
            modifier = Modifier.weight(1f),
            activeColor = Color(0xFF6750A4)
        )
        if (showRemoteCtlBtn) {
            ControlButton(
                text = "远程控制",
                icon = Icons.Default.PowerSettingsNew,
                canClick = canClick,
                onClick = onRemoteControlClick,
                modifier = Modifier.weight(1f),
                activeColor = ControlBlue
            )
        }

    }
}


@Composable
private fun ControlButton(
    text: String,
    icon: ImageVector,
    canClick: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = if (canClick) onClick else ({}),
        modifier = modifier
            .height(56.dp)
            .alpha(if (canClick) 1f else 0.6f),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (canClick) activeColor.copy(alpha = 0.1f) else Gray50,
            contentColor = if (canClick) activeColor else Color.DarkGray
        ),
        enabled = canClick
    ) {
        Icon(
            imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1
        )
    }
}

/**
 * 美观的标签组件
 */
@Composable
fun DeviceTag(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), // 浅色背景
        shape = RoundedCornerShape(4.dp), // 轻微圆角，显得更有工业/科技感
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp, color = MaterialTheme.colorScheme.primary // 字体颜色
            )
        )
    }
}

@Composable
fun DetailCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 0.5.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ControlBlue, // 标题使用品牌色突出
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

/**
 * 优化后的信息展示行
 */
@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = TextGray, fontSize = 14.sp)
            Text(
                text = value ?: "--",
                color = TextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = LineColor)
    }
}

@Composable
fun DeviceRealDataCardModern(
    data: DeviceModelData, onHistoryClick: () -> Unit
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFF7F9FF), Color.White),
        start = Offset(0f, 0f),
        end = Offset(500f, 500f)
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(95.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onHistoryClick() },
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color(0xFFF2F2F7))
    ) {
        Column(
            modifier = Modifier
                .background(gradientBrush)
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = data.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1E)
                    ),
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (!data.unit.isNullOrBlank()) {
                    Text(
                        text = " (${data.unit})",
                        style = TextStyle(fontSize = 10.sp, color = Color(0xFF8E8E93))
                    )
                }
            }
            Text(text = data.value?.ifEmpty { "--" } ?: "--", style = TextStyle(
                fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3478F6)
            ))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    modifier = Modifier.size(12.dp),
                    tint = Color(0xFFD1D1D6)
                )
            }
        }
    }
}


/**
 * 日期期间选择组件
 * @param tip 显示的时间区间
 * @return 返回开始时间和结束时间
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModern(
    startDate: String,
    endDate: String,
    limitDays: Int,
    tip: String,
    onRangeSelected: (String, String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        onClick = { showPicker = true }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DateRange, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = tip,
                modifier = Modifier.weight(1f),
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            )
            Icon(Icons.Default.KeyboardArrowRight, null, tint = TextSecondary)
        }
    }

    if (showPicker) {
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = try { sdf.parse(startDate)?.time } catch (e: Exception) { null },
            initialSelectedEndDateMillis = try { sdf.parse(endDate)?.time } catch (e: Exception) { null }
        )

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = state.selectedStartDateMillis
                    val end = state.selectedEndDateMillis
                    if (start != null && end != null) {
                        onRangeSelected(sdf.format(Date(start)), sdf.format(Date(end)))
                        showPicker = false
                    }
                }) { Text("确认", fontWeight = FontWeight.Bold) }
            }
        ) {
            DateRangePicker(state = state, title = null, headline = null, showModeToggle = false)
        }
    }
}

/**
 * 历史数据卡片
 * */
@Composable
fun HistoryDataCard(data: HistoryData) {
    var isExpanded by remember { mutableStateOf(false) }
    val isLongContent =isJsonValid(data.value)
    if (isLongContent){
        data.value = rememberFormattedJson(data.value).toString()
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 0.5.dp, // 极轻微的阴影
        border = BorderStroke(0.5.dp, Color(0xFFE5E5EA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(AccentBlue, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = data.eventTs,
                    style = TextStyle(fontSize = 12.sp, color = TextSecondary)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = data.name,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clickable(enabled = isLongContent) { isExpanded = !isExpanded },
                color = Color(0xFFF9F9FB),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = data.value,
                        style = TextStyle(
                            fontFamily = if (isLongContent) FontFamily.Monospace else FontFamily.Default,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF3A3A3C)
                        ),
                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isLongContent) {
                        Text(
                            text = if (isExpanded) "收起详情" else "点击展开详情",
                            modifier = Modifier.padding(top = 8.dp),
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                        )
                    }
                }
            }
        }
    }
}


/**
 * 将 JSON 字符串转换为带高亮的 AnnotatedString
 */
@Composable
fun rememberFormattedJson(json: String?, keyColor: Color = Color(0xFFD32F2F)): AnnotatedString {
    return remember(json) {
        val prettyJson = formatJson(json)
        buildAnnotatedString {
            val pattern = Pattern.compile("\"(.*)\"\\s*:")
            val matcher = pattern.matcher(prettyJson)
            var lastIndex = 0
            while (matcher.find()) {
                append(prettyJson.substring(lastIndex, matcher.start()))
                withStyle(style = SpanStyle(color = keyColor, fontWeight = FontWeight.Bold)) {
                    append(matcher.group())
                }
                lastIndex = matcher.end()
            }
            append(prettyJson.substring(lastIndex))
        }
    }
}

//格式化json
fun formatJson(json: String?): String {
    if (json.isNullOrBlank()) return "--"
    return try {
        val jsonElement = JsonParser().parse(json)
        val gson = GsonBuilder().setPrettyPrinting() // 核心：设置美化打印（带缩进）
            .disableHtmlEscaping() // 防止特殊字符被转义
            .create()
        gson.toJson(jsonElement)
    } catch (e: Exception) {
        json
    }
}

fun isJsonValid(json: String?): Boolean {
    if (json.isNullOrBlank()) return false
    return try {
        val element = JsonParser().parse(json)
        // 只有当它是 JSON 对象或 JSON 数组时，才认为是我们需要的 "结构化 JSON"
        element.isJsonObject || element.isJsonArray
    } catch (e: Exception) {
        false
    }
}

/**
 * 历史数据页面
 * */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryDataListView(
    limitDays: Int,
    startDate: String,
    endDate: String,
    historyDataList: List<HistoryData>,
    hasMore: Boolean,
    onRangeSelected: (String, String) -> Unit,
    onLoadMore: (String, String) -> Unit
) {
    val title = if (startDate.isEmpty() || endDate.isEmpty()) "选择日期范围" else "$startDate 至 $endDate"

    // 整个页面背景设为浅灰色
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // 1. 顶部日期选择条 (现在它是悬浮感设计的)
        DateRangePickerModern(
            limitDays = limitDays,
            startDate = startDate,
            endDate = endDate,
            tip = title,
            onRangeSelected = onRangeSelected
        )

        // 2. 列表内容
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (historyDataList.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyDataView("暂无历史记录")
                    }
                }
            } else {
                items(historyDataList) { data ->
                    HistoryDataCard(data)
                }

                if (hasMore) {
                    item {
                        LaunchedEffect(historyDataList.size) {
                            onLoadMore(startDate, endDate)
                        }
                        Box(
                            Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                                color = AccentBlue
                            )
                        }
                    }
                }
            }
        }
    }
}