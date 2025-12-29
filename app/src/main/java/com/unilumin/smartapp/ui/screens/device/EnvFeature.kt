import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.data.EnvData
import com.unilumin.smartapp.client.data.EnvDisplayInfo
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.ui.components.RemoteControlButtonGroup
import com.unilumin.smartapp.ui.screens.device.FeatureContentContainer

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun EnvFeatureContent(lightDevice: LightDevice, onDetailClick: (LightDevice) -> Unit) {
    val displayItems = lightDevice.envData?.let { getDisplayItems(it) }
    if (displayItems.isNullOrEmpty()) return
    FeatureContentContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Box(modifier = Modifier.heightIn(max = 600.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    items(displayItems) { item ->
                        EnvSensorCard(item)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            RemoteControlButtonGroup(
                canClick = lightDevice.state == 1,
                showRemoteCtlBtn = false,
                onRemoteControlClick = { },
                onHistoryClick = {})
        }
    }
}

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


@Composable
fun EnvSensorCard(info: EnvDisplayInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 标题：名称 + 单位
        Text(
            text = "${info.label}${if (info.unit.isNotEmpty()) "(${info.unit})" else ""}",
            fontSize = 11.sp,
            color = Color(0xFF999999), // 浅灰色，突出数值
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 14.sp,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp)) // 固定间距，避免拉伸
        // 数值
        Text(
            text = info.value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3B7CFF), // 采用更柔和的商务蓝
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}