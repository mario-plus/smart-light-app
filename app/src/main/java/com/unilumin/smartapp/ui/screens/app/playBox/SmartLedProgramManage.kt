package com.unilumin.smartapp.ui.screens.app.playBox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.OndemandVideo
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant.ledProgramStatusOptions
import com.unilumin.smartapp.client.data.LedProgramRes
import com.unilumin.smartapp.ui.components.InfoRowItem
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader
import com.unilumin.smartapp.ui.viewModel.ScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLedProgramManage(
    screenViewModel: ScreenViewModel
) {

    val checkState by screenViewModel.checkState.collectAsState()
    val searchQuery by screenViewModel.searchQuery.collectAsState()
    val totalCount by screenViewModel.totalCount.collectAsState()
    val ledProgramPagingFlow = screenViewModel.ledProgramPagingFlow.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SearchHeader(
            statusOptions = ledProgramStatusOptions,
            currentStatus = checkState,
            searchQuery = searchQuery,
            searchTitle = "搜索播放表名称",
            onStatusChanged = { screenViewModel.updateCheckState(it) },
            onSearchChanged = { screenViewModel.updateSearch(it) })
        PagingList(
            totalCount = totalCount,
            lazyPagingItems = ledProgramPagingFlow,
            itemKey = { it.id },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            emptyMessage = "暂无播放表信息",
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) { ledProgram ->
            LedProgramCard(ledProgram = ledProgram)
        }
    }
}

@Composable
fun LedProgramCard(ledProgram: LedProgramRes) {
    val (statusText, statusTextColor, statusBgColor) = when (ledProgram.reviewStatus) {
        0 -> Triple("待审核", Color(0xFFE6A23C), Color(0xFFFDF6EC)) // 橙色/警告
        1 -> Triple("审核通过", Color(0xFF67C23A), Color(0xFFF0F9EB)) // 绿色/成功
        2 -> Triple("审核不通过", Color(0xFFF56C6C), Color(0xFFFEF0F0)) // 红色/错误
        else -> Triple("未知状态", Color(0xFF909399), Color(0xFFF4F4F5)) // 灰色/默认
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // --- 顶部：标题与状态 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ledProgram.name.toString(), // 替换为真实字段
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                )

                Surface(
                    color = statusBgColor, shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusTextColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    val resolution = "${ledProgram.width} × ${ledProgram.height}"
                    InfoRowItem(
                        icon = Icons.Rounded.OndemandVideo, label = "分辨率", value = resolution
                    )
                    val creatorInfo = "${ledProgram.createByName}  ·  ${ledProgram.createTime}"
                    InfoRowItem(
                        icon = Icons.Rounded.AccountCircle, label = "创建", value = creatorInfo
                    )

                    val reviewerInfo = "${ledProgram.reviewUser}  ·  ${ledProgram.reviewTime}"
                    InfoRowItem(
                        icon = Icons.Rounded.VerifiedUser, label = "审核", value = reviewerInfo
                    )
                    val remark = ledProgram.remark
                    if (!remark.isNullOrBlank()) {
                        InfoRowItem(
                            icon = Icons.Rounded.EditNote,
                            label = "备注",
                            value = remark
                        )
                    }
                }
            }
        }
    }
}

