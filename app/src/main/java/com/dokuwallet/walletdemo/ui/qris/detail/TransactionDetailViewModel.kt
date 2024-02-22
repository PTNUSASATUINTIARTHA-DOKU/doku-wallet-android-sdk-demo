package com.dokuwallet.walletdemo.ui.qris.detail

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.GetTokenB2BReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.QueryQrReq
import com.dokuwallet.coresdk.utils.WalletEndPoint
import com.dokuwallet.walletdemo.model.WalletConfig
import com.dokuwallet.walletdemo.utils.CommonUtils
import com.dokuwallet.walletdemo.utils.DataHolder
import com.dokuwallet.walletsdk.utils.WalletPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class TransactionDetailViewModel: ViewModel() {
    private val walletConfig = WalletConfig.default()

    private var timer: Timer? = null

    private val _transactionDetailState = MutableStateFlow(TransactionDetailState())
    val transactionDetailState: StateFlow<TransactionDetailState> =
        _transactionDetailState.asStateFlow()

    fun startQueryQris(componentActivity: ComponentActivity, onSuccess: (state : String) -> Unit = {}) {
        _transactionDetailState.value = transactionDetailState.value.copy(isLoading = true)
        val queryQrReq = QueryQrReq(
            DataHolder.referenceNumber,
            DataHolder.partnerReferenceNumber,
            "51",
            "2997"
        )

        val b2BHeader = CommonUtils.generateHeaderB2B(
            clientId = walletConfig.clientId,
            clientKey = walletConfig.clientKey,
            b2bToken = DataHolder.tokenB2B,
            requestBody = queryQrReq,
            httpMethod = "POST",
            endpointUrl = "/${WalletEndPoint.queryQr}",
        )

        WalletPage.startQueryQris(
            currentViewController = componentActivity,
            b2BHeader = b2BHeader,
            queryQrReq = queryQrReq,
            onFailure = { errorData ->
                _transactionDetailState.value = transactionDetailState.value.copy(isLoading = false)
                when (errorData.code) {
                    "4015101" -> {
                        startTokenB2B(componentActivity) {
                            startQueryQris(componentActivity, onSuccess)
                        }
                    }
                    else -> {
                        Log.d("TAG", "error : $errorData")
                    }
                }
            }) {
            _transactionDetailState.value = transactionDetailState.value.copy(isLoading = false)
            DataHolder.referenceNumber = it.originalReferenceNo.toString()
            DataHolder.partnerReferenceNumber = it.originalPartnerReferenceNo.toString()
            when (it.transactionStatusDesc) {
                "Success" -> onSuccess("success")
                "Pending" -> startTimer(15_000) {
                    startQueryQris(componentActivity, onSuccess)
                }
                else -> onSuccess("failed")
            }
        }
    }

    fun startTimer(totalTime: Long, onTimesUp: () -> Unit) {
        var remainingTime = totalTime

        timer?.cancel()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                _transactionDetailState.value =
                    transactionDetailState.value.copy(timer = remainingTime/ 1000)
                remainingTime -= 1000
                if (transactionDetailState.value.timer < 0) {
                    timer?.cancel()
                    _transactionDetailState.value = transactionDetailState.value.copy(timer = 0)
                    viewModelScope.launch {
                        onTimesUp()
                    }
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
        timer = null
        super.onCleared()
    }
}