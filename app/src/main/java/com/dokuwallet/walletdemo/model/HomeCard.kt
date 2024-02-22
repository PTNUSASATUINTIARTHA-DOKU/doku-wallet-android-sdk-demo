package com.dokuwallet.walletdemo.model

import androidx.annotation.DrawableRes

data class HomeCard(
    val title: String,
    @DrawableRes val icon: Int,
    val action: () -> Unit
)
