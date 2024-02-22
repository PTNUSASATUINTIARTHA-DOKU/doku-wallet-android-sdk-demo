package com.dokuwallet.walletdemo.ui.qris.detail.result

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.ui.qris.detail.TransactionDetailViewModel
import com.dokuwallet.walletdemo.ui.theme.BottomSheetCaptionTextStyle
import com.dokuwallet.walletdemo.ui.theme.BottomSheetTitleTextStyle
import com.dokuwallet.walletdemo.ui.theme.PageDescTextStyle
import com.dokuwallet.walletdemo.ui.theme.PagePaddingStyle
import com.dokuwallet.walletdemo.ui.theme.PageTitleTextStyle
import com.dokuwallet.walletdemo.ui.theme.PlaceholderTextStyle

@Preview
@Composable
fun TransactionDetailResult(
    modifier: Modifier = Modifier,
    viewModel: TransactionDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    state: String = "failed"
) {
    val successImage = R.drawable.state_success_payment
    val failedImage = R.drawable.state_failed
    val image = if (state == "success") successImage else failedImage
    val titleText = if (state == "success") R.string.success_receive_qris else R.string.failed_receive_qris
    val captionText = if (state == "success") R.string.success_receive_qris_caption else R.string.failed_receive_qris_caption

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column (
            modifier = modifier
                .padding(PagePaddingStyle)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 24.dp),
                painter = painterResource(id = image),
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = titleText),
                style = BottomSheetTitleTextStyle,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = captionText),
                style = BottomSheetCaptionTextStyle,
                textAlign = TextAlign.Center
            )
        }

    }
}