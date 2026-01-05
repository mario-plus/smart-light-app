
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.data.SystemFileInfo
import com.unilumin.smartapp.ui.components.DetailRow
import com.unilumin.smartapp.ui.components.LoadingContent
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.theme.TextDark
import com.unilumin.smartapp.ui.viewModel.ProfileViewModel
import com.unilumin.smartapp.ui.viewModel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemInfoScreen(
    retrofitClient: RetrofitClient,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ViewModelFactory {
            ProfileViewModel(retrofitClient, context)
        }
    )

    // 使用 collectAsState 观察数据流
    val systemInfo by profileViewModel.systemInfo.collectAsState()
    // 观察 ViewModel 状态
    val isLoading by profileViewModel.isLoading.collectAsState()


    LaunchedEffect(Unit) {
        profileViewModel.getSystemInfo()
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Column(modifier = Modifier.background(CardWhite)) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "系统资源监控", style = TextStyle(
                                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "返回",
                                    tint = TextDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                }
            }
        }, containerColor = PageBackground
    ) { padding ->
        LoadingContent(isLoading = isLoading) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (systemInfo.system != null) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardPieCard(
                                "CPU 使用率",
                                systemInfo.cpu?.used ?: 0.0,
                                MaterialTheme.colorScheme.primary,
                                Modifier.weight(1f)
                            )
                            DashboardPieCard(
                                "内存使用率",
                                systemInfo.memory?.usage ?: 0.0,
                                Color(0xFF4CAF50),
                                Modifier.weight(1f)
                            )
                        }
                    }
                    // 2. 基础系统信息
                    item {
                        InfoSectionCard("基础信息") {
                            systemInfo.system?.let {
                                DetailRow("计算机名", it.computerName)
                                DetailRow("IP 地址", it.computerIp)
                                DetailRow("操作系统", it.osName)
                                DetailRow("系统架构", it.osArch)
                            }
                        }
                    }
                    // 3. JVM 运行状态
                    item {
                        InfoSectionCard("JVM 状态") {
                            systemInfo.jvm?.let {
                                DetailRow("JVM 名称", it.name)
                                DetailRow("Java 版本", it.version)
                                DetailRow("已用堆内存", "${it.used} MB")
                                DetailRow("空闲堆内存", "${it.free} MB")
                                DetailRow("运行时间", it.runTime)
                                Spacer(Modifier.height(8.dp))
                                UsageLinearBar("JVM 内存占用率", it.usage)
                            }
                        }
                    }
                    // 4. 磁盘空间信息
                    item {
                        Text(
                            "磁盘空间",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    items(systemInfo.sysFiles ?: emptyList()) { file ->
                        DiskCard(file)
                    }
                }

            }
        }
    }
}

// --- 漂亮的辅助组件 ---

@Composable
fun DashboardPieCard(title: String, usage: Double, color: Color, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(targetValue = usage.toFloat())

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    drawArc(
                        color = color.copy(alpha = 0.2f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx())
                    )
                    drawArc(
                        color = if (usage > 85) Color.Red else color,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 3.6f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text("${usage.toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            content()
        }
    }
}


@Composable
fun UsageLinearBar(label: String, usage: Double) {
    val progressValue = (usage / 100.0).coerceIn(0.0, 1.0).toFloat()
    val progressColor = if (usage > 90) Color(0xFFE57373) else MaterialTheme.colorScheme.primary
    val trackColor = progressColor.copy(alpha = 0.15f)

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${usage.toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
        }

        // --- 自定义进度条，解决断裂问题 ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp) // 足够粗
                .background(trackColor, CircleShape) // 轨道背景
        ) {
            // 进度部分
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressValue) // 根据比例占据宽度
                    .fillMaxHeight()
                    .background(progressColor, CircleShape) // 进度条圆角
            )
        }
    }
}

@Composable
fun DiskCard(file: SystemFileInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标区域
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.width(16.dp))

            // 右侧信息区域
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = file.dirName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = file.sysTypeName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "已用 ${file.used} / 共 ${file.total}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                UsageLinearBar(label = "", usage = file.usage)
            }
        }
    }
}