package com.twohearts.app.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File

@Composable
internal fun VideoPreviewDialog(path: String, onDismiss: () -> Unit) {
    var failed by remember(path) { mutableStateOf(!File(path).isFile) }
    var player by remember { mutableStateOf<VideoView?>(null) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, player) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) player?.pause()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Video", style = MaterialTheme.typography.titleLarge)
                if (failed) {
                    ThStatusBanner(StatusVariant.ERROR, "Couldn’t play this video.")
                } else {
                    AndroidView(
                        factory = { context ->
                            VideoView(context).apply {
                                setMediaController(MediaController(context))
                                setOnErrorListener { _, _, _ -> failed = true; true }
                                setOnPreparedListener { if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) start() }
                                setVideoURI(Uri.fromFile(File(path)))
                                player = this
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(240.dp),
                        onRelease = { it.stopPlayback(); player = null },
                    )
                }
                ThButton(onDismiss, variant = ButtonVariant.GHOST, text = "Close")
            }
        }
    }
}
