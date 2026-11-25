package com.twohearts.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.AnnotatedString
import java.io.File
import java.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowDialog

internal class NativeComposeScene(private val outDir: File) : AutoCloseable {
    val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
    // Drive recomposition after invoking a native semantics action in the paused Android looper.
    private val frameClock = BroadcastFrameClock()
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob() + frameClock)
    private val recomposer = Recomposer(scope.coroutineContext)
    private val composeView = ComposeView(controller.get())
    private var frameTime = 0L
    val activityView: View get() = controller.get().window.decorView

    fun setContent(content: @Composable () -> Unit) {
        scope.launch { recomposer.runRecomposeAndApplyChanges() }
        composeView.setParentCompositionContext(recomposer)
        composeView.setContent(content)
        controller.get().setContentView(composeView)
        advanceFrames()
    }

    private fun advanceFrames() {
        repeat(4) {
            Snapshot.sendApplyNotifications()
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(16))
            frameTime += 16_000_000L
            frameClock.sendFrame(frameTime)
        }
    }

    fun dialogView(): View {
        advanceFrames()
        layout(activityView)
        return checkNotNull(ShadowDialog.getLatestDialog()?.window) { "Expected an open dialog" }.decorView
    }

    fun closeDialog() {
        check(clickDialog("Close"))
    }

    fun layout(view: View) {
        val metrics = controller.get().resources.displayMetrics
        advanceFrames()
        view.measure(View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(metrics.heightPixels, View.MeasureSpec.EXACTLY))
        view.layout(0, 0, metrics.widthPixels, metrics.heightPixels)
        advanceFrames()
    }

    fun click(label: String, occurrence: Int = 0): Boolean =
        clickNode(occurrence) { it.config.getOrNull(SemanticsProperties.Text)?.any { text -> text.text == label } == true }

    fun clickDescription(label: String): Boolean =
        clickNode(0) { it.config.getOrNull(SemanticsProperties.ContentDescription)?.contains(label) == true }

    private fun clickNode(occurrence: Int, matches: (SemanticsNode) -> Boolean): Boolean {
        layout(activityView)
        val actions = nodes(activityView).filter(matches).mapNotNull { it.config.getOrNull(SemanticsActions.OnClick)?.action }
        val handled = actions.getOrNull(occurrence)?.invoke() == true
        advanceFrames()
        layout(activityView)
        return handled
    }

    fun dialogText(label: String) = hasText(dialogView(), label)
    fun activityText(label: String) = hasText(activityView, label)

    fun activityDescription(label: String): Boolean = nodes(activityView).any {
        it.config.getOrNull(SemanticsProperties.ContentDescription)?.contains(label) == true
    }
    fun scrollActivityBy(pixels: Float): Boolean {
        layout(activityView)
        val action = nodes(activityView).firstNotNullOfOrNull { it.config.getOrNull(SemanticsActions.ScrollBy)?.action }
        val handled = action?.invoke(0f, pixels) == true
        advanceFrames()
        layout(activityView)
        return handled
    }

    fun activityError(message: String): Boolean = nodes(activityView).any {
        it.config.getOrNull(SemanticsProperties.Error) == message
    }

    fun clickDialog(label: String): Boolean {
        val view = dialogView()
        layout(view)
        val action = nodes(view).firstOrNull {
            it.config.getOrNull(SemanticsProperties.Text)?.any { item -> item.text == label } == true &&
                it.config.getOrNull(SemanticsActions.OnClick) != null
        }?.config?.getOrNull(SemanticsActions.OnClick)?.action
        val handled = action?.invoke() == true
        advanceFrames()
        layout(activityView)
        return handled
    }

    fun setText(label: String, text: String): Boolean = setText(activityView, label, text)

    fun setDialogText(label: String, text: String): Boolean = setText(dialogView(), label, text)

    private fun setText(view: View, label: String, text: String): Boolean {
        layout(view)
        val action = nodes(view).firstOrNull {
            it.config.getOrNull(SemanticsProperties.ContentDescription)?.contains(label) == true &&
                it.config.getOrNull(SemanticsActions.SetText) != null
        }?.config?.getOrNull(SemanticsActions.SetText)?.action
        val handled = action?.invoke(AnnotatedString(text)) == true
        advanceFrames()
        layout(activityView)
        return handled
    }

    private fun hasText(view: View, label: String): Boolean = nodes(view).any { node ->
        node.config.getOrNull(SemanticsProperties.Text)?.any { it.text == label } == true ||
            node.config.getOrNull(SemanticsProperties.EditableText)?.text == label
    }

    private fun nodes(view: View) = checkNotNull(view.findViewOfType(ViewRootForTest::class.java))
        .semanticsOwner.getAllSemanticsNodes(mergingEnabled = true)

    fun captureDialog(name: String) = capture(dialogView(), name)

    fun capture(view: View, name: String) {
        layout(view)
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        try {
            view.draw(Canvas(bitmap))
            outDir.mkdirs()
            File(outDir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } finally {
            bitmap.recycle()
        }
    }

    override fun close() {
        composeView.disposeComposition()
        recomposer.cancel()
        scope.cancel()
        controller.close()
    }
}

internal fun <T : Any> View.findViewOfType(type: Class<T>): T? {
    if (type.isInstance(this)) return type.cast(this)
    if (this is ViewGroup) repeat(childCount) { getChildAt(it).findViewOfType(type)?.let { found -> return found } }
    return null
}
