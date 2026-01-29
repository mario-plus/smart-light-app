package com.unilumin.smartapp.ui.screens.profile

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.menuItems
import com.unilumin.smartapp.ui.components.ProfileMenuItem
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Emerald500
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray200
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray50
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.Red500
import com.unilumin.smartapp.ui.viewModel.ProfileViewModel
import com.unilumin.smartapp.ui.viewModel.ViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    imageLoader: ImageLoader,
    retrofitClient: RetrofitClient,
    onLogout: () -> Unit,
    onItemClick: (name: String, profileViewModel: ProfileViewModel) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ViewModelFactory {
            ProfileViewModel(retrofitClient, application)
        })

    val scope = rememberCoroutineScope()
    val currentProject by profileViewModel.currentProject.collectAsState()
    val projectList by profileViewModel.projectList.collectAsState()
    val userInfo by profileViewModel.userInfo.collectAsState()
    val userAvatarUrl by profileViewModel.userAvatarUrl.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        profileViewModel.fetchProjects()
        profileViewModel.fetchUserInfo()
    }


    // --- 底部抽屉 (切换项目) ---
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    "切换项目",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                LazyColumn {
                    items(
                        items = projectList, key = { it.id }) { project ->
                        val isSelected = project.id == currentProject?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    profileViewModel.switchProject(project)
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        if (!sheetState.isVisible) showBottomSheet = false
                                    }
                                }
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.name,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Blue600 else Gray900
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    null,
                                    tint = Blue600,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Divider(
                            color = Gray50,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF0F7FF), Gray50), startY = 0f, endY = 500f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Surface(
                        shape = CircleShape,
                        color = Gray200,
                        modifier = Modifier
                            .size(80.dp)
                            .border(4.dp, Color.White, CircleShape)
                            .shadow(8.dp, CircleShape, spotColor = Gray200)
                    ) {
                        if (!userAvatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(userAvatarUrl)
                                    .crossfade(true).build(),
                                imageLoader = imageLoader,
                                contentDescription = "User Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Person,
                                    null,
                                    tint = Gray400,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Emerald500, CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Column {
                    Text(
                        text = userInfo?.nickname ?: "加载中...",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Gray900,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFDBEAFE), shape = RoundedCornerShape(8.dp)
                        ) { // Blue100
                            Text(
                                text = userInfo?.username ?: "--",
                                fontSize = 12.sp,
                                color = Blue600,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Surface(
                shape = RoundedCornerShape(24.dp), modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        12.dp,
                        RoundedCornerShape(24.dp),
                        spotColor = Color(0xFFBFDBFE).copy(alpha = 0.6f)
                    )
                    .clickable { showBottomSheet = true }) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE)),
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 1000f)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(52.dp),
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Business,
                                    null,
                                    tint = Blue600,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "当前所属项目",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Blue600.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentProject?.name ?: "加载中...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gray900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.6f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.SwapVert,
                                    null,
                                    tint = Blue600,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    val unifiedIconBg = Color(0xFFEFF6FF)
                    val unifiedIconTint = Blue600
                    menuItems.forEachIndexed { index, item ->
                        val canClick = item.third != null
                        ProfileMenuItem(
                            title = item.first,
                            icon = item.second,
                            iconColor = unifiedIconTint,
                            iconBg = unifiedIconBg,
                            trailingText = item.third,
                            showArrow = !canClick,
                            onClick = {
                                if (!canClick) {
                                    onItemClick(item.first, profileViewModel)
                                }
                            })
                        if (index < menuItems.size - 1) {
                            Divider(
                                color = Gray100.copy(alpha = 0.5f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. 退出登录按钮
            Button(
                onClick = {
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEF2F2), contentColor = Red500
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                border = BorderStroke(1.dp, Color(0xFFFEE2E2))
            ) {
                Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("退出登录", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}