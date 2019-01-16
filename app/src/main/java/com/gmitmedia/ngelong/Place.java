package com.gmitmedia.ngelong;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gmitmedia.helpers.Cache;
import com.gmitmedia.helpers.Save;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gmitmedia.ngelong.Functions.urlEncodeUTF8;
import static com.gmitmedia.ngelong.SinglePlaceActivity.ITEM_KEY;

/**
 * Created by melvin on 25/09/2016.
 */
public class Place {
    public int id;
    public String name;
    public String text, submission_date, price_suffix, deal_link, address, telephone, email, pinImageUrl;
    public String imageUrl[];
    public double gpslat, gpslng, avgrating;
    public int viewed, shared, favorited, price, featured, totalratings;

    Target target;
    Place p;
    int place_i;


    interface onDownloadedListener {
        void onDownloaded(Place item);
    }

    interface onMultipleDownloadedListener {
        void onDownloaded(List<Place> items);
    }

    public Place(int id, String name, String[] imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public Place(String name) {
        this.name = name;
    }


    /**
     * Decode place from JSON
     *
     * @param JSONItem
     */
    public Place(JSONObject JSONItem, Context context) {
        try {
            id = JSONItem.getInt("id");
            name = JSONItem.getString("name");
            text = JSONItem.getString("text");
            submission_date = JSONItem.getString("submission_date");
            viewed = JSONItem.getInt("viewed");
            shared = JSONItem.getInt("shared");
            favorited = JSONItem.getInt("favorited");
            gpslat = JSONItem.getDouble("gpslat");
            gpslng = JSONItem.getDouble("gpslng");
            price = JSONItem.getInt("price");
            featured = JSONItem.getInt("featured");
            price_suffix = JSONItem.getString("price_suffix");
            deal_link = JSONItem.getString("deal_link");
            address = JSONItem.getString("address");
            telephone = JSONItem.getString("telephone");
            email = JSONItem.getString("email");
            avgrating = JSONItem.getDouble("avgrating");
            totalratings = JSONItem.getInt("totalratings");

            JSONArray JSONimageURL = new JSONArray(JSONItem.getString("image"));
            imageUrl = new String[JSONimageURL.length()];
            for (int i = 0; i < JSONimageURL.length(); i++) {
                imageUrl[i] = Configurations.SERVER_URL + "uploads/" + JSONimageURL.getString(i);
            }

            if (JSONItem.has("pinimage")) {
                JSONArray JSONPinImageURL = new JSONArray(JSONItem.getString("pinimage"));
                if (JSONPinImageURL.length() > 0) {
                    pinImageUrl = Configurations.SERVER_URL + "uploads/" + JSONPinImageURL.getString(0);
                    //loadPinBitmap(context, pinImageUrl);
                }

            } else {
                pinImageUrl = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //LOAD MULTIPLE PLACES----------------------------------------------------------------------

    /**
     * Load places from server
     *
     * @param activity
     * @param offset
     * @param limit
     * @param search
     * @param downloadedListener
     */
    public static void loadMultiple(FragmentActivity activity, int offset, int limit, String search, final onMultipleDownloadedListener downloadedListener) {
        loadMultiple(activity, offset, limit, search, "", downloadedListener);
    }

    /**
     * Load places from server
     *
     * @param activity
     * @param offset
     * @param limit
     * @param search
     * @param category
     * @param downloadedListener
     */
    public static void loadMultiple(final FragmentActivity activity, int offset, int limit, String search, String category, final onMultipleDownloadedListener downloadedListener) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("search", search);
        params.put("category", category);
        loadMultiple(activity, offset, limit, params, false, downloadedListener);
    }

    /**
     * Load places by ids (for favorites)
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
        loadMultiple(activity, 0, 100, params, false, downloadedListener);
    }

    /**
     * Load places from server with parameter hashmap
     *
     * @param activity
     * @param offset
     * @param limit
     * @param params
     * @param downloadedListener
     */
    public static void loadMultiple(final FragmentActivity activity, int offset, int limit, Map<String, String> params, boolean top, final onMultipleDownloadedListener downloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        final String url = Configurations.SERVER_URL + "api/places/" + ((top) ? ("top/viewed/") : ("")) + +offset + "/" + limit + "?" + urlEncodeUTF8(params);
        Log.e("URL", url);
        final Cache cache = new Cache(activity);
        StringRequest arrayreq = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                //save to cache
                cache.store(url, responseStr);
                //decode and return items
                decodeMultiple(activity, responseStr, downloadedListener);
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
                    decodeMultiple(activity, responseStr, downloadedListener);
                } else
                    Functions.noInternetAlert(activity);

            }
        });
        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }


    /**
     * Decode places received from server or cache
     *
     * @param responseStr
     * @param downloadedListener
     */
    public static void decodeMultiple(Context context, String responseStr, final onMultipleDownloadedListener downloadedListener) {
        try {
            System.out.println(responseStr);
            JSONArray response = new JSONArray(responseStr);
            System.out.println(response.toString(2));
            List<Place> Places = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                Places.add(new Place(jsonObject, context));
            }
            downloadedListener.onDownloaded(Places);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //LOAD SINGLE ----------------------------------------------------------------------------------

    /**
     * Load a single place by id
     *
     * @param activity
     * @param id
     * @param downloadedListener
     */
    public static void loadSingle(final FragmentActivity activity, int id, final onDownloadedListener downloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        final String url = Configurations.SERVER_URL + "api/place/" + id;
        final Cache cache = new Cache(activity);
        Log.e("URL", url);
        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                //save to cache
                cache.store(url, responseStr);
                //decode and return items
                decodeSingle(activity, responseStr, downloadedListener);
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
                    decodeSingle(activity, responseStr, downloadedListener);
                } else
                    Functions.noInternetAlert(activity);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(req);
    }


    /**
     * Decode a single place
     *
     * @param responseStr
     * @param downloadedListener
     */
    public static void decodeSingle(final FragmentActivity activity, String responseStr, final onDownloadedListener downloadedListener) {
        try {
            JSONObject response = new JSONObject(responseStr);
            System.out.println(response.toString(2));
            final Place place = new Place(response, activity);
            downloadedListener.onDownloaded(place);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //PLACES STATISTICS-------------------------------------------------------------------------------

    public void favorite(Context context) {
        send(context, "favorite/" + id);
    }

    public static void favoriteByID(Context context, int id) {
        send(context, "favorite/" + id);
    }

    public void shared(Context context) {
        send(context, "shared/" + id);
    }

    public static void sharedByID(Context context, int id) {
        send(context, "shared/" + id);
    }


    public void viewed(Context context) {
        send(context, "viewed/" + id);
    }


    public static void send(Context context, String data) {
        if (context != null) {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(context);//TODO:When app is closed. Do not do
            String url = Configurations.SERVER_URL + "api/place/" + data;

            StringRequest arrayreq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String responseStr) {

                }
            }, new Response.ErrorListener() {
                @Override
                // Handles errors that occur due to Volley
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            // Add the request to the RequestQueue.
            queue.add(arrayreq);
        }
    }


    //PLACE FAVORITE---------------------------------------------------------------------------------

    /**
     * Set this as favorite
     *
     * @param context
     * @param isFavorite
     */
    public void setFavorite(Context context, boolean isFavorite) {
        setFavoriteById(context, isFavorite, id);
    }

    public static void setFavoriteById(Context context, boolean isFavorite, int id) {
        if (isFavorite) {
            favoriteByID(context, id);
            Save.addToArray(id, "favorites", context);
        } else {
            int[] allFavorites = Save.loadIntArray("favorites", context);
            for (int i = 0; i < allFavorites.length; i++) {
                if (allFavorites[i] == id) {
                    Save.removeFromIntArray(i, "favorites", context);
                    return;
                }
            }
        }
    }

    /**
     * Is this place favorited?
     *
     * @param context
     * @return
     */
    public boolean isFavorite(Context context) {
        return isFavoriteById(context, id);
    }

    public static boolean isFavoriteById(Context context, int id) {
        //get favorites
        int[] allFavorites = Save.loadIntArray("favorites", context);
        for (int i = 0; i < allFavorites.length; i++) {
            if (allFavorites[i] == id) {
                return true;
            }
        }
        return false;
    }


    /**
     * Get a list of all the favorites
     *
     * @param activity
     * @param downloadedListener
     */
    public static void getFavorites(FragmentActivity activity, final onMultipleDownloadedListener downloadedListener) {
        int[] allFavorites = Save.loadIntArray("favorites", activity);
        if (allFavorites.length > 0)
            loadMultiple(activity, allFavorites, downloadedListener);
        else
            downloadedListener.onDownloaded(new ArrayList<Place>());
    }

    //PLACE FROM INTENT--------------------------------------------------------------------------

    /**
     * Gets a place id from intent.
     *
     * @param intent
     * @param savedInstanceState
     * @return
     */
    public static int getIdFromIntent(Intent intent, Bundle savedInstanceState) {
        //from share
        String action = intent.getAction();
        String data = intent.getDataString();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            String itemIdstr = data.substring(data.lastIndexOf("/") + 1);
            try {
                int itemId = Integer.parseInt(itemIdstr);
                return itemId;
            } catch (Exception e) {

            }
        }

        //from bundle
        if (savedInstanceState == null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                return extras.getInt(ITEM_KEY, -10);
            }
        } else {
            if (savedInstanceState.containsKey(ITEM_KEY))
                return (int) savedInstanceState.getSerializable(ITEM_KEY);
        }

        //no id found
        return -10;
    }

    //PLACE SHARE--------------------------------------------------------------------------------

    /**
     * Share on social media
     */
    public void share(Activity activity) {
        shareById(activity, id);
    }

    public static void shareById(Activity activity, int id) {
        sharedByID(activity, id);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getResources().getString(R.string.share_message) + " " + "http://" + activity.getResources().getString(R.string.deep_link) + "/" + id);
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent, "Share via"));
    }


    public interface PinLoadedListener {
        void onLoaded(Bitmap bitmap, Place place, int place_i);

        void onFailed(Place place, int place_i);
    }

    public void loadPinBitmap(Context context, int placei, final PinLoadedListener pinLoadedListener) {
        p = this;
        this.place_i = placei;

        if (target == null) target = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                pinLoadedListener.onLoaded(bitmap, p, place_i);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                pinLoadedListener.onFailed(p, place_i);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }

        };

        Picasso.with(context).load(pinImageUrl).into(target);

    }

}
