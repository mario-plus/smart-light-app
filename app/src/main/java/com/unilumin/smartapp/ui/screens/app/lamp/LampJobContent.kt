package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.client.data.LampJobInfo
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.components.GridMultiSelectBar
import com.unilumin.smartapp.ui.theme.CardBgColor
import com.unilumin.smartapp.ui.theme.DividerColor
import com.unilumin.smartapp.ui.theme.FailColor
import com.unilumin.smartapp.ui.theme.ProcessingColor
import com.unilumin.smartapp.ui.theme.SuccessColor
import com.unilumin.smartapp.ui.theme.TextPrimary
import com.unilumin.smartapp.ui.theme.TextSecondary
import com.unilumin.smartapp.ui.viewModel.LampViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampJobContent(
    lampViewModel: LampViewModel
) {
    val sceneOptions = lampViewModel.sceneOptions.collectAsState()
    val sceneSelectIds = lampViewModel.selectSceneIds.collectAsState()


    val lampJobFlow = lampViewModel.lampJobFlow.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
        lampViewModel.getJobScene()
    }

    BaseLampListScreen(
        statusOptions = DeviceConstant.jobOrStrategyStatusOptions,
        viewModel = lampViewModel,
        pagingItems = lampJobFlow,
        //TODO 接口有问题，下拉分页查询，出现重复数据，会直接导致app崩溃 keySelector = { it.id }
        keySelector = null,
        searchTitle = "搜索业务对象名称",
        middleContent = {
            GridMultiSelectBar(
                title = "全部场景",
                options = sceneOptions.value,          // 传入 Pair 列表
                selectedKeys = sceneSelectIds.value, // 传入 Key 集合
                onSelectionChanged = { newIds -> // 回调 Key 集合
                    lampViewModel.updateSceneIds(newIds)
                }
            )
        }
    ) { item ->
        LampJobItem(sceneOptions.value, item, onItemClick = {})
    }
}


@Composable
fun LampJobItem(
    sceneOptions: List<Pair<Int, String>>,
    item: LampJobInfo?, // 1. 修改这里：允许 item 为 null (应对 Paging 占位符)
    modifier: Modifier = Modifier,
    onItemClick: (LampJobInfo) -> Unit = {}
) {
    // 2. 如果 item 为空（正在加载下一页），显示一个骨架屏或者空白占位
    if (item == null) {
        // 这里可以放一个 Card 占位，或者直接 Spacer
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 3.dp)
                .height(100.dp), // 给个默认高度
            colors = CardDefaults.cardColors(containerColor = CardBgColor),
            shape = RoundedCornerShape(12.dp)
        ) {}
        return
    }

    // --- 以下是原本的逻辑，但加上了字段判空 ---

    val (statusColor, statusText) = remember(item.status) {
        getStatusVisuals(item.status)
    }

    // 3. 安全获取业务类型名称
    val businessTypeText = remember(item.businessType, sceneOptions) {
        sceneOptions.find { it.first == item.businessType }?.second ?: "未知类型"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .clickable { onItemClick(item) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 4. 业务对象名称判空 (Crash 高发点)
                Text(
                    text = item.businessName ?: "未知对象", // 加上 ?:
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(text = statusText, color = statusColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 标签行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoTag(text = businessTypeText, color = Color(0xFF42A5F5))
                Spacer(modifier = Modifier.weight(1f))
                // 5. 执行次数判空
                Text(
                    text = "执行次数:${item.tryNum ?: 0}", // 加上 ?:
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 任务名称
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Devices,
                    contentDescription = null,
                    tint = TextPrimary.copy(alpha = 0.9f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                // 6. 任务名称判空
                Text(
                    text = item.name ?: "未命名任务", // 加上 ?:
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 7. 时间字段判空 (Crash 高发点)
                DateInfoItem(
                    label = "创建时间",
                    time = item.createDate
                )
                if (item.exeDate.isNotEmpty()) { // 使用 isNullOrEmpty 更安全
                    DateInfoItem(
                        label = "执行时间",
                        time = item.exeDate
                    )
                }
            }
        }
    }
}

// --- 辅助组件 ---

@Composable
fun StatusBadge(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50)) // 半透明背景
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoTag(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun DateInfoItem(
    label: String,
    time: String?, // 1. 修改这里：参数改为可空 String?
    icon: ImageVector? = null
) {
    // 2. 如果时间为空，给默认空字符串
    val safeTime = time ?: ""

    // 3. 安全截取字符串，防止 StringIndexOutOfBoundsException
    val displayTime = if (safeTime.length > 16) {
        safeTime.substring(5, 16).replace("T", " ")
    } else {
        safeTime
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        } else {
            Text(
                text = "$label ",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.7f)
            )
        }
        Text(
            text = displayTime,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}


private fun getStatusVisuals(status: Int): Pair<Color, String> {
    return when (status) {
        1 -> TextSecondary to "待执行"
        2 -> ProcessingColor to "执行中"
        3 -> SuccessColor to "成功"
        4 -> FailColor to "失败"
        else  -> TextSecondary to "未知"
    }
}





