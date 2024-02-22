package com.dokuwallet.walletdemo.ui.qris.payment

import com.dokuwallet.coresdk.domain.model.walletmodel.DecodeQrDomain
import com.dokuwallet.coresdk.domain.model.walletmodel.PaymentQrDomain

data class PaymentQrisState(
    val isLoading: Boolean = false,
    val qrContent: String? = null,
    val isScanFlag: Boolean = false,
    val decodeQrDomain: DecodeQrDomain? = null,
    val paymentQrDomain : PaymentQrDomain? = null,
    val paymentAmount: String = "",
    val transactionAmount : String = "",
    val totalPayment: String = "",
    val transactionDate : String = "",
    val isError: Boolean = true,
    val errorField : Boolean = false,
)