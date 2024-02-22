package com.dokuwallet.walletdemo.ui.account

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.model.ValidateType
import com.dokuwallet.walletdemo.ui.account.state.AccountCreationState
import com.dokuwallet.walletdemo.ui.account.state.VerifyOtpState
import com.dokuwallet.walletdemo.ui.component.CustomPrimaryButton
import com.dokuwallet.walletdemo.ui.component.CustomTextField
import com.dokuwallet.walletdemo.ui.component.GeneralBottomSheetContent
import com.dokuwallet.walletdemo.ui.navigation.MainNavOption
import com.dokuwallet.walletdemo.ui.theme.LightWhite
import com.dokuwallet.walletdemo.ui.theme.Neutral60
import com.dokuwallet.walletdemo.ui.theme.PageDescTextStyle
import com.dokuwallet.walletdemo.ui.theme.PagePaddingStyle
import com.dokuwallet.walletdemo.ui.theme.PageTitleTextStyle
import com.dokuwallet.walletdemo.utils.CommonUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountCreation(
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel,
    navigateTo: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val componentActivity: ComponentActivity = LocalContext.current as ComponentActivity

    val accountCreationState: AccountCreationState by viewModel.accountCreationState.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    var bottomSheetSubText by remember { mutableStateOf("") }
    val maxPhoneNumberLength = 14

    val verifyOtpState: VerifyOtpState by viewModel.verifyOtpState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (accountCreationState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = modifier
                    .padding(PagePaddingStyle),
            ) {
                Text(
                    text = stringResource(R.string.account_creation_title),
                    style = PageTitleTextStyle
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.account_creation_desc),
                    style = PageDescTextStyle
                )

                CustomTextField(
                    modifier = Modifier
                        .padding(top = 24.dp),
                    fieldTitle = stringResource(R.string.full_name),
                    text = accountCreationState.accountCreationFields.fullName,
                    hint = stringResource(R.string.full_name_hint),
                    validateType = ValidateType.NAME
                ) { value, isError ->
                    val updatedListErrorField = accountCreationState.listErrorField
                    updatedListErrorField[0] = isError
                    val filteredValue = CommonUtils.filterAlphaOnly(value)

                    viewModel.setAccountCreationState(
                        accountCreationState.copy(
                            accountCreationFields = accountCreationState.accountCreationFields.copy(
                                fullName = filteredValue
                            ),
                            listErrorField = updatedListErrorField
                        )
                    )
                }

                CustomTextField(
                    modifier = Modifier
                        .padding(top = 24.dp),
                    fieldTitle = stringResource(R.string.email),
                    text = accountCreationState.accountCreationFields.email,
                    hint = stringResource(R.string.email_hint),
                    validateType = ValidateType.EMAIL
                ) { value, isError ->
                    val updatedListErrorField = accountCreationState.listErrorField
                    updatedListErrorField[1] = isError

                    viewModel.setAccountCreationState(
                        accountCreationState.copy(
                            accountCreationFields = accountCreationState.accountCreationFields.copy(
                                email = value
                            ),
                            listErrorField = updatedListErrorField
                        )
                    )
                }

                CustomTextField(
                    modifier = Modifier
                        .padding(top = 24.dp),
                    fieldTitle = stringResource(R.string.phone_number),
                    text = accountCreationState.accountCreationFields.phoneNumber,
                    hint = stringResource(R.string.phone_number_hint),
                    validateType = ValidateType.PHONE_NUMBER,
                    maxChar = maxPhoneNumberLength
                ) { value, isError ->
                    val updatedListErrorField = accountCreationState.listErrorField
                    updatedListErrorField[2] = isError

                    viewModel.setAccountCreationState(
                        accountCreationState.copy(
                            accountCreationFields = accountCreationState.accountCreationFields.copy(
                                phoneNumber = value
                            ),
                            listErrorField = updatedListErrorField
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1F))

                CustomPrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.next),
                    isEnable = accountCreationState.listErrorField.none { it }
                ) {
//                    if (verifyOtpState.timer > 0L) {
//                        bottomSheetSubText = "Previous OTP not expired yet"
//                        showBottomSheet = true
//                    } else {
                    coroutineScope.launch {
                        viewModel.startAccountCreation(componentActivity,
                            onSuccess = {
                                navigateTo(MainNavOption.VERIFY_OTP_SCREEN)
                            },
                            onFailure = { failureMessage ->
                                if (failureMessage.isNotEmpty()) {
                                    bottomSheetSubText = failureMessage
                                    showBottomSheet = true
                                }

                    })
                }}

                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                        },
                        sheetState = sheetState,
                        containerColor = LightWhite
                    ) {
                        GeneralBottomSheetContent(title = R.string.info, caption = bottomSheetSubText, image = R.drawable.confirmation_alert) {
                            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}