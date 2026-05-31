package com.bloodsugar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bloodsugar.ui.MainScreen
import com.bloodsugar.ui.theme.BloodSugarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BloodSugarTheme {
                MainScreen()
            }
        }
    }
}
