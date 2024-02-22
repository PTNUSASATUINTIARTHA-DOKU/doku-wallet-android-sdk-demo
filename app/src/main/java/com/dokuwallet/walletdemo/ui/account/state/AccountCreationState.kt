package com.dokuwallet.walletdemo.ui.account.state

import com.dokuwallet.coresdk.domain.model.walletmodel.AccountCreationDomain

data class AccountCreationState(
    val isLoading: Boolean = false,
    val accountCreationFields: AccountCreationFields = AccountCreationFields(),
    val accountCreationDomain: AccountCreationDomain? = null,
    val listErrorField: MutableList<Boolean> = List(3) { true }.toMutableList()
)

data class AccountCreationFields(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
)