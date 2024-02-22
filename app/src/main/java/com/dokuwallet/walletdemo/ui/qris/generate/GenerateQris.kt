package com.dokuwallet.walletdemo.ui.qris.generate

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.model.ValidateType
import com.dokuwallet.walletdemo.ui.component.CustomDropdownField
import com.dokuwallet.walletdemo.ui.component.CustomPrimaryButton
import com.dokuwallet.walletdemo.ui.component.CustomTextField
import com.dokuwallet.walletdemo.ui.navigation.MainNavOption
import com.dokuwallet.walletdemo.ui.theme.CardBigTextStyle
import com.dokuwallet.walletdemo.ui.theme.ColorPrimary
import com.dokuwallet.walletdemo.ui.theme.FieldTextStyle
import com.dokuwallet.walletdemo.ui.theme.LightWhite
import com.dokuwallet.walletdemo.ui.theme.Neutral30
import com.dokuwallet.walletdemo.ui.theme.Neutral40
import com.dokuwallet.walletdemo.ui.theme.PageDescTextStyle
import com.dokuwallet.walletdemo.ui.theme.PagePaddingStyle
import com.dokuwallet.walletdemo.utils.CommonUtils
import kotlinx.coroutines.launch

@Composable
fun GenerateQris(
    modifier: Modifier = Modifier,
    viewModel: GenerateQrisViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navigateTo: (String) -> Unit,
) {
    val componentActivity = LocalContext.current as ComponentActivity
    val coroutineScope = rememberCoroutineScope()
    val generateQrisState: GenerateQrisState by viewModel.generateQrisState.collectAsState()

    val items = listOf("Open for tips", "Fixed fee")
    val maxChar = 8

    Box(modifier = modifier.fillMaxSize()) {
        if (generateQrisState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = modifier
                    .padding(PagePaddingStyle)
            ) {
                CustomTextField(
                    modifier = Modifier
                        .padding(top = 24.dp),
                    fieldTitle = stringResource(R.string.transaction_amount),
                    text = generateQrisState.generateQrisFields.transactionAmount,
                    hint = stringResource(R.string.transaction_amount_hint),
                    validateType = ValidateType.AMOUNT,
                    maxChar = maxChar,
                    isMoneyFormat = true
                ) { value, isError ->
                    val updatedListErrorField = generateQrisState.listErrorField
                    updatedListErrorField[0] = isError
                    viewModel.setGenerateQrisState(
                        generateQrisState.copy(
                            generateQrisFields = generateQrisState.generateQrisFields.copy(
                                transactionAmount = value,
                            ),
                            listErrorField = updatedListErrorField
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 24.dp).fillMaxWidth()
                ) {
                    Switch(
                        checked = generateQrisState.generateQrisFields.isHaveFee,
                        onCheckedChange = {
                            viewModel.setGenerateQrisState(
                                generateQrisState.copy(
                                    generateQrisFields = generateQrisState.generateQrisFields.copy(
                                        isHaveFee = it
                                    )
                                )
                            )
                        },
                        modifier = Modifier.scale(0.7f).size(50.dp, 5.dp).align(Alignment.Top).padding(top = 16.dp),
                        colors = SwitchDefaults.colors(
                            checkedBorderColor = Color.Transparent,
                            checkedTrackColor = ColorPrimary,
                            checkedThumbColor = Color.White,
                            uncheckedBorderColor = Color.Transparent,
                            uncheckedTrackColor = Neutral40,
                            uncheckedThumbColor = Color.White,
                        ),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column (
                        modifier = Modifier.align(Alignment.Top)
                    ) {
                        Text(
                            text = stringResource(R.string.transaction_fee),
                            style = FieldTextStyle
                        )
                        Text(
                            text = stringResource(R.string.transaction_fee_desc),
                            style = PageDescTextStyle
                        )
                    }
                }

                if (generateQrisState.generateQrisFields.isHaveFee) {
                    CustomDropdownField(
                        modifier = Modifier.padding(top = 24.dp),
                        fieldTitle = "Tipe Fee",
                        listItems = items,
                        selectedItem = generateQrisState.generateQrisFields.selectedItem
                    ) { index ->
                        viewModel.setGenerateQrisState(
                            generateQrisState.copy(
                                generateQrisFields = generateQrisState.generateQrisFields.copy(
                                    selectedItem = index
                                )
                            )
                        )
                    }

                    if (generateQrisState.generateQrisFields.selectedItem == 1) {
                        CustomTextField(
                            modifier = Modifier
                                .padding(top = 24.dp),
                            fieldTitle = stringResource(R.string.fee_amount),
                            text = generateQrisState.generateQrisFields.feeAmount,
                            hint = stringResource(R.string.fee_amount_hint),
                            validateType = ValidateType.AMOUNT,
                            isMoneyFormat = true,
                            maxChar = maxChar
                        ) { value, isError ->
                            val updatedListErrorField = generateQrisState.listErrorField
                            updatedListErrorField[1] = isError

                            viewModel.setGenerateQrisState(
                                generateQrisState.copy(
                                    generateQrisFields = generateQrisState.generateQrisFields.copy(
                                        feeAmount = value,
                                    ),
                                    listErrorField = updatedListErrorField
                                )
                            )
                        }

                        Card(
                            modifier = Modifier
                                .padding(top = 24.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = LightWhite),
                            border = BorderStroke(1.dp, Neutral30)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.total_payment),
                                    style = FieldTextStyle
                                )
                                Text(
                                    text = generateQrisState.generateQrisFields.totalPayment,
                                    style = CardBigTextStyle,
                                    color = Neutral30
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1F))

                CustomPrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.next),
                    isEnable = when {
                        !generateQrisState.generateQrisFields.isHaveFee || generateQrisState.generateQrisFields.selectedItem == 0 -> {
                            !generateQrisState.listErrorField[0]
                        }

                        else -> generateQrisState.listErrorField.none { it }
                    }
                ) {
                    coroutineScope.launch {
                        viewModel.startGenerateQris(componentActivity) { fileName, totalPayment ->
                            navigateTo("${MainNavOption.TRANSACTION_DETAIL_SCREEN}/$fileName/$totalPayment")
                        }
                    }
                }
            }
        }
    }
}