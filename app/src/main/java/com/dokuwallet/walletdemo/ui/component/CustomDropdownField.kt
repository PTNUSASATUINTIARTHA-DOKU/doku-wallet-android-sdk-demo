package com.dokuwallet.walletdemo.ui.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.ui.theme.FieldPaddingStyle
import com.dokuwallet.walletdemo.ui.theme.FieldTextStyle
import com.dokuwallet.walletdemo.ui.theme.Neutral30

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownField(
    modifier: Modifier = Modifier,
    fieldTitle: String,
    listItems: List<String>,
    selectedItem: Int,
    onValueChanged: (index: Int) -> Unit
) {
    var text by remember { mutableStateOf(listItems[selectedItem]) }

    var isExpanded by remember { mutableStateOf(false) }

    var dropDownWidth by remember { mutableIntStateOf(0) }

    Column(modifier) {
        Text(text = fieldTitle, style = FieldTextStyle)

        Spacer(modifier = Modifier.height(4.dp))

        BasicTextField(
            value = text,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged {
                    dropDownWidth = it.width
                }
                .height(40.dp)
                .border(
                    1.dp,
                    Neutral30,
                    RoundedCornerShape(4.dp)
                )
                .clickable {
                    isExpanded = !isExpanded
                },
            textStyle = FieldTextStyle,
            singleLine = true,
            decorationBox = { innerTextField ->
                TextFieldDefaults.TextFieldDecorationBox(
                    value = "",
                    innerTextField = innerTextField,
                    enabled = false,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = remember { MutableInteractionSource() },
                    contentPadding = FieldPaddingStyle,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        containerColor = Color.Transparent
                    ),
                    shape = TextFieldDefaults.outlinedShape,
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_down_16),
                            contentDescription = null
                        )
                    }
                )
            },
            enabled = false,
            readOnly = true,
        )

        Spacer(modifier = Modifier.height(4.dp))

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { },
            modifier = Modifier
                .width(with(LocalDensity.current){dropDownWidth.toDp()})
                .background(Color.White)
        ) {
            listItems.forEachIndexed { index, s ->
                DropdownMenuItem(
                    text = {
                        Text(text = s, style = FieldTextStyle)
                    },
                    onClick = {
                        text = s
                        isExpanded = false
                        onValueChanged(index)
                    },
                )
            }
        }
    }
}