package com.unilumin.smartapp.ui.screens.application.playBox

import UniversalMediaViewer
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.paging.compose.collectAsLazyPagingItems
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.fileStatusOptions
import com.unilumin.smartapp.client.constant.DeviceConstant.fileTypeOptions
import com.unilumin.smartapp.client.data.FolderNode
import com.unilumin.smartapp.client.data.LedMaterialInfoVO
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.InfoRowItem
import com.unilumin.smartapp.ui.components.ModernStateSelector
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader
import com.unilumin.smartapp.ui.viewModel.ScreenViewModel
import com.unilumin.smartapp.util.TimeUtil.formatIsoTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLedFileManage(
    screenViewModel: ScreenViewModel,
    imageLoader: ImageLoader,
    retrofitClient: RetrofitClient
) {
    val searchQuery by screenViewModel.searchQuery.collectAsState()
    val fileType by screenViewModel.fileType.collectAsState()
    val fileStatus by screenViewModel.fileStatus.collectAsState()
    val totalCount by screenViewModel.totalCount.collectAsState()
    val ledFilePagingFlow = screenViewModel.ledFilePagingFlow.collectAsLazyPagingItems()

    var folderStack by remember { mutableStateOf(listOf(FolderNode(0L, "全部文件"))) }
    var previewFile by remember { mutableStateOf<LedMaterialInfoVO?>(null) }

    BackHandler(enabled = folderStack.size > 1 || previewFile != null) {
        if (previewFile != null) {
            previewFile = null
        } else {
            val newStack = folderStack.dropLast(1)
            folderStack = newStack
            screenViewModel.updateParentId(newStack.last().id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ... 保持 SearchHeader 和 ModernStateSelector 不变 ...
        SearchHeader(
            statusOptions = fileTypeOptions,
            currentStatus = fileType,
            searchQuery = searchQuery,
            searchTitle = "搜索素材名称或目录名称",
            onStatusChanged = { screenViewModel.updateFileType(it) },
            onSearchChanged = { screenViewModel.updateSearch(it) }
        )

        ModernStateSelector(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
            options = fileStatusOptions,
            selectedValue = fileStatus,
            onValueChange = { screenViewModel.updateFileStatus(it) }
        )

        if (folderStack.size > 1) {
            BreadcrumbRow(
                folderStack = folderStack,
                onNodeClick = { index ->
                    val newStack = folderStack.take(index + 1)
                    folderStack = newStack
                    screenViewModel.updateParentId(newStack.last().id)
                },
                onBackClick = {
                    val newStack = folderStack.dropLast(1)
                    folderStack = newStack
                    screenViewModel.updateParentId(newStack.last().id)
                }
            )
        }

        PagingList(
            totalCount = totalCount,
            lazyPagingItems = ledFilePagingFlow,
            modifier = Modifier.weight(1f),
            //TODO 后端分页数据有问题，加载产生重复数据
//            itemKey= { file ->
//                "${file.id}_${file.type}_${file.relativePath.hashCode()}"
//            },
            contentPadding = PaddingValues(16.dp)
        ) { file ->
            LedMaterialCard(
                imageLoader = imageLoader,
                item = file,
                onClick = {
                    if (file.type == 1) {
                        val targetId = file.id ?: 0L
                        folderStack = folderStack + FolderNode(targetId, file.name ?: "未知")
                        screenViewModel.updateParentId(targetId)
                    } else {
                        previewFile = file
                    }
                }
            )
        }
    }

    // --- 优化后的预览弹窗 ---
    if (previewFile != null) {
        Dialog(
            onDismissRequest = { previewFile = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                securePolicy = androidx.compose.ui.window.SecureFlagPolicy.Inherit,
                dismissOnBackPress = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                var realMediaUrl by remember { mutableStateOf<String?>(null) }
                var isFetchingUrl by remember { mutableStateOf(true) }

                // 获取 URL 逻辑，增加异常捕获防止崩溃
                LaunchedEffect(previewFile) {
                    isFetchingUrl = true
                    try {
                        val rawPath = previewFile?.relativePath
                        if (!rawPath.isNullOrBlank()) {
                            realMediaUrl = screenViewModel.getMinioUrl(rawPath)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isFetchingUrl = false
                    }
                }

                if (isFetchingUrl) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                } else if (!realMediaUrl.isNullOrBlank()) {
                    UniversalMediaViewer(
                        modifier = Modifier.fillMaxSize(),
                        url = realMediaUrl!!,
                        suffix = previewFile?.suffix,
                        imageLoader = imageLoader,
                        okHttpClient = retrofitClient.getExoOkHttpClient()
                    )
                } else {
                    Text("获取文件地址失败", color = Color.White, modifier = Modifier.align(Alignment.Center))
                }

                // 关闭按钮
                IconButton(
                    onClick = { previewFile = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 40.dp, end = 16.dp) // 避开状态栏
                        .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(50))
                ) {
                    Icon(Icons.Rounded.Close, "关闭", tint = Color.White)
                }
            }
        }
    }
}
/**
 * 现代化面包屑导航栏
 */
@Composable
fun BreadcrumbRow(
    folderStack: List<FolderNode>,
    onNodeClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(folderStack.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "返回上一级",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            folderStack.forEachIndexed { index, node ->
                val isLast = index == folderStack.size - 1

                Text(
                    text = node.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = if (isLast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(enabled = !isLast) { onNodeClick(index) }
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
                if (!isLast) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "分隔符",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedMaterialCard(
    imageLoader: ImageLoader,
    item: LedMaterialInfoVO,
    onClick: () -> Unit
) {
    val isFileDirectory = item.type == 1
    var imageLoadError by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.name ?: "未知素材",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                if (!isFileDirectory) {
                    DeviceStatus(
                        status = item.reviewStatus,
                        mapOf(
                            0 to Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "待审核"),
                            1 to Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), "审核中"),
                            2 to Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), "审核通过"),
                            3 to Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "审核不通过"),
                            4 to Triple(Color(0xFFF3E5F5), Color(0xFF7B1FA2), "申诉中")
                        )
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = "文件夹",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            if (!isFileDirectory) {
                val imageUrl = item.pictureMinioUrl
                Spacer(modifier = Modifier.height(12.dp))
                if (!imageUrl.isNullOrBlank() && !imageLoadError) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = "素材封面",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        onError = {
                            imageLoadError = true
                        }
                    )
                } else {
                    DefaultFileCover(suffix = item.suffix)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val formattedTime = formatIsoTime(item.createTime)
            InfoRowItem(Icons.Rounded.Schedule, "创建时间", formattedTime)
        }
    }
}


/**
 * 提取独立的默认封面组件
 */
@Composable
fun DefaultFileCover(suffix: String?) {
    val safeSuffix = suffix?.lowercase()?.trim() ?: ""
    val displaySuffix = if (safeSuffix.isNotEmpty()) safeSuffix.uppercase() else "UNKNOWN"

    val (fileIcon, bgColor, iconTint) = when (safeSuffix) {
        "txt", "doc", "docx", "pdf" ->
            Triple(Icons.Rounded.Description, Color(0xFFE3F2FD), Color(0xFF1976D2))

        "ppt", "pptx" ->
            Triple(Icons.Rounded.Slideshow, Color(0xFFFFEBEE), Color(0xFFD32F2F))

        "mp3", "wav", "flac", "aac" ->
            Triple(Icons.Rounded.MusicNote, Color(0xFFFFF8E1), Color(0xFFF57F17))

        "mp4", "avi", "mkv", "mov" ->
            Triple(Icons.Rounded.Movie, Color(0xFFF3E5F5), Color(0xFF7B1FA2))

        else ->
            Triple(
                Icons.Rounded.InsertDriveFile,
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant
            )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = fileIcon,
                contentDescription = "默认文件封面",
                tint = iconTint,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = iconTint.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displaySuffix,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = iconTint
                )
            }
        }
    }
}