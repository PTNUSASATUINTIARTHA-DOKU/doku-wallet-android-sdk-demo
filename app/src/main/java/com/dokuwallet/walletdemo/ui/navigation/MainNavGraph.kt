package com.dokuwallet.walletdemo.ui.navigation

import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dokuwallet.walletdemo.ui.account.AccountBinding
import com.dokuwallet.walletdemo.ui.account.AccountCreation
import com.dokuwallet.walletdemo.ui.account.AccountViewModel
import com.dokuwallet.walletdemo.ui.account.VerifyOtp
import com.dokuwallet.walletdemo.ui.account.state.AccountCreationState
import com.dokuwallet.walletdemo.ui.home.HomeScreen
import com.dokuwallet.walletdemo.ui.qris.detail.TransactionDetail
import com.dokuwallet.walletdemo.ui.qris.detail.result.TransactionDetailResult
import com.dokuwallet.walletdemo.ui.qris.generate.GenerateQris
import com.dokuwallet.walletdemo.ui.qris.payment.ConfirmationQris
import com.dokuwallet.walletdemo.ui.qris.payment.PaymentQris
import com.dokuwallet.walletdemo.ui.qris.payment.PaymentQrisViewModel
import com.dokuwallet.walletdemo.ui.qris.payment.ScanQris
import com.dokuwallet.walletdemo.ui.qris.payment.PaymentQrisState
import com.dokuwallet.walletdemo.ui.qris.payment.receipt.PaymentQrisReceipt
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val navActions = remember(navController) {
        NavigationActions(navController)
    }
    val accountViewModel: AccountViewModel = viewModel()
    val paymentQrisViewModel: PaymentQrisViewModel = viewModel()

    val currentRoute = navController.currentDestination?.route
    when {
        currentRoute == MainNavOption.SCAN_QRIS_SCREEN -> paymentQrisViewModel.setPaymentQrisState(PaymentQrisState())
//        currentRoute == MainNavOption.ACCOUNT_CREATION_SCREEN ->  {
//            accountViewModel.setAccountCreationState(
//                AccountCreationState()
//            )
//        }
        currentRoute?.startsWith(MainNavOption.HOME_SCREEN) == true -> {
            paymentQrisViewModel.setPaymentQrisState(PaymentQrisState())
            accountViewModel.setAccountCreationState(AccountCreationState())
        }
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = MainNavOption.HOME_SCREEN
    ) {
        composable(
            "${MainNavOption.HOME_SCREEN}?messageType={messageType}&message={message}",
            arguments = listOf(
                navArgument("messageType") {
                    nullable = true
                    type = NavType.StringType
                },
                navArgument("message") {
                    nullable = true
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val messageType = backStackEntry.arguments?.getString("messageType")
            val message = backStackEntry.arguments?.getString("message")
            HomeScreen(modifier = modifier, navigateTo = navActions::navigate, messageType, message)
        }
        composable(MainNavOption.ACCOUNT_CREATION_SCREEN) {
            AccountCreation(
                modifier = modifier,
                viewModel = accountViewModel,
                navigateTo = navActions::navigate
            )
        }
        composable(MainNavOption.VERIFY_OTP_SCREEN) {
            VerifyOtp(
                modifier = modifier,
                viewModel = accountViewModel,
                navigateTo = navActions::navigate
            )
        }
        composable(MainNavOption.ACCOUNT_BINDING_SCREEN) {
            AccountBinding(
                modifier = modifier,
                viewModel = accountViewModel,
                navigateTo = navActions::navigate
            )
        }
        composable(MainNavOption.GENERATE_QRIS_SCREEN) {
            GenerateQris(
                modifier = modifier,
                navigateTo = navActions::navigate
            )
        }
        try {
            composable(
                "${MainNavOption.TRANSACTION_DETAIL_SCREEN}/{fileName}/{totalPayment}",
                arguments = listOf(
                    navArgument("fileName") { type = NavType.StringType },
                    navArgument("totalPayment") { type = NavType.StringType },
                )
            ) { backStackEntry ->

                val fileName = backStackEntry.arguments?.getString("fileName")
                val totalPayment = backStackEntry.arguments?.getString("totalPayment")

                val bitmap = BitmapFactory.decodeFile(File(context.cacheDir, fileName).absolutePath)

                TransactionDetail(
                    modifier = modifier,
                    qrBitmap = bitmap,
                    totalPayment = totalPayment.toString(),
                    navigateTo = navActions::navigate
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        composable(
            MainNavOption.SCAN_QRIS_SCREEN
        ) {
            ScanQris(
                modifier = modifier,
                viewModel = paymentQrisViewModel,
                navigateTo = navActions::navigate
            )
        }
        composable(
            MainNavOption.INQUIRY_QRIS_SCREEN
        ) {
            PaymentQris(
                modifier = modifier,
                viewModel = paymentQrisViewModel,
                navigateTo = navActions::navigate
            )
        }
        composable(
            MainNavOption.CONFIRMATION_QRIS_SCREEN
        ) {
            ConfirmationQris(
                modifier = modifier,
                viewModel = paymentQrisViewModel,
                navigateTo = navActions::navigate
            )
        }
        composable(
            "${MainNavOption.TRANSACTION_RESULT_SCREEN}/{state}",
            arguments = listOf(
                navArgument("state") {
                    nullable = false
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val state = backStackEntry.arguments?.getString("state")
            TransactionDetailResult(
                modifier = modifier,
                state = state.toString()
            )
        }
        composable(
            "${MainNavOption.PAYMENT_QRIS_RECEIPT_SCREEN}/{state}",
            arguments = listOf(
                navArgument("state") {
                    nullable = false
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val state = backStackEntry.arguments?.getString("state")
            PaymentQrisReceipt(
                modifier = modifier,
                viewModel = paymentQrisViewModel,
                state = state.toString(),
                navigateTo = navActions::navigate
            )
        }
    }
}

class NavigationActions(private val navHostController: NavHostController) {
    fun navigate(destination: String) {
        navHostController.navigate(destination) {
            when (destination) {
                MainNavOption.HOME_SCREEN -> {
                    popUpTo(0)
                }
                in listOf(
                    "${MainNavOption.PAYMENT_QRIS_RECEIPT_SCREEN}/success",
                    "${MainNavOption.PAYMENT_QRIS_RECEIPT_SCREEN}/failed",
                    "${MainNavOption.TRANSACTION_RESULT_SCREEN}/success",
                    "${MainNavOption.TRANSACTION_RESULT_SCREEN}/failed",
                    "${MainNavOption.SCAN_QRIS_SCREEN}",
                    "${MainNavOption.GENERATE_QRIS_SCREEN}"
                ) -> {
                    navHostController.navigate("${MainNavOption.HOME_SCREEN}?messageType=null&message=null") {
                        popUpTo(MainNavOption.HOME_SCREEN) {
                            inclusive = true
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
inline fun <reified T : ViewModel> viewModel(): T {
    val viewModelStoreOwner = LocalView.current.findViewTreeViewModelStoreOwner()!!
    return ViewModelProvider(viewModelStoreOwner)[T::class.java]
}