package com.unilumin.smartapp.ui.screens.device


import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.data.DeviceStatusAnalysis
import com.unilumin.smartapp.client.data.DeviceStatusAnalysisResp
import com.unilumin.smartapp.ui.components.EmptyDataView
import com.unilumin.smartapp.ui.components.LoadingContent
import com.unilumin.smartapp.ui.theme.AccentOrange
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.DividerColor
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.theme.PrimaryBlue
import com.unilumin.smartapp.ui.theme.SoftBackground
import com.unilumin.smartapp.ui.theme.SuccessGreen
import com.unilumin.smartapp.ui.theme.TextDark
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel
import kotlinx.coroutines.delay

/**
 * 离线报表页面
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceStatusChartScreen(
    retrofitClient: RetrofitClient, onBack: () -> Unit
) {

    val context = LocalContext.current
    val deviceViewModel: DeviceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceViewModel(retrofitClient, context) as T
        }
    })

    // 观察 ViewModel 状态
    val isLoading by deviceViewModel.isLoading.collectAsState()
    val deviceStatusAnalysisData by deviceViewModel.deviceStatusAnalysisData.collectAsState()

    //

    LaunchedEffect(Unit) {
        while (true) {
            deviceViewModel.deviceStatusAnalysis()
            delay(5000) // 每 5 秒刷新一次
        }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Column(modifier = Modifier.background(CardWhite)) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "离线报表", style = TextStyle(
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
            deviceStatusAnalysisData?.let { data ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        OfflineRateDashboard(data)
                    }
                    item {
                        Text(
                            text = "设备分类详情",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(data.deviceStatusAnalysis) { analysis ->
                        DeviceCategoryCard(item = analysis, onDetailClick = {
                            //点击事件，用dialog显示离线设备

                        })
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            } ?: run {
                EmptyDataView("暂无数据")
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun OfflineRateDashboard(data: DeviceStatusAnalysisResp) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp), // 留出一点空间给阴影
        shape = RoundedCornerShape(24.dp), // 更圆润的角显得更有现代感
        color = Color.White,
        shadowElevation = 8.dp, // 增加阴影高度提升厚重感
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
                // 底色圆环 (厚度增加)
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 14.dp,
                    color = Color(0xFFEFF2F8)
                )
                // 进度圆环
                CircularProgressIndicator(
                    progress = (data.offlineRatio).toFloat(),
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 14.dp,
                    color = PrimaryBlue,
                    trackColor = Color.Transparent
                )
                // 中间文字区域
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.1f", data.offlineRatio * 100)}%",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black, // 使用最粗体
                        color = PrimaryBlue,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "当前设备离线率",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 使用带背景的行，增加整体感
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftBackground, RoundedCornerShape(16.dp))
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "离线设备", value = data.offlineSum.toString(), AccentOrange)
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(DividerColor)) // 分割线
                StatItem(label = "在线总数", value = data.onlineSum.toString(), PrimaryBlue)
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(DividerColor)) // 分割线
                StatItem(label = "设备总数", value = data.sum.toString(), SuccessGreen)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}


@SuppressLint("DefaultLocale")
@Composable
fun DeviceCategoryCard(
    item: DeviceStatusAnalysis,
    onDetailClick: (DeviceStatusAnalysis) -> Unit // 增加回调参数
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 顶层容器：包含图标、标题和右上角的详情按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 左侧图标
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftBackground,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp),
                        tint = PrimaryBlue
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 中间标题
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextDark,
                    modifier = Modifier.weight(1f) // 占据剩余空间，将按钮推向最右侧
                )

                // 右上角详情入口
                androidx.compose.material3.TextButton(
                    onClick = { onDetailClick(item) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "详情",
                        fontSize = 13.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 数据展示区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9FAFC), RoundedCornerShape(12.dp)) // 给数据区加个浅色底，增加厚重感
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailColumn("总数", item.sum.toString(), PrimaryBlue.copy(alpha = 0.7f))
                Box(modifier = Modifier.width(1.dp).height(20.dp).background(DividerColor))
                DetailColumn("离线数", item.offlineSum.toString(), Color(0xFFE57373))
                Box(modifier = Modifier.width(1.dp).height(20.dp).background(DividerColor))

                // 离线率高亮显示
                Column(
                    modifier = Modifier
                        .background(AccentOrange.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DetailColumn(
                        "离线率",
                        "${String.format("%.1f", item.offlineRatio * 100)}%",
                        AccentOrange,
                        isBold = true
                    )
                }
            }
        }
    }
}

@Composable
fun DetailColumn(label: String, value: String, valueColor: Color, isBold: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray.copy(alpha = 0.6f)
        )
    }
}


