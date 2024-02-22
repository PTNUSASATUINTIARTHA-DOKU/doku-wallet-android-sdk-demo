package com.dokuwallet.walletdemo.ui.qris.payment

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dokuwallet.walletdemo.R
import com.dokuwallet.walletdemo.ui.component.GeneralBottomSheetContent
import com.dokuwallet.walletdemo.ui.navigation.MainNavOption
import com.dokuwallet.walletdemo.ui.theme.LightWhite
import com.dokuwallet.walletdemo.ui.theme.Transparent50
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQris(
    modifier: Modifier = Modifier,
    viewModel: PaymentQrisViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navigateTo: (String) -> Unit,
) {
    val paymentQrisState: PaymentQrisState by viewModel.paymentQrisState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val componentActivity = LocalContext.current as ComponentActivity
    val context = LocalContext.current

    var isPermissionGranted by remember {
        mutableStateOf(false)
    }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var bottomSheetSubText by remember { mutableStateOf("") }


    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            isPermissionGranted = isGranted
            if (isGranted) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    val compoundBarcodeView: CompoundBarcodeView = remember {
        CompoundBarcodeView(context)
    }

    if (isPermissionGranted) {
        DisposableEffect(Unit) {
            val capture = CaptureManager(context as Activity, compoundBarcodeView)
            capture.initializeFromIntent(context.intent, null)
            compoundBarcodeView.statusView.visibility = View.GONE
            compoundBarcodeView.viewFinder.visibility = View.GONE
            compoundBarcodeView.resume()
            capture.decode()

            val barcodeCallback = object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult?) {
                    if (paymentQrisState.isScanFlag) {
                        return // Do nothing if the scan flag is true
                    }
                    result?.text?.let { barCodeQr ->
                        coroutineScope.launch {
                            viewModel.startDecodeQris(
                                componentActivity,
                                barCodeQr,
                                onFailure = {
                                        failureMessage ->
                                    if (failureMessage != null) {
                                        if (failureMessage.isNotEmpty()) {
                                            bottomSheetSubText = failureMessage
                                            showBottomSheet = true
                                        }
                                    }
                                },
                                onSuccess = { screenOption ->
                                    compoundBarcodeView.pause() // Pause the scanning when needed
                                    if (screenOption == "11") {
                                        navigateTo(MainNavOption.INQUIRY_QRIS_SCREEN)
                                    } else {
                                        viewModel.updateTotalPayment()
                                        navigateTo(MainNavOption.CONFIRMATION_QRIS_SCREEN)
                                    }
                                })
                        }
                    }
                }

                override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
                    // No implementation needed
                }
            }

            compoundBarcodeView.decodeContinuous(barcodeCallback)

            onDispose {
                compoundBarcodeView.pause()
            }
        }
    }

    LaunchedEffect(CameraSelector.LENS_FACING_BACK) {
        val permissionCheckResult =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            )
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (paymentQrisState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            if (isPermissionGranted) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize(),
                    factory = { compoundBarcodeView }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Transparent50)
                    .padding(vertical = 28.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    modifier = Modifier.height(28.dp),
                    painter = painterResource(id = R.drawable.ic_doku),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        Color.White
                    )
                )

                Spacer(modifier = Modifier.width(5.dp))

                Image(
                    modifier = Modifier.height(28.dp),
                    painter = painterResource(id = R.drawable.ic_qris),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        Color.White
                    )
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState,
            containerColor = LightWhite,
        ) {
            GeneralBottomSheetContent(title = R.string.info, caption = bottomSheetSubText, image = R.drawable.confirmation_alert) {
                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showBottomSheet = false
                        navigateTo(MainNavOption.SCAN_QRIS_SCREEN)
                    }
                }
            }
        }
    }
}