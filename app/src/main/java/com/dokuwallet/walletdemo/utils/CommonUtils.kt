package com.dokuwallet.walletdemo.utils

import android.content.Context
import android.os.Build
import android.util.Log
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.AuthHeader
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.B2B2CHeader
import com.dokuwallet.coresdk.data.network.parameter.walletparameter.B2BHeader
import com.dokuwallet.coresdk.utils.CommonUtils
import com.dokuwallet.walletdemo.model.ValidateType
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object CommonUtils {
    private const val PHONE_NUMBER_MIN = 11
    private var isPhoneFormatInvalid = false
    private var isAmountTooSmall = false
    private var isAmountTooBig = false
    private var isAmountPaymentTooSmall = false
    private const val minAmount = 1
    private const val maxAmount = 10000000
    private const val minAmountPayment = 1000

    suspend fun Context.getCameraProvider(): ProcessCameraProvider =
        suspendCoroutine { continuation ->
            ProcessCameraProvider.getInstance(this).also { cameraProvider ->
                cameraProvider.addListener({
                    continuation.resume(cameraProvider.get())
                }, ContextCompat.getMainExecutor(this))
            }
        }

    fun getParamFromUrl(url: String?, param: String): String? {
        val regex = when (param) {
            "accountId" -> Regex("(?<=\\?accountId=)\\d+")
            "authCode" -> Regex("(?<=\\?authCode=)[^&]+")
            else -> Regex("")
        }
        val matchResult = url?.let { regex.find(it) }
        return matchResult?.value
    }

    fun validateInput(
        title: String,
        value: String,
        validateType: ValidateType
    ): String {
        val newTitle = if (title == "OTP") {
            title
        } else {
            title.lowercase().replaceFirstChar {
                it.uppercase()
            }
        }
        val isValid: Boolean

        return if (value.isEmpty()) {
            "$newTitle wajib diisi"
        } else {
            isValid = when (validateType) {
                ValidateType.EMAIL -> Patterns.EMAIL_ADDRESS.matcher(value).matches()
                ValidateType.NAME -> isValidName(value)
                ValidateType.PHONE_NUMBER -> isValidPhoneNumber(value)
                ValidateType.AMOUNT -> isValidAmount(value, false)
                ValidateType.PAYMENT_AMOUNT -> isValidAmount(value, true)
                ValidateType.OTP -> isValidOtp(value)
                else -> true
            }
            if (!isValid) {
                when (validateType) {
                    ValidateType.PHONE_NUMBER -> if (isPhoneFormatInvalid) {
                        "Format nomor telepon salah"
                    } else {
                        "$newTitle tidak valid"
                    }
                    ValidateType.AMOUNT -> {
                        when {
                            isAmountTooBig -> "$newTitle maksimum Rp10.000.000"
                            isAmountTooSmall -> "$newTitle minimum Rp1"
                            else -> "$newTitle tidak valid"
                        }
                    }
                    ValidateType.PAYMENT_AMOUNT -> {
                        when {
                            isAmountPaymentTooSmall -> "$newTitle minimum Rp1000"
                            isAmountTooBig -> "$newTitle maksimum Rp10.000.000"
                            else -> "$newTitle tidak valid"
                        }
                    }
                    else -> "$newTitle tidak valid"
                }
            }
            else ""
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        if (!phoneNumber.startsWith("628")) {
            isPhoneFormatInvalid = true
            return false
        }
        if (phoneNumber.length < PHONE_NUMBER_MIN) {
            isPhoneFormatInvalid = false
            return false
        }
        return true
    }

    private fun isValidName(value: String): Boolean {
        if (value.length < 3) {
            return false
        }
        return true
    }

    private fun isValidAmount(value : String, isPayment : Boolean): Boolean {
        isAmountTooBig = false
        isAmountTooSmall = false
        isAmountPaymentTooSmall = false
        if (value.toLong() > maxAmount){
            isAmountTooBig = true
            return false
        } else if (!isPayment)  {
            if (value.toLong() < minAmount) {
                isAmountTooSmall = true
                return false
            }
        } else if (isPayment) {
            if (value.toLong() < minAmountPayment) {
                isAmountPaymentTooSmall = true
                return false
            }
        }
        return true
    }

    private fun isValidOtp(value : String): Boolean {
        if (value.length < 6) {
            return false
        }
        return true
    }

    fun getNewUUID() = UUID.randomUUID().toString().replace("-", "")

    fun Long?.formatWithComma(): String =
        NumberFormat.getNumberInstance(Locale("id", "ID")).format(this ?: 0)

    fun String?.formatWithComma(): String =
        try {
            val value = this?.toLongOrNull() ?: 0
            NumberFormat.getNumberInstance(Locale("id", "ID")).format(value)
        } catch (e: NumberFormatException) {
            // Handle the case when the string cannot be converted to a number
            this ?: ""
        }

    fun String.extractDigits(): String {
        val numberRegex = "\\d+"
        val regex = Regex(numberRegex)
        val matches = regex.findAll(this)
        val resultBuilder = StringBuilder()
        for (match in matches) {
            resultBuilder.append(match.value)
        }
        return resultBuilder.toString()
    }

    fun generateHeaderAuth(privateKey: String, clientId: String): AuthHeader {
        val timeStamp = CommonUtils.getCurrentTimestamp(2)
        return AuthHeader(
            clientId,
            timeStamp,
            CommonUtils.sha256WithRSA(
                CommonUtils.stringToSignToken(clientId, timeStamp),
                privateKey
            )
        )
    }

    fun <T> generateHeaderB2B(
        clientId: String,
        clientKey: String,
        b2bToken: String,
        requestBody: T,
        httpMethod: String,
        endpointUrl: String
    ): B2BHeader {
        val timeStamp = CommonUtils.getCurrentTimestamp(2)

        val stringToSign = CommonUtils.stringToSign(
            b2bToken = b2bToken,
            requestBody = requestBody,
            httpMethod = httpMethod,
            endpointUrl = endpointUrl,
            timestamp = timeStamp
        )

        return B2BHeader(
            xTimestamp = timeStamp,
            xPartnerId = clientId,
            xExternalId = timeStamp.substring(11, 19).replace(":", "")
                .plus(CommonUtils.generateRandomNumber(5)),
            xSignature = CommonUtils.hmacSha512(
                stringToSign,
                clientKey
            ),
            authorization = "Bearer $b2bToken",
        )
    }

    fun <T> generateHeaderB2B2C(
        clientId: String,
        clientKey: String,
        b2bToken: String,
        b2b2cToken: String,
        requestBody: T,
        httpMethod: String,
        endpointUrl: String
    ): B2B2CHeader {
        val timeStamp = CommonUtils.getCurrentTimestamp(2)

        val stringToSign = CommonUtils.stringToSign(
            b2bToken,
            requestBody,
            httpMethod,
            endpointUrl,
            timeStamp
        )

        return B2B2CHeader(
            xTimestamp = timeStamp,
            xPartnerId = clientId,
            xExternalId = timeStamp.substring(11, 19).replace(":", "")
                .plus(CommonUtils.generateRandomNumber(5)),
            xSignature = CommonUtils.hmacSha512(
                stringToSign,
                clientKey
            ),
            authorization = "Bearer $b2bToken",
            authorizationCustomer = "Bearer $b2b2cToken",
        )
    }

    fun filterAlphaOnly(input: String): String {
        val allowedPattern = Regex("[a-zA-Z\\s]")
        return input.filter { it.toString().matches(allowedPattern) }
    }

    fun filterNumericOnly(input: String): String {
        val allowedPattern = Regex("[0-9]")
        return if(input.isNotEmpty()) {
            input.toLong().toString().filter { it.toString().matches(allowedPattern)}
        } else {
            input
        }
    }

    fun limitCharacters(input: String, length: Int): String {
        val limitedText = if (input.length > length) {
            input.substring(0, length) // Limit to 14 characters
        } else {
            input
        }
        return limitedText
    }

    fun extractPreTitle(input: String): String {
        val keyword = "Masukkan"
        val index = input.indexOf(keyword)

        return if (index != -1) {
            input.substring(index + keyword.length).trim()
        } else {
            input
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDate(input: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy - HH:mm:ss", Locale("id"))
        return input.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateFromIso(inputDate: String): String {
        val inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy - HH:mm:ss", Locale("id", "ID"))

        val zonedDateTime = ZonedDateTime.parse(inputDate, inputFormatter)
        val localDateTime = LocalDateTime.ofInstant(zonedDateTime.toInstant(), ZoneId.systemDefault())

        return localDateTime.format(outputFormatter)
    }

    fun getDateOnly(input : String):String {
        return if (input.isNotEmpty()) {
            input.split("-")[0].trim()
        } else {
            input
        }
    }

    fun getTimeOnly(input : String):String {
        return if (input.isNotEmpty()) {
            input.split("-")[1].trim()
        } else {
            input
        }
    }

    fun String?.removeSeconds(): String = this?.takeIf { it.isNotEmpty() }?.run {
        lastIndexOf(':')?.let { substring(0, it) } ?: this
    } ?: ""

    fun spacedPhoneNumber(inputString: String): String {
        val spacedStringBuilder = StringBuilder()
        val firstFour = inputString.substring(0, 4)
        spacedStringBuilder.append(firstFour).append(" ")
        val remaining = inputString.substring(4)
        var i = 0
        while (i < remaining.length) {
            val endIndex = kotlin.math.min(i + 3, remaining.length)
            spacedStringBuilder.append(remaining.substring(i, endIndex))
            if (endIndex != remaining.length) {
                spacedStringBuilder.append(" ")
            }
            i += 3
        }
        return spacedStringBuilder.toString()
    }
}