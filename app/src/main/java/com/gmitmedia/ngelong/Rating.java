package com.gmitmedia.ngelong;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gmitmedia.helpers.Cache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gmitmedia.ngelong.Functions.urlEncodeUTF8;

/**
 * Created by melvin on 25/09/2016.
 */
public class Rating {
    public int id;
    public String username;
    public String  datecreated;
    public boolean hasrating;
    public double rating;


    interface onDownloadedListener {
        void onDownloaded(Rating item);
    }

    interface onMultipleDownloadedListener {
        void onDownloaded(List<Rating> items);
    }


    interface onDoneListener {
        void onDone();

        void onError();
    }


    public Rating() {
        rating=0;
        hasrating=false;
    }
    /**
     * Decode from JSON
     *
     * @param JSONItem
     */
    public Rating(JSONObject JSONItem) {
        try {
            id = JSONItem.getInt("id");
            rating = JSONItem.getDouble("rating");
            datecreated = JSONItem.getString("datecreated");
            username = JSONItem.getString("username");
            if (JSONItem.has("hasrating"))
                hasrating = JSONItem.getBoolean("hasrating");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //LOAD MULTIPLE REVIEWS----------------------------------------------------------------------

    /**
     * Load from server
     *
     * @param activity
     * @param offset
     * @param limit
     * @param placeid
     * @param downloadedListener
     */
    public static void loadMultiple(FragmentActivity activity, int offset, int limit, String placeid, final onMultipleDownloadedListener downloadedListener) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("placeid", placeid);
        loadMultiple(activity, offset, limit, params, downloadedListener);

    }


    /**
     * Load by ids
     *
     * @param activity
     * @param ids
     * @param downloadedListener
     */
    public static void loadMultiple(final FragmentActivity activity, final int[] ids, final onMultipleDownloadedListener downloadedListener) {
        JSONArray idsJSON = new JSONArray();
        for (int i = 0; i < ids.length; i++) {
            idsJSON.put(ids[i]);
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", idsJSON.toString());
        loadMultiple(activity, 0, 100, params, downloadedListener);
    }

    /**
     * Load from server with parameter hashmap
     *
     * @param activity
     * @param offset
     * @param limit
     * @param params
     * @param downloadedListener
     */
    public static void loadMultiple(final FragmentActivity activity, int offset, int limit, Map<String, String> params, final onMultipleDownloadedListener downloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        final String url = Configurations.SERVER_URL + "api/ratings/" + offset + "/" + limit + "?" + urlEncodeUTF8(params);
        Log.e("URL", url);
        final Cache cache = new Cache(activity);
        StringRequest arrayreq = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                //save to cache
                cache.store(url, responseStr);
                //decode and return articles
                decodeMultiple(responseStr, downloadedListener);
            }
        }, new Response.ErrorListener() {
            @Override
            // Handles errors that occur due to Volley
            public void onErrorResponse(VolleyError error) {
                //No connection to server. Probably no internet
                Log.e("Volley", "Error");
                error.printStackTrace();
                //Try to load from cache else view warning
                String responseStr = cache.load(url);
                if (responseStr != null) {
                    System.out.println("loading cached data: " + responseStr);
                    decodeMultiple(responseStr, downloadedListener);
                } else
                    Functions.noInternetAlert(activity);

            }
        });
        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }

    public static void addRating(final FragmentActivity activity, final int rating, final int placeid, final onDoneListener onDoneListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        String url = Configurations.SERVER_URL + "api/rating?email=" + User.getCurrentUserEmail(activity) + "&password=" + User.getCurrentUserPassword(activity) + "&usertoken=" + User.getCurrentUserToken();

        Log.v("url", url);
        StringRequest arrayreq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                try {
                    System.out.println(responseStr);
                    JSONObject response = new JSONObject(responseStr);
                    if (response.getBoolean("success")) {
                        System.out.println(response.toString(2));
                        onDoneListener.onDone();
                    } else {
                        Functions.errorAlert(activity, response.getString("error"));
                        onDoneListener.onError();
                    }
                }
                // Try and catch are included to handle any errors due to JSON
                catch (JSONException e) {
                    e.printStackTrace();
                    onDoneListener.onError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            // Handles errors that occur due to Volley
            public void onErrorResponse(VolleyError error) {
                Functions.noInternetAlert(activity);
                Log.e("Volley", "Error");
                error.printStackTrace();
                onDoneListener.onError();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters
                Map<String, String> params = new HashMap<String, String>();
                params.put("rating", ""+rating);
                params.put("placeid", "" + placeid);
                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }


    /**
     * Decode rating received from server or cache
     *
     * @param responseStr
     * @param downloadedListener
     */
    public static void decodeMultiple(String responseStr, final onMultipleDownloadedListener downloadedListener) {
        try {
            System.out.println(responseStr);
            JSONArray response = new JSONArray(responseStr);
            System.out.println(response.toString(2));
            List<Rating> Reviews = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                Reviews.add(new Rating(jsonObject));
            }
            downloadedListener.onDownloaded(Reviews);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //LOAD SINGLE ----------------------------------------------------------------------------------

    /**
     * Load a single rating by placeid
     *
     * @param activity
     * @param placeid
     * @param downloadedListener
     */
    public static void loadSingle(final FragmentActivity activity, int placeid, final onDownloadedListener downloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        final String url = Configurations.SERVER_URL + "api/myrating/" + placeid + "?email=" + User.getCurrentUserEmail(activity) + "&password=" + User.getCurrentUserPassword(activity) + "&usertoken=" + User.getCurrentUserToken();
        Log.e("URL", url);
        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                try {
                    System.out.println(responseStr);
                    JSONObject response = new JSONObject(responseStr);
                    if (response.getBoolean("success")) {
                        if(response.getBoolean("hasrating")) {
                            System.out.println(response.toString(2));
                            decodeSingle(activity, responseStr, downloadedListener);
                        }else{
                            downloadedListener.onDownloaded(new Rating());
                        }
                    } else {
                        Functions.errorAlert(activity, response.getString("error"));
                    }
                }
                // Try and catch are included to handle any errors due to JSON
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            // Handles errors that occur due to Volley
            public void onErrorResponse(VolleyError error) {
                //No connection to server. Probably no internet
                Log.e("Volley", "Error");
                error.printStackTrace();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(req);
    }



    /**
     * Decode a single rating
     *
     * @param responseStr
     * @param downloadedListener
     */
    public static void decodeSingle(final FragmentActivity activity, String responseStr, final onDownloadedListener downloadedListener) {
        try {
            JSONObject response = new JSONObject(responseStr);
            System.out.println(response.toString(2));
            final Rating review = new Rating(response);
            downloadedListener.onDownloaded(review);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
