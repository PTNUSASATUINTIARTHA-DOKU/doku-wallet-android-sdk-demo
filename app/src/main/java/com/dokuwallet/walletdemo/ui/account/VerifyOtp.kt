package com.dokuwallet.walletdemo.ui.account

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.model.ValidateType
import com.dokuwallet.walletdemo.ui.account.state.VerifyOtpState
import com.dokuwallet.walletdemo.ui.component.CustomPrimaryButton
import com.dokuwallet.walletdemo.ui.component.CustomSecondaryButton
import com.dokuwallet.walletdemo.ui.component.CustomTextField
import com.dokuwallet.walletdemo.ui.component.GeneralBottomSheetContent
import com.dokuwallet.walletdemo.ui.navigation.MainNavOption
import com.dokuwallet.walletdemo.ui.theme.LightWhite
import com.dokuwallet.walletdemo.ui.theme.PageDescTextStyle
import com.dokuwallet.walletdemo.ui.theme.PagePaddingStyle
import com.dokuwallet.walletdemo.ui.theme.PageTitleTextStyle
import com.dokuwallet.walletdemo.ui.theme.SmallTextStyle
import com.dokuwallet.walletdemo.utils.CommonUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyOtp(
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel,
    navigateTo: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val componentActivity: ComponentActivity = LocalContext.current as ComponentActivity

    val verifyOtpState: VerifyOtpState by viewModel.verifyOtpState.collectAsState()

    val maxOtpLength = 6

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    var bottomSheetSubText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        if (verifyOtpState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = modifier
                    .padding(PagePaddingStyle),
            ) {
                Text(
                    text = stringResource(R.string.otp_verification),
                    style = PageTitleTextStyle
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(
                        R.string.otp_verification_desc,
                        CommonUtils.spacedPhoneNumber(verifyOtpState.phoneNumber)
                    ),
                    style = PageDescTextStyle
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                ) {
                    CustomTextField(
                        modifier = Modifier
                            .weight(1F),
                        fieldTitle = stringResource(R.string.otp),
                        text = verifyOtpState.otp,
                        hint = stringResource(R.string.otp_hint),
                        validateType = ValidateType.OTP,
                        maxChar = maxOtpLength
                    ) { value, isError ->
                        viewModel.setVerifyOtpState(
                            verifyOtpState.copy(
                                otp = value,
                                isError = isError
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(23.dp))
                        CustomSecondaryButton(
                            text = stringResource(R.string.resend),
                            isEnable = verifyOtpState.timer == 0L
                        ) {
                            viewModel.resendOtp(componentActivity)
                        }

                        if (verifyOtpState.timer != 0L) {
                            Text(
                                modifier = Modifier.padding(top = 4.dp),
                                text = stringResource(
                                    R.string.repeat_in, String.format(
                                        stringResource(R.string.timer_format),
                                        verifyOtpState.timer / 60,
                                        verifyOtpState.timer % 60
                                    )
                                ),
                                style = SmallTextStyle
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1F))

                CustomPrimaryButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.next),
                    isEnable = !verifyOtpState.isError && verifyOtpState.otp.length == 6
                ) {
                    viewModel.startVerifyOtp(componentActivity, onSuccess = {
                        navigateTo(MainNavOption.ACCOUNT_BINDING_SCREEN)
                        // new code
                        viewModel.setVerifyOtpState(
                            verifyOtpState.copy(
                                otp = "",
                            )
                        )
                        // new code
                    }, onFailure = {
                            failureMessage ->
                        if (failureMessage.isNotEmpty()) {
                            bottomSheetSubText = failureMessage
                            showBottomSheet = true
                        }
                        // new code
                        viewModel.setVerifyOtpState(
                            verifyOtpState.copy(
                                otp = "",
                            )
                        )
                        // new code
                    })
                }

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