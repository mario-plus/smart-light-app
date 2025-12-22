package com.unilumin.smartapp.ui.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.unilumin.smartapp.client.data.LoopInfo
import com.unilumin.smartapp.ui.theme.Amber50
import com.unilumin.smartapp.ui.theme.Amber500
import com.unilumin.smartapp.ui.theme.Amber700
import com.unilumin.smartapp.ui.theme.BgLightGray
import com.unilumin.smartapp.ui.theme.Blue50
import com.unilumin.smartapp.ui.theme.Blue600
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
import com.unilumin.smartapp.ui.theme.Orange50
import com.unilumin.smartapp.ui.theme.Orange500
import com.unilumin.smartapp.ui.theme.Red50
import com.unilumin.smartapp.ui.theme.Red500
import com.unilumin.smartapp.ui.theme.White

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
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
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
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
                            .clickable { onQueryChange("") }
                    )
                }
            }
        }
    )
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
        containerColor = Color.White.copy(alpha = 0.9f),
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            "dashboard" to Triple("概览", Icons.Rounded.Dashboard, Icons.Outlined.Dashboard),
            "devices" to Triple("设备", Icons.Rounded.List, Icons.Outlined.List),
            "sites" to Triple("站点", Icons.Rounded.LocationOn, Icons.Outlined.LocationOn),
//            "monitor" to Triple("监控", Icons.Rounded.Videocam, Icons.Outlined.Videocam),
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
fun SwitchButton(isOn: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isOn) Amber50 else Gray100,
        border = if (isOn) BorderStroke(1.dp, Color(0xFFFEF3C7)) else null,
        modifier = Modifier
            .height(32.dp)
            .clickable {

            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                Icons.Rounded.PowerSettingsNew,
                null,
                tint = if (isOn) Amber700 else Gray500,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (isOn) "已开启" else "已关闭",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOn) Amber700 else Gray500
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
            modifier = Modifier.size(16.dp),
            color = Gray400,
            strokeWidth = 2.dp
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
        color = bgColor,
        shape = RoundedCornerShape(percent = 50),
        modifier = Modifier.height(24.dp)
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
    // 1. 同时定义颜色和状态描述文字
    val (circleColor, stateText) = when (loop.state) {
        1 -> Color(0xFF4CAF50) to "通电"
        0 -> Color(0xFFF44336) to "断电"
        else -> Color(0xFF9E9E9E) to "未知"
    }
    // 2. 定义 Tooltip 状态
    val tooltipState = rememberTooltipState()
    // 3. 使用 TooltipBox 包裹内容
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            // 悬浮显示的提示框内容
            PlainTooltip {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stateText,
                        fontWeight = FontWeight.Bold,
                        color = White // Tooltip 背景通常较深，根据主题可能需要调整
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "更新: ${loop.updateTime ?: "--"}", // 防止时间为空
                        fontSize = 10.sp,
                        color = White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        state = tooltipState
    ) {
        // 4. 原有的圆圈组件 (作为触发源)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .background(color = circleColor, shape = CircleShape)
        ) {
            Text(
                text = "${loop.loopNum}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
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
                color = iconBg,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Gray900
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
                Icons.Rounded.ChevronRight,
                null,
                tint = Gray200,
                modifier = Modifier.size(22.dp)
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
                    text = title,
                    fontSize = 14.sp, // 稍微改小一点，显得精致
                    color = Color(0xFF333333),
                    fontWeight = FontWeight.Bold
                )
            }

            // --- 2. 中间滑块区域 (自适应宽度) ---
            // 使用 weight(1f) 让它填满标题和数值中间的所有空间
            Slider(
                value = initValue.toFloat(),
                onValueChange = { newValue ->
                    onValueChange(newValue.toInt())
                },
                onValueChangeFinished = {
                    onValueChangeFinished(initValue)
                },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = ControlBlue,
                    inactiveTrackColor = BgLightGray.copy(alpha = 0.8f) // 轨道稍微深一点点
                ),
                modifier = Modifier
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
                }
            )

            // --- 3. 右侧数值区域 ---
            // 使用 Box 给定最小宽度，防止数字 9 -> 10 时宽度变化导致滑块抖动
            Box(
                modifier = Modifier
                    .width(46.dp) // 给定一个固定宽度，足以容纳 "100%"
                    .padding(start = 8.dp),
                contentAlignment = Alignment.CenterEnd // 文字靠右对齐
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


//远程控制按钮
@Composable
fun RemoteControlButton(
    canClick: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "远程控制"
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp)
            .alpha(if (canClick) 1f else 0.6f),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (canClick) ControlBlue.copy(alpha = 0.1f) else Gray50,
            contentColor = if (canClick) ControlBlue else Color.DarkGray
        )
    ) {
        Icon(
            imageVector = Icons.Default.PowerSettingsNew,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}