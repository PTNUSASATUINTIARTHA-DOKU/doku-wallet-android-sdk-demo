package com.dokuwallet.walletdemo.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.ui.theme.BottomSheetCaptionTextStyle
import com.dokuwallet.walletdemo.ui.theme.BottomSheetTitleTextStyle

@Composable
fun GeneralBottomSheetContent(
    title: Int,
    caption: String,
    image: Int,
    onButtonClicked: () -> Unit,
) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = image),
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(id = title) , style = BottomSheetTitleTextStyle, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = caption, style = BottomSheetCaptionTextStyle, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(16.dp))

        CustomPrimaryButton(
            modifier = Modifier
                .fillMaxWidth(),
            text = stringResource(R.string.ok),
            isEnable = true
        ) {
            onButtonClicked()
        }
    }
}