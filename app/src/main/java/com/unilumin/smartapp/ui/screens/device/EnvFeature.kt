import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.EnvData
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.client.service.DeviceService
import com.unilumin.smartapp.ui.components.RemoteControlButton
import com.unilumin.smartapp.ui.screens.device.FeatureContentContainer
import com.unilumin.smartapp.ui.screens.dialog.EnvDataDialog
import kotlinx.coroutines.launch

@Composable
fun EnvFeatureContent(lightDevice: LightDevice, retrofitClient: RetrofitClient) {
    val deviceService = remember(retrofitClient) {
        retrofitClient.getService(DeviceService::class.java)
    }
    var showDialog by remember { mutableStateOf(false) }
    var envData by remember { mutableStateOf<EnvData?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    FeatureContentContainer {
        Column(modifier = Modifier.fillMaxWidth()) {
            RemoteControlButton(
                text = "查看环境数据",
                canClick = true,
                onClick = {
                    showDialog = true
                    isLoading = true
                    scope.launch {
                        try {
                            val result = UniCallbackService<EnvData>().parseDataNewSuspend(
                                deviceService.getEnvData(lightDevice.id),
                                context
                            )
                            envData = result
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
        }
    }

    if (showDialog) {
        EnvDataDialog(
            data = envData,
            isLoading = isLoading,
            onDismiss = {
                showDialog = false
                envData = null // 关闭时重置数据
            }
        )
    }
}