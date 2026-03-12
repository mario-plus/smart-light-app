package com.unilumin.smartapp.ui.screens.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.ui.components.OfflineDeviceItem
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.TimeFilterSegment
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.theme.TextDark
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineDeviceDetailSheet(
    deviceViewModel: DeviceViewModel,
    onDismiss: () -> Unit,
) {

    val lazyPagingItems = deviceViewModel.offlineDeviceList.collectAsLazyPagingItems()

    val totalCount = deviceViewModel.totalCount.collectAsState()

    val timeType = deviceViewModel.chartType.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PageBackground, // 使用您定义的淡蓝色背景
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f) // 占据屏幕 85% 高度
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "离线设备详情",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(16.dp))
                TimeFilterSegment(
                    selectedType = timeType.value,
                    onTypeSelected = { e -> deviceViewModel.updateChartType(e) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // --- 列表区域 ---
            PagingList(
                totalCount = totalCount.value,
                lazyPagingItems = lazyPagingItems,
                modifier = Modifier.weight(1f),
                itemKey = { it.id },
                emptyMessage = "未找到相关设备",
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
            ) { device ->
                OfflineDeviceItem(device)
            }
        }
    }
}