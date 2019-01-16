package com.gmitmedia.helpers;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;


/**
 * Created by melvin on 17/09/2015.
 */
public class AdvertHelper {

    Activity activity;

    private InterstitialAd interstitial;
    boolean ShowingAd = false;
    AdRequest adRequest;

    //banner adView
    private AdView adView;

    //ad unit ids
    String InterstitialAd_unit_id;
    String BannerAd_unit_id;


    public AdvertHelper(Activity activity, String InterstitialAd_unit_id, String BannerAd_unit_id) {
        this.activity = activity;

        this.InterstitialAd_unit_id = InterstitialAd_unit_id;
        this.BannerAd_unit_id = BannerAd_unit_id;
    }

    //Interstitial listener
    public interface InterstitialListener {
        void onClosed();

        void onNotLoaded();
    }

    public InterstitialListener interstitialListener;

    //banner listener
    public interface BannerListener {

        void onLoaded();

        void onRefreshScreenRequired();
    }

    public BannerListener bannerListener;


    public void initialiseInterstitialAd() {
        initialiseInterstitialAd(new String[]{});
    }

    /**
     * Call once to initialise banner ad
     */
    public void initialiseInterstitialAd(String[] testDevices) {
        if (InterstitialAd_unit_id.length() > 0) {
            // Create the interstitial
            interstitial = new InterstitialAd(activity);
            interstitial.setAdUnitId(InterstitialAd_unit_id);

            // Create ad request.
            AdRequest.Builder builder = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            for (int i = 0; i < testDevices.length; i++) {
                builder.addTestDevice(testDevices[i]);
            }
            adRequest = builder.build();

            interstitial.loadAd(adRequest);
            interstitial.setAdListener(new AdListener() {
                //public void onAdLoaded() {
                //Toast.makeText(activity, "loaded ad", Toast.LENGTH_LONG).show();
                //}

                public void onAdClosed() {
                    //Toast.makeText(activity, "ad closed", Toast.LENGTH_LONG).show();
                    interstitial.loadAd(adRequest);
                    ShowingAd = false;
                    //callback
                    if (interstitialListener != null)
                        interstitialListener.onClosed();

                }


            });
        }
    }

    /**
     * Call to open interstitial ad.
     */
    public void openInterstitialAd(final InterstitialListener listener) {
        if (InterstitialAd_unit_id.length() > 0) {
            ShowingAd = true;
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    //set callback
                    interstitialListener = listener;

                    if (interstitial.isLoaded()) {
                        interstitial.show();

                    } else {
                        //not loaded
                        if (interstitialListener != null)
                            interstitialListener.onNotLoaded();

                        //no ad to show
                        ShowingAd = false;
                        //Toast.makeText(activity, "ad lost", Toast.LENGTH_LONG).show();
                        interstitial.loadAd(adRequest);
                    }

                }
            });
        }
    }


    /**
     * Show Google Admob banner
     *
     * @param inLayout - Show banner in layout or under layout
     */
    public void showBanner(final boolean inLayout, final LinearLayout linear_layout, final RelativeLayout layout, final BannerListener bannerListener) {
        //banner ad
        if (BannerAd_unit_id.length() > 0) {
            // Create an ad.
            adView = new AdView(activity);
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(BannerAd_unit_id);

            if (inLayout) {
                //make ad visible on bottom of screen over surface
                RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params1.addRule(RelativeLayout.CENTER_HORIZONTAL);
                adView.setLayoutParams(params1);
            } else {
                //make ad visible on bottom of screen under surface
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.BOTTOM;
                params.weight = 0;
                adView.setLayoutParams(params);

            }

            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device.
            final AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("275D94C2B5B93B3C4014933E75F92565")///nexus7//////testing
                    .addTestDevice("91608B19766D984A3F929C31EC6AB947") /////////////////testing//////////////////remove///////////
                    .addTestDevice("6316D285813B01C56412DAF4D3D80B40") ///test htc sensesion xl
                    .addTestDevice("8C416F4CAF490509A1DA82E62168AE08")//asus transformer
                    .addTestDevice("EA8AA9C3AA2BD16A954F592C6F935628")//motorola moto G
                    .addTestDevice("7B4C6D080C02BA40EF746C4900BABAD7")//Galaxy S4
                    .build();

            // Start loading the ad in the background.
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                public void onAdLoaded() {

                    //callbacks
                    if (bannerListener != null)
                        bannerListener.onLoaded();


                    View parent = (View) adView.getParent();
                    if (parent != null) {
                        if (!(parent.equals(layout) || parent.equals(linear_layout))) {
                            if (inLayout)
                                layout.addView(adView);
                            else
                                linear_layout.addView(adView);

                            //callback
                            if (bannerListener != null)
                                bannerListener.onRefreshScreenRequired();
                        }
                    } else {
                        //add new banner ad to screen
                        if (inLayout)
                            layout.addView(adView);
                        else
                            linear_layout.addView(adView);
                        //callback
                        if (bannerListener != null)
                            bannerListener.onRefreshScreenRequired();
                    }
                }
            });
        }
    }


    //always call the following methods

    public void onResume() {
        if (adView != null) {
            adView.resume();
        }
    }

    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
    }

    public void onDestroy() {
        // Destroy the AdView.
        if (adView != null) {
            adView.destroy();
        }
    }
}
