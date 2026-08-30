package com.twohearts.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.twohearts.app.ui.components.ConfirmDialog
import com.twohearts.app.ui.components.TwoHeartsPreview
import com.twohearts.app.ui.theme.TwoHeartsTheme
import com.twohearts.app.ui.theme.TextScalingLevel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkMode by remember { mutableStateOf(false) }
            var textScaling by remember { mutableStateOf(TextScalingLevel.DEFAULT) }

            TwoHeartsTheme(
                darkMode = darkMode,
                textScalingLevel = textScaling
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TwoHeartsPreview(
                        onToggleDarkMode = { darkMode = !darkMode },
                        darkMode = darkMode,
                        textScaling = textScaling,
                        onTextScalingChange = { textScaling = it }
                    )
                }
            }
        }
    }
}
