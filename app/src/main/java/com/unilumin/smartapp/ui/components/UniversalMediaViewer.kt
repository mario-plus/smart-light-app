
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
    imageLoader: ImageLoader,      // <--- 传入全局配置好的 ImageLoader
    okHttpClient: OkHttpClient    // <--- 传入全局配置好的 OkHttpClient
) {
    val safeSuffix = suffix?.lowercase()?.trim() ?: ""

    val isImage = safeSuffix in listOf("jpg", "jpeg", "png", "webp", "bmp")
    val isGif = safeSuffix == "gif"
    val isVideoOrAudio = safeSuffix in listOf("mp4", "avi", "mkv", "mov", "mp3", "wav", "flac", "aac")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when {
            //gif没有动
            isImage || isGif -> {
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
    val context = LocalContext.current

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(url)
                .crossfade(true)
                .build(),
            imageLoader = imageLoader, // 直接使用传进来的，内置支持 GIF 和忽略 SSL
            contentDescription = "媒体预览",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            onSuccess = { isLoading = false },
            onError = { isLoading = false }
        )

        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
private fun ExoPlayerViewer(url: String, okHttpClient: OkHttpClient) {
    val context = LocalContext.current

    val exoPlayer = remember {
        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                val mediaItem = MediaItem.fromUri(url)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true // 自动播放
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}