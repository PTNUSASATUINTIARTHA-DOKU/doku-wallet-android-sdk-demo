package com.dokuwallet.walletdemo.ui.qris.detail

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.ui.component.CustomPrimaryButton
import com.dokuwallet.walletdemo.ui.component.CustomSecondaryButton
import com.dokuwallet.walletdemo.ui.navigation.MainNavOption
import com.dokuwallet.walletdemo.ui.theme.CardBigTextStyle
import com.dokuwallet.walletdemo.ui.theme.CardPaddingStyle
import com.dokuwallet.walletdemo.ui.theme.LightWhite
import com.dokuwallet.walletdemo.ui.theme.Neutral10
import com.dokuwallet.walletdemo.ui.theme.Neutral30
import com.dokuwallet.walletdemo.ui.theme.Neutral40
import com.dokuwallet.walletdemo.ui.theme.Neutral80
import com.dokuwallet.walletdemo.ui.theme.PlaceholderTextStyle
import com.dokuwallet.walletdemo.ui.theme.ScrollablePagePaddingStyle
import com.dokuwallet.walletdemo.ui.theme.SmallCardTextStyle
import com.dokuwallet.walletdemo.ui.theme.SmallTextStyle

@Composable
fun TransactionDetail(
    modifier: Modifier = Modifier,
    viewModel: TransactionDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    qrBitmap: Bitmap,
    totalPayment: String,
    navigateTo: (String) -> Unit,
) {
    val transactionDetailState: TransactionDetailState by viewModel.transactionDetailState.collectAsState()
    val componentActivity = LocalContext.current as androidx.activity.ComponentActivity

    val iconWallets = listOf(
        R.drawable.ic_doku,
        R.drawable.ic_gopay,
        R.drawable.ic_ovo,
        R.drawable.ic_linkaja,
        R.drawable.ic_dana,
        R.drawable.ic_bca_mobile,
        R.drawable.ic_bni_mobile,
        R.drawable.ic_livin_mandiri
    )

    LaunchedEffect(viewModel) {
        viewModel.startTimer(15_000) {
            viewModel.startQueryQris(componentActivity) {
                navigateTo("${MainNavOption.TRANSACTION_RESULT_SCREEN}/$it")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            transactionDetailState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            transactionDetailState.error != null -> {
                // ERROR
            }
            else -> {
                LazyColumn(
                    modifier = modifier
                        .padding(ScrollablePagePaddingStyle)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))

                        Card(
                            modifier = Modifier
                                .padding(top = 24.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = LightWhite),
                            border = BorderStroke(1.dp, Neutral30)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.total_payment),
                                    style = PlaceholderTextStyle
                                )
                                Text(
                                    text = totalPayment,
                                    style = CardBigTextStyle,
                                    color = Neutral80
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Silakan scan QRIS berikut",
                            style = PlaceholderTextStyle,
                            textAlign = TextAlign.Center
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(Neutral10),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(CardPaddingStyle),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row {
                                    Text(text = "Powered by", style = SmallCardTextStyle)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_qris),
                                        contentDescription = null
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Image(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillWidth
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(text = "Bayar melalui", style = SmallCardTextStyle)

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    iconWallets.forEach {
                                        Image(
                                            painter = painterResource(id = it),
                                            contentDescription = null
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "atau e-Wallet lainnya",
                                    style = SmallTextStyle,
                                    color = Neutral40
                                )
                            }
                        }
                    }

                    if (transactionDetailState.timer >= 0L) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Melakukan cek status otomatis dalam",
                                    style = PlaceholderTextStyle,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = "${transactionDetailState.timer % 60} Detik",
                                    style = SmallCardTextStyle,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))

                        CustomPrimaryButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Cek Status",
                            isEnable = true
                        ) {
                            viewModel.startQueryQris(componentActivity = componentActivity) {
                                navigateTo("${MainNavOption.TRANSACTION_RESULT_SCREEN}/$it")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        CustomSecondaryButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Kembali ke Daftar Transaksi",
                            isEnable = true
                        ) {
                            navigateTo(MainNavOption.HOME_SCREEN)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}