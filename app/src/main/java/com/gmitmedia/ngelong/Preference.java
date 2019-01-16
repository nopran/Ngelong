package com.gmitmedia.ngelong;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by melvin on 06/06/2017.
 * <p>
 * This handles loading of preferences from server
 */
public class Preference {

    interface onPreferenceDownloadedListener {
        void onPreferenceDownloaded(String string);
    }

    /**
     * Load a single preference from server by name
     *
     * @param context
     * @param name                 - name of preference
     * @param preferenceDownloaded - Callback to return preference
     */
    public static void load(final Context context, final String name, final onPreferenceDownloadedListener preferenceDownloaded) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        //combine url
        String url = Configurations.SERVER_URL + "api/preference/" + name;

        //generate request
        StringRequest req = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                save_localpref(response, "preference" + name, context);
                preferenceDownloaded.onPreferenceDownloaded(response);
            }
        }, new Response.ErrorListener() {
            @Override
            // Handles errors that occur due to Volley
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", "Error");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(req);
    }

    public static String getCached(final Context context, final String name) {
        return load_localpref("preference" + name, context);
    }

    /**
     * load local preference
     *
     * @param identifier
     * @param context
     * @return
     */
    private static String load_localpref(String identifier, Context context) {
        if (context != null) {
            // load preferences
            SharedPreferences hiscores = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            String value = hiscores.getString("pref" + identifier, "");
            return value;
        }
        return "";
    }

    /**
     * Save local preference
     *
     * @param value
     * @param identifier
     * @param context
     */
    private static void save_localpref(String value, String identifier, Context context) {
        //load preferences
        SharedPreferences values = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor values_editor = values.edit();
        values_editor.putString("pref" + identifier, value);
        values_editor.commit();
    }


}
