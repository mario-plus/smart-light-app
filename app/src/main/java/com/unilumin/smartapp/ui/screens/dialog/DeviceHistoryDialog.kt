package com.unilumin.smartapp.ui.screens.dialog

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.unilumin.smartapp.client.data.DeviceModelData
import com.unilumin.smartapp.client.data.HistoryData
import com.unilumin.smartapp.ui.components.EmptyDataView
import com.unilumin.smartapp.ui.components.HeaderSection
import com.unilumin.smartapp.ui.components.HistoryDataListView
import com.unilumin.smartapp.ui.components.InfoRibbon
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 遥测，属性的历史数据
 * */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeviceHistoryDialog(
    selectedDeviceModelData: DeviceModelData?,
    historyDataList: List<HistoryData>, // 接收外部传入的数据源
    hasMore: Boolean,                   // 接收外部传入的分页状态
    onLoadData: (String, String, Boolean, List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    var startDate by remember { mutableStateOf(LocalDate.now().plusDays(-7).format(formatter)) }
    var endDate by remember { mutableStateOf(LocalDate.now().format(formatter)) }

    // 弹窗启动时，根据选中的 key 触发加载
    LaunchedEffect(selectedDeviceModelData) {
        selectedDeviceModelData?.key?.let { key ->
            onLoadData(startDate, endDate, true, listOf(key))
        }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
                .padding(vertical = 16.dp),
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HeaderSection("历史数据详情",onDismiss)
                if (selectedDeviceModelData != null) {
                    InfoRibbon(selectedDeviceModelData)
                }
                // 这里渲染的是父组件传入的共享列表
                HistoryDataListView(
                    limitDays = 14,
                    startDate = startDate,
                    endDate = endDate,
                    historyDataList = historyDataList,
                    hasMore = hasMore,
                    onRangeSelected = { start, end ->
                        startDate = start
                        endDate = end
                        selectedDeviceModelData?.key?.let {
                            onLoadData(start, end, true, listOf(it))
                        }
                    },
                    onLoadMore = { start, end ->
                        selectedDeviceModelData?.key?.let {
                            onLoadData(start, end, false, listOf(it))
                        }
                    }
                )

                if (historyDataList.isEmpty()) {
                    EmptyDataView("暂无数据")
                }
            }
        }
    }
}