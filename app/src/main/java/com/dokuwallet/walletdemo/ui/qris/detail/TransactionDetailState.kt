package com.dokuwallet.walletdemo.ui.qris.detail

import com.dokuwallet.coresdk.vo.ErrorData

data class TransactionDetailState(
    val isLoading: Boolean = false,
    val timer: Long = 0L,
    val error: ErrorData? = null
)