package com.dokuwallet.walletdemo.model

data class WalletConfig(
    val clientId: String,
    val privateKey: String,
    val clientKey: String,
    val secretKey: String,
    val redirectUrl: String
) {
    companion object {
        fun default(): WalletConfig =
            WalletConfig(
                clientId = "",
                privateKey = "",
                clientKey = "",
                secretKey = "",
                redirectUrl = "https://walletsdk.com"
            )
    }
}