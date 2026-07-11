package com.focusreset.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BillingRepository(context: Context) : PurchasesUpdatedListener {
    private val _premium = MutableStateFlow(false)
    val premium: StateFlow<Boolean> = _premium
    private val client = BillingClient.newBuilder(context).setListener(this).enablePendingPurchases().build()

    fun connect(onReady: () -> Unit = {}) {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() = Unit
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) { restore(); onReady() }
            }
        })
    }

    private fun restore() {
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { _, purchases ->
            _premium.value = purchases.any { it.products.contains(PRODUCT_ID) && it.purchaseState == Purchase.PurchaseState.PURCHASED }
            purchases.filter { !it.isAcknowledged }.forEach { purchase -> client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()) {} }
        }
    }

    fun launch(activity: Activity, details: ProductDetails) {
        val offer = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return
        val params = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).setOfferToken(offer).build()
        client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) { if (result.responseCode == BillingClient.BillingResponseCode.OK) restore() }
    companion object { const val PRODUCT_ID = "focus_reset_premium" }
}
