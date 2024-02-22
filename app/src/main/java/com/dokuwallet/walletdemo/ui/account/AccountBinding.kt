package com.dokuwallet.walletdemo.ui.account

import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.dokuwallet.walletdemo.model.WalletConfig
import com.dokuwallet.walletdemo.ui.account.state.AccountWebViewState
import com.dokuwallet.walletdemo.ui.navigation.MainNavOption
import com.dokuwallet.walletdemo.utils.CommonUtils
import com.dokuwallet.walletdemo.utils.DataHolder

@Composable
fun AccountBinding(
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel,
    navigateTo: (String) -> Unit,
) {
    val componentActivity = LocalContext.current as ComponentActivity
    val walletConfig = WalletConfig.default()

    val accountWebViewState: AccountWebViewState by viewModel.accountWebViewState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (accountWebViewState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                if (url?.contains(walletConfig.redirectUrl) == true) {
                                    CommonUtils.getParamFromUrl(url, "accountId")?.let {
                                        if (url.contains(walletConfig.redirectUrl)) {
                                            viewModel.startAccountBinding(componentActivity, it)
                                        }
                                    }

                                    CommonUtils.getParamFromUrl(url, "authCode")?.let {
                                        viewModel.setAccountWebViewState(
                                            accountWebViewState.copy(
                                                authCode = it
                                            )
                                        )
                                        DataHolder.authCode = it
                                        navigateTo("${MainNavOption.HOME_SCREEN}?messageType=success&message=Akun kamu berhasil dibuat.")
                                    }
                                } else {
                                    viewModel.setAccountWebViewState(
                                        accountWebViewState.copy(
                                            isLoading = false
                                        )
                                    )
                                }
                            }

                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: Bitmap?
                            ) {
                                if (url?.contains(walletConfig.redirectUrl) == true) {
                                    viewModel.setAccountWebViewState(
                                        accountWebViewState.copy(
                                            isLoading = true
                                        )
                                    )
                                }
                                super.onPageStarted(view, url, favicon)
                            }
                        }
                        webChromeClient = WebChromeClient()

                        loadUrl(accountWebViewState.url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}