package com.unilumin.smartapp.ui.screens.dialog

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
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
import com.unilumin.smartapp.client.data.SequenceTsl
import com.unilumin.smartapp.ui.components.HeaderSection
import com.unilumin.smartapp.ui.components.HistoryDataView
import com.unilumin.smartapp.ui.components.InfoRibbon
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * @param selectedDeviceModelData 当前选中数据的元数据
 * @param limitDays 限制时间选择区间最大天数
 * @param data 订阅的历史数据
 * @param isLoading 是否正在加载数据
 * @param onLoadData 刷新数据接口
 * */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChartDataDialog(
    selectedDeviceModelData: DeviceModelData?,
    onDismiss: () -> Unit,
    limitDays: Int,
    data: List<SequenceTsl>,
    isLoading: Boolean = false,
    onLoadData: (String, String) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    var startDate by remember { mutableStateOf(LocalDate.now().plusDays(-7).format(formatter)) }
    var endDate by remember { mutableStateOf(LocalDate.now().format(formatter)) }
    var isInitializing by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        onLoadData(startDate, endDate)
        isInitializing = false
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
                HeaderSection(selectedDeviceModelData?.name + "-历史数据", onDismiss)
                if (selectedDeviceModelData != null) {
                    InfoRibbon(selectedDeviceModelData)
                }
                Divider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val safeIsLoading = isLoading || isInitializing
                    val isChartEnabled =
                        selectedDeviceModelData?.type == "long" || selectedDeviceModelData?.type == "double"
                    HistoryDataView(
                        isLoading = safeIsLoading,
                        showChart = isChartEnabled,
                        limitDays = limitDays,
                        startDate = startDate,
                        endDate = endDate,
                        data = data,
                        onRangeSelected = { start, end ->
                            startDate = start
                            endDate = end
                            onLoadData(start, end)
                        })
                }
            }
        }
    }
}