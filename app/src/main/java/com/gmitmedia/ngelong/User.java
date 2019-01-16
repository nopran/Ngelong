package com.gmitmedia.ngelong;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.gmitmedia.helpers.Save;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This holds the information about a single user and allows user to login...
 * <p>
 * <p>
 * I have put a lot of effort to ensure that everything is working fine. If you find any problem
 * feel free to contact me via the CodeCanyon item's comment section or send me a personal
 * message from here: https://codecanyon.net/user/neurondigital?ref=neurondigital#contact
 * <p/>
 * Thanks for buying this Template!
 * Check out my other products on CodeCanyon: http://codecanyon.net/user/neurondigital/portfolio?ref=neurondigital
 */


public class User {
    int id;
    String fbuserid;
    String fbusertoken;
    String username;
    String email;

    interface onLoginListener {
        void onLogin(String email);
    }

    interface onUserListener {
        void onUser(User user);
    }

    interface onDoneListener {
        void onDone();

        void onError();
    }

    /**
     * Login to server using facebook token as authentication.
     *
     * @param activity
     * @param usertoken
     * @param loginListener
     */
    public static void login(final FragmentActivity activity, final String usertoken, final onLoginListener loginListener) {

        // Instantiate the RequestQueue.
        String url = Configurations.SERVER_URL + "api/login?usertoken=" + usertoken;
        loginWithUrl(activity, url, new onLoginListener() {
            @Override
            public void onLogin(String email) {
                setCurrentUserCredentials(activity, email);
                loginListener.onLogin(email);
            }
        });
    }

    /**
     * Login user with email an password
     * @param activity
     * @param email
     * @param password
     * @param loginListener
     */
    public static void login(final FragmentActivity activity, final String email, final String password, final onLoginListener loginListener) {

        // Instantiate the RequestQueue.
        String url = Configurations.SERVER_URL + "api/login?email=" + email + "&password=" + password;
        loginWithUrl(activity, url, new onLoginListener() {
            @Override
            public void onLogin(String email) {
                setCurrentUserCredentials(activity, email, password);
                loginListener.onLogin(email);
            }
        });
    }

    /**
     * login directly by url
     * @param activity
     * @param url
     * @param loginListener
     */
    public static void loginWithUrl(final FragmentActivity activity, String url, final onLoginListener loginListener) {
        RequestQueue queue = Volley.newRequestQueue(activity);
        JsonObjectRequest arrayreq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        System.out.println(response.toString(2));
                        loginListener.onLogin(response.getString("email"));
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
                Functions.noInternetAlert(activity);
                Log.e("Volley", "Error");
                error.printStackTrace();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }


    /**
     * Register user by email and password
     * @param activity
     * @param username
     * @param password
     * @param email
     * @param loginListener
     */
    public static void register(final FragmentActivity activity, final String username, final String password, final String email, final onLoginListener loginListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        String url = Configurations.SERVER_URL + "api/register?username=" + username + "&password=" + password + "&email=" + email;

        JsonObjectRequest arrayreq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        System.out.println(response.toString(2));
                        setCurrentUserCredentials(activity, email, password);
                        loginListener.onLogin(email);
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
                Functions.noInternetAlert(activity);
                Log.e("Volley", "Error");
                error.printStackTrace();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }

    /**
     * Get user details from server
     *
     * @param activity
     * @param userListener
     */
    public static void getCurrentUser(final FragmentActivity activity, final onUserListener userListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        String url = Configurations.SERVER_URL + "api/user/email?email=" + getCurrentUserEmail(activity) + "&password=" + getCurrentUserPassword(activity) + "&usertoken=" + getCurrentUserToken();

        JsonObjectRequest arrayreq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    System.out.println(response.toString(2));
                    User user = new User();
                    user.username = response.getString("username");
                    user.fbuserid = response.getString("fbuserid");
                    user.id = response.getInt("id");
                    user.email = response.getString("email");
                    userListener.onUser(user);
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
                Functions.noInternetAlert(activity);
                Log.e("Volley", "Error");
                error.printStackTrace();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }

    /**
     * Set user details
     * @param activity
     * @param username
     * @param newpassword
     * @param confirmPassword
     * @param onDoneListener
     */
    public static void setCurrentUser(final FragmentActivity activity, String username, String newpassword, String confirmPassword, final onDoneListener onDoneListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        String url = Configurations.SERVER_URL + "setuser?email=" + getCurrentUserEmail(activity) + "&password=" + getCurrentUserPassword(activity) + "&usertoken=" + getCurrentUserToken();
        if (username.length() > 0)
            url += "&username=" + username;
        if (newpassword.length() > 0) {
            url += "&newpassword=" + newpassword;
            url += "&confirmpassword=" + confirmPassword;
        }
        Log.v("url", url);
        JsonObjectRequest arrayreq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
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
        });
        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }


    /**
     * Save user to local storage
     *
     * @param context
     * @param email
     * @param password
     */
    public static void setCurrentUserCredentials(Context context, String email, String password) {
        Save.saveString(email, "current_user_email", context);
        Save.saveString(password, "current_user_password", context);
    }


    /**
     * just save email to local storage
     * @param context
     * @param email
     */
    public static void setCurrentUserCredentials(Context context, String email) {
        Save.saveString(email, "current_user_email", context);
    }


    /**
     * Get current user user token from local storage
     * @return
     */
    public static String getCurrentUserToken() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            return accessToken.getToken();
        }
        return "";
    }

    /**
     * Get user email from local storage
     *
     * @param context
     * @return
     */
    public static String getCurrentUserEmail(Context context) {
        return Save.loadString("current_user_email", context);
    }

    /**
     * Get user password from local storage
     *
     * @param context
     * @return
     */
    public static String getCurrentUserPassword(Context context) {
        return Save.loadString("current_user_password", context);
    }


    /**
     * User is logged in if the token/password and email are saved locally
     * @param context
     * @return
     */
    public static boolean isUserLoggedIn(Context context) {
        if (getCurrentUserEmail(context) != null) {
            if (getCurrentUserEmail(context).length() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * If user is not logged in go to login page
     * @param context
     * @return
     */
    public static boolean isUserLoggedInElseTry(Context context) {
        if (isUserLoggedIn(context)) {
            return true;
        }

        //go to login page
        Intent intent = new Intent(context, SplashScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        return false;
    }


    /**
     * Call this function to logout user
     *
     * @param context
     */
    public static void logout(Context context) {
        setCurrentUserCredentials(context, "", "");
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(context, SplashScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

    }





}
