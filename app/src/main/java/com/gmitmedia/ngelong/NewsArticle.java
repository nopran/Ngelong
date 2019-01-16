package com.gmitmedia.ngelong;

import android.content.Context;
import android.content.Intent;
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
public class NewsArticle {
    public int id;
    public String name;
    public String text, submission_date, authorName;
    public int has_video, is_headline, author_id;
    public String imageUrl[];
    public int viewed, shared, favorited, allowComments;

    public static int HEADLINE = 1, NOT_HEADLINE = 0;


    interface onDownloadedListener {
        void onDownloaded(NewsArticle item);
    }

    interface onMultipleDownloadedListener {
        void onDownloaded(List<NewsArticle> items);
    }

    public NewsArticle(int id, String name, String[] imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public NewsArticle(String name) {
        this.name = name;
    }


    /**
     * Decode article from JSON
     *
     * @param JSONItem
     */
    public NewsArticle(JSONObject JSONItem) {
        try {
            id = JSONItem.getInt("id");
            name = JSONItem.getString("name");
            text = JSONItem.getString("text");
            submission_date = JSONItem.getString("submission_date");
            has_video = JSONItem.getInt("has_video");
            is_headline = JSONItem.getInt("is_headline");
            author_id = JSONItem.getInt("author_id");
            viewed = JSONItem.getInt("viewed");
            shared = JSONItem.getInt("shared");
            favorited = JSONItem.getInt("favorited");
            allowComments = JSONItem.getInt("allow_comments");
            authorName = JSONItem.getString("author");

            JSONArray JSONimageURL = new JSONArray(JSONItem.getString("image"));
            imageUrl = new String[JSONimageURL.length()];
            for (int i = 0; i < JSONimageURL.length(); i++) {
                imageUrl[i] = Configurations.SERVER_URL + "uploads/" + JSONimageURL.getString(i);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //LOAD MULTIPLE ARTICLES----------------------------------------------------------------------

    /**
     * Load articles from server
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
     * Load articles from server
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
     * Load articles by ids (for favorites)
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
     * Load articles from server with parameter hashmap
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
        final String url = Configurations.SERVER_URL + "api/news/" + ((top) ? ("top/viewed/") : ("")) + +offset + "/" + limit + "?" + urlEncodeUTF8(params);
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


    /**
     * Decode articles received from server or cache
     *
     * @param responseStr
     * @param downloadedListener
     */
    public static void decodeMultiple(String responseStr, final onMultipleDownloadedListener downloadedListener) {
        try {
            System.out.println(responseStr);
            JSONArray response = new JSONArray(responseStr);
            System.out.println(response.toString(2));
            List<NewsArticle> newsArticles = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                newsArticles.add(new NewsArticle(jsonObject));
            }
            downloadedListener.onDownloaded(newsArticles);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //LOAD SINGLE ----------------------------------------------------------------------------------

    /**
     * Load a single article by id
     *
     * @param activity
     * @param id
     * @param downloadedListener
     */
    public static void loadSingle(final FragmentActivity activity, int id, final onDownloadedListener downloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        final String url = Configurations.SERVER_URL + "api/new/" + id;
        final Cache cache = new Cache(activity);
        Log.e("URL", url);
        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                //save to cache
                cache.store(url, responseStr);
                //decode and return articles
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
     * Decode a single article
     *
     * @param responseStr
     * @param downloadedListener
     */
    public static void decodeSingle(final FragmentActivity activity, String responseStr, final onDownloadedListener downloadedListener) {
        try {
            JSONObject response = new JSONObject(responseStr);
            System.out.println(response.toString(2));
            final NewsArticle newsArticle = new NewsArticle(response);
            downloadedListener.onDownloaded(newsArticle);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //NEWS STATISTICS-------------------------------------------------------------------------------

//    public void favorite(Context context) {
//        send(context, "favorite/" + id);
//    }
//
//    public static void favoriteByID(Context context, int id) {
//        send(context, "favorite/" + id);
//    }
//
//    public void shared(Context context) {
//        send(context, "shared/" + id);
//    }
//
//    public static void sharedByID(Context context, int id) {
//        send(context, "shared/" + id);
//    }


    public void viewed(Context context) {
        send(context, "viewed/" + id);
    }


    public static void send(Context context, String data) {
        if (context != null) {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(context);//TODO:When app is closed. Do not do
            String url = Configurations.SERVER_URL + "api/new/" + data;

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






    //ARTICLE FROM INTENT--------------------------------------------------------------------------

    /**
     * Gets a article id from intent.
     *
     * @param intent
     * @param savedInstanceState
     * @return
     */
    public static int getIdFromIntent(Intent intent, Bundle savedInstanceState) {
//        //from share
//        String action = intent.getAction();
//        String data = intent.getDataString();
//        if (Intent.ACTION_VIEW.equals(action) && data != null) {
//            String articleIdstr = data.substring(data.lastIndexOf("/") + 1);
//            try {
//                int articleId = Integer.parseInt(articleIdstr);
//                return articleId;
//            } catch (Exception e) {
//
//            }
//        }

        //from bundle
        if (savedInstanceState == null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                return extras.getInt(SingleNewsArticleActivity.ITEM_KEY, -10);
            }
        } else {
            if (savedInstanceState.containsKey(SingleNewsArticleActivity.ITEM_KEY))
                return (int) savedInstanceState.getSerializable(SingleNewsArticleActivity.ITEM_KEY);
        }

        //no id found
        return -10;
    }
//
//    //ARTICLE SHARE--------------------------------------------------------------------------------
//
//    /**
//     * Share on social media
//     */
//    public void share(Activity activity) {
//        shareById(activity, id);
//    }
//
//    public static void shareById(Activity activity, int id) {
//        sharedByID(activity, id);
//        Intent sendIntent = new Intent();
//        sendIntent.setAction(Intent.ACTION_SEND);
//        sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getResources().getString(R.string.share_message) + " " + "http://" + activity.getResources().getString(R.string.deep_link) + "/" + id);
//        sendIntent.setType("text/plain");
//        activity.startActivity(Intent.createChooser(sendIntent, "Share via"));
//    }

}
