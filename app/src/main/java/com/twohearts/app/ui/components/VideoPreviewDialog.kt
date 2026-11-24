package com.twohearts.app.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File

@Composable
internal fun VideoPreviewDialog(path: String, onDismiss: () -> Unit) {
    VideoPreviewDialog(Uri.fromFile(File(path)), onDismiss)
}

@Composable
internal fun VideoPreviewDialog(uri: Uri, onDismiss: () -> Unit) {
    var failed by remember(uri) { mutableStateOf(uri.scheme == "file" && uri.path?.let { File(it).isFile } != true) }
    var ready by remember(uri) { mutableStateOf(false) }
    var player by remember { mutableStateOf<VideoView?>(null) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, player) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) player?.pause()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    ThModal(open = true, onClose = onDismiss, label = "Video preview", modifier = Modifier.verticalScroll(rememberScrollState())) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Video preview", style = MaterialTheme.typography.titleLarge)
            if (failed) {
                ThStatusBanner(StatusVariant.ERROR, "Couldn’t play this video.")
            } else {
                if (!ready) Text("Opening video…", style = MaterialTheme.typography.bodyMedium)
                key(uri) {
                    AndroidView(
                        factory = { context ->
                            VideoView(context).apply {
                                setMediaController(MediaController(context))
                                setOnErrorListener { _, _, _ -> failed = true; true }
                                setOnPreparedListener {
                                    ready = true
                                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) start()
                                }
                                try {
                                    setVideoURI(uri)
                                } catch (_: SecurityException) {
                                    failed = true
                                } catch (_: IllegalArgumentException) {
                                    failed = true
                                }
                                player = this
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(208.dp),
                        onRelease = { it.stopPlayback(); player = null },
                    )
                }
            }
            ThButton(onDismiss, variant = ButtonVariant.GHOST, full = true, text = "Close")
        }
    }
}
