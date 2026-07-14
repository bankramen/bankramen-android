package com.uson.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.uson.myapplication.core.api.BankramenApiFactory
import com.uson.myapplication.core.auth.AuthGraph
import com.uson.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BankramenApiFactory.initialize(applicationContext)
        AuthGraph.kakaoLoginCallbackHandler(applicationContext).handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = false, dynamicColor = false) {
                AppRoot()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        AuthGraph.kakaoLoginCallbackHandler(applicationContext).handleIntent(intent)
    }
}
