package com.gmitmedia.helpers;

import android.app.Activity;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * Created by melvin on 17/09/2015.
 */
public class AnalyticsHelper {
    Activity activity;

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    public Tracker tracker;
    protected HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public AnalyticsHelper(Activity activity) {
        this.activity = activity;
    }


    /**
     * Initialise Google Analytics
     *
     * @param PROPERTY_ID - Analytics id
     * @return Tracker
     */
    public synchronized Tracker initialiseAnalytics(String PROPERTY_ID) {
        if (!mTrackers.containsKey(TrackerName.APP_TRACKER)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(activity);
            //if (debug_mode)
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            analytics.setDryRun(false);
            tracker = analytics.newTracker(PROPERTY_ID);
            mTrackers.put(TrackerName.APP_TRACKER, tracker);

        }

        return mTrackers.get(TrackerName.APP_TRACKER);
    }

    /**
     * Add a view to Google Analytics
     */
    public void AnalyticsView() {
        tracker.enableAutoActivityTracking(true);
        tracker.send(new HitBuilders.AppViewBuilder().build());
        GoogleAnalytics.getInstance(activity.getBaseContext()).dispatchLocalHits();
    }

    /* pause, destroy, resume................................................ */
    public void onStart() {
        //Get an Analytics tracker to report app starts &amp; uncaught exceptions etc.
        GoogleAnalytics.getInstance(activity).reportActivityStart(activity);
    }

    public void onStop() {
        //Stop the analytics tracking
        GoogleAnalytics.getInstance(activity).reportActivityStop(activity);
    }
}
