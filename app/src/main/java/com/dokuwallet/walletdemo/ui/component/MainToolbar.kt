package com.dokuwallet.walletdemo.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.ui.theme.ColorPrimary
import com.dokuwallet.walletdemo.ui.theme.Neutral90

@Composable
fun MainToolbar(
    modifier: Modifier = Modifier,
    isHome: Boolean,
    isReceipt: Boolean,
    title: String = "",
    onBackPressed: () -> Unit,
    onClosePressed: () -> Unit
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        when {
            isHome -> {
                Icon(
                    painter = painterResource(id = R.drawable.ic_doku),
                    contentDescription = null,
                    tint = ColorPrimary
                )
            }
            isReceipt -> {

            }
            title == stringResource(id = R.string.detail_transaksi) -> {
                IconButton(
                    modifier = Modifier.size(28.dp),
                    onClick =  onClosePressed
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
            }
            title == "" -> {
                IconButton(
                    modifier = Modifier.size(28.dp),
                    onClick =  onClosePressed
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
            }
            else -> {
                IconButton(
                    modifier = Modifier.size(28.dp),
                    onClick = onBackPressed
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(32.dp))

        Text(
            text = if (isHome) stringResource(R.string.home_toolbar_title) else title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = Neutral90
        )
    }
}
