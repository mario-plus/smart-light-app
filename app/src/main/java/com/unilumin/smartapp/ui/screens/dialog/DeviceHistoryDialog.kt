package com.unilumin.smartapp.ui.screens.dialog

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.HistoryData
import com.unilumin.smartapp.client.data.HistoryDataReq
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.service.DeviceService
import com.unilumin.smartapp.ui.components.HistoryDataListView
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 遥测，属性的历史数据
 * */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeviceHistoryDialog(
    deviceId: Long,
    keys: List<String>,
    deviceService: DeviceService,
    onDismiss: () -> Unit // 补充关闭事件回调
) {
    val scope = rememberCoroutineScope()
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(LocalDate.now().plusDays(-7).format(formatter)) }
    var endDate by remember { mutableStateOf(LocalDate.now().format(formatter)) }
    val historyDataList = remember { mutableStateListOf<HistoryData>() }
    var pageIndex by remember { mutableIntStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }

    suspend fun loadHistoryData(
        startTime: String,
        endTime: String,
        isRefresh: Boolean = false,
        keys: List<String>
    ) {
        if (isRefresh) {
            pageIndex = 1
            historyDataList.clear()
            hasMore = true
        }
        if (!hasMore) return
        isLoading = true
        try {
            var format = LocalDateTime.now().format(timeFormat)
            var start: String? = null
            var end: String? = null
            if (startTime.isNotBlank()) {
                start = "$startTime $format"
            }
            if (endTime.isNotBlank()) {
                end = "$endTime $format"
            }
            val response = UniCallbackService<PageResponse<HistoryData>>().parseDataNewSuspend(
                deviceService.getDeviceHistoryData(
                    HistoryDataReq(
                        deviceIds = listOf(deviceId.toString()),
                        startTime = start,
                        endTime = end,
                        keys = keys,
                        curPage = pageIndex,
                        pageSize = 20
                    )
                ), context
            )
            val newList = response?.list ?: emptyList()
            val totalCount = response?.total ?: 0
            if (isRefresh) {
                historyDataList.clear()
            }
            historyDataList.addAll(newList)
            pageIndex++
            hasMore = newList.isNotEmpty() && historyDataList.size < totalCount
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(keys) {
        loadHistoryData(startTime = startDate, endTime = endDate, true, keys)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "历史记录查询",
                        modifier = Modifier.align(Alignment.CenterStart),
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                    )
                    androidx.compose.material3.IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(24.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.Gray
                        )
                    }
                }

                HistoryDataListView(
                    limitDays = 14,
                    startDate = startDate,
                    endDate = endDate,
                    historyDataList,
                    hasMore = hasMore,
                    onRangeSelected = { start, end ->
                        startDate = start
                        endDate = end
                        scope.launch {
                            loadHistoryData(
                                start,
                                end,
                                isRefresh = true,
                                keys = keys
                            )
                        }
                    },
                    onLoadMore = { start, end ->
                        scope.launch {
                            loadHistoryData(
                                start,
                                end,
                                isRefresh = false,
                                keys = keys
                            )
                        }
                    }
                )
            }
        }
    }
}