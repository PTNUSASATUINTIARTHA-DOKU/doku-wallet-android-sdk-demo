package com.dokuwallet.walletdemo.ui.qris.generate

data class GenerateQrisState(
    val isLoading: Boolean = false,
    val generateQrisFields: GenerateQrisFields = GenerateQrisFields(),
    val isError: Boolean = true,
    val listErrorField: MutableList<Boolean> = List(2) { true }.toMutableList()
)

data class GenerateQrisFields(
    val transactionAmount: String = "",
    val isHaveFee: Boolean = false,
    val selectedItem: Int = 0,
    val feeAmount: String = "",
    val totalPayment: String = "",
)