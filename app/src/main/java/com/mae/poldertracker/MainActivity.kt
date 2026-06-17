package com.mae.poldertracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mae.poldertracker.ui.navigation.PolderTrackerNavGraph
import com.mae.poldertracker.ui.theme.PolderTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolderTrackerTheme {
                PolderTrackerNavGraph()
            }
        }
    }
}
