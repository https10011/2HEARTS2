package com.twohearts.app.ui.screens.memories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoriesHome(
    memories: List<Memory>, onMemoryClick: (String) -> Unit, onAddMemory: () -> Unit, onBack: () -> Unit,
    media: Map<String, List<MediaAsset>> = emptyMap(),
) {
    val colors = LocalTwoHeartsColors.current
    Scaffold(
        topBar = { Header(title = "Memories", onBack = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddMemory, icon = { Icon(ThIcons.Add, null) }, text = { Text("New memory") }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        },
    ) { padding ->
        if (memories.isEmpty()) MemoryEmptyState(onAddMemory, Modifier.padding(padding)) else LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            item {
                Text("The moments that made us", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("${memories.size} ${if (memories.size == 1) "memory" else "memories"}, kept privately on this device", color = colors.textSecondary)
            }
            itemsIndexed(memories, key = { _, item -> item.id }) { index, memory ->
                MemoryStoryCard(memory, media[memory.id].orEmpty(), index == 0) { onMemoryClick(memory.id) }
            }
        }
    }
}

@Composable
private fun MemoryEmptyState(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalTwoHeartsColors.current
    Column(modifier.fillMaxSize().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(92.dp).clip(RoundedCornerShape(30.dp)).background(colors.surfaceBlush), contentAlignment = Alignment.Center) {
            Icon(ThIcons.PhotoLibrary, null, tint = colors.burgundy, modifier = Modifier.size(42.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Your story starts here", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("Save a photo, a date, or a few words from a moment you want to keep.", color = colors.textSecondary, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAdd) { Icon(ThIcons.Add, null); Spacer(Modifier.width(8.dp)); Text("Save your first memory") }
    }
}

@Composable
private fun MemoryStoryCard(memory: Memory, media: List<MediaAsset>, featured: Boolean, onClick: () -> Unit) {
    val colors = LocalTwoHeartsColors.current
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), color = if (featured) colors.surfaceBlush else colors.surfaceElevated, tonalElevation = if (featured) 2.dp else 0.dp) {
        Column {
            MemoryMediaFrame(media.firstOrNull(), Modifier.fillMaxWidth().height(if (featured) 220.dp else 160.dp))
            Column(Modifier.padding(18.dp)) {
                Text(DateTimeHelper.formatDisplay(memory.memoryDate).uppercase(), style = MaterialTheme.typography.labelMedium, color = colors.roseDeep)
                Spacer(Modifier.height(7.dp))
                Text(memory.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                memory.caption?.takeIf { it.isNotBlank() }?.let { Spacer(Modifier.height(8.dp)); Text(it, color = colors.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis) }
                if (media.size > 1) { Spacer(Modifier.height(10.dp)); Text("${media.size} photos and videos", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary) }
            }
        }
    }
}

@Composable
fun MemoryMediaFrame(asset: MediaAsset?, modifier: Modifier = Modifier) {
    val colors = LocalTwoHeartsColors.current
    val bitmap by produceState<android.graphics.Bitmap?>(null, asset?.filePath) {
        value = if (asset?.mimeType?.startsWith("image/") == true) withContext(Dispatchers.IO) {
            BitmapFactory.decodeFile(asset.filePath)
        } else null
    }
    Box(modifier.clip(RoundedCornerShape(20.dp)).background(colors.surfaceWarm), contentAlignment = Alignment.Center) {
        if (bitmap != null) Image(bitmap!!.asImageBitmap(), contentDescription = "Memory photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Icon(if (asset?.mimeType?.startsWith("video/") == true) ThIcons.Play else ThIcons.Image, null, tint = if (asset == null) colors.roseMuted else colors.burgundy, modifier = Modifier.size(52.dp))
        if (asset != null) Text(if (asset.mimeType.startsWith("video/")) "Video" else "Photo", Modifier.align(Alignment.BottomStart).padding(14.dp), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
    }
}
