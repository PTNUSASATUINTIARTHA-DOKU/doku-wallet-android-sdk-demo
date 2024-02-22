package com.dokuwallet.walletdemo.ui.qris.generate

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.AdditionalInfoReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.AmountReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.GenerateQrReq
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.GetTokenB2BReq
import com.dokuwallet.coresdk.utils.WalletEndPoint
import com.dokuwallet.walletdemo.model.WalletConfig
import com.dokuwallet.walletdemo.utils.CommonUtils
import com.dokuwallet.walletdemo.utils.CommonUtils.extractDigits
import com.dokuwallet.walletdemo.utils.CommonUtils.formatWithComma
import com.dokuwallet.walletdemo.utils.DataHolder
import com.dokuwallet.walletsdk.utils.WalletPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class GenerateQrisViewModel : ViewModel() {
    private val walletConfig = WalletConfig.default()

    private val _generateQrisState = MutableStateFlow(GenerateQrisState())
    val generateQrisState: StateFlow<GenerateQrisState> = _generateQrisState.asStateFlow()

    fun setGenerateQrisState(generateQrisState: GenerateQrisState) {
        var newTransactionAmount = "0"
        var newFeeAmount = "0"

        generateQrisState.generateQrisFields.transactionAmount.let {
            if (it.isNotEmpty()) newTransactionAmount = it
        }

        generateQrisState.generateQrisFields.feeAmount.let {
            if (it.isNotEmpty()) newFeeAmount = it
        }

        val totalPayment: Long = try {
            newTransactionAmount.extractDigits().toLong() + newFeeAmount.extractDigits().toLong()
        } catch (e: Exception) {
            0L
        }

        _generateQrisState.value =
            generateQrisState.copy(
                generateQrisFields = generateQrisState.generateQrisFields.copy(
                    totalPayment = if (totalPayment == 0L) "-" else "Rp${totalPayment.formatWithComma()}"
                )
            )
    }

    fun startGenerateQris(componentActivity: ComponentActivity, onSuccess: (String, String) -> Unit) {
        _generateQrisState.value = generateQrisState.value.copy(isLoading = true)
        var newTransactionAmount = "0"
        var newFeeAmount = "0"

        generateQrisState.value.generateQrisFields.transactionAmount.let {
            if (it.isNotEmpty()) newTransactionAmount = it
        }

        generateQrisState.value.generateQrisFields.feeAmount.let {
            if (it.isNotEmpty()) newFeeAmount = it
        }

        val feeType = when {
            !generateQrisState.value.generateQrisFields.isHaveFee -> "1"
            generateQrisState.value.generateQrisFields.selectedItem == 1 -> "2"
            else -> "3"
        }

        val generateQrReq = GenerateQrReq(
            partnerReferenceNo = CommonUtils.getNewUUID(),
            amount = AmountReq(
                newTransactionAmount,
                "IDR"
            ),
            feeAmount = if (feeType == "2") {
                AmountReq(
                    newFeeAmount,
                    "IDR"
                )
            } else null,
            merchantId = "2997",
            terminalId = "K45",
            additionalInfo = AdditionalInfoReq(
                postalCode = "13120",
                feeType = feeType
            )
        )

        val b2BHeader = CommonUtils.generateHeaderB2B(
            clientId = walletConfig.clientId,
            clientKey = walletConfig.clientKey,
            b2bToken = DataHolder.tokenB2B,
            requestBody = generateQrReq,
            httpMethod = "POST",
            endpointUrl = "/${WalletEndPoint.generateQr}",
        )

        WalletPage.startGenerateQris(
            currentViewController = componentActivity,
            b2BHeader = b2BHeader,
            generateQrReq = generateQrReq,
            onFailure = { errorData ->
                _generateQrisState.value = generateQrisState.value.copy(isLoading = false)

                when (errorData.code) {
                    "4014701" -> {
                        startTokenB2B(componentActivity) {
                            startGenerateQris(componentActivity, onSuccess)
                        }
                    }
                }
            }) { data, qrBitmap ->
            _generateQrisState.value = generateQrisState.value.copy(isLoading = false)

            DataHolder.referenceNumber = data.referenceNo.toString()
            DataHolder.partnerReferenceNumber = data.partnerReferenceNo.toString()
            DataHolder.qrContent = data.qrContent.toString()

            val fileName = "qr_${data.referenceNo?.get(4)}.png"
            val tempFile = File(componentActivity.cacheDir, fileName)
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, tempFile.outputStream())

            onSuccess(fileName, generateQrisState.value.generateQrisFields.totalPayment)
        }
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
}