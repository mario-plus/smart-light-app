package com.unilumin.smartapp.ui.screens.application.playBox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SettingsRemote
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.data.LedCommandReq
import com.unilumin.smartapp.client.data.LedGroupLogBO
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.InfoRowItem
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.theme.Green50
import com.unilumin.smartapp.ui.theme.Green500
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.theme.Red500
import com.unilumin.smartapp.ui.viewModel.ScreenViewModel

/**
 * 智慧屏幕控制页
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLedGroupContent(
    screenViewModel: ScreenViewModel, onBack: () -> Unit
) {
    // 播放盒分组信息
    val selectLedGroup by screenViewModel.selectLedGroup.collectAsState()
    val ledDevFuncMaps by screenViewModel.ledDevFuncMaps.collectAsState()

    // 播放盒分组日志
    val ledGroupLogPagingFlow = screenViewModel.ledGroupLogPagingFlow.collectAsLazyPagingItems()

    // 引入协程作用域（用于延迟弹窗，避开窗口焦点冲突）
    val scope = rememberCoroutineScope()

    // 底部弹窗状态控制
    var showLogSheet by remember { mutableStateOf(false) }
    // 推荐设置为 true，避免部分机型测量高度异常导致只弹出一半
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 滚动状态
    val scrollState = rememberScrollState()
    val publishMenuItems = remember(ledDevFuncMaps) {
        val items = mutableListOf<PublishMenuItem>()
        items.add(PublishMenuItem("操作日志", Icons.Rounded.ListAlt) {
            ledGroupLogPagingFlow.refresh()
            showLogSheet = true
        })
        if (ledDevFuncMaps.containsKey("programPublic")) {
            items.add(PublishMenuItem("发布播放表", Icons.Rounded.ListAlt) {})
        }
        if (ledDevFuncMaps.containsKey("sendSchedule")) {
            items.add(PublishMenuItem("发布播放方案", Icons.Rounded.PlayCircleOutline) {})
            items.add(PublishMenuItem("发布控制方案", Icons.Rounded.SettingsRemote) {})
        }
        items.toList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CommonTopAppBar(
                    title = selectLedGroup?.name ?: "",
                    subTitle = "播放盒分组",
                    onBack = { onBack() },
                )
            },
            containerColor = PageBackground
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                RemoteControlSection(
                    initialVolume = 0,
                    initialBrightness = 0,
                    onVolumeChangeFinished = { newVolume ->
                        screenViewModel.ledCommand(
                            LedCommandReq(
                                groupId = selectLedGroup?.id,
                                type = 12,
                                value = newVolume
                            )
                        )
                    },
                    onBrightnessChangeFinished = { newBrightness ->
                        screenViewModel.ledCommand(
                            LedCommandReq(
                                groupId = selectLedGroup?.id,
                                type = 4,
                                value = newBrightness
                            )
                        )
                    },
                    onActionClick = { actionType ->
                        when (actionType) {
                            ActionType.SCREEN_ON -> screenViewModel.ledCommand(
                                LedCommandReq(groupId = selectLedGroup?.id, type = 2, value = 0)
                            )

                            ActionType.SCREEN_OFF -> screenViewModel.ledCommand(
                                LedCommandReq(groupId = selectLedGroup?.id, type = 1, value = 0)
                            )

                            ActionType.SCREENSHOT -> screenViewModel.ledCommand(
                                LedCommandReq(groupId = selectLedGroup?.id, type = 5, value = 0)
                            )

                            ActionType.REBOOT -> screenViewModel.ledCommand(
                                LedCommandReq(groupId = selectLedGroup?.id, type = 3, value = 0)
                            )
                        }
                    })
                // 3. 动态发布管理区域
                PublishManagementSection(menuItems = publishMenuItems)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showLogSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLogSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(50)
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "执行日志",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxHeight(0.8f)) {
                    PagingList(
                        lazyPagingItems = ledGroupLogPagingFlow,
                        emptyMessage = "暂无执行日志记录",
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 0.dp,
                            bottom = 32.dp
                        )
                    ) { logItem ->
                        if (logItem != null) {
                            LogItemCard(logItem)
                        }
                    }
                }
            }
        }
    } // Box 结束
}


@Composable
fun LogItemCard(log: LedGroupLogBO, modifier: Modifier = Modifier) {
    val isSuccess = log.state == 2
    // 动态配色方案
    val statusColor = if (isSuccess) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    val containerColor =
        if (isSuccess) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SettingsRemote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = log.deviceName ?: "未命名设备",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DeviceStatus(
                    log.state,
                    mapOf(
                        2 to Triple(Green50, Green500, "成功"),
                        1 to Triple(Red500, Red500, "失败")
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            InfoRowItem(
                icon = Icons.Rounded.Terminal,
                label = "指令类型",
                value = log.instructName ?: "未知指令"
            )

            if (!isSuccess && !log.errorName.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(containerColor, RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = statusColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.errorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LabelValueItem(icon = Icons.Rounded.Person, text = log.createName ?: "未知用户")
                LabelValueItem(icon = Icons.Rounded.Schedule, text = log.createTime ?: "--")
            }
        }
    }
}

/**
 * 底部小标签
 */
@Composable
private fun LabelValueItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}