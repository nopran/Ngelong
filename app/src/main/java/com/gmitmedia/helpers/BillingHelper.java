package com.gmitmedia.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.vending.billing.IabBroadcastReceiver;
import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;
import com.gmitmedia.ngelong.R;

import static com.gmitmedia.ngelong.Configurations.SKU_PREMIUM;

/**
 * Created by melvin on 17/09/2015.
 */
public class BillingHelper {

    IabHelper mHelper;
    Activity activity;

    Boolean setupDone = false;
    Boolean inventoryQuery = false;
    private boolean isPremium = false;
    //static final String SKU_PREMIUM = "premium_upgrade";//"android.test.purchased";//

    //developer payload
    private String developerPayload = "";

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    static final String KEY_PREMIUM = "is_premium";

    public interface RefreshListener {
        void onRefresh(boolean isPremium, Inventory inventory);
    }

    RefreshListener refreshListener;

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener;
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener;

    // Provides purchase notification while this app is running
    public IabBroadcastReceiver mBroadcastReceiver;

    public BillingHelper(Activity activity, RefreshListener refreshListener, final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener, IabHelper.OnConsumeFinishedListener mConsumeFinishedListener, String publicKey) {
        this.activity = activity;
        // this.refreshListener = refreshListener;
        // public key
        String base64EncodedPublicKey = publicKey;

        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(activity, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);

        //refresh inventory
        this.refreshListener = refreshListener;
        //purchase listener
        this.mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                if (result.isFailure()) {
                    // Handle error
                    System.out.println("Purchase error " + result);
                    return;
                } else if (purchase.getSku().equals(SKU_PREMIUM)) {
                    //bought
                    System.out.println("premium Purchase successful " + result);
                    isPremium = true;
                } else {
                    System.out.println("Purchase successful " + result);
                    if (mPurchaseFinishedListener != null)
                        mPurchaseFinishedListener.onIabPurchaseFinished(result, purchase);
                }

            }
        };

        //consume listener
        this.mConsumeFinishedListener = mConsumeFinishedListener;

        startSetup();

    }

    public void startSetup() {

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    System.out.println("Problem setting up In-app Billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;


                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    System.out.println("Problem setting up In-app Billing: " + result);
                } else {
                    setupDone = true;
                    refreshInventory();

                }
            }
        });
    }

    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        refreshInventory();
    }

    public void refreshInventory() {
        if (mHelper != null) {
            if (setupDone) {
                if (!inventoryQuery) {
                    inventoryQuery = true;
                    try {
                        mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

                                System.out.println("Query inventory finished.");

                                // Have we been disposed of in the meantime? If so, quit.
                                if (mHelper == null) return;

                                // Is it a failure?
                                if (result.isFailure()) {
                                    System.out.println("Failed to query inventory: " + result);
                                    return;
                                }

//                            //TODO: consume to test
//                                if (inventory.hasPurchase(SKU_PREMIUM)) {
//                                    try {
//                                        mHelper.consumeAsync(inventory.getPurchase(SKU_PREMIUM), null);
//                                    } catch (IabHelper.IabAsyncInProgressException e) {
//                                        System.out.println("Error querying inventory. Another async operation in progress.");
//                                    }
//                                }

                                System.out.println("Query inventory was successful.");

                                // Do we have the premium upgrade?
                                Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
                                isPremium = (premiumPurchase != null);
                                if (isPremium)
                                    saveBoolean(true, KEY_PREMIUM, activity);
                                else {
//                                     saveBoolean(false, KEY_PREMIUM, activity);//TODO: REMOVE to consume
//                                     Log.v("Billing", "Not Premium but was once premium.");
                                }

                                //refresh with new inventory
                                refreshListener.onRefresh(isPremium(activity), inventory);

                                System.out.println("User is " + (isPremium ? "PREMIUM" : "NOT PREMIUM"));
                                System.out.println("Initial inventory query finished; enabling main UI.");
                                inventoryQuery = false;
                            }
                        });
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        System.out.println("Error querying inventory. Another async operation in progress.");
                    }
                }
            } else {
                startSetup();
            }
        }
    }

    public void purchasePremium() {
        if (mHelper != null) {
            if (setupDone) {
                try {
                    mHelper.launchPurchaseFlow(activity, SKU_PREMIUM, RC_REQUEST, mPurchaseFinishedListener, developerPayload);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    System.out.println("Error querying inventory. Another async operation in progress.");
                }
            }
        }
    }

    public void purchaseItem(String Sku) {
        if (mHelper != null) {
            if (setupDone) {

                try {
                    mHelper.launchPurchaseFlow(activity, Sku, RC_REQUEST, mPurchaseFinishedListener, developerPayload);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    System.out.println("Error querying inventory. Another async operation in progress.");
                }
            }
        }
    }

    public void consumeItem(Purchase item) {
        try {
            mHelper.consumeAsync(item, mConsumeFinishedListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            System.out.println("Error querying inventory. Another async operation in progress.");
        }

    }


    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return (!mHelper.handleActivityResult(requestCode, resultCode, data));

    }


    public static boolean isPremium(Context context) {
        System.out.println("isPremium called : " + loadBoolean(KEY_PREMIUM, context));
        return loadBoolean(KEY_PREMIUM, context);
    }

    public void onDestroy() {

        if (mHelper != null)
            mHelper.disposeWhenFinished();

        mHelper = null;
    }

    public static boolean saveBoolean(boolean value, String key, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getString(R.string.save_preference_name), 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static boolean loadBoolean(String key, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getString(R.string.save_preference_name), 0);
        return prefs.getBoolean(key, false);
    }

}