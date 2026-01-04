package com.unilumin.smartapp.ui.screens.device

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.client.data.LoopCtlReq
import com.unilumin.smartapp.client.data.NewResponseData
import com.unilumin.smartapp.client.service.DeviceService
import com.unilumin.smartapp.ui.components.LoopCircleItem
import com.unilumin.smartapp.ui.components.RemoteControlButtonGroup
import com.unilumin.smartapp.ui.screens.dialog.LoopControlDialog
import com.unilumin.smartapp.ui.theme.Gray400
import kotlinx.coroutines.launch
import retrofit2.Call

/**
 * 回路 (Loop) 特有内容
 */
@Composable
fun LoopFeatureContent(
    lightDevice: LightDevice,
    retrofitClient: RetrofitClient,
    onDetailClick: (LightDevice) -> Unit
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val deviceService = remember(retrofitClient) {
        retrofitClient.getService(DeviceService::class.java)
    }
    var showDialog by remember { mutableStateOf(false) }


    //设备控制按钮
    suspend fun loopCtl(id: Long, numList: List<Int>, onOff: Int) {
        try {
            val call: Call<NewResponseData<String?>?>? = deviceService.loopCtl(
                LoopCtlReq(listOf(id), numList, onOff)
            )
            UniCallbackService<String>().parseDataNewSuspend(call, context)
            Toast.makeText(context, "操作成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    FeatureContentContainer {
        val loops = lightDevice.loops ?: emptyList()
        if (loops.isNotEmpty()) {
            @OptIn(ExperimentalLayoutApi::class) FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 8
            ) {
                loops.forEach { loop ->
                    LoopCircleItem(loop)
                }
                Spacer(modifier = Modifier.height(12.dp))

                RemoteControlButtonGroup(
                    canClick = lightDevice.state == 1,
                    showRemoteCtlBtn = true,
                    onRemoteControlClick = {
                        showDialog = true
                    },
                    onHistoryClick = { onDetailClick(lightDevice) }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无回路信息", fontSize = 12.sp, color = Gray400)
            }
        }
    }
    if (showDialog) {
        LoopControlDialog(
            deviceName = lightDevice.name,
            loopInfos = lightDevice.loops,
            onDismiss = { showDialog = false }, onConfirm = { action, loops ->
                scope.launch { loopCtl(lightDevice.id, loops, action) }
                showDialog = false
            })
    }
}

