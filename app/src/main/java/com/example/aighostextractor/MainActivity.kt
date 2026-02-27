package com.example.aighostextractor

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.aighostextractor.ui.theme.AIGhostExtractorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("GHOST", "MainActivity (Compose) started")
        enableEdgeToEdge()
        setContent {
            AIGhostExtractorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GhostExtractorScreen()
                }
            }
        }
    }
}
