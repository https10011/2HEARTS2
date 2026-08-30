package com.twohearts.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.twohearts.app.ui.theme.TwoHeartsTheme

/**
 * TwoHearts — Main Activity
 *
 * This is the entry point for the native Android application.
 * Currently a placeholder scaffold — features will be migrated
 * from the legacy React/Vite/Capacitor implementation per the
 * migration roadmap in Migration/TWOHEARTS-MASTER-AUDIT-AND-MIGRATION-ROADMAP.md.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TwoHeartsTheme {
                TwoHeartsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoHeartsApp() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("TwoHearts") }
            )
        }
    ) { innerPadding ->
        Greeting(
            name = "TwoHearts",
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Welcome to $name",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun TwoHeartsAppPreview() {
    TwoHeartsTheme {
        TwoHeartsApp()
    }
}
