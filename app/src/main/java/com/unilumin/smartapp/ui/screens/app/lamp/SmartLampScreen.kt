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
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_LIGHT
import com.unilumin.smartapp.client.constant.DeviceConstant.getSmartAppName
import com.unilumin.smartapp.client.data.SystemConfig
import com.unilumin.smartapp.ui.screens.app.lamp.LampGatewayContent
import com.unilumin.smartapp.ui.screens.app.lamp.LampLightContent
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.Gray50
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.SystemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLampScreen(
    retrofitClient: RetrofitClient, onBack: () -> Unit
) {
    val context = LocalContext.current

    // 初始化 ViewModel
    val systemViewModel: SystemViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SystemViewModel(retrofitClient, context) as T
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
                        title = currentTitle, // 标题可以动态变化，也可以固定
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
                    LampLightContent(retrofitClient)
                }
                // 这里扩展其他页面 ID
                SMART_LAMP_GATEWAY -> {
                    // GroupContent(retrofitClient)
                    LampGatewayContent(retrofitClient)
                }

                "SMART_LAMP_STRATEGY" -> {
                    EmptyContentPlaceholder("策略管理")
                }

                else -> {
                    EmptyContentPlaceholder("未开发的功能")
                }
            }
        }
    }
}


@Composable
fun EmptyContentPlaceholder(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("当前页面: $text")
    }
}

// --- 下面是你提供的 TopBar 代码，保持不变即可 ---

@Composable
fun CommonTopAppBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp, // 稍微调小一点，72dp有点太高了，标准通常是56或64
    menuItems: List<SystemConfig> = emptyList(),
    onMenuItemClick: (SystemConfig) -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val textMain = Color(0xFF1A1C1E)
    // 这里你需要确保 Gray50 已经定义，如果没有定义，暂时用 Color.LightGray 代替
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFF0F7FF), Gray50), startY = 0f, endY = 500f
    )

    Surface(
        modifier = modifier.fillMaxWidth(), color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(brush = gradientBrush)
                .padding(horizontal = 4.dp)
        ) {
            IconButton(
                onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = textMain,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = title,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain,
                    letterSpacing = 0.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.Center)
            )

            if (menuItems.isNotEmpty()) {
                Box(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = "Menu",
                            tint = textMain,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    ReferenceStyleDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        items = menuItems,
                        onItemClick = { systemConfig ->
                            menuExpanded = false
                            onMenuItemClick(systemConfig)
                        })
                }
            }
        }
    }
}

@Composable
fun ReferenceStyleDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<SystemConfig>,
    onItemClick: (SystemConfig) -> Unit
) {
    val menuShape = RoundedCornerShape(16.dp)
    MaterialTheme(shapes = Shapes(extraSmall = menuShape)) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onDismissRequest() },
            modifier = Modifier
                .widthIn(min = 150.dp)
                .shadow(
                    12.dp, menuShape, spotColor = Color.Black.copy(alpha = 0.2f)
                )
                .clip(menuShape)
                .background(Color.White)
                .border(0.5.dp, Color(0xFFF0F0F0), menuShape)
        ) {
            items.forEach { smartApp ->
                if (smartApp.isSelected) {
                    DropdownMenuItem(leadingIcon = {
                        Icon(
                            smartApp.icon, null, Modifier.size(20.dp), Color.Gray
                        )
                    }, text = {
                        Text(
                            smartApp.name, fontSize = 14.sp, fontWeight = FontWeight.Medium
                        )
                    }, onClick = { onItemClick(smartApp) })
                }
            }
        }
    }
}