package com.dokuwallet.walletdemo.ui.component

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dokuwallet.walletdemo.model.ValidateType
import com.dokuwallet.walletdemo.ui.theme.FieldErrorTextStyle
import com.dokuwallet.walletdemo.ui.theme.FieldPaddingStyle
import com.dokuwallet.walletdemo.ui.theme.FieldTextStyle
import com.dokuwallet.walletdemo.ui.theme.Neutral30
import com.dokuwallet.walletdemo.ui.theme.PlaceholderTextStyle
import com.dokuwallet.walletdemo.ui.theme.Red05
import com.dokuwallet.walletdemo.ui.theme.Red70
import com.dokuwallet.walletdemo.utils.CommonUtils
import com.dokuwallet.walletdemo.utils.CommonUtils.extractDigits
import com.dokuwallet.walletdemo.utils.CommonUtils.extractPreTitle
import com.dokuwallet.walletdemo.utils.CommonUtils.filterNumericOnly
import com.dokuwallet.walletdemo.utils.MoneyFormatTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    fieldTitle: String,
    text: String,
    hint: String? = null,
    validateType: ValidateType = ValidateType.NONE,
    isMoneyFormat: Boolean = false,
    maxChar: Int? = null,
    onValueChanged: (value: String, isError: Boolean) -> Unit
) {
    val isAllowEmpty = validateType == ValidateType.NONE
    var validationError: String? by remember {
        mutableStateOf("")
    }

    Column(modifier) {
        Text(text = fieldTitle, style = FieldTextStyle)

        Spacer(modifier = Modifier.height(4.dp))

        BasicTextField(
            value = text,
            onValueChange = {
                var sanitizedValue = if (validateType == ValidateType.NAME) {
                    it
                } else {
                    it.trim()
                }
                if (maxChar != null) {
                    sanitizedValue = CommonUtils.limitCharacters(sanitizedValue, maxChar)
                }
                if (validateType == ValidateType.AMOUNT || validateType == ValidateType.PAYMENT_AMOUNT) {
                    sanitizedValue = filterNumericOnly(sanitizedValue.filter { it.isDigit() }.trim())
                }
                if (!isAllowEmpty) validationError =
                    CommonUtils.validateInput(extractPreTitle(fieldTitle), sanitizedValue, validateType)
                onValueChanged(if (isMoneyFormat) sanitizedValue.extractDigits() else sanitizedValue, !(validationError.isNullOrEmpty()))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .border(
                    1.dp,
                    if (validationError != "") Red70 else Neutral30,
                    RoundedCornerShape(4.dp)
                ),
            textStyle = FieldTextStyle,
            keyboardOptions = KeyboardOptions(
                keyboardType = when (validateType) {
                    ValidateType.EMAIL -> KeyboardType.Email
                    ValidateType.NAME -> KeyboardType.Text
                    ValidateType.PHONE_NUMBER -> KeyboardType.Phone
                    ValidateType.NUMBER -> KeyboardType.Number
                    ValidateType.AMOUNT -> KeyboardType.Number
                    ValidateType.PAYMENT_AMOUNT -> KeyboardType.Number
                    ValidateType.OTP -> KeyboardType.Number
                    ValidateType.NONE -> KeyboardType.Text
                }
            ),
            singleLine = true,
            visualTransformation = if (isMoneyFormat && text.isNotEmpty()) MoneyFormatTransformation() else VisualTransformation.None,
            decorationBox = { innerTextField ->
                val containerColor = if (validationError != "") Red05 else Color.Transparent
                TextFieldDefaults.DecorationBox(
                    value = text,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = remember { MutableInteractionSource() },
                    placeholder = {
                        if (hint != null) {
                            Text(text = hint, style = PlaceholderTextStyle)
                        }
                    },
                    shape = OutlinedTextFieldDefaults.shape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = containerColor,
                        unfocusedContainerColor = containerColor,
                        disabledContainerColor = containerColor,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    contentPadding = FieldPaddingStyle,
                )
            }
        )

        if (validationError != "") {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = validationError.toString(),
                style = FieldErrorTextStyle
            )
        }
    }
}