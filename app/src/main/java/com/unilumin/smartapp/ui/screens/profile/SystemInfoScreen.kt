import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator // 引入 CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox // 1. 引入下拉刷新组件
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState // 1. 引入下拉刷新状态
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unilumin.smartapp.client.data.SystemFileInfo
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.DetailCard
import com.unilumin.smartapp.ui.components.DetailRow
import com.unilumin.smartapp.ui.components.UsageLinearBar
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.theme.TextDark
import com.unilumin.smartapp.ui.viewModel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemInfoScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    // 使用 collectAsState 观察数据流
    val systemInfo by profileViewModel.systemInfo.collectAsState()
    // 观察 ViewModel 状态
    val isLoading by profileViewModel.isLoading.collectAsState()

    // 定义：是否是首次加载（无数据且正在加载）
    // 这样做的目的是：首次进入显示全屏 Loading，后续下拉刷新时列表不消失，只显示顶部刷新球
    val isFirstLoad = systemInfo.system == null && isLoading

    val refreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        // 如果没有数据，才自动请求（避免重复刷新），或者根据需求每次进入都请求
        if (systemInfo.system == null) {
            profileViewModel.getSystemInfo()
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(title = "系统资源监控", onBack = { onBack() })
        }, containerColor = PageBackground
    ) { padding ->
        if (isFirstLoad) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            PullToRefreshBox(
                isRefreshing = isLoading, // 绑定 ViewModel 的 loading 状态
                state = refreshState,
                onRefresh = { profileViewModel.getSystemInfo() },
                modifier = Modifier.padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (systemInfo.system != null) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // CPU 监控
                                val cpuDetails = listOf(
                                    "核心数" to "${systemInfo.cpu?.cpuNum ?: 0}",
                                    "系统" to "${systemInfo.cpu?.sys ?: 0.0}%",
                                    "用户" to "${systemInfo.cpu?.used ?: 0.0}%"
                                )
                                DashboardPieCard(
                                    title = "CPU空闲率",
                                    usage = systemInfo.cpu?.free ?: 0.0,
                                    color = MaterialTheme.colorScheme.primary,
                                    details = cpuDetails,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // 内存监控
                                val memDetails = listOf(
                                    "总量" to "${systemInfo.memory?.total ?: 0.0}G",
                                    "已用" to "${systemInfo.memory?.used ?: 0.0}G",
                                    "剩余" to "${systemInfo.memory?.free ?: 0.0}G"
                                )
                                DashboardPieCard(
                                    title = "内存使用率",
                                    usage = systemInfo.memory?.usage ?: 0.0,
                                    color = Color(0xFF4CAF50),
                                    details = memDetails,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        // 2. 基础系统信息
                        item {
                            DetailCard("基础信息") {
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
                            DetailCard("JVM 状态") {
                                systemInfo.jvm?.let {
                                    DetailRow("JVM 名称", it.name)
                                    DetailRow("Java 版本", it.version)
                                    DetailRow("已用堆内存", "${it.used} MB")
                                    DetailRow("空闲堆内存", "${it.free} MB")
                                    DetailRow("启动时间", it.startTime)
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
}

// ... DashboardPieCard 和 DiskCard 代码保持不变 ...


@Composable
fun DashboardPieCard(
    title: String,
    usage: Double, // 建议确保传入的是 0-100 之间的数值
    color: Color,
    details: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    // 自动判断：如果传入的是 0.71 这种小数，自动转为 71
    val normalizedUsage = if (usage > 0 && usage <= 1.0) usage * 100.0 else usage
    val animatedProgress by animateFloatAsState(targetValue = normalizedUsage.toFloat())

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧圆环
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // 底色圆环
                        drawArc(
                            color = color.copy(alpha = 0.1f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx())
                        )
                        // 进度圆环
                        drawArc(
                            color = if (normalizedUsage > 85) Color.Red else color,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 3.6f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "${normalizedUsage.toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(24.dp))

                // 右侧详情
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    details.forEach { (label, value) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 磁盘卡片
 * */
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