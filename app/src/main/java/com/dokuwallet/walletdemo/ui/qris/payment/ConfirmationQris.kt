package com.dokuwallet.walletdemo.ui.qris.payment

import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.ui.component.CustomPrimaryButton
import com.dokuwallet.walletdemo.ui.navigation.MainNavOption
import com.dokuwallet.walletdemo.ui.theme.ButtonTextStyle
import com.dokuwallet.walletdemo.ui.theme.ColorPrimary
import com.dokuwallet.walletdemo.ui.theme.FieldTextStyle
import com.dokuwallet.walletdemo.ui.theme.LightWhite
import com.dokuwallet.walletdemo.ui.theme.Neutral10
import com.dokuwallet.walletdemo.ui.theme.Neutral20
import com.dokuwallet.walletdemo.ui.theme.Neutral30
import com.dokuwallet.walletdemo.ui.theme.Neutral40
import com.dokuwallet.walletdemo.ui.theme.Neutral90
import com.dokuwallet.walletdemo.ui.theme.PagePaddingStyle
import com.dokuwallet.walletdemo.ui.theme.PageTitleTextStyle
import com.dokuwallet.walletdemo.ui.theme.Red05
import com.dokuwallet.walletdemo.ui.theme.SmallCardTextStyle
import com.dokuwallet.walletdemo.utils.CommonUtils.formatWithComma
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConfirmationQris(
    modifier: Modifier = Modifier,
    viewModel: PaymentQrisViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navigateTo: (String) -> Unit,
) {
    val componentActivity = LocalContext.current as ComponentActivity
    val coroutineScope = rememberCoroutineScope()
    val paymentQrisState: PaymentQrisState by viewModel.paymentQrisState.collectAsState()
    val feeTypeDescription : String = paymentQrisState.decodeQrDomain?.additionalInfo?.feeTypeDescription ?: ""
    val options = listOf(
        "2000",
        "5000",
        "10000",
        "15000",
        "20000"
    )
    val onSelectionChange = { text: String ->
        viewModel.setPaymentQrisState(
            paymentQrisState.copy(
                paymentAmount = "$text.00",
            )
        )
        viewModel.updateTotalPaymentOpenAmount()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(PagePaddingStyle)
    ) {
        if (paymentQrisState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
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

                Row(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.amount),
                        style = FieldTextStyle,
                        color = Neutral40
                    )
                    Text(
                        text = "Rp${
                            paymentQrisState.transactionAmount?.removeSuffix(
                                ".00"
                            ).formatWithComma()
                        }",
                        style = FieldTextStyle,
                        color = Neutral90
                    )
                }

                if (feeTypeDescription == "OPEN_TIPS") {
                    Text(
                        text = stringResource(id = R.string.tips),
                        style = FieldTextStyle,
                        color = Neutral40,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    FlowRow(modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        options.forEach { text ->
                            Box(
                                modifier = Modifier
                                    .clip(shape = RoundedCornerShape(size = 8.dp))
                                    .clickable { onSelectionChange(text) }
                                    .border(
                                        shape = RoundedCornerShape(size = 8.dp),
                                        color = if (paymentQrisState.paymentAmount.removeSuffix(".00") == text) ColorPrimary else Neutral20,
                                        width = 1.dp
                                    )
                                    .requiredWidth(102.dp)
                                    .requiredHeight(46.dp)
                            ) {
                                Text(
                                    text = "Rp${text.formatWithComma()}",
                                    style = FieldTextStyle,
                                    color = Neutral40,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.fee),
                            style = FieldTextStyle,
                            color = Neutral40
                        )
                        Text(
                            text = "Rp${
                                paymentQrisState.paymentAmount.removeSuffix(".00").formatWithComma()
                            }",
                            style = FieldTextStyle,
                            color = Neutral90
                        )
                    }
                }

                Spacer(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Neutral30)
                )

                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.total),
                        style = FieldTextStyle,
                        color = Neutral90,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Rp${paymentQrisState.totalPayment}",
                        style = PageTitleTextStyle,
                        color = Neutral90
                    )
                }

                Spacer(modifier = Modifier.weight(1F))

                CustomPrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.pay),
                    isEnable = true
                ) {
                    coroutineScope.launch {
                        viewModel.startPaymentQris(componentActivity, onSuccess = { navigateTo("${MainNavOption.PAYMENT_QRIS_RECEIPT_SCREEN}/success") }, onFailure = { navigateTo("${MainNavOption.PAYMENT_QRIS_RECEIPT_SCREEN}/failed")})
                    }
                }
            }
        }
    }
}