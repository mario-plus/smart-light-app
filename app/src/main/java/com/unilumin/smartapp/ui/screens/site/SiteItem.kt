package com.unilumin.smartapp.ui.screens.site

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.data.SiteInfo
import com.unilumin.smartapp.ui.theme.Blue50
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray200
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray50
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray700
import com.unilumin.smartapp.ui.theme.Gray900

@Composable
fun SiteCardItem(siteInfo: SiteInfo, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp), // 保持大圆角
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // 降低阴影，更扁平
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ================== 1. 头部：图标 + 标题 + 胶囊标签 ==================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 图标容器：增加一点渐变感或更柔和的背景
                Surface(
                    shape = CircleShape,
                    color = Blue50, // 浅蓝色背景
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Business, // 或其他图标
                            contentDescription = null,
                            tint = Blue600,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 标题与编号
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = siteInfo.name ?: "未知站点",
                        fontSize = 17.sp, // 稍微加大
                        fontWeight = FontWeight.Bold,
                        color = Gray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "NO.${siteInfo.number ?: "-"}",
                        fontSize = 13.sp,
                        color = Gray400, // 颜色更淡一点，拉开层次
                        fontFamily = FontFamily.Monospace // 等宽字体显示编号更有科技感
                    )
                }

                // 数量标签：改为实心胶囊，去边框
                Surface(
                    color = if ((siteInfo.deviceNum ?: 0) > 0) Blue50 else Gray100, // 有设备亮蓝，无设备置灰
                    shape = RoundedCornerShape(50), // 完全圆角
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${siteInfo.deviceNum ?: 0}",
                            fontWeight = FontWeight.Bold,
                            color = if ((siteInfo.deviceNum ?: 0) > 0) Blue600 else Gray500,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "台",
                            fontSize = 11.sp,
                            color = if ((siteInfo.deviceNum ?: 0) > 0) Blue600 else Gray500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================== 2. 信息区：轻量化设计 ==================
            // 使用浅灰色背景容器，但更紧凑，或者直接用分割线
            Surface(
                color = Gray50, // 极浅的灰色
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：型号
                    Column(modifier = Modifier.weight(1.5f)) { // 权重给大一点，因为型号通常长
                        Text("站点型号", fontSize = 11.sp, color = Gray400)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = siteInfo.configName?.takeIf { it.isNotBlank() } ?: "-",
                            fontSize = 13.sp,
                            color = Gray700,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 中间分割线
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(Gray200)
                            .padding(horizontal = 12.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    // 右侧：类型
                    Column(modifier = Modifier.weight(1f)) {
                        Text("站点类型", fontSize = 11.sp, color = Gray400)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = siteInfo.lamppoleTypeName ?: "普通型",
                            fontSize = 13.sp,
                            color = Gray700,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ================== 3. 底部：地址 ==================
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.LocationOn,
                    null,
                    tint = Gray400,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = siteInfo.projectRoadName?.takeIf { it.isNotBlank() } ?: "暂无地址信息",
                    fontSize = 12.sp,
                    color = Gray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}