package com.dokuwallet.walletdemo.utils

object DataHolder {
    var tokenB2B = ""
    var tokenB2B2C = ""
    var authCode = ""
    var accountId = ""

    var qrContent = ""
    var referenceNumber = ""
        get() {
            return if (field == "") CommonUtils.getNewUUID() else field
        }
    var partnerReferenceNumber = ""
        get() {
            return if (field == "") CommonUtils.getNewUUID() else field
        }
}