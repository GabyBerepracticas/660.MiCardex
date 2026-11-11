package com.example.micardex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.micardex.ui.CardexApp
import com.example.micardex.ui.CardexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CardexTheme {
                CardexApp()
            }
        }
    }
}
