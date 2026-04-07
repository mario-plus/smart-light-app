package com.unilumin.smartapp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.client.data.DeviceModelData
import com.unilumin.smartapp.client.data.HistoryData
import com.unilumin.smartapp.client.data.SequenceTsl
import com.unilumin.smartapp.client.data.SystemConfig
import com.unilumin.smartapp.ui.screens.dashboard.InfoRowItem
import com.unilumin.smartapp.ui.theme.AccentBlue
import com.unilumin.smartapp.ui.theme.BackgroundGray
import com.unilumin.smartapp.ui.theme.Blue50
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.BluePrimary
import com.unilumin.smartapp.ui.theme.Border
import com.unilumin.smartapp.ui.theme.ControlBlue
import com.unilumin.smartapp.ui.theme.DividerColor
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray200
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.Green50
import com.unilumin.smartapp.ui.theme.Green500
import com.unilumin.smartapp.ui.theme.GreenStatus
import com.unilumin.smartapp.ui.theme.HeaderBg
import com.unilumin.smartapp.ui.theme.LineColor
import com.unilumin.smartapp.ui.theme.Orange50
import com.unilumin.smartapp.ui.theme.Orange500
import com.unilumin.smartapp.ui.theme.PlaceholderColor
import com.unilumin.smartapp.ui.theme.PrimaryText
import com.unilumin.smartapp.ui.theme.RedStatus
import com.unilumin.smartapp.ui.theme.SearchBarBg
import com.unilumin.smartapp.ui.theme.SecondaryText
import com.unilumin.smartapp.ui.theme.TextDark
import com.unilumin.smartapp.ui.theme.TextGray
import com.unilumin.smartapp.ui.theme.TextPrimary
import com.unilumin.smartapp.ui.theme.TextSecondary
import com.unilumin.smartapp.ui.theme.TextSub
import com.unilumin.smartapp.ui.theme.White
import com.unilumin.smartapp.ui.viewModel.LampViewModel
import com.unilumin.smartapp.util.JsonUtils
import com.unilumin.smartapp.util.JsonUtils.parseJsonToKeyValue
import com.unilumin.smartapp.util.TimeUtil
import com.unilumin.smartapp.util.ToastUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = false
                                }
                                launchSingleTop = true
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
fun InfoLabelValue(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = "$label: ", fontSize = 12.sp, color = Gray400)
        Text(
            text = value,
            fontSize = 12.sp,
            color = Gray500,
            modifier = Modifier.weight(1f)
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
fun DeviceStatus(
    status: Int?, statusMapping: Map<Int, Triple<Color, Color, String>> = mapOf(
        1 to Triple(Green50, Green500, "在线"), 0 to Triple(Gray100, Gray500, "离线")
    ), defaultStatus: Triple<Color, Color, String> = Triple(Orange50, Orange500, "未知")
) {
    val (bgColor, fgColor, text) = statusMapping[status] ?: defaultStatus
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
                modifier = Modifier.padding(bottom = 0.5.dp)
            )
        }
    }
}


@Composable
fun DetailCard(
    title: String,
    modifier: Modifier = Modifier,
    titleAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 0.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween, // 左右两端对齐
                verticalAlignment = Alignment.CenterVertically // 垂直居中对齐
            ) {
                // 左侧标题
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937) // 比如 Gray900
                )
                if (titleAction != null) {
                    titleAction()
                }
            }
            content()
        }
    }
}

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
    data: DeviceModelData, onAnalysisClick: () -> Unit, modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onAnalysisClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Border)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderBg)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = data.name,
                    style = TextStyle(
                        fontSize = 15.sp, color = PrimaryText, fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (data.keyDes.isNotBlank()) {
                    Text(
                        text = data.keyDes, style = TextStyle(
                            fontSize = 12.sp,
                            color = SecondaryText,
                            fontFamily = FontFamily.Monospace
                        ), maxLines = 1
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = data.value?.takeIf { it.isNotBlank() } ?: "--",
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryText,
                            letterSpacing = (-0.5).sp
                        ),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE, // 无限循环
                                animationMode = MarqueeAnimationMode.Immediately
                            ),
                        maxLines = 1
                    )
                    if (!data.unit.isNullOrBlank()) {
                        Text(
                            text = data.unit,
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = SecondaryText
                            )
                        )
                    }
                }
                InfoRowItem(
                    icon = Icons.Outlined.AccessTime,
                    label = "最近更新时间",
                    value = data.updateTime?.takeIf { it.isNotBlank() } ?: "--"
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
                        ToastUtil.showError(context, "日期范围不能超过${limitDays}天")
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
 */
@Composable
fun HistoryDataCard(data: HistoryData) {
    var isExpanded by remember { mutableStateOf(false) }
    val isLongContent = JsonUtils.isJsonValid(data.value)
    val keyValuePairs = remember(data.value) {
        if (isLongContent) parseJsonToKeyValue(data.value) else emptyList()
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
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                        )
                    ) {
                        append(data.name)
                    }
                    withStyle(
                        style = SpanStyle(
                            fontSize = 12.sp, fontWeight = FontWeight.Normal, color = TextSecondary
                        )
                    ) {
                        append(" [${data.key}]")
                    }
                })
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clickable(enabled = isLongContent && keyValuePairs.isNotEmpty()) {
                        isExpanded = !isExpanded
                    }, color = Color(0xFFF9F9FB), shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (isLongContent && keyValuePairs.isNotEmpty()) {
                        val displayPairs = if (isExpanded) keyValuePairs else keyValuePairs.take(3)
                        displayPairs.forEachIndexed { index, pair ->
                            InfoLabelValue(pair.first, pair.second)
                            if (index < displayPairs.size - 1) {
                                Divider(
                                    color = Color(0xFFEFEFEF),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                        if (keyValuePairs.size > 3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isExpanded) "收起详情" else "共 ${keyValuePairs.size} 项，点击展开",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AccentBlue
                                    )
                                )
                            }
                        }
                    } else {
                        Text(
                            text = data.value,
                            style = TextStyle(
                                fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF3A3A3C)
                            ),
                            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
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
                                (relativeX / (chartWidthPx / (sortedData.size - 1).coerceAtLeast(1))).roundToInt()
                                    .coerceIn(0, sortedData.size - 1)
                            selectedIndex = index
                        }
                    })
            }) {
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
                startY = 0f,
                endY = chartHeight
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
            val timestamp = TimeUtil.formatTs(sortedData[index].ts, "MM-dd HH:mm")
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
                "${
                    TimeUtil.formatTs(
                        dataItem.ts,
                        TimeUtil.DEFAULT_PATTERN
                    )
                }\n数值: ${dataItem.value}"
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

/**
 * @param isLoading 数据加载中
 * @param showChart 针对数字类型的历史数据，可以展示图标显示信息
 * @param limitDays 时间选择限制天数
 * @param startDate 开始时间
 * @param endDate 结束时间
 * @param data 历史数据
 * @param onRangeSelected 时间更改触发动作
 * */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryDataView(
    isLoading: Boolean,
    showChart: Boolean,
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
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            if (chartData.isEmpty()) {
                item { EmptyDataView("该时间区间无历史数据") }
            } else {
                if (showChart) {
                    item {
                        ChartCard(chartData)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                item {
                    TableHeader()
                }
                itemsIndexed(tableData) { index, item ->
                    TableRow(item, isLast = index == tableData.size - 1)
                }
            }
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
            TimeUtil.formatTs(item.ts),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceDataGrid(
    dataList: List<DeviceModelData>,
    onAnalysisClick: (DeviceModelData) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 1
    ) {
        dataList.forEach { data ->
            Box(modifier = Modifier.fillMaxWidth()) {
                val onClick = remember(data, onAnalysisClick) {
                    { onAnalysisClick(data) }
                }
                DeviceRealDataCardModern(
                    data = data, onAnalysisClick = onClick
                )
            }
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
 * @param onAddClick 悬浮按钮
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> PagingList(
    lazyPagingItems: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    totalCount: Int? = null,
    forceLoading: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    emptyMessage: String = "暂无相关数据",
    itemKey: ((T) -> Any)? = null,
    onAddClick: (() -> Unit)? = null,
    itemContent: @Composable (T) -> Unit
) {
    val refreshState = lazyPagingItems.loadState.refresh
    val shouldShowFullLoading =
        forceLoading || (refreshState is LoadState.Loading && lazyPagingItems.itemCount == 0)
    val scope = rememberCoroutineScope()

    val isScrolledDown by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 2
        }
    }
    val showScrollToTop = isScrolledDown
    var showHeader by remember { mutableStateOf(false) }

    LaunchedEffect(totalCount, refreshState) {
        if (totalCount != null && totalCount > 0 && refreshState is LoadState.NotLoading) {
            showHeader = true
            delay(2000)
            showHeader = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = refreshState is LoadState.Loading && !shouldShowFullLoading,
            onRefresh = { lazyPagingItems.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    ),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "成功加载 $totalCount 条数据",
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
                items(
                    count = lazyPagingItems.itemCount,
                    key = if (itemKey != null) lazyPagingItems.itemKey { itemKey(it) } else null
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
                                        .padding(16.dp), Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF29FFA2)
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
                                    Text("加载失败", color = Color.Gray, fontSize = 14.sp)
                                    Button(
                                        onClick = { retry() },
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text("重试")
                                    }
                                }
                            }
                        }

                        loadState.refresh is LoadState.NotLoading && itemCount == 0 -> {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emptyMessage, color = Color.Gray)
                                }
                            }
                        }

                        loadState.append.endOfPaginationReached && itemCount > 0 -> {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    Alignment.Center
                                ) {
                                    Text(
                                        text = "— 到底啦，没有更多数据了 —",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 底部操作区：统一样式的悬浮按钮
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 回到顶部按钮
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = scaleIn(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = scaleOut(spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Surface(
                    onClick = { scope.launch { listState.animateScrollToItem(0) } },
                    modifier = Modifier.size(48.dp), // 统一尺寸 48dp
                    shape = CircleShape,
                    color = Color(0xFF2979FF).copy(alpha = 0.2f), // 统一透明底色
                    contentColor = Color(0xFF2979FF), // 统一图标颜色
                    shadowElevation = 0.dp // 统一无阴影
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = "回到顶部",
                            modifier = Modifier.size(24.dp) // 统一图标大小
                        )
                    }
                }
            }

            // 新增按钮
            if (onAddClick != null) {
                Surface(
                    onClick = onAddClick,
                    modifier = Modifier.size(48.dp), // 统一尺寸 48dp
                    shape = CircleShape,
                    color = Color(0xFF2979FF).copy(alpha = 0.2f), // 统一透明底色
                    contentColor = Color(0xFF2979FF), // 统一图标颜色
                    shadowElevation = 0.dp // 统一无阴影
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "",
                            modifier = Modifier.size(24.dp) // 统一图标大小
                        )
                    }
                }
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
            text = label, fontSize = 13.sp, color = Color(0xFF90A4AE)
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
            text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color
        )
    }
}

@Composable
fun DeviceStatusRow(
    isDisable: Boolean, hasAlarm: Boolean, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween, // 关键：一个靠左，一个靠右
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 可用状态 - 靠最左
        StatusItem(
            label = "可用状态:", text = if (isDisable) "禁用" else "启用", isError = isDisable
        )
        // 工作状态 - 靠最右
        StatusItem(label = "工作状态:", text = if (hasAlarm) "告警" else "正常", isError = hasAlarm)
    }
}

@Composable
private fun StatusItem(label: String, text: String, isError: Boolean) {
    val color = if (isError) RedStatus else GreenStatus
    val icon = if (isError) Icons.Default.HighlightOff else Icons.Default.CheckCircle

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 13.sp, color = TextSub)
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 13.sp, color = color, fontWeight = FontWeight.Medium)
    }
}


@Composable
fun CommonTopAppBar(
    title: String,
    subTitle: String? = null, // 新增：可选的二级标题参数
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    menuItems: List<SystemConfig> = emptyList(),
    onMenuItemClick: (SystemConfig) -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val textMain = Color(0xFF1D1B20)
    val textSecondary = Color(0xFF757575) // 新增：副标题专属的次级文本颜色

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(height)
                .padding(horizontal = 4.dp)
        ) {
            // === 左侧：返回按钮 ===
            IconButton(
                onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back", tint = textMain, modifier = Modifier.size(24.dp)
                )
            }

            // === 中间：主标题与副标题 ===
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 48.dp), // 增加水平内边距，防止长标题与左右图标重叠
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        // 优化：如果有副标题，主标题字号稍微缩小以适应 56dp 的高度
                        fontSize = if (subTitle.isNullOrEmpty()) 18.sp else 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textMain,
                        letterSpacing = 0.2.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 仅当传入了副标题时才渲染这部分
                if (!subTitle.isNullOrEmpty()) {
                    Text(
                        text = subTitle,
                        style = TextStyle(
                            fontSize = 12.sp, // 字体更小
                            fontWeight = FontWeight.Normal, // 字重变轻
                            color = textSecondary // 颜色变淡，拉开视觉层级
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // === 右侧：操作区 ===
            if (menuItems.isNotEmpty()) {
                Box(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = "Menu",
                            tint = textMain,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    ReferenceStyleDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        items = menuItems,
                        onItemClick = { systemConfig ->
                            menuExpanded = false
                            onMenuItemClick(systemConfig)
                        })
                }
            }
        }
    }
}

/**
 *  配置选择框
 * */
@Composable
fun ReferenceStyleDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<SystemConfig>,
    onItemClick: (SystemConfig) -> Unit
) {
    val menuShape = RoundedCornerShape(16.dp)
    MaterialTheme(shapes = Shapes(extraSmall = menuShape)) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onDismissRequest() },
            modifier = Modifier
                .widthIn(min = 150.dp)
                .shadow(
                    12.dp, menuShape, spotColor = Color.Black.copy(alpha = 0.2f)
                )
                .clip(menuShape)
                .background(Color.White)
                .border(0.5.dp, Color(0xFFF0F0F0), menuShape)
        ) {
            items.forEach { smartApp ->
                if (smartApp.isSelected) {
                    DropdownMenuItem(leadingIcon = {
                        Icon(
                            smartApp.icon, null, Modifier.size(20.dp), Color.Gray
                        )
                    }, text = {
                        Text(
                            smartApp.name, fontSize = 14.sp, fontWeight = FontWeight.Medium
                        )
                    }, onClick = { onItemClick(smartApp) })
                }
            }
        }
    }

}


/**
 * 网络状态+搜索栏
 * @param currentStatus 网络状态
 * @param searchQuery 关键词
 * @param searchTitle 搜索框提示词
 * @param onStatusChanged 网络状态更改
 * @param onSearchChanged 关键词更改
 * */
@Composable
fun SearchHeader(
    searchQuery: String,
    searchTitle: String,
    onSearchChanged: (String) -> Unit,
    statusOptions: List<Pair<Int, String>>? = null,
    currentStatus: Int = -1,
    onStatusChanged: (Int) -> Unit = {}
) {
    var statusExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            color = SearchBarBg,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically
            ) {
                if (!statusOptions.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .fillMaxHeight()
                            .clickable { statusExpanded = true }
                            .padding(start = 16.dp, end = 8.dp),
                        contentAlignment = Alignment.CenterStart) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = statusOptions.find { it.first == currentStatus }?.second
                                    ?: "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF333333)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            statusOptions.forEach { (value, label) ->
                                DropdownMenuItem(text = {
                                    Text(
                                        text = label,
                                        color = if (value == currentStatus) Color.Blue else BluePrimary,
                                        fontWeight = if (value == currentStatus) FontWeight.Bold else FontWeight.Normal
                                    )
                                }, onClick = {
                                    onStatusChanged(value)
                                    statusExpanded = false
                                })
                            }
                        }
                    }
                    VerticalDivider(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp), color = DividerColor
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF999999),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Box(contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = searchTitle, color = PlaceholderColor, fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchChanged,
                            textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                            singleLine = true,
                            cursorBrush = SolidColor(BluePrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * 智慧路灯分页数据页面组件
 * @param statusOptions 状态key-value
 * @param searchTitle 搜索框提示词
 * @param pagingItems 分页数据源
 * @param middleContent 搜索框和分页数据，中间插入组件
 * @param itemContent 分页数据项卡片组件
 * */
@Composable
fun <T : Any> BaseLampListScreen(
    statusOptions: List<Pair<Int, String>> = DeviceConstant.statusOptions,
    searchTitle: String,
    viewModel: LampViewModel,
    pagingItems: LazyPagingItems<T>,
    keySelector: ((T) -> Any)? = null,
    onAddClick: (() -> Unit)? = null,
    middleContent: (@Composable () -> Unit)? = null,
    itemContent: @Composable (T) -> Unit
) {

    val deviceState by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val isSwitching by viewModel.isSwitch.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .zIndex(1f)
        ) {
            SearchHeader(
                statusOptions = statusOptions,
                currentStatus = deviceState,
                searchQuery = searchQuery,
                searchTitle = searchTitle,
                onStatusChanged = { viewModel.updateState(it) },
                onSearchChanged = { viewModel.updateSearch(it) })
            if (middleContent != null) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    middleContent()
                }
            }
        }
        PagingList(
            totalCount = totalCount,
            lazyPagingItems = pagingItems,
            forceLoading = isSwitching,
            modifier = Modifier.weight(1f),
            itemKey = keySelector,
            contentPadding = PaddingValues(top = 0.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
            itemContent = itemContent,
            onAddClick = onAddClick
        )
    }
}


/**
 * 多选组件，横向布局那种多选项
 * @param title 多选框全选名称
 * @param options  选项
 * @param selectedKeys 当前选项
 * @param onSelectionChanged 选中事件
 * */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <K> GridMultiSelectBar(
    title: String = "全部",
    options: List<Pair<K, String>>,
    selectedKeys: Set<K>,
    onSelectionChanged: (Set<K>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    // 箭头旋转动画
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "arrow"
    )

    val isAllSelected = selectedKeys.isEmpty() || selectedKeys.size == options.size

    // 外层容器：使用 Column + animateContentSize 实现由内容撑开高度
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .animateContentSize() // 关键：高度变化动画
    ) {

        // --- 1. 顶部操作栏 ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp) // 给一个固定高度，防止切换内容时高度跳动
                .padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically
        ) {

            // 左侧区域：关键逻辑修改！
            Box(
                modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart
            ) {
                if (expanded) {
                    Text(
                        text = "",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            SimpleChipItem(
                                text = title,
                                isSelected = isAllSelected,
                                onClick = { if (!isAllSelected) onSelectionChanged(emptySet()) })
                        }
                        items(options) { (key, label) ->
                            val isSelected = selectedKeys.contains(key)
                            SimpleChipItem(
                                text = label,
                                isSelected = if (isAllSelected) false else isSelected,
                                onClick = {
                                    toggleSelection(
                                        key,
                                        isSelected,
                                        isAllSelected,
                                        selectedKeys,
                                        onSelectionChanged
                                    )
                                })
                        }
                    }
                }
            }

            // 右侧：分割线 + 展开/收起按钮
            Spacer(modifier = Modifier.width(4.dp))
            VerticalDivider(
                modifier = Modifier.height(20.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.padding(start = 4.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    modifier = Modifier.rotate(rotation),
                    tint = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- 2. 展开的网格区域 (FlowRow) ---
        // 只有展开时才显示，并且 animateContentSize 会把它平滑地“推”出来
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // 网格内容
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // A. 全部按钮 (网格里的)
                    TextFilterItem(
                        text = "全部", // 网格里的全部
                        isSelected = isAllSelected, onClick = { onSelectionChanged(emptySet()) })

                    // B. 其他选项
                    options.forEach { (key, label) ->
                        val isSelected = selectedKeys.contains(key)
                        TextFilterItem(
                            text = label,
                            isSelected = if (isAllSelected) false else isSelected,
                            onClick = {
                                toggleSelection(
                                    key, isSelected, isAllSelected, selectedKeys, onSelectionChanged
                                )
                            })
                    }
                }
            }

            // 展开时的底部分割线
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun TextFilterItem(
    text: String, isSelected: Boolean, onClick: () -> Unit
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = 0.5f
        )
    val textColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .height(36.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) { onClick() }, shape = RoundedCornerShape(8.dp), color = backgroundColor
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SimpleChipItem(
    text: String, isSelected: Boolean, onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val border = if (isSelected) null else BorderStroke(
        1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    )

    Surface(
        modifier = Modifier
            .height(30.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) { onClick() },
        shape = RoundedCornerShape(15.dp),
        color = backgroundColor,
        border = border
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

private fun <K> toggleSelection(
    key: K,
    isSelected: Boolean,
    isAllSelected: Boolean,
    currentKeys: Set<K>,
    onChanged: (Set<K>) -> Unit
) {
    val newKeys = currentKeys.toMutableSet()
    if (isAllSelected) {
        onChanged(setOf(key))
    } else {
        if (isSelected) newKeys.remove(key) else newKeys.add(key)
        onChanged(newKeys)
    }
}


@Composable
fun <Int> ModernStateSelector(
    options: List<Pair<Int, String>>,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(containerColor)
            .padding(4.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        // 在遍历时直接解构 Pair，让代码语义更清晰
        options.forEach { (value, label) ->
            val isSelected = value == selectedValue

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "bgColor"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "textColor"
            )
            val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .then(
                        if (isSelected) Modifier.border(
                            0.5.dp, Color.Black.copy(alpha = 0.05f), CircleShape
                        )
                        else Modifier
                    )
                    .clickable { onValueChange(value) }, contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label, // 使用 Pair 的 first
                    color = textColor, fontSize = 13.sp, fontWeight = fontWeight, maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeekStrategySection(weekValue: String?) {
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val values = weekValue?.split(",") ?: emptyList()
    val activeDays = weekDays.filterIndexed { index, _ ->
        values.getOrNull(index)?.trim() == "1"
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "周期策略",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (activeDays.isEmpty()) {
            Text(
                text = "暂无执行计划",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 24.dp)
            )
        } else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeDays.forEach { day ->
                    WeekDayChip(day)
                }
            }
        }
    }
}


@Composable
private fun WeekDayChip(day: String) {
    Surface(
        modifier = Modifier.shadow(
            elevation = 2.dp,
            shape = RoundedCornerShape(6.dp),
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
    ) {
        Text(
            text = day,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}



/**
 * 单选下拉框
 * @param items 数据源
 * @param selectedItem 被选
 * @param itemLabel  显示的名称(下拉数据列表对应的key)
 * @param onItemSelected 选中事件
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CommonDropdownMenu(
    items: List<T>,
    selectedItem: T?,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "请选择",
    placeholder: String = "请选择",
    maxHeight: Dp = 360.dp // 新增参数：默认限制在240dp，约展示4-5个选项
) {
    // 组件内部维护展开/收起的状态
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            // 使用传入的 itemLabel 函数提取显示文本
            value = selectedItem?.let { itemLabel(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            // 推荐加上默认的颜色配置，确保展开时的状态颜色变化符合 Material 3 规范
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            // 核心优化：限制下拉框的最大高度
            modifier = Modifier.heightIn(max = maxHeight)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = itemLabel(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

/**
 * 通用步骤进度指示器组件
 *
 * @param steps 步骤的文案列表，例如 listOf("第一步", "第二步", "第三步")
 * @param currentStep 当前进行到的步骤索引（从 0 开始）
 * @param modifier 外部传入的修饰符
 * @param dividerWidth 两个步骤圆圈之间的连接线长度
 */
@Composable
fun StepProgressIndicator(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier,
    dividerWidth: Dp = 40.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        steps.forEachIndexed { index, label ->
            StepCircle(
                step = index + 1, // 显示给用户的数字从 1 开始
                isActive = index <= currentStep,
                label = label
            )
            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(dividerWidth)
                        .height(2.dp)
                        .background(
                            if (currentStep > index) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

/**
 * 步骤小圆圈及其下方文字
 */
@Composable
fun StepCircle(
    step: Int,
    isActive: Boolean,
    label: String
) {
    val bgColor =
        if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor =
        if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step.toString(),
                color = textColor,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CommonConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier =Modifier,
    confirmText: String = "确认",
    dismissText: String = "取消",
    icon: ImageVector? = null,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(24.dp)
    )
}