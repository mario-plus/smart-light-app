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
import androidx.compose.material.icons.rounded.Devices
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.data.LedDevGroupRes
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.InfoRowItem
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Green50
import com.unilumin.smartapp.ui.theme.Green500
import com.unilumin.smartapp.ui.viewModel.ScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLedGroupManage(
    screenViewModel: ScreenViewModel
) {
    val searchQuery by screenViewModel.searchQuery.collectAsState()
    val totalCount by screenViewModel.totalCount.collectAsState()
    val ledGroupPagingFlow = screenViewModel.ledGroupPagingFlow.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SearchHeader(
            searchQuery = searchQuery,
            searchTitle = "搜索分组名称或备注信息",
            onSearchChanged = { screenViewModel.updateSearch(it) }
        )

        PagingList(
            totalCount = totalCount,
            lazyPagingItems = ledGroupPagingFlow,
            itemKey = { it.id },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            emptyMessage = "暂无分组信息",
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) { ledGroup ->
            LedGroupCard(ledGroup = ledGroup as LedDevGroupRes)
        }
    }
}

/**
 * 分组信息卡片组件
 */
@Composable
fun LedGroupCard(ledGroup: LedDevGroupRes) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ledGroup.name ?: "未命名分组",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                )
                DeviceStatus(
                    ledGroup.groupState, mapOf(
                        0 to Triple(Green50, Green500, "正常"),
                        1 to Triple(Gray100, Gray500, "异常")
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // --- 主体信息区块：浅色底纹包裹详情 ---
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
                    // 所属产品：优先取 productName，为空则取 primaryClassName
                    InfoRowItem(
                        icon = Icons.Rounded.Devices,
                        label = "产品",
                        value = ledGroup.productName.toString()
                    )

                    // 将创建者和创建时间组合显示，视觉更紧凑
                    val creatorInfo =
                        "${ledGroup.createName ?: "-"}  ·  ${ledGroup.createTime ?: "-"}"
                    InfoRowItem(
                        icon = Icons.Rounded.AccountCircle,
                        label = "创建",
                        value = creatorInfo
                    )
                }
            }
        }
    }
}