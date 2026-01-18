import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_GATEWAY
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_GROUP
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_LIGHT
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_LOOP
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_STRATEGY
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LIGHT_GATEWAY
import com.unilumin.smartapp.client.constant.DeviceConstant.getSmartAppName
import com.unilumin.smartapp.client.data.SystemConfig
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.EmptyDataView
import com.unilumin.smartapp.ui.screens.app.lamp.LampGatewayContent
import com.unilumin.smartapp.ui.screens.app.lamp.LampGroupContent
import com.unilumin.smartapp.ui.screens.app.lamp.LampLightContent
import com.unilumin.smartapp.ui.screens.app.lamp.LampLightGwContent
import com.unilumin.smartapp.ui.screens.app.lamp.LampLoopCtlContent
import com.unilumin.smartapp.ui.screens.app.lamp.LampStrategyContent
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.Gray50
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.LampViewModel
import com.unilumin.smartapp.ui.viewModel.SystemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLampScreen(
    retrofitClient: RetrofitClient, onBack: () -> Unit
) {
    val context = LocalContext.current

    val application = context.applicationContext as Application
    // 初始化 ViewModel
    val systemViewModel: SystemViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SystemViewModel(retrofitClient, application) as T
        }
    })

    val lampViewModel: LampViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LampViewModel(retrofitClient, application) as T
        }
    })


    // 获取功能列表
    val lampFunctions by systemViewModel.lampFunctions.collectAsState()
    var currentFunctionId by remember(lampFunctions) {
        mutableStateOf(
            lampFunctions.firstOrNull { it.isSelected }?.id ?: SMART_LAMP_LIGHT
        )
    }

    // 动态获取当前页面的标题（可选，如果标题需要跟随功能变化）
    val currentTitle = remember(currentFunctionId) {
        lampFunctions.find { it.id == currentFunctionId }?.name ?: getSmartAppName(SMART_LAMP)
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Column(modifier = Modifier.background(CardWhite)) {
                    CommonTopAppBar(
                        title = currentTitle,
                        onBack = { onBack() },
                        menuItems = lampFunctions,
                        onMenuItemClick = { systemConfig ->
                            currentFunctionId = systemConfig.id
                        })
                }
            }
        }, containerColor = PageBackground
    ) { padding ->
        // 3. 【核心修改】根据状态 ID 渲染不同的内容
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentFunctionId) {
                // 单灯管理页面
                SMART_LAMP_LIGHT -> {
                    LampLightContent(lampViewModel)
                }
                // 集中控制器
                SMART_LAMP_GATEWAY -> {
                    LampGatewayContent(lampViewModel)
                }
                // 回路控制器
                SMART_LAMP_LOOP -> {
                    LampLoopCtlContent(lampViewModel)
                }

                SMART_LIGHT_GATEWAY -> {
                    LampLightGwContent(lampViewModel)
                }

                SMART_LAMP_GROUP -> {
                    LampGroupContent(lampViewModel)
                }

                SMART_LAMP_STRATEGY -> {
                    LampStrategyContent(lampViewModel)
                }

                else -> {
                    EmptyDataView("未开发的功能")
                }
            }
        }
    }
}
