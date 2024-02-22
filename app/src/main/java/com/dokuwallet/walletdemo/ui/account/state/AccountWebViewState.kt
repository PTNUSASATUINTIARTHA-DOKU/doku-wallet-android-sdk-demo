package com.dokuwallet.walletdemo.ui.account.state

data class AccountWebViewState(
    val isLoading: Boolean = false,
    val url: String = "",
    val accountId: String = "",
    val authCode: String = "",
    val isError: Boolean = true
)