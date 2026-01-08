package com.unilumin.smartapp.ui.components

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.QrCode
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
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.unilumin.smartapp.client.data.DeviceModelData
import com.unilumin.smartapp.client.data.HistoryData
import com.unilumin.smartapp.client.data.LoopInfo
import com.unilumin.smartapp.client.data.OfflineDevice
import com.unilumin.smartapp.client.data.SequenceTsl
import com.unilumin.smartapp.ui.theme.AccentBlue
import com.unilumin.smartapp.ui.theme.AlarmBg
import com.unilumin.smartapp.ui.theme.AlarmRed
import com.unilumin.smartapp.ui.theme.Amber50
import com.unilumin.smartapp.ui.theme.Amber500
import com.unilumin.smartapp.ui.theme.BackgroundGray
import com.unilumin.smartapp.ui.theme.BgLightGray
import com.unilumin.smartapp.ui.theme.Blue50
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.CardBorder
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.ControlBlue
import com.unilumin.smartapp.ui.theme.Emerald50
import com.unilumin.smartapp.ui.theme.Emerald600
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray200
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray50
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.Green50
import com.unilumin.smartapp.ui.theme.Green500
import com.unilumin.smartapp.ui.theme.LineColor
import com.unilumin.smartapp.ui.theme.OfflineGray
import com.unilumin.smartapp.ui.theme.Orange50
import com.unilumin.smartapp.ui.theme.Orange500
import com.unilumin.smartapp.ui.theme.PrimaryBlue
import com.unilumin.smartapp.ui.theme.Red50
import com.unilumin.smartapp.ui.theme.Red500
import com.unilumin.smartapp.ui.theme.SafeBg
import com.unilumin.smartapp.ui.theme.SafeGreen
import com.unilumin.smartapp.ui.theme.TextDark
import com.unilumin.smartapp.ui.theme.TextGray
import com.unilumin.smartapp.ui.theme.TextPrimary
import com.unilumin.smartapp.ui.theme.TextSecondary
import com.unilumin.smartapp.ui.theme.TextTitle
import com.unilumin.smartapp.ui.theme.White
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern
import kotlin.math.roundToInt

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
//                            popUpTo(navController.graph.startDestinationId) { saveState = true }
//                            launchSingleTop = true
//                            restoreState = true
                            navController.navigate(route) {
                                // 1. 弹出到起始目的地，并关闭 saveState 避免缓存子页堆栈
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = false
                                }
                                // 2. 确保栈中只有一个目的地实例
                                launchSingleTop = true
                                // 3. 关键：不恢复之前的状态（子页面状态会被销毁，回到根页面）
                                restoreState = false
                            }
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

fun InfoLabelValue(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: ", fontSize = 12.sp, color = Gray400)
        Text(
            text = value,
            fontSize = 12.sp,
            color = Gray500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
    icon: ImageVector,
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
    data: DeviceModelData,
    onHistoryClick: () -> Unit,
    onAnalysisClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 采用图片中的拟物化配色
    val cardBg = Color(0xFFF2EFE9)       // 暖白色背景
    val headerBg = Color(0xFFC2D1D9)     // 顶部标题栏淡蓝灰色
    val bottomBarBg = Color(0xFFC2D1D9)  // 底部操作栏淡蓝灰色
    val dividerColor = Color(0xFF9BAAB5) // 深一点的蓝灰分割线
    val primaryText = Color(0xFF2D3436)  // 深灰色文字
    val secondaryText = Color(0xFF4A5568) // 辅助文字

    Surface(
        modifier = modifier
            .padding(4.dp) // 【关键】减少外部间距，确保3列布局不拥挤
            .height(120.dp), // 减小高度以适配3列布局
        shape = RoundedCornerShape(12.dp), color = cardBg,
        // 使用 physical elevation 配合浅色边框模拟厚度
        shadowElevation = 3.dp, border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f))
    ) {
        Column {
            // --- 顶部标题栏 ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(headerBg)
                    .padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = data.name, style = TextStyle(
                        fontSize = 11.sp, color = secondaryText, fontWeight = FontWeight.Bold
                    ), maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }

            // --- 中间数据区 ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = (if (data.value.isNullOrBlank()) "--" else data.value).toString(),
                        style = TextStyle(
                            fontSize = 22.sp, // 适配小卡片的字号
                            fontWeight = FontWeight.Black, color = primaryText
                        )
                    )
                    if (!data.unit.isNullOrBlank()) {
                        Text(
                            text = data.unit,
                            modifier = Modifier.padding(start = 1.dp, bottom = 3.dp),
                            style = TextStyle(fontSize = 9.sp, color = secondaryText)
                        )
                    }
                }
            }

            // --- 底部操作栏 ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(bottomBarBg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton("列表", onHistoryClick, Modifier.weight(1f), secondaryText)
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .height(12.dp)
                        .background(dividerColor)
                )
                val isChartEnabled = data.type == "long" || data.type == "double"
                ActionButton(
                    text = "图表",
                    onClick = onAnalysisClick,
                    modifier = Modifier.weight(1f),
                    color = secondaryText,
                    enabled = isChartEnabled
                )

            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String, onClick: () -> Unit, modifier: Modifier, color: Color, enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text, style = TextStyle(
                fontSize = 10.sp,
                color = if (enabled) color else color.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold
            )
        )
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
    startDate: String, endDate: String, limitDays: Int, // 限制的总天数
    tip: String, onRangeSelected: (String, String) -> Unit
) {
    var context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }
    val sdf = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        onClick = { showPicker = true }) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DateRange,
                null,
                tint = Color(0xFF3478F6),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = tip,
                modifier = Modifier.weight(1f),
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            )
            Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Gray)
        }
    }

    if (showPicker) {
        val selectableDates = remember(limitDays) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return true
                }
            }
        }
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = try {
                sdf.parse(startDate)?.time
            } catch (e: Exception) {
                null
            }, initialSelectedEndDateMillis = try {
                sdf.parse(endDate)?.time
            } catch (e: Exception) {
                null
            }, selectableDates = selectableDates
        )
        DatePickerDialog(onDismissRequest = { showPicker = false }, confirmButton = {
            TextButton(onClick = {
                val start = state.selectedStartDateMillis
                val end = state.selectedEndDateMillis
                if (start != null && end != null) {
                    val diffDays = (end - start) / (24 * 60 * 60 * 1000)
                    if (diffDays > limitDays) {
                        Toast.makeText(
                            context, "选择范围不能超过${limitDays}天", Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        onRangeSelected(sdf.format(Date(start)), sdf.format(Date(end)))
                        showPicker = false
                    }
                }
            }) { Text("确认", fontWeight = FontWeight.Bold) }
        }) {
            DateRangePicker(
                state = state, title = {
                    Text(
                        text = "选择日期范围 (最多${limitDays}天)",
                        modifier = Modifier.padding(16.dp)
                    )
                }, headline = null, showModeToggle = false
            )
        }
    }
}

/**
 * 历史数据卡片
 * */
@Composable
fun HistoryDataCard(data: HistoryData) {
    var isExpanded by remember { mutableStateOf(false) }
    val isLongContent = isJsonValid(data.value)
    val displayValue = remember(data.value) {
        if (isLongContent) {
            getFormattedJsonAnnotatedString(data.value)
        } else {
            AnnotatedString(data.value)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 0.5.dp,
        border = BorderStroke(0.5.dp, Color(0xFFE5E5EA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. 时间轴头部
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(AccentBlue, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = data.eventTs, style = TextStyle(fontSize = 12.sp, color = TextSecondary)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. 标题区域：使用 buildAnnotatedString 实现大小字拼接
            Text(
                text = buildAnnotatedString {
                    // 主标题：名称
                    withStyle(
                        style = SpanStyle(
                            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                        )
                    ) {
                        append(data.name)
                    }
                    // 副标题：Key (小字)
                    withStyle(
                        style = SpanStyle(
                            fontSize = 12.sp, // 较小的字号
                            fontWeight = FontWeight.Normal, // 较细的字重
                            color = TextSecondary // 较淡的颜色
                        )
                    ) {
                        append(" [${data.key}]")
                    }
                })

            Spacer(modifier = Modifier.height(12.dp))

            // 3. 内容区域
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
                        text = displayValue,
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
                                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentBlue
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
fun getFormattedJsonAnnotatedString(
    json: String?, keyColor: Color = Color(0xFFD32F2F)
): AnnotatedString {
    val prettyJson = formatJson(json) // 假设你的 formatJson 也是普通函数
    return buildAnnotatedString {
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalTextApi::class)
@Composable
fun LineChartComponent(data: List<SequenceTsl>, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
    val tooltipStyle = MaterialTheme.typography.labelMedium.copy(color = Color.White)

    // 1. 数据预处理
    val sortedData = remember(data) { data.sortedBy { it.ts } }
    val values = remember(sortedData) { sortedData.map { it.value.toFloatOrNull() ?: 0f } }
    val maxVal = remember(values) { (values.maxOrNull() ?: 1f).coerceAtLeast(1f) }
    val minVal = remember(values) { values.minOrNull() ?: 0f }
    val range = (maxVal - minVal).coerceAtLeast(1f)

    // 交互状态：记录当前触摸的索引
    var selectedIndex by remember(data) { mutableStateOf(-1) }

    Canvas(
        modifier = modifier
            .padding(10.dp)
            .pointerInput(sortedData) {
                detectTapGestures(
                    onPress = { offset ->
                        val leftPaddingPx = 45.dp.toPx()
                        val chartWidthPx = size.width - leftPaddingPx
                        if (offset.x >= leftPaddingPx) {
                            val relativeX = offset.x - leftPaddingPx
                            val index =
                                (relativeX / (chartWidthPx / (sortedData.size - 1).coerceAtLeast(1)))
                                    .roundToInt()
                                    .coerceIn(0, sortedData.size - 1)
                            selectedIndex = index
                        }
                    }
                )
            }
    ) {
        val leftPadding = 45.dp.toPx()
        val bottomPadding = 30.dp.toPx()
        val chartWidth = size.width - leftPadding
        val chartHeight = size.height - bottomPadding

        if (sortedData.size < 2) return@Canvas

        // 2. 绘制纵轴 Y 轴标签和网格 (补全了数值绘制)
        val yTickCount = 4
        for (i in 0..yTickCount) {
            val yValue = minVal + (range / yTickCount) * i
            val yPos = chartHeight - (i * (chartHeight / yTickCount))

            // 绘制水平辅助线
            drawLine(
                color = Color.LightGray.copy(alpha = 0.4f),
                start = Offset(leftPadding, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 1.dp.toPx()
            )

            // 绘制 Y 轴数值标签
            drawText(
                textMeasurer = textMeasurer,
                text = String.format("%.1f", yValue),
                style = labelStyle,
                topLeft = Offset(0f, yPos - 15f) // 放在刻度线左上方
            )
        }

        // 3. 计算坐标点
        val spacing = chartWidth / (sortedData.size - 1)
        val points = values.indices.map { i ->
            Offset(
                x = leftPadding + (i * spacing),
                y = chartHeight - ((values[i] - minVal) / range) * chartHeight
            )
        }

        // 4. 绘制填充渐变和折线
        val fillPath = Path().apply {
            moveTo(leftPadding, chartHeight)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(size.width, chartHeight)
            close()
        }
        drawPath(
            fillPath, Brush.verticalGradient(
                listOf(Color(0xFF3D7EFE).copy(alpha = 0.2f), Color.Transparent),
                startY = 0f, endY = chartHeight
            )
        )

        drawPath(
            path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.forEach { lineTo(it.x, it.y) }
            },
            color = Color(0xFF3D7EFE),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 5. 绘制横轴 X 轴标签 (仅首尾两点)
        val xEndpoints = listOf(0, sortedData.size - 1)
        xEndpoints.forEachIndexed { i, index ->
            val timestamp = formatTs(sortedData[index].ts, "MM-dd HH:mm")
            val textLayout = textMeasurer.measure(timestamp, labelStyle)
            val xPos = points[index].x

            // 第一个左对齐，最后一个右对齐
            val xOffset = if (i == 0) 0f else -textLayout.size.width.toFloat()

            drawText(
                textMeasurer = textMeasurer,
                text = timestamp,
                style = labelStyle,
                topLeft = Offset(xPos + xOffset, chartHeight + 10f)
            )
        }

        // 6. 绘制交互十字线与 Tooltip (点击后显示)
        if (selectedIndex != -1 && selectedIndex < points.size) {
            val selectedPoint = points[selectedIndex]
            val dataItem = sortedData[selectedIndex]

            // 垂直虚线
            drawLine(
                color = Color(0xFF3D7EFE),
                start = Offset(selectedPoint.x, 0f),
                end = Offset(selectedPoint.x, chartHeight),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )

            // 高亮圆点
            drawCircle(Color(0xFF3D7EFE), radius = 6.dp.toPx(), center = selectedPoint)
            drawCircle(Color.White, radius = 3.dp.toPx(), center = selectedPoint)

            // Tooltip 文字：显示完整日期时间
            val tooltipText =
                "${formatTs(dataItem.ts, "yyyy-MM-dd HH:mm:ss")}\n数值: ${dataItem.value}"
            val tooltipResult = textMeasurer.measure(tooltipText, tooltipStyle)

            val rectWidth = tooltipResult.size.width + 24f
            val rectHeight = tooltipResult.size.height + 24f

            // 自动计算 Tooltip 坐标，确保不遮挡且不出界
            val tooltipX =
                (selectedPoint.x - rectWidth / 2).coerceIn(leftPadding, size.width - rectWidth)
            val tooltipY = (selectedPoint.y - rectHeight - 30f).coerceAtLeast(0f)

            drawRoundRect(
                color = Color.Black.copy(alpha = 0.8f),
                topLeft = Offset(tooltipX, tooltipY),
                size = Size(rectWidth, rectHeight),
                cornerRadius = CornerRadius(8.dp.toPx())
            )

            drawText(
                textMeasurer = textMeasurer,
                text = tooltipText,
                style = tooltipStyle,
                topLeft = Offset(tooltipX + 12f, tooltipY + 12f)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTs(ts: Long, pattern: String = "yyyy-MM-dd HH:mm"): String {
    return try {
        val instant = Instant.ofEpochMilli(ts)
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        instant.atZone(ZoneId.systemDefault()).format(formatter)
    } catch (e: Exception) {
        ""
    }
}

/**
 * 设备历史数据图表分析
 * */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChartDataView(
    limitDays: Int,
    startDate: String,
    endDate: String,
    data: List<SequenceTsl>,
    onRangeSelected: (String, String) -> Unit,
) {
    val title =
        if (startDate.isEmpty() || endDate.isEmpty()) "选择日期范围" else "$startDate 至 $endDate"
    val chartData = remember(data.size, data.lastOrNull()) {
        data.sortedBy { it.ts }
    }
    val tableData = remember(data.size, data.firstOrNull()) {
        data.sortedByDescending { it.ts }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            DateRangePickerModern(
                limitDays = limitDays,
                startDate = startDate,
                endDate = endDate,
                tip = title,
                onRangeSelected = onRangeSelected
            )
        }

        item {
            if (chartData.isEmpty()) {
                EmptyDataView("暂无数据")
            } else {
                ChartCard(chartData)
                Spacer(modifier = Modifier.height(16.dp))
                TableHeader()
            }
        }
        itemsIndexed(tableData) { index, item ->
            TableRow(item, isLast = index == tableData.size - 1)
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChartCard(data: List<SequenceTsl>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "数值趋势",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            LineChartComponent(
                data = data, modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }
    }
}

@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color(0xFFE3E9F2))
            .padding(16.dp)
    ) {
        Text(
            "时间节点",
            modifier = Modifier.weight(1.5f),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            "监控数值",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            fontSize = 14.sp
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TableRow(item: SequenceTsl, isLast: Boolean) {
    val shape = if (isLast) RoundedCornerShape(
        bottomStart = 16.dp, bottomEnd = 16.dp
    ) else RoundedCornerShape(0.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color.White, shape = shape)
            .padding(16.dp)
    ) {
        Text(
            formatTs(item.ts),
            modifier = Modifier.weight(1.5f),
            fontSize = 14.sp,
            color = Color.DarkGray
        )
        Text(
            item.value,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3D7EFE)
        )
    }
    if (!isLast) {
        Divider(
            modifier = Modifier.padding(horizontal = 32.dp),
            thickness = 0.5.dp,
            color = Color(0xFFF1F3F4)
        )
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
    val title =
        if (startDate.isEmpty() || endDate.isEmpty()) "选择日期范围" else "$startDate 至 $endDate"

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

        if (historyDataList.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (historyDataList.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyDataView("暂无数据")
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
                                Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
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
        } else {
            EmptyDataView("暂未找到数据")
        }
    }
}

/**
 * 顶部标题栏
 */
@Composable
fun HeaderSection(text: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text(
            text = text, style = TextStyle(
                fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1C1C1E)
            )
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .background(Color(0xFFF2F2F7), RoundedCornerShape(12.dp))
                .size(32.dp)
        ) {
            Icon(
                Icons.Default.Close, "Close", tint = Color.DarkGray, modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * 新增：信息提示条 (包含滚动描述和单位)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfoRibbon(data: DeviceModelData) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        color = Color(0xFFF0F7FF),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF007AFF),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val description = data.keyDes.ifBlank { "暂无描述信息" }
            Text(
                text = description, modifier = Modifier
                    .weight(1f)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE, initialDelayMillis = 2000, velocity = 30.dp
                    ), style = TextStyle(
                    color = Color(0xFF007AFF), fontSize = 13.sp, fontWeight = FontWeight.Medium
                ), maxLines = 1
            )
            if (!data.unit.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    color = Color(0xFF007AFF), shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "单位: ${data.unit}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = TextStyle(
                            color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceDataGrid(
    dataList: List<DeviceModelData>,
    onHistoryClick: (DeviceModelData) -> Unit,
    onAnalysisClick: (DeviceModelData) -> Unit
) {
    if (dataList.isEmpty()) return
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        maxItemsInEachRow = 3,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dataList.forEach { data ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                DeviceRealDataCardModern(
                    data = data,
                    onHistoryClick = { onHistoryClick(data) },
                    onAnalysisClick = { onAnalysisClick(data) }
                )
            }
        }
        val itemFillCount = (3 - (dataList.size % 3)) % 3
        repeat(itemFillCount) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }

}

/**
 * 缓冲加载
 * */
@Composable
fun LoadingContent(isLoading: Boolean, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.05f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ControlBlue)
            }
        } else {
            content()
        }
    }
}

/**
 * 进度条
 * */
@Composable
fun UsageLinearBar(label: String, usage: Double) {
    val progressValue = (usage / 100.0).coerceIn(0.0, 1.0).toFloat()
    val progressColor = if (usage > 90) Color(0xFFE57373) else MaterialTheme.colorScheme.primary
    val trackColor = progressColor.copy(alpha = 0.15f)

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${usage.toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
        }

        // --- 自定义进度条，解决断裂问题 ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp) // 足够粗
                .background(trackColor, CircleShape) // 轨道背景
        ) {
            // 进度部分
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressValue) // 根据比例占据宽度
                    .fillMaxHeight()
                    .background(progressColor, CircleShape) // 进度条圆角
            )
        }
    }
}

@Composable
private fun TotalCountHeader(totalCount: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // 留出一点边距
        color = Color(0xFFF5F7FA), // 非常淡的灰蓝色背景，现代化风格
        shape = RoundedCornerShape(8.dp), // 小圆角
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center // 居中显示
        ) {
            Icon(
                imageVector = Icons.Default.DataUsage, // 或者 Icons.Default.List
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = buildAnnotatedString {
                    append("共检索到 ")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("$totalCount")
                    }
                    append(" 条数据")
                },
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
        }
    }
}


/**
 * 分页数据展示逻辑
 * @param lazyPagingItems 数据源
 * @param forceLoading 强制切换，避免切换过程残留的旧数据
 * @param modifier 布局修饰符
 * @param itemKey 唯一表示函数，比如传入deviceId
 * @param contentPadding 内边距
 * @param verticalArrangement 垂直边距
 * @param emptyMessage 空状态提示词
 * @param itemContent 业务布局，如DeviceCardItem，SiteCardItem
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> PagingList(
    lazyPagingItems: LazyPagingItems<T>,
    totalCount: Int? = null,
    forceLoading: Boolean = false,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    itemKey: ((T) -> Any)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    emptyMessage: String = "未找到相关数据",
    itemContent: @Composable (T) -> Unit
) {
    val refreshState = lazyPagingItems.loadState.refresh
    val shouldShowFullLoading =
        forceLoading || (refreshState is LoadState.Loading && lazyPagingItems.itemCount == 0)

    var showHeader by remember { mutableStateOf(false) }
    LaunchedEffect(totalCount, refreshState) {
        if (totalCount != null && totalCount > 0 && refreshState is LoadState.NotLoading) {
            showHeader = true
            delay(1500)
            showHeader = false
        }
    }
    PullToRefreshBox(
        isRefreshing = refreshState is LoadState.Loading && !shouldShowFullLoading,
        onRefresh = { lazyPagingItems.refresh() },
        modifier = modifier
    ) {
        LoadingContent(shouldShowFullLoading) {
            LazyColumn(
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement,
                modifier = Modifier.fillMaxSize()
            ) {
                item(key = "header_total_count") {
                    AnimatedVisibility(
                        visible = showHeader,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        if (totalCount != null) {
                            Box(modifier = Modifier.padding(bottom = 4.dp)) {
                                TotalCountHeader(totalCount = totalCount)
                            }
                        }
                    }
                }

                items(
                    count = lazyPagingItems.itemCount,
                    key = lazyPagingItems.itemKey { item ->
                        itemKey?.invoke(item) ?: item.hashCode()
                    }
                ) { index ->
                    lazyPagingItems[index]?.let { itemContent(it) }
                }

                lazyPagingItems.apply {
                    when {
                        loadState.append is LoadState.Loading -> {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }

                        loadState.refresh is LoadState.Error || loadState.append is LoadState.Error -> {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("加载失败", color = Color.Gray)
                                    Button(
                                        onClick = { retry() },
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) { Text("重试") }
                                }
                            }
                        }

                        loadState.refresh is LoadState.NotLoading && itemCount == 0 -> {
                            item {
                                Box(Modifier.fillParentMaxSize(), Alignment.Center) {
                                    Text(emptyMessage, color = Color.Gray)
                                }
                            }
                        }

                        loadState.append.endOfPaginationReached && itemCount > 0 -> {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    Alignment.Center
                                ) {
                                    Text(
                                        "— 已加载全部 $itemCount 条数据 —",
                                        color = Color(0xFFCCCCCC),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeFilterSegment(selectedType: Int, onTypeSelected: (Int) -> Unit) {
    val options = listOf(0 to "最近活跃时间", 1 to "最近7天", 2 to "最近30天", 3 to "最近90天")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8EAF6), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (type, label) ->
            val isSelected = selectedType == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onTypeSelected(type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) PrimaryBlue else Color.Gray
                )
            }
        }
    }
}

@Composable
fun OfflineDeviceItem(
    device: OfflineDevice,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F7FA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = Color(0xFF3D5AFE)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = device.deviceName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = device.productName,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (device.alarmType == 1) {
                        StatusChip(
                            text = "告警",
                            color = AlarmRed,
                            bgColor = AlarmBg,
                            icon = Icons.Rounded.Warning
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val stateText = if (device.deviceState == 1) "已启用" else "已停用"
                    val stateColor = if (device.deviceState == 1) SafeGreen else OfflineGray
                    val stateBg = if (device.deviceState == 1) SafeBg else Color(0xFFF5F5F5)
                    Text(
                        text = stateText,
                        fontSize = 11.sp,
                        color = stateColor,
                        modifier = Modifier
                            .background(stateBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))

            // --- 第二行：具体信息 (网格布局或流式布局) ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 序列号
                InfoRowItem(
                    icon = Icons.Default.QrCode,
                    label = "SN:",
                    value = device.serialNum
                )
                // 厂商信息
                InfoRowItem(
                    icon = Icons.Default.Apartment,
                    label = "厂商:",
                    value = device.productFactoryName
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- 底部：最后上线时间 (强调离线背景) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF8E1), RoundedCornerShape(6.dp)) // 淡黄色背景提醒注意
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFFFA000)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "最后上线: ${device.lastActiveTime ?: "--"}",
                    fontSize = 12.sp,
                    color = Color(0xFFF57C00),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// 辅助组件：信息行
@Composable
fun InfoRowItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color(0xFFB0BEC5)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF90A4AE)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// 辅助组件：状态胶囊
@Composable
fun StatusChip(text: String, color: Color, bgColor: Color, icon: ImageVector? = null) {
    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(50)) // 全圆角
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}