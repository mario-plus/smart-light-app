package com.unilumin.smartapp.ui.screens.dialog

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.unilumin.smartapp.ui.components.ChartDataView
import com.unilumin.smartapp.ui.components.HeaderSection
import com.unilumin.smartapp.ui.components.InfoRibbon
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChartDataDialog(
    selectedDeviceModelData: DeviceModelData?,
    onDismiss: () -> Unit,
    limitDays: Int,
    data: List<SequenceTsl>,
    onLoadData: (String, String) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    var startDate by remember { mutableStateOf(LocalDate.now().plusDays(-7).format(formatter)) }
    var endDate by remember { mutableStateOf(LocalDate.now().format(formatter)) }

    LaunchedEffect(Unit) {
        onLoadData(startDate, endDate)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ){

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
                HeaderSection("数据分析详情",onDismiss)
                if (selectedDeviceModelData != null) {
                    InfoRibbon(selectedDeviceModelData)
                }
                Divider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                // 内容区域
                ChartDataView(
                    limitDays = limitDays,
                    startDate = startDate,
                    endDate = endDate,
                    data = data,
                    onRangeSelected = { start, end ->
                        startDate = start
                        endDate = end
                        onLoadData(start, end)
                    }
                )
            }
        }
    }
}