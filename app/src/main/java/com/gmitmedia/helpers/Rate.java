package com.gmitmedia.helpers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.gmitmedia.helpers.Alert.NegativeButtonListener;
import com.gmitmedia.helpers.Alert.PositiveButtonListener;


/**
 * This handles the rate. Asks the user to rate application on Google Play and if no is pressed this will ask again after a couple of clicks
 */
public class Rate {

    /**
     * Rate after several gameplays
     *
     * @param activity
     * @param showRateAfterXStarts         - Number of gameplays
     * @param rate_title                   title of alert shown
     * @param rate_text                    text shown in alert
     * @param unable_to_reach_market_error - error to display if market could not be found
     * @return true if opened alert box
     */
    public static boolean rateWithCounter(final FragmentActivity activity, int showRateAfterXStarts, String rate_title, String rate_text, final String unable_to_reach_market_error, String acceptText, String cancelText) {
        if (load_localpref("rate", activity) < 100) {

            //ask user to rate
            save_localpref(load_localpref("rate", activity) + 1, "rate", activity);
            if (load_localpref("rate", activity) == showRateAfterXStarts || load_localpref("rate", activity) == (showRateAfterXStarts * 4)) {

                //open alert menu
                Alert alert = new Alert();
                alert.DisplayText(rate_title, rate_text, acceptText, cancelText, activity);
                alert.show(activity.getSupportFragmentManager(), rate_title);
                alert.setPositiveButtonListener(new PositiveButtonListener() {
                    @Override
                    public void onPositiveButton(String input) {
                        rate(activity, unable_to_reach_market_error);

                        //disable rate
                        save_localpref(100, "rate", activity);

                    }
                });
                alert.setNegativeButtonListener(new NegativeButtonListener() {
                    @Override
                    public void onNegativeButton(String input) {

                    }
                });
                return true;
            }

        }
        return false;
    }

    /**
     * Open Google Play according to package name
     *
     * @param activity
     * @param unable_to_reach_market_error
     */
    public static void rate(Activity activity, String unable_to_reach_market_error) {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, unable_to_reach_market_error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * load local preference
     *
     * @param identifier
     * @param activity
     * @return
     */
    private static int load_localpref(String identifier, Activity activity) {
        // load preferences
        SharedPreferences hiscores = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        int score = hiscores.getInt("pref" + identifier, 0);
        return score;
    }

    /**
     * Save local preference
     *
     * @param pref
     * @param identifier
     * @param activity
     */
    private static void save_localpref(int pref, String identifier, Activity activity) {
        //load preferences
        SharedPreferences hiscores = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        SharedPreferences.Editor hiscores_editor = hiscores.edit();

        hiscores_editor.putInt("pref" + identifier, pref);

        hiscores_editor.commit();
    }
}
