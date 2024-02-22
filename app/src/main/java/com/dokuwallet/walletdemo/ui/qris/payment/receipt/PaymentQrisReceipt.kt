package com.dokuwallet.walletdemo.ui.qris.payment.receipt

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.ui.component.CustomPrimaryButton
import com.dokuwallet.walletdemo.ui.navigation.MainNavOption
import com.dokuwallet.walletdemo.ui.qris.payment.PaymentQrisState
import com.dokuwallet.walletdemo.ui.qris.payment.PaymentQrisViewModel
import com.dokuwallet.walletdemo.ui.theme.BottomSheetTitleTextStyle
import com.dokuwallet.walletdemo.ui.theme.FieldTextStyle
import com.dokuwallet.walletdemo.ui.theme.FieldTitleTextStyle
import com.dokuwallet.walletdemo.ui.theme.LargeAmountTextStyle
import com.dokuwallet.walletdemo.ui.theme.Neutral30
import com.dokuwallet.walletdemo.ui.theme.Neutral90
import com.dokuwallet.walletdemo.ui.theme.PageDescTextStyle
import com.dokuwallet.walletdemo.ui.theme.PagePaddingStyle
import com.dokuwallet.walletdemo.ui.theme.PageTitleTextStyle
import com.dokuwallet.walletdemo.ui.theme.SmallCardTextStyle
import com.dokuwallet.walletdemo.ui.theme.SmallDateTextStyle
import com.dokuwallet.walletdemo.utils.CommonUtils
import com.dokuwallet.walletdemo.utils.CommonUtils.formatWithComma
import com.dokuwallet.walletdemo.utils.CommonUtils.removeSeconds
import kotlinx.coroutines.launch
import java.lang.Integer.parseInt

data class PaymentData(val title: String, val data: String)
@Composable
fun PaymentQrisReceipt(
    modifier: Modifier = Modifier,
    viewModel: PaymentQrisViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    state : String = "failed",
    navigateTo: (String) -> Unit,
) {
    val successIcon = R.drawable.icon_success
    val failedIcon = R.drawable.icon_failed
    val icon = if (state == "success") successIcon else failedIcon
    val titleText = if (state == "success") R.string.success_payment else R.string.failed_payment
    val paymentQrisState: PaymentQrisState by viewModel.paymentQrisState.collectAsState()
    val paymentDataList = listOf(
        if (paymentQrisState.paymentQrDomain?.referenceNo?.isNotEmpty() == true) PaymentData(title = "No Referensi", data = paymentQrisState.paymentQrDomain?.referenceNo ?: "") else null,
        PaymentData(title = "Tanggal", data = CommonUtils.getDateOnly(paymentQrisState.transactionDate)),
        PaymentData(title = "Waktu", data = CommonUtils.getTimeOnly(paymentQrisState.transactionDate)),
        PaymentData(title = "Nominal", data = "Rp${paymentQrisState.transactionAmount?.removeSuffix(
            ".00"
        ).formatWithComma()}"),
        if (paymentQrisState.paymentAmount != "0.00") PaymentData(title = "Fee", data = "Rp${
            paymentQrisState.paymentAmount.removeSuffix(
            ".00"
        ).formatWithComma()}") else null
    )
    val filteredList = paymentDataList.filterNotNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(PagePaddingStyle), contentAlignment = Alignment.Center
    ) {
                Column(
                    modifier = modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 16.dp),
                        painter = painterResource(id = icon),
                        contentDescription = null
                    )
                    Text(
                        text = "${paymentQrisState.totalPayment}",
                        style = LargeAmountTextStyle,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = stringResource(id = titleText),
                        style = FieldTextStyle,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "${paymentQrisState?.transactionDate.removeSeconds()}",
                        style = SmallDateTextStyle,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                modifier = Modifier.align(Alignment.Start),
                                text = paymentQrisState.decodeQrDomain?.merchantName ?: "",
                                style = FieldTitleTextStyle,
                                textAlign = TextAlign.Start
                            )
                            Text(
                                modifier = Modifier.align(Alignment.Start),
                                text = paymentQrisState.decodeQrDomain?.merchantLocation ?: "",
                                style = FieldTextStyle,
                                textAlign = TextAlign.Start
                            )
                    Spacer(modifier = Modifier.height(24.dp))
                    PaymentDataList(paymentDataList = filteredList)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TOTAL BAYAR",
                            style = FieldTitleTextStyle,
                        )
                        Text(
                            text = "Rp${paymentQrisState.totalPayment}",
                            style = FieldTitleTextStyle,
                        )
                    }
                    Spacer(modifier = Modifier.weight(1F))

                    CustomPrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.back_to_home),
                        isEnable = true
                    ) {
                        navigateTo(MainNavOption.HOME_SCREEN)
                    }
                }
            }
}

@Composable
fun PaymentDataList(paymentDataList: List<PaymentData>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(paymentDataList) { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    style = PageDescTextStyle,
                )
                Text(
                    text = item.data,
                    style = FieldTextStyle,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
