package com.unilumin.smartapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.mock.ALARM_LIST
import com.unilumin.smartapp.ui.components.AppCard
import com.unilumin.smartapp.ui.components.StatusBadge
import com.unilumin.smartapp.ui.theme.Amber50
import com.unilumin.smartapp.ui.theme.Amber500
import com.unilumin.smartapp.ui.theme.Blue50
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Emerald50
import com.unilumin.smartapp.ui.theme.Emerald500
import com.unilumin.smartapp.ui.theme.Emerald600
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray200
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.Red50
import com.unilumin.smartapp.ui.theme.Red500
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun DashboardScreen(retrofitClient: RetrofitClient) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("运维概览", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gray900)
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gray100),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Gray500)
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Red500, CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = 8.dp)
                        )
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Online Rate Card
                AppCard(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Emerald50, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Wifi, null, tint = Emerald600, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("设备在线", fontSize = 14.sp, color = Gray500)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("98.5", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gray900)
                        Text("%", fontSize = 14.sp, color = Gray400, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { 0.985f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = Emerald500,
                        trackColor = Gray100,
                    )
                }

                // Lighting Rate Card
                AppCard(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Amber50, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.WbSunny, null, tint = Amber500, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("亮灯率", fontSize = 14.sp, color = Gray500)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("45.2", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gray900)
                        Text("%", fontSize = 14.sp, color = Gray400, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { 0.452f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = Amber500,
                        trackColor = Gray100,
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("实时告警", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray900)
                Surface(color = Blue50, shape = RoundedCornerShape(16.dp)) {
                    Text(
                        "查看全部",
                        color = Blue600,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        items(ALARM_LIST) { alarm ->
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (alarm.level) {
                            "high" -> Red50
                            "medium" -> Amber50
                            else -> Blue50
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Warning,
                                null,
                                tint = when (alarm.level) {
                                    "high" -> Red500
                                    "medium" -> Amber500
                                    else -> Blue600
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(alarm.msg, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray900)
                            Text(alarm.time, fontSize = 12.sp, color = Gray400)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(alarm.level)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("待处理", fontSize = 12.sp, color = Gray400)
                        }
                    }
                    Icon(Icons.Rounded.ChevronRight, null, tint = Gray200)
                }
            }
        }
    }
}