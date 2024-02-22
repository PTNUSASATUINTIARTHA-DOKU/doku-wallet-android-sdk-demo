package com.dokuwallet.walletdemo.ui.account

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.AccountBindingReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.AccountCreationReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.GetTokenB2BReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.SuccessParamsReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.VerifyOtpReq
import com.dokuwallet.coresdk.utils.WalletEndPoint
import com.dokuwallet.walletdemo.model.WalletConfig
import com.dokuwallet.walletdemo.ui.account.state.AccountCreationState
import com.dokuwallet.walletdemo.ui.account.state.AccountWebViewState
import com.dokuwallet.walletdemo.ui.account.state.VerifyOtpState
import com.dokuwallet.walletdemo.utils.CommonUtils
import com.dokuwallet.walletdemo.utils.DataHolder
import com.dokuwallet.walletsdk.utils.WalletPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class AccountViewModel : ViewModel() {
    private var referenceNumber = ""
        get() {
            return if (field == "") CommonUtils.getNewUUID() else field
        }
    private var partnerReferenceNumber = ""
        get() {
            return if (field == "") CommonUtils.getNewUUID() else field
        }

    private val walletConfig = WalletConfig.default()

    private val _accountCreationState = MutableStateFlow(AccountCreationState())
    val accountCreationState: StateFlow<AccountCreationState> = _accountCreationState.asStateFlow()

    private val _verifyOtpState = MutableStateFlow(VerifyOtpState())
    val verifyOtpState: StateFlow<VerifyOtpState> = _verifyOtpState.asStateFlow()

    private val _accountWebViewState = MutableStateFlow(AccountWebViewState())
    val accountWebViewState: StateFlow<AccountWebViewState> = _accountWebViewState.asStateFlow()

    private var timer: Timer? = null

    fun setAccountCreationState(accountCreationState: AccountCreationState) {
        _accountCreationState.value = accountCreationState
    }

    fun setVerifyOtpState(verifyOtpState: VerifyOtpState) {
        _verifyOtpState.value = verifyOtpState
    }

    fun setAccountWebViewState(accountWebViewState: AccountWebViewState) {
        _accountWebViewState.value = accountWebViewState
    }

    fun startAccountCreation(componentActivity: ComponentActivity, onSuccess: () -> Unit = {},
                             onFailure: (String) -> Unit ) {
        _accountCreationState.value = accountCreationState.value.copy(
            isLoading = true
        )

        val accountCreationReq = AccountCreationReq(
            CommonUtils.getNewUUID(),
            accountCreationState.value.accountCreationFields.email,
            accountCreationState.value.accountCreationFields.fullName,
            accountCreationState.value.accountCreationFields.phoneNumber,
            walletConfig.redirectUrl
        )

        val b2BHeader = CommonUtils.generateHeaderB2B(
            clientId = walletConfig.clientId,
            clientKey = walletConfig.clientKey,
            b2bToken = DataHolder.tokenB2B,
            requestBody = accountCreationReq,
            httpMethod = "POST",
            endpointUrl = "/${WalletEndPoint.accountCreation}",
        )

        var failureMessage : String? = null

        WalletPage.startAccountCreation(
            currentViewController = componentActivity,
            b2BHeader = b2BHeader,
            accountCreationReq = accountCreationReq,
            onFailure = { errorData ->
                _accountCreationState.value = accountCreationState.value.copy(
                    isLoading = false
                )
                _verifyOtpState.value = verifyOtpState.value.copy(isLoading = false)

                when (errorData.code) {
                    "4010601" -> {
                        startTokenB2B(componentActivity) {
                            startAccountCreation(componentActivity, onSuccess, onFailure)
                        }
                    }

                    "4000600" -> {
                        failureMessage = errorData.message ?: "Unknown Error"
                        onFailure(failureMessage ?: "")
                    }

                    else -> {
                        failureMessage = errorData.message ?: "Unknown Error"
                        onFailure(failureMessage ?: "")
                    }

                }
            }
        ) {
            _accountCreationState.value = accountCreationState.value.copy(
                isLoading = false,
                accountCreationDomain = it
            )
            _verifyOtpState.value =
                verifyOtpState.value.copy(phoneNumber = accountCreationState.value.accountCreationFields.phoneNumber)
            referenceNumber = it.referenceNo.toString()
            partnerReferenceNumber = it.partnerReferenceNo.toString()

            startTimer(300_000)
            onSuccess()
        }
    }

    fun startVerifyOtp(componentActivity: ComponentActivity, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val verifyOtpReq = VerifyOtpReq(
            partnerReferenceNumber,
            referenceNumber,
            verifyOtpState.value.otp
        )

        val b2BHeader = CommonUtils.generateHeaderB2B(
            clientId = walletConfig.clientId,
            clientKey = walletConfig.clientKey,
            b2bToken = DataHolder.tokenB2B,
            requestBody = verifyOtpReq,
            httpMethod = "POST",
            endpointUrl = "/${WalletEndPoint.verifyOtp}",
        )

        WalletPage.startVerifyOtp(
            currentViewController = componentActivity,
            b2BHeader = b2BHeader,
            verifyOtpReq = verifyOtpReq,
            onFailure = { errorData ->
                onFailure(errorData.message ?: "")
            }) {
            _verifyOtpState.value = verifyOtpState.value.copy(verifyOtpDomain = it)
            _accountWebViewState.value = accountWebViewState.value.copy(
                url = "${it.qparamsURL}?token=${it.qparams?.token}"
            )
            onSuccess()
        }
    }

    fun resendOtp(componentActivity: ComponentActivity) {
        _verifyOtpState.value = verifyOtpState.value.copy(isLoading = true)
        viewModelScope.launch {
            startAccountCreation(componentActivity) {
                _verifyOtpState.value = verifyOtpState.value.copy(isLoading = false)
                startTimer(300_000)
            }
        }
    }

    fun startAccountBinding(componentActivity: ComponentActivity, accountId: String) {
        _accountWebViewState.value = accountWebViewState.value.copy(isLoading = true)
        val accountBindingReq = AccountBindingReq(
            CommonUtils.getNewUUID(),
            walletConfig.redirectUrl,
            SuccessParamsReq(accountId = accountId)
        )

        val b2BHeader = CommonUtils.generateHeaderB2B(
            clientId = walletConfig.clientId,
            clientKey = walletConfig.clientKey,
            b2bToken = DataHolder.tokenB2B,
            requestBody = accountBindingReq,
            httpMethod = "POST",
            endpointUrl = "/${WalletEndPoint.accountBinding}",
        )

        WalletPage.startAccountBinding(
            currentViewController = componentActivity,
            b2BHeader = b2BHeader,
            accountBindingReq = accountBindingReq,
            onFailure = { errorData ->
                _accountWebViewState.value =
                    accountWebViewState.value.copy(isLoading = false, accountId = accountId)
            }) {
            _accountWebViewState.value = accountWebViewState.value.copy(
                isLoading = false,
                url = it.nextAction.toString(),
                accountId = accountId
            )
        }
    }

    private fun startTimer(totalTime: Long) {
        var remainingTime = totalTime

        timer?.cancel() // Cancel the previous timer if any
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                _verifyOtpState.value = verifyOtpState.value.copy(timer = remainingTime / 1000)
                remainingTime -= 1000

                if (remainingTime < 0) {
                    timer?.cancel()
                    _verifyOtpState.value = verifyOtpState.value.copy(timer = 0)
                }
            }
        }, 0, 1000)
    }

    private fun startTokenB2B(componentActivity: ComponentActivity, onSuccess: () -> Unit) {
        val authHeader = CommonUtils.generateHeaderAuth(
            walletConfig.privateKey,
            walletConfig.clientId
        )
        WalletPage.startGetB2BToken(
            currentViewController = componentActivity,
            authHeader = authHeader,
            getTokenB2BReq = GetTokenB2BReq("client_credentials"),
            onFailure = { errorData ->

            }) {
            DataHolder.tokenB2B = it.accessToken.toString()
            onSuccess()
        }
    }

    override fun onCleared() {
        timer?.cancel()
        super.onCleared()
    }
}