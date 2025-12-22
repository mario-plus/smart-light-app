package com.unilumin.smartapp.ui.screens.dialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.unilumin.smartapp.client.data.EnvData
import com.unilumin.smartapp.client.data.EnvDisplayInfo
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray50

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EnvDataDialog(
    data: EnvData?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f) // 宽度稍大一些，适合并排显示
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // --- 头部 ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFF2563EB))
                    Spacer(Modifier.width(10.dp))
                    Text("环境监测详情", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                if (isLoading) {
                    Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (data != null) {
                    // 1. 数据分组：每 8 个一组
                    val allItems = remember(data) { getDisplayItems(data) }
                    val pagedItems = remember(allItems) { allItems.chunked(8) }

                    // 2. 状态记录
                    val pagerState = rememberPagerState(pageCount = { pagedItems.size })

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // --- 左右滑动 Pager ---
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth().height(360.dp),
                            pageSpacing = 16.dp,
                            verticalAlignment = Alignment.Top
                        ) { pageIndex ->
                            val currentPageItems = pagedItems[pageIndex]

                            // --- 每页内部的网格布局 (4行 2列) ---
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                userScrollEnabled = false, // 禁用网格自带滚动，由外层 Pager 处理滑动
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(currentPageItems) { item ->
                                    EnvDataItemCard(item) // 使用之前定义的简约卡片组件
                                }
                            }
                        }

                        // --- 3. 底部指示器 ---
                        if (pagedItems.size > 1) {
                            Row(
                                Modifier.height(24.dp).padding(top = 12.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(pagedItems.size) { iteration ->
                                    val color = if (pagerState.currentPage == iteration) Color(0xFF2563EB) else Color.LightGray
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .size(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnvDataItemCard(item: EnvDisplayInfo) {
    Surface(
        color = Gray50.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.label, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = item.value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blue600
                )
                Spacer(Modifier.width(4.dp))
                Text(item.unit, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
    }
}

// 数据转换模型

 fun getDisplayItems(data: EnvData): List<EnvDisplayInfo> {
    val list = mutableListOf<EnvDisplayInfo>()
    data.pm2_5?.let { list.add(EnvDisplayInfo("PM2.5", it, "μg/m³")) }
    data.pm10?.let { list.add(EnvDisplayInfo("PM10", it, "μg/m³")) }
    data.ta?.let { list.add(EnvDisplayInfo("大气温度", it, "°C")) }
    data.ua?.let { list.add(EnvDisplayInfo("大气相对湿度", it, "%RH")) }
    data.lightIntensity?.let { list.add(EnvDisplayInfo("光照", it, "Lux")) }
    data.noise?.let { list.add(EnvDisplayInfo("噪声", it, "dB")) }
    data.windspeed?.let { list.add(EnvDisplayInfo("风速", it, "m/s")) }
    data.pa?.let { list.add(EnvDisplayInfo("大气压", it, "hPa")) }
    data.precipitation?.let { list.add(EnvDisplayInfo("降水量", it, "mm")) }
    data.windDirection?.let { list.add(EnvDisplayInfo("风向", it, "")) }
    data.dust?.let { list.add(EnvDisplayInfo("粉尘", it.toString(), "ug/m3")) }
    data.so2?.let { list.add(EnvDisplayInfo("二氧化硫", it.toString(), "PPB")) }
    data.co?.let { list.add(EnvDisplayInfo("一氧化碳", it.toString(), "PPM")) }
    data.co2?.let { list.add(EnvDisplayInfo("二氧化碳", it.toString(), "PPM")) }
    data.no2?.let { list.add(EnvDisplayInfo("二氧化氮", it.toString(), "PPB")) }
    data.o3?.let { list.add(EnvDisplayInfo("臭氧", it.toString(), "PPB")) }
    data.sm?.let { list.add(EnvDisplayInfo("平均风速值", it.toString(), "m/s")) }
    data.sn?.let { list.add(EnvDisplayInfo("最小风速", it.toString(), "m/s")) }
    data.sx?.let { list.add(EnvDisplayInfo("最大风速", it.toString(), "m/s")) }
    data.dm?.let { list.add(EnvDisplayInfo("平均风向", it.toString(), "")) }
    data.dn?.let { list.add(EnvDisplayInfo("最小风向", it.toString(), "")) }
    data.dx?.let { list.add(EnvDisplayInfo("最大风向", it.toString(), "")) }
    data.ch2o?.let { list.add(EnvDisplayInfo("甲醛", it.toString(), "mg/m³")) }
    data.ns?.let { list.add(EnvDisplayInfo("平均噪声", it.toString(), "dB")) }
    data.ni?.let { list.add(EnvDisplayInfo("最小噪声", it.toString(), "dB")) }
    data.nx?.let { list.add(EnvDisplayInfo("最大噪声", it.toString(), "dB")) }
    data.tvoc?.let { list.add(EnvDisplayInfo("挥发性有机化合物", it.toString(), "ppm")) }
    data.temperature?.let { list.add(EnvDisplayInfo("温度", it.toString(), "℃")) }
    data.humidity?.let { list.add(EnvDisplayInfo("湿度", it.toString(), "%RH")) }


    return if (list.isEmpty()) listOf(EnvDisplayInfo("提示", "暂无数据", "")) else list
}