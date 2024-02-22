package com.dokuwallet.walletdemo.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.ui.component.MainToolbar
import com.dokuwallet.walletdemo.ui.theme.WalletDemoTheme

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    Column(
        modifier = Modifier
            .background(Color.White)
    ) {
        MainToolbar(
            modifier = Modifier
                .background(Color.White),
            isHome = currentDestination?.route?.contains(MainNavOption.HOME_SCREEN) == true,
            isReceipt = currentDestination?.route?.contains(MainNavOption.PAYMENT_QRIS_RECEIPT_SCREEN) == true,
            title = navController.currentDestination.toString().let {
                return@let when {
                    it.contains(MainNavOption.ACCOUNT_CREATION_SCREEN) -> stringResource(R.string.account_creation)
                    it.contains(MainNavOption.VERIFY_OTP_SCREEN) -> stringResource(R.string.account_creation)
                    it.contains(MainNavOption.ACCOUNT_BINDING_SCREEN) -> stringResource(R.string.account_binding)
                    it.contains(MainNavOption.GENERATE_QRIS_SCREEN) -> stringResource(R.string.generate_qris)
                    it.contains(MainNavOption.TRANSACTION_DETAIL_SCREEN) -> stringResource(R.string.detail_transaksi)
                    it.contains(MainNavOption.SCAN_QRIS_SCREEN) -> stringResource(R.string.payment_qris)
                    it.contains(MainNavOption.INQUIRY_QRIS_SCREEN) -> stringResource(R.string.payment_qris)
                    it.contains(MainNavOption.CONFIRMATION_QRIS_SCREEN) -> stringResource(R.string.payment_confirmation)
                    it.contains(MainNavOption.TRANSACTION_RESULT_SCREEN) -> ""
                    else -> ""
                }
            },
            onBackPressed =  {
                navController.popBackStack()
            }
        ) {
            navController.navigate(MainNavOption.HOME_SCREEN) {
                popUpTo(0)
            }
        }

        MainNavGraph(
            modifier = Modifier
                .background(Color.White)
                .fillMaxHeight(),
            navController = navController
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun MainScreenPrev() {
    WalletDemoTheme {
        MainScreen()
    }
}