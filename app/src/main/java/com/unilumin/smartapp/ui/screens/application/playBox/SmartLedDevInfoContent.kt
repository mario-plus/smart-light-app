package com.unilumin.smartapp.ui.screens.application.playBox

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ImageNotSupported
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.PowerOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Screenshot
import androidx.compose.material.icons.rounded.SettingsRemote
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.unilumin.smartapp.client.data.LedCommandReq
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.ScreenViewModel

/**
 * 智慧屏幕控制页
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLedDevInfoContent(
    imageLoader: ImageLoader, screenViewModel: ScreenViewModel, onBack: () -> Unit
) {
    // 播放盒信息
    val selectLedDevInfo by screenViewModel.selectLedDevInfo.collectAsState()
    val screenshot by screenViewModel.screenshot.collectAsState()

    val ledDevFuncMaps by screenViewModel.ledDevFuncMaps.collectAsState()
    // 滚动状态
    val scrollState = rememberScrollState()
    val publishMenuItems = remember(ledDevFuncMaps) {
        val items = mutableListOf<PublishMenuItem>()
        if (ledDevFuncMaps.containsKey("programPublic")) {
            items.add(PublishMenuItem("发布播放表", Icons.Rounded.ListAlt) {})
        }
        if (ledDevFuncMaps.containsKey("playSchedule")) {
            items.add(PublishMenuItem("发布播放方案", Icons.Rounded.PlayCircleOutline) {})
        }
        if (ledDevFuncMaps.containsKey("controlSchedule")) {
            items.add(PublishMenuItem("发布控制方案", Icons.Rounded.SettingsRemote) {})
        }
        items.toList()
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = selectLedDevInfo?.name ?: "设备详情",
                onBack = { onBack() },
            )
        }, containerColor = PageBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 设备截图区域
            ScreenshotSection(
                imageUrl = screenshot, imageLoader = imageLoader, onRefresh = {
                    screenViewModel.ledDevDetail(selectLedDevInfo!!.id)
                })
            if (ledDevFuncMaps.containsKey("con_center")) {
                RemoteControlSection(
                    initialVolume = selectLedDevInfo?.volume?.toInt() ?: 0,
                    initialBrightness = selectLedDevInfo?.brightness?.toInt() ?: 0,
                    onVolumeChangeFinished = { newVolume ->
                        screenViewModel.ledCommand(
                            LedCommandReq(
                                deviceId = selectLedDevInfo?.id, type = 12, value = newVolume
                            )
                        )
                    },
                    onBrightnessChangeFinished = { newBrightness ->
                        screenViewModel.ledCommand(
                            LedCommandReq(
                                deviceId = selectLedDevInfo?.id, type = 4, value = newBrightness
                            )
                        )
                    },
                    onActionClick = { actionType ->
                        when (actionType) {
                            ActionType.SCREEN_ON -> screenViewModel.ledCommand(LedCommandReq(deviceId = selectLedDevInfo?.id, type = 2, value = 0))
                            ActionType.SCREEN_OFF -> screenViewModel.ledCommand(LedCommandReq(deviceId = selectLedDevInfo?.id, type = 1, value = 0))
                            ActionType.SCREENSHOT -> screenViewModel.ledCommand(LedCommandReq(deviceId = selectLedDevInfo?.id, type = 5, value = 0))
                            ActionType.REBOOT -> screenViewModel.ledCommand(LedCommandReq(deviceId = selectLedDevInfo?.id, type = 3, value = 0))
                        }
                    })
            }
            // 3. 动态发布管理区域
            PublishManagementSection(menuItems = publishMenuItems)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ScreenshotSection(
    imageUrl: String?, imageLoader: ImageLoader, onRefresh: () -> Unit
) {
    // 保留时间戳用于触发 Compose 重组
    var refreshTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "RefreshRotation"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当前播放画面",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        rotationAngle += 360f
                        refreshTimestamp = System.currentTimeMillis() // 触发本地重新构建 ImageRequest
                        onRefresh() // 调用远端刷新
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "刷新截图",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.rotate(animatedRotation)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNullOrEmpty()) {
                    Icon(
                        imageVector = Icons.Rounded.ImageNotSupported,
                        contentDescription = "暂无截图",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .setParameter("refresh_time", refreshTimestamp) // 改变参数，强制 Coil 认为这是一个新请求
                            .memoryCachePolicy(CachePolicy.DISABLED) // 禁用此图片的内存缓存
                            .diskCachePolicy(CachePolicy.DISABLED)   // 禁用此图片的磁盘缓存
                            .crossfade(true)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = "设备截图",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            // 💡 修复点：添加一层 Box，防止进度条被拉伸到填充满 200dp 的高度
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                            }
                        },
                        error = {
                            // 💡 修复点：为 Error 图标也增加约束包裹
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ImageNotSupported,
                                    contentDescription = "加载失败",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
 fun RemoteControlSection(
    initialVolume: Int,
    initialBrightness: Int,
    onVolumeChangeFinished: (Int) -> Unit,
    onBrightnessChangeFinished: (Int) -> Unit,
    onActionClick: (ActionType) -> Unit
) {
    var volume by remember { mutableFloatStateOf(initialVolume.toFloat()) }
    var brightness by remember { mutableFloatStateOf(initialBrightness.toFloat()) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "远程控制",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            BeautifulControlSlider(
                icon = Icons.Rounded.VolumeUp,
                title = "音量",
                value = volume,
                valueRange = 0f..100f,
                accentColor = Color(0xFF2F78FF),
                onValueChange = { volume = it },
                onValueChangeFinished = { onVolumeChangeFinished(volume.toInt()) })

            Spacer(modifier = Modifier.height(4.dp))

            BeautifulControlSlider(
                icon = Icons.Rounded.BrightnessMedium,
                title = "亮度",
                value = brightness,
                valueRange = 0f..100f,
                accentColor = Color(0xFFFF9800),
                onValueChange = { brightness = it },
                onValueChangeFinished = { onBrightnessChangeFinished(brightness.toInt()) })

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton("开屏", Icons.Rounded.Power, MaterialTheme.colorScheme.primary) {
                    onActionClick(ActionType.SCREEN_ON)
                }
                QuickActionButton("关屏", Icons.Rounded.PowerOff, MaterialTheme.colorScheme.error) {
                    onActionClick(ActionType.SCREEN_OFF)
                }
                QuickActionButton("截屏", Icons.Rounded.Screenshot, MaterialTheme.colorScheme.secondary) {
                    onActionClick(ActionType.SCREENSHOT)
                }
                QuickActionButton("重启", Icons.Rounded.RestartAlt, MaterialTheme.colorScheme.tertiary) {
                    onActionClick(ActionType.REBOOT)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BeautifulControlSlider(
    icon: ImageVector,
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333),
            modifier = Modifier.width(36.dp)
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished,
            interactionSource = interactionSource,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .shadow(4.dp, CircleShape, spotColor = accentColor)
                        .background(Color.White, CircleShape)
                        .border(1.5.dp, accentColor.copy(alpha = 0.3f), CircleShape)
                )
            },
            track = { sliderState ->
                val fraction =
                    (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(accentColor)
                    )
                }
            })

        Box(
            modifier = Modifier
                .width(42.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF5F7FA))
                .border(1.dp, Color(0xFFE8ECEF), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toInt().toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String, icon: ImageVector, tint: Color, onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = tint)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}


data class PublishMenuItem(
    val title: String, val icon: ImageVector, val onClick: () -> Unit
)

@Composable
 fun PublishManagementSection(
    menuItems: List<PublishMenuItem>
) {
    if (menuItems.isEmpty()) return
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            menuItems.forEachIndexed { index, item ->
                PublishItemRow(
                    title = item.title, icon = item.icon, onClick = item.onClick
                )

                if (index < menuItems.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PublishItemRow(
    title: String, icon: ImageVector, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight, contentDescription = "进入", tint = Color.Gray
        )
    }
}

enum class ActionType {
    SCREEN_ON, SCREEN_OFF, SCREENSHOT, REBOOT
}