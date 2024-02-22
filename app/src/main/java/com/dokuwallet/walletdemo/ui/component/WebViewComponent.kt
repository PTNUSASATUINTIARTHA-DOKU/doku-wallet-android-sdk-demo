package com.dokuwallet.walletdemo.ui.component

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.dokuwallet.walletdemo.model.WalletConfig
import com.dokuwallet.walletdemo.utils.CommonUtils

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewComponent(url: String) {
    val context = LocalContext.current
    val walletConfig = WalletConfig.default()

    AndroidView(factory = {
        WebView(context).apply {
            settings.javaScriptEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    if (url?.contains(walletConfig.redirectUrl) == true) {
                        CommonUtils.getParamFromUrl(url, "accountId")?.let {
//                            prefManager.putAccountId(it)
//                            if (url.contains(WalletPage.redirectUrl)) {
//                                accountBinding()
//                            }
                        }

                        CommonUtils.getParamFromUrl(url, "authCode")?.let {
                        }
                    } else {
//                        showLoading(false)
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    if (url?.contains(walletConfig.redirectUrl) == true) {
//                        showLoading(true)
                    }
                    super.onPageStarted(view, url, favicon)
                }
            }
            webChromeClient = WebChromeClient()

            loadUrl(url)
        }
    })
}