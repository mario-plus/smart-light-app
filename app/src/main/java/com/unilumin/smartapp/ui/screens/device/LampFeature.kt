package com.unilumin.smartapp.ui.screens.device

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.constant.DeviceType.colorTempSupportedList
import com.unilumin.smartapp.client.data.LampCtlReq
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.service.DeviceService
import com.unilumin.smartapp.ui.components.InfoColumn
import com.unilumin.smartapp.ui.components.RemoteControlButton
import com.unilumin.smartapp.ui.components.VerticalDivider
import com.unilumin.smartapp.ui.screens.dialog.DeviceControlDialog
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray50
import kotlinx.coroutines.launch
import retrofit2.Call

@SuppressLint("DefaultLocale", "UnusedBoxWithConstraintsScope")
@Composable
fun LampFeatureContent(
    lightDevice: LightDevice, retrofitClient: RetrofitClient
) {
    val deviceService = remember(retrofitClient) {
        retrofitClient.getService(DeviceService::class.java)
    }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    //设备控制按钮
    suspend fun lampCtl(cmdType: Int, cmdValue: Int) {
        try {
            val call: Call<NewResponseData<String?>?>? = deviceService.lampCtl(
                LampCtlReq(
                    cmdType = cmdType,
                    cmdValue = cmdValue,
                    ids = listOf(lightDevice.id),
                    subSystemType = 1
                )
            )
            UniCallbackService<String>().parseDataNewSuspend(call, context)
            Toast.makeText(context, "操作成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    FeatureContentContainer {
        Column(modifier = Modifier.fillMaxWidth()) {
            BoxWithConstraints {
                val isWideScreen = maxWidth > 600.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gray50, RoundedCornerShape(12.dp))
                        .border(1.dp, Gray100, RoundedCornerShape(12.dp))
                        .then(if (!isWideScreen) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = if (isWideScreen) Arrangement.SpaceEvenly else Arrangement.spacedBy(
                        16.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. 开关状态
                    val powerState = when(lightDevice.onOff) {
                        1 -> "开"
                        0 -> "关"
                        else -> "--"
                    }
                    InfoColumn("开关", powerState, isHighlight = true)
                    VerticalDivider()
                    // 2. 亮度
                    val brightness = lightDevice.bright1?.let { "$it%" } ?: "--"
                    InfoColumn("亮度", brightness, isHighlight = true)
                    // 3. 色温 (仅在产品列表中才显示)
                    if (lightDevice.productId in colorTempSupportedList) {
                        VerticalDivider()
                        val colorTemp = lightDevice.bright2?.let { "$it%" } ?: "--"
                        InfoColumn("色温", colorTemp, isHighlight = true)
                    }
                    VerticalDivider()
                    // 4. 电压 (处理 Float/Double 的格式化)
                    val voltageStr = lightDevice.voltage?.let { String.format("%.1fV", it) } ?: "--"
                    InfoColumn("电压", voltageStr)
                    VerticalDivider()
                    // 5. 电流
                    val currentStr = lightDevice.current?.let { String.format("%.1fmA", it) } ?: "--"
                    InfoColumn("电流", currentStr)
                    VerticalDivider()
                    // 6. 功率
                    val powerStr = lightDevice.power?.let { String.format("%.1fW", it) } ?: "--"
                    InfoColumn("功率", powerStr)
                    VerticalDivider()
                    // 7. 功率因子
                    val factorStr = lightDevice.factor?.let { String.format("%.1f", it) } ?: "--"
                    InfoColumn("功率因子", factorStr)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (lightDevice.state == 1) {
                RemoteControlButton(
                    canClick = true, onClick = {
                        showDialog = true
                    })
            }

        }
    }
    if (showDialog) {
        DeviceControlDialog(
            lightDevice.productId,
            lightDevice.name,
            initialBrightness = lightDevice.bright1,
            initColorT = lightDevice.bright2,
            onDismiss = { showDialog = false },
            onClick = { a, b ->
                scope.launch { lampCtl(a, b) }
            })
    }
}



