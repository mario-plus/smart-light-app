
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import okhttp3.OkHttpClient

@Composable
fun UniversalMediaViewer(
    modifier: Modifier = Modifier,
    url: String,
    suffix: String?,
    imageLoader: ImageLoader,
    okHttpClient: OkHttpClient
) {
    val safeSuffix = suffix?.lowercase()?.trim() ?: ""
    val isImage = safeSuffix in listOf("jpg", "jpeg", "png", "webp", "bmp", "gif")
    val isVideoOrAudio = safeSuffix in listOf("mp4", "avi", "mkv", "mov", "mp3", "wav", "flac", "aac")

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when {
            isImage -> {
                CoilImageViewer(url = url, imageLoader = imageLoader)
            }
            isVideoOrAudio -> {
                ExoPlayerViewer(url = url, okHttpClient = okHttpClient)
            }
            else -> {
                Text(text = "暂不支持预览该格式: $safeSuffix", color = Color.White)
            }
        }
    }
}

@Composable
private fun CoilImageViewer(url: String, imageLoader: ImageLoader) {
    var isLoading by remember { mutableStateOf(true) }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        imageLoader = imageLoader, // 确保外部传入的 imageLoader 已经配置了 GifDecoder
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize(),
        onState = { state ->
            isLoading = state is coil.compose.AsyncImagePainter.State.Loading
        }
    )

    if (isLoading) {
        CircularProgressIndicator(color = Color.White)
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun ExoPlayerViewer(url: String, okHttpClient: OkHttpClient) {
    val context = LocalContext.current

    // 使用 remember(url) 确保当 URL 变化时重新创建播放器
    val exoPlayer = remember(url) {
        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = true
            }
    }

    // 确保组件销毁时释放资源，防止内存泄露
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                // 优化：视频拉伸模式，防止黑边或拉伸变形
                resizeMode  = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            view.player = exoPlayer
        }
    )
}