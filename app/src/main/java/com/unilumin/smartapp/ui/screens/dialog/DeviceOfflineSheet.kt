package com.unilumin.smartapp.ui.screens.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.data.OfflineDevice
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.StatusChip
import com.unilumin.smartapp.ui.screens.dashboard.InfoRowItem
import com.unilumin.smartapp.ui.theme.AlarmBg
import com.unilumin.smartapp.ui.theme.AlarmRed
import com.unilumin.smartapp.ui.theme.CardBorder
import com.unilumin.smartapp.ui.theme.OfflineGray
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.theme.PrimaryBlue
import com.unilumin.smartapp.ui.theme.SafeBg
import com.unilumin.smartapp.ui.theme.SafeGreen
import com.unilumin.smartapp.ui.theme.TextDark
import com.unilumin.smartapp.ui.theme.TextSecondary
import com.unilumin.smartapp.ui.theme.TextTitle
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
@Composable
fun TimeFilterSegment(selectedType: Int, onTypeSelected: (Int) -> Unit) {
    val options = listOf(0 to "最近活跃时间", 1 to "最近7天", 2 to "最近30天", 3 to "最近90天")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8EAF6), RoundedCornerShape(12.dp))
            .padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (type, label) ->
            val isSelected = selectedType == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onTypeSelected(type) }, contentAlignment = Alignment.Center
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
    device: OfflineDevice, onClick: () -> Unit = {}
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
                            .background(Color(0xFFF5F7FA)), contentAlignment = Alignment.Center
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
                    icon = Icons.Default.QrCode, label = "SN:", value = device.serialNum
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