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
                    InfoColumn(
                        "开关", if (lightDevice.onOff == 1) "开" else "关", isHighlight = true
                    )
                    VerticalDivider()
                    InfoColumn("亮度", "${lightDevice.bright1}%", isHighlight = true)
                    VerticalDivider()
                    InfoColumn("色温", "${lightDevice.bright2}%", isHighlight = true)
                    VerticalDivider()
                    InfoColumn("电压", "${String.format("%.1f", lightDevice.voltage)}V")
                    VerticalDivider()
                    InfoColumn("电流", "${String.format("%.1f", lightDevice.current)}mA")
                    VerticalDivider()
                    InfoColumn("功率", "${String.format("%.1f", lightDevice.power)}W")
                    VerticalDivider()
                    InfoColumn("功率因子", String.format("%.1f", lightDevice.factor))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (lightDevice.state == 1) {
                //离线隐藏控制按钮
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