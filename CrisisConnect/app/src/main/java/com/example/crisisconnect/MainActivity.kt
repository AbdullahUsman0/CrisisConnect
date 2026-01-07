package com.example.crisisconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.crisisconnect.ui.navigation.AppNavGraph
import com.example.crisisconnect.ui.theme.CrisisConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            // Wrapping everything in the theme
            CrisisConnectTheme {
                Surface {   // Material3 Surface for proper background + theme support
                    AppNavGraph()
                }
            }
        }
    }
}
