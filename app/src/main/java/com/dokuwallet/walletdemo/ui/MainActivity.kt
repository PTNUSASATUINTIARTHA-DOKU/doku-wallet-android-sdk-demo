package com.dokuwallet.walletdemo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dokuwallet.walletdemo.ui.navigation.MainScreen
import com.dokuwallet.walletdemo.ui.theme.WalletDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalletDemoTheme {
                MainScreen()
            }
        }
    }
}