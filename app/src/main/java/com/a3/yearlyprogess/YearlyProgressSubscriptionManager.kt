package com.a3.yearlyprogess

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


sealed class SubscriptionStatus {
    data object NotSubscribed : SubscriptionStatus()
    data object Subscribed : SubscriptionStatus()
    data object Error : SubscriptionStatus()
    data object Loading : SubscriptionStatus()
}


class YearlyProgressSubscriptionManager(private val context: Context) {

    private var billingClient: BillingClient
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.forEach { purchase ->
                handlePurchase(purchase)
            }
        }
    }

    private val _shouldShowAds = MutableLiveData<SubscriptionStatus>(SubscriptionStatus.Loading)
    val shouldShowAds: LiveData<SubscriptionStatus> get() = _shouldShowAds

    init {
        val pendingPurchasesParams =
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        billingClient = BillingClient.newBuilder(context).setListener(purchasesUpdatedListener)
            .enablePendingPurchases(pendingPurchasesParams).build()

        var billingConnectionTryCount = 0
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup finished successfully")
                    Log.d(TAG, "Querying purchases")
                    shouldShowAds()
                }
            }

            override fun onBillingServiceDisconnected() {
                if (billingConnectionTryCount < 5) {
                    billingClient.startConnection(this)
                    billingConnectionTryCount++
                }
            }
        })
    }

    suspend fun processPurchases() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder().setProductId(
                AD_FREE_PRODUCT_ID
            ).setProductType(BillingClient.ProductType.SUBS).build()
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }

        val productDetailsList = productDetailsResult.productDetailsList

        if (productDetailsList.isNullOrEmpty()) {
            Toast.makeText(context, "No subscription found", Toast.LENGTH_LONG).show()
            _shouldShowAds.postValue(SubscriptionStatus.Error)
            return
        }

        val selectedOfferToken = productDetailsList[0].subscriptionOfferDetails?.first()?.offerToken

        if (selectedOfferToken == null) {
            Toast.makeText(context, "No subscription found", Toast.LENGTH_LONG).show()
            _shouldShowAds.postValue(SubscriptionStatus.Error)
            return
        }



        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetailsList[0]).setOfferToken(selectedOfferToken).build()
        )

        val billingFlowParams =
            BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList)
                .build()

        val billingResult =
            billingClient.launchBillingFlow(context as AppCompatActivity, billingFlowParams)

    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                        .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _shouldShowAds.postValue(SubscriptionStatus.Subscribed)
                        Log.d(TAG, "handlePurchase: Purchase acknowledged")
                    }
                }
            }
        }
    }

    fun shouldShowAds() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchases ->
            Log.d(TAG, "shouldShowAds Billing Result: $billingResult")
            Log.d(TAG, "shouldShowAds Purchases: $purchases")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val isSubscribed = purchases.any { it.products.contains(AD_FREE_PRODUCT_ID) }
                _shouldShowAds.postValue(
                    if (isSubscribed) SubscriptionStatus.Subscribed
                    else SubscriptionStatus.NotSubscribed
                )
            } else {
                _shouldShowAds.postValue(SubscriptionStatus.Error)
            }
        }
    }

    fun redirectToSubscriptionPage() {
        val packageName = context.packageName
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/account/subscriptions?sku=${AD_FREE_PRODUCT_ID}&package=${packageName}")
        )
        context.startActivity(intent)
    }

    companion object {
        private const val TAG = "YearlyProgressSubscriptionManager"
        private const val AD_FREE_PRODUCT_ID = "ad_free_perks"
    }
}