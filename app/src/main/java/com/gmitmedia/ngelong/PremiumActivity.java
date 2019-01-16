package com.gmitmedia.ngelong;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gmitmedia.helpers.BillingHelper;

/**
 * Premium Activity to enable user to purchase premium
 */
public class PremiumActivity extends AppCompatActivity {

    Context context;
    Activity activity;
    BillingHelper billingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.context = this;
        this.activity = this;
        setContentView(R.layout.activity_premium);

        //set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // close activity on back button pressed
                finish();
            }
        });

        //just for debugging
        System.out.println("open premium page ");

        //TODO: initialise background. Also need to add ImageView to activity_premium
        //ImageView backgroundImage = (ImageView) findViewById(R.id.image);
        // Picasso.with(context).load(R.drawable.<your_drawable>).fit().centerCrop().into(backgroundImage);

        //initialise billing
        billingHelper = new BillingHelper(activity,
                new BillingHelper.RefreshListener() {
                    @Override
                    public void onRefresh(boolean isPremium, Inventory inventory) {
                        //close activity if premium
                        if (isPremium) {
                            finish();
                        }
                    }
                },
                new IabHelper.OnIabPurchaseFinishedListener() {
                    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                        System.out.println("Purchase successful " + result);
                    }
                },
                new IabHelper.OnConsumeFinishedListener() {
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                    }
                }, Configurations.PUBLIC_KEY);


        //get typefaces
        Typeface robotoLight = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");

        //ads title
        TextView title_ads = (TextView) findViewById(R.id.title_ads);
        title_ads.setTypeface(robotoLight);

        //purchase button
        ButtonRectangle purchase = (ButtonRectangle) findViewById(R.id.purchase);
        purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                billingHelper.purchasePremium();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (billingHelper.onActivityResult(requestCode, resultCode, data)) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingHelper != null)
            billingHelper.onDestroy();
    }

}
