package com.dokuwallet.walletdemo.ui.qris.payment

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.model.ValidateType
import com.dokuwallet.walletdemo.ui.component.CustomPrimaryButton
import com.dokuwallet.walletdemo.ui.component.CustomTextField
import com.dokuwallet.walletdemo.ui.navigation.MainNavOption
import com.dokuwallet.walletdemo.ui.theme.LightWhite
import com.dokuwallet.walletdemo.ui.theme.Neutral30
import com.dokuwallet.walletdemo.ui.theme.Neutral90
import com.dokuwallet.walletdemo.ui.theme.PagePaddingStyle
import com.dokuwallet.walletdemo.ui.theme.PageTitleTextStyle
import com.dokuwallet.walletdemo.ui.theme.SmallCardTextStyle
import com.dokuwallet.walletdemo.utils.CommonUtils

@Composable
fun PaymentQris(
    modifier: Modifier = Modifier,
    viewModel: PaymentQrisViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navigateTo: (String) -> Unit,
) {
    val paymentQrisState: PaymentQrisState by viewModel.paymentQrisState.collectAsState()
    val maxAmount = 8
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(PagePaddingStyle)
    ) {
        Column {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = LightWhite),
                border = BorderStroke(1.dp, Neutral30)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = paymentQrisState.decodeQrDomain?.merchantName ?: "",
                        style = PageTitleTextStyle,
                        color = Neutral90
                    )
                    Text(
                        text = paymentQrisState.decodeQrDomain?.merchantLocation ?: "",
                        style = SmallCardTextStyle,
                        color = Neutral30
                    )
                }
            }

            CustomTextField(
                modifier = Modifier
                    .padding(top = 24.dp),
                fieldTitle = stringResource(R.string.payment_amount),
                text = paymentQrisState.transactionAmount.removeSuffix(".00"),
                hint = stringResource(R.string.fee_amount_hint),
                validateType = ValidateType.PAYMENT_AMOUNT,
                isMoneyFormat = true,
                maxChar = maxAmount
            ) { value, isError ->
                viewModel.setPaymentQrisState(
                    paymentQrisState.copy(
                        transactionAmount = value,
                        errorField = isError
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1F))
            CustomPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.next),
                isEnable = !paymentQrisState.errorField && paymentQrisState.transactionAmount.isNotEmpty()
            ) {
                viewModel.setPaymentQrisState(
                    paymentQrisState.copy(
                        transactionAmount = "${paymentQrisState.transactionAmount}.00",
                    )
                )
                viewModel.updateTotalPaymentOpenAmount()
                navigateTo(MainNavOption.CONFIRMATION_QRIS_SCREEN)
            }
        }
    }
}