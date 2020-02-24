package com.dwaynewrld.Dreambook;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    BillingClient m_BillingClient;
    TextView responseText = findViewById(R.id.responseText);
    int lines = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void setResponseToView(String _message)
    {
        if(lines == 0)
        {
            lines = lines + 1;
            responseText.setText((lines) + _message);
        }
        else
        {
            lines = lines + 1;
            responseText.setText(responseText.getText() + "\n" + (lines) + _message);
        }
    }

    public void setUpBillingClient(View view)
    {
        m_BillingClient = BillingClient.newBuilder(MainActivity.this)
                .enablePendingPurchases()
                .setListener(this)
                .build();
        m_BillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingResponseCode.OK)
                {
                    Toast.makeText(MainActivity.this, "Got the Connection to the API.", Toast.LENGTH_SHORT).show();
                    setResponseToView("Got the Connection to the API.");
                    List<String> skuList = new ArrayList<>();
                    skuList.add("1011201726");
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    m_BillingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener()
                    {
                        @Override
                        public void onSkuDetailsResponse(BillingResult billingResult,List<SkuDetails> skuDetailsList) {
                            if (billingResult.getResponseCode() == BillingResponseCode.OK && skuDetailsList != null)
                            {
                                for (SkuDetails skuDetails : skuDetailsList)
                                {
                                    String sku = skuDetails.getSku();
                                    String price = skuDetails.getPrice();
                                    if ("1011201726".equals(sku))
                                    {
                                        Toast.makeText(MainActivity.this, "The price is: "+price, Toast.LENGTH_SHORT).show();
                                        setResponseToView("The price is: "+price);
                                        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                                .setSkuDetails(skuDetails)
                                                .build();
                                        BillingResult responseCode = m_BillingClient.launchBillingFlow(MainActivity.this,flowParams);
                                        if(responseCode.getResponseCode() == BillingResponseCode.OK)
                                        {
                                            Toast.makeText(MainActivity.this, "Billing launched successfully.", Toast.LENGTH_SHORT).show();
                                            setResponseToView("Billing launched successfully.");
                                        }
                                    }
                                    else
                                    {
                                        Toast.makeText(MainActivity.this, "An Unknown error occurred.", Toast.LENGTH_SHORT).show();
                                        setResponseToView("An Unknown error occurred.");
                                    }
                                }
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this, ""+billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                                setResponseToView(""+billingResult.getDebugMessage());
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(MainActivity.this, ""+billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                    setResponseToView(""+billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(MainActivity.this, "You are now disconnected.", Toast.LENGTH_SHORT).show();
                setResponseToView("You are now disconnected.");
            }
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if(billingResult.getResponseCode() == BillingResponseCode.OK && list!=null)
        {
            Toast.makeText(this, "Purchases updated.", Toast.LENGTH_SHORT).show();
            setResponseToView("Purchases updated.");
            for(Purchase purchase : list)
            {
                handlePurchase(purchase);
            }
        }
        else if(billingResult.getResponseCode() == BillingResponseCode.USER_CANCELED)
        {
            Toast.makeText(this, "Purchased Canceled by user.", Toast.LENGTH_SHORT).show();
            setResponseToView("Purchased Canceled by user.");
        }
        else if(billingResult.getResponseCode() == BillingResponseCode.ITEM_ALREADY_OWNED)
        {
            Toast.makeText(this, "The item being requested is already owned.", Toast.LENGTH_SHORT).show();
            setResponseToView("The item being requested is already owned.");
        }
        else
        {
            Toast.makeText(this, billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
            setResponseToView(billingResult.getDebugMessage());
        }
    }
    public void handlePurchase(Purchase _purchase)
    {
        if(_purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
        {

            // Important : Don't Forget to do this.
            // Grant the entitlement of the corresponding product to the user and if required store the Purchase token somewhere.

            if(!_purchase.isAcknowledged())
            {
                ConsumeParams consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(_purchase.getPurchaseToken())
                        .setDeveloperPayload(_purchase.getDeveloperPayload())
                        .build();
                m_BillingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                        if(billingResult.getResponseCode() == BillingResponseCode.OK)
                        {
                            Toast.makeText(MainActivity.this, "Purchase of token: "+ purchaseToken + "consumed.", Toast.LENGTH_SHORT).show();
                            setResponseToView("Purchase of token: " + purchaseToken + "consumed.");
                        }
                    }
                });
            }
        }

    }
}