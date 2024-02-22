package com.dokuwallet.walletdemo.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dokuwallet.walletdemo.ui.theme.ButtonPaddingStyle
import com.dokuwallet.walletdemo.ui.theme.ButtonTextStyle
import com.dokuwallet.walletdemo.ui.theme.ColorPrimary
import com.dokuwallet.walletdemo.ui.theme.Neutral20
import com.dokuwallet.walletdemo.ui.theme.Neutral40

@Composable
fun CustomSecondaryButton(
    modifier: Modifier = Modifier,
    text: String,
    isEnable: Boolean,
    onButtonClicked: () -> Unit
) {
    Button(
        onClick = onButtonClicked,
        modifier = modifier
            .border(1.dp, if (isEnable) ColorPrimary else Neutral20, RoundedCornerShape(4.dp))
            .height(40.dp),
        enabled = isEnable,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = ColorPrimary,
            disabledContainerColor = Neutral20,
            disabledContentColor = Neutral40
        ),
        contentPadding = ButtonPaddingStyle,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(text = text, style = ButtonTextStyle)
    }
}