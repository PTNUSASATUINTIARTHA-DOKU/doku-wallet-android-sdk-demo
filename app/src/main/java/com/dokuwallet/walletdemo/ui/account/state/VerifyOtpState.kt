package com.dokuwallet.walletdemo.ui.account.state

import com.dokuwallet.coresdk.domain.model.walletmodel.VerifyOtpDomain

data class VerifyOtpState(
    val isLoading: Boolean = false,
    val otp: String = "",
    val phoneNumber: String = "",
    val timer: Long = 0L,
    val verifyOtpDomain: VerifyOtpDomain? = null,
    val isError: Boolean = true
)