package com.dokuwallet.walletdemo.ui.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.model.HomeCard
import com.dokuwallet.walletdemo.ui.navigation.MainNavOption
import com.dokuwallet.walletdemo.ui.theme.Green10
import com.dokuwallet.walletdemo.ui.theme.Green70
import com.dokuwallet.walletdemo.ui.theme.LightGray
import com.dokuwallet.walletdemo.ui.theme.ToastTextStyle

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateTo: (String) -> Unit,
    messageType: String?,
    message: String?,
) {
    var isShow by remember {
        mutableStateOf(!messageType.isNullOrEmpty() && !message.isNullOrEmpty())
    }

    val listOfCards = listOf(
        HomeCard(
            stringResource(R.string.account_creation_binding),
            R.drawable.ic_human
        ) {
            navigateTo(MainNavOption.ACCOUNT_CREATION_SCREEN)
        },
        HomeCard(
            stringResource(R.string.generate_qris),
            R.drawable.ic_qr_code
        ) {
            navigateTo(MainNavOption.GENERATE_QRIS_SCREEN)
        },
        HomeCard(
            stringResource(R.string.payment_qris),
            R.drawable.ic_commerce
        ) {
          navigateTo(MainNavOption.SCAN_QRIS_SCREEN)
        },
    )

    LaunchedEffect(isShow) {
        Handler(Looper.getMainLooper()).postDelayed({
            isShow = false
        }, 3000)
    }

    Box(modifier = modifier) {
        Column(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.home_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(24.dp))

            listOfCards.forEachIndexed { index, card ->
                Column {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(LightGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { card.action() }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = card.icon),
                                contentDescription = null,
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = card.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (isShow) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 16.dp)
                    .background(Green10, RoundedCornerShape(4.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .size(16.dp),
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Green70
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = message.toString(), style = ToastTextStyle, color = Green70)
            }
        }
    }
}


