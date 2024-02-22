package com.dokuwallet.walletdemo.ui.qris.payment

import android.annotation.SuppressLint
import android.os.Build
import android.util.JsonWriter
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.AdditionalInfoPaymentReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.AdditionalInfoReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.AmountReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.DecodeQrReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.GetTokenB2B2CReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.GetTokenB2BReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.PaymentQrReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.QueryAccountBindingReq
import com.dokuwallet.coresdk.utils.WalletEndPoint
import com.dokuwallet.coresdk.vo.ErrorData
import com.dokuwallet.walletdemo.model.WalletConfig
import com.dokuwallet.walletdemo.utils.CommonUtils
import com.dokuwallet.walletdemo.utils.CommonUtils.formatDate
import com.dokuwallet.walletdemo.utils.CommonUtils.formatDateFromIso
import com.dokuwallet.walletdemo.utils.CommonUtils.formatWithComma
import com.dokuwallet.walletdemo.utils.DataHolder
import com.dokuwallet.walletsdk.utils.WalletPage
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import com.dokuwallet.coresdk.utils.CommonUtils as coreUtils

class PaymentQrisViewModel : ViewModel() {
    private val walletConfig = WalletConfig.default()
    private val _paymentQrisState = MutableStateFlow(PaymentQrisState())
    val paymentQrisState: StateFlow<PaymentQrisState> = _paymentQrisState.asStateFlow()
    var qrContents=""
    fun setPaymentQrisState(paymentQrisState: PaymentQrisState) {
        _paymentQrisState.value = paymentQrisState
    }

    fun startDecodeQris(
        componentActivity: ComponentActivity,
        qrCode: String,
        onSuccess: (String?) -> Unit,
        onFailure: (String?) -> Unit
    ) {
        _paymentQrisState.value = paymentQrisState.value.copy(isLoading = true, isScanFlag = true)

        val decodeQrReq = DecodeQrReq(
            CommonUtils.getNewUUID(),
            qrCode,
            coreUtils.getCurrentTimestamp(2)
        )

        val b2BHeader = CommonUtils.generateHeaderB2B(
            clientId = walletConfig.clientId,
            clientKey = walletConfig.clientKey,
            b2bToken = DataHolder.tokenB2B,
            requestBody = decodeQrReq,
            httpMethod = "POST",
            endpointUrl = "/${WalletEndPoint.decodeQr}",
        )

        WalletPage.startDecodeQris(
            currentViewController = componentActivity,
            b2BHeader = b2BHeader,
            decodeQrReq = decodeQrReq,
            onFailure = { errorData ->
                _paymentQrisState.value = paymentQrisState.value.copy(isLoading = false)
                when (errorData.code) {
                    "4014801" -> {
                        startTokenB2B(componentActivity) {
                            startDecodeQris(componentActivity, qrCode, onSuccess, onFailure)
                        }
                    }
                    "4014800" -> {
                        onFailure(errorData.message)
                    }
                    else -> {
                        onFailure(errorData.message)
                    }
                }
            }) { data ->
            val newData = data.copy(
                transactionAmount = data.transactionAmount?.copy(
                    value = data.transactionAmount?.value ?: "0.00"
                ),
                feeAmount = data.feeAmount?.copy(
                    value = data.feeAmount?.value ?: "0.00"
                )
            )
            qrContents=qrCode
            _paymentQrisState.value = paymentQrisState.value.copy(
                isLoading = false,
                decodeQrDomain = newData,
                paymentAmount = newData.feeAmount?.value ?: "0.00",
                transactionAmount = newData.transactionAmount?.value ?: ""
            )
            onSuccess(newData.additionalInfo?.pointOfInitiationMethod)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    fun startPaymentQris(componentActivity: ComponentActivity, onSuccess: () -> Unit, onFailure: () -> Unit) {
        _paymentQrisState.value = paymentQrisState.value.copy(isLoading = true)

        val paymentQrReq = PaymentQrReq(
            partnerReferenceNo = CommonUtils.getNewUUID(),
            amount = AmountReq(
                paymentQrisState.value.transactionAmount,
                paymentQrisState.value.decodeQrDomain?.transactionAmount?.currency ?: "IDR"
            ),
            feeAmount = AmountReq(
                paymentQrisState.value.paymentAmount,
                paymentQrisState.value.decodeQrDomain?.transactionAmount?.currency ?: "IDR"
            ),
            additionalInfo = AdditionalInfoPaymentReq(
                qrContent = qrContents
            )
        )

        val b2B2cHeader = CommonUtils.generateHeaderB2B2C(
            clientId = walletConfig.clientId,
            clientKey = walletConfig.clientKey,
            b2bToken = DataHolder.tokenB2B,
            b2b2cToken = DataHolder.tokenB2B2C,
            requestBody = paymentQrReq,
            httpMethod = "POST",
            endpointUrl = "/${WalletEndPoint.paymentQr}",
        )

            WalletPage.startPaymentQris(
                currentViewController = componentActivity,
                b2B2CHeader = b2B2cHeader,
                paymentQrReq = paymentQrReq,
                onFailure = { errorData ->
                    _paymentQrisState.value = paymentQrisState.value.copy(
                        isLoading = false, transactionDate = formatDate(
                            LocalDateTime.now()
                        )
                    )

                    when (errorData.code) {
                        "4015001" -> {
                            startQueryAccountBinding(componentActivity = componentActivity) {
                                startTokenB2B2C(componentActivity) {
                                    startPaymentQris(componentActivity, onSuccess, onFailure)
                                }
                            }
                        }

                        else -> {
                            onFailure()
                        }
                    }
                }) { data ->
                _paymentQrisState.value = paymentQrisState.value.copy(
                    isLoading = false, paymentQrDomain = data,
                    transactionAmount = data.amount?.value ?: "0.00",
                    paymentAmount = data.feeAmount?.value ?: "0.00",
                    transactionDate = formatDateFromIso(data.transactionDate ?: "")
                )
                updateTotalPaymentReceipt()
                onSuccess()
            }
    }

    fun updateTotalPayment() {
        if (paymentQrisState.value.decodeQrDomain == null) return

        val totalPayment =
            paymentQrisState.value.decodeQrDomain?.transactionAmount?.value?.removeSuffix(".00")!!
                .toLong() + (paymentQrisState.value.paymentAmount.removeSuffix(".00")).toLong()

        _paymentQrisState.value = paymentQrisState.value.copy(
            totalPayment = totalPayment.formatWithComma()
        )
    }

    fun updateTotalPaymentOpenAmount() {
        if (paymentQrisState.value.decodeQrDomain == null) return

        val totalPayment =
            paymentQrisState.value.transactionAmount?.removeSuffix(".00")!!
                .toLong() + (paymentQrisState.value.paymentAmount.removeSuffix(".00")).toLong()

        _paymentQrisState.value = paymentQrisState.value.copy(
            totalPayment = totalPayment.formatWithComma()
        )
    }

    fun updateTotalPaymentReceipt() {
        if (paymentQrisState.value.decodeQrDomain == null) return

        val totalPayment =
            paymentQrisState.value.paymentQrDomain?.amount?.value?.removeSuffix(".00")!!
                .toLong() + (paymentQrisState.value.paymentQrDomain?.feeAmount?.value?.removeSuffix(".00"))!!.toLong()

        _paymentQrisState.value = paymentQrisState.value.copy(
            totalPayment = totalPayment.formatWithComma()
        )
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

    private fun startTokenB2B2C(componentActivity: ComponentActivity, onSuccess: () -> Unit) {
        val authHeader = CommonUtils.generateHeaderAuth(
            walletConfig.privateKey,
            walletConfig.clientId
        )
        WalletPage.startGetB2B2CToken(
            currentViewController = componentActivity,
            authHeader = authHeader,
            getTokenB2B2CReq = GetTokenB2B2CReq(
                grantType = "authorization_code",
                authCode = DataHolder.authCode
            ),
            onFailure = { errorData ->

            }) {
            DataHolder.tokenB2B2C = it.accessToken.toString()
            onSuccess()
        }
    }

    private fun startQueryAccountBinding(componentActivity: ComponentActivity, onSuccess: () -> Unit) {
        val queryAccountBindingReq = QueryAccountBindingReq(
            additionalInfo = AdditionalInfoReq(
                accountId = DataHolder.accountId
            ),
            partnerReferenceNo = CommonUtils.getNewUUID(),
        )

        val b2BHeader = CommonUtils.generateHeaderB2B(
            clientId = walletConfig.clientId,
            clientKey = walletConfig.clientKey,
            b2bToken = DataHolder.tokenB2B,
            requestBody = queryAccountBindingReq,
            httpMethod = "POST",
            endpointUrl = "/${WalletEndPoint.queryAccountBinding}",
        )

        WalletPage.startQueryAccountBinding(
            b2BHeader = b2BHeader,
            currentViewController = componentActivity,
            queryAccountBindingReq = queryAccountBindingReq,
            onFailure = { ErrorData -> Log.d("TAG", "Error query acc binding : $ErrorData")})
        {
                val decodeAuthCode = coreUtils.decryptInputKey(
                it.additionalInfo?.authCode.toString(),
                walletConfig.secretKey
            )
            DataHolder.authCode = decodeAuthCode
            onSuccess()
        }
    }
}