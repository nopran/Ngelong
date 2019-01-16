package com.gmitmedia.ngelong;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.gmitmedia.ngelong.Configurations.SHOW_SPLASH_SCREEN_BACKGROUND_IMAGE;

public class SplashScreenActivity extends AppCompatActivity {

    FragmentActivity activity;
    LoginButton loginButton;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        //go directly to main Activity if users are disabled
        if (!Configurations.ENABLE_USER_SYSTEM) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        //go directly to main Activity if user already logged in.
        if (User.isUserLoggedIn(activity)) {
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        //set layout
        setContentView(R.layout.activity_splash_screen);

        //set background image
        ImageView background = (ImageView) findViewById(R.id.background);
        if (SHOW_SPLASH_SCREEN_BACKGROUND_IMAGE)
            background.setVisibility(View.VISIBLE);


        // Facebook login button
        loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("email");
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // get token from facebook
                String token = AccessToken.getCurrentAccessToken().getToken();
                System.out.println("login Success: " + token);
                //login to server
                User.login(activity, token, new User.onLoginListener() {

                    @Override
                    public void onLogin(String email) {
                        System.out.println("php login Success: " + email);
                        Intent intent = new Intent(activity, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancel() {
                System.out.println("login Cancel");

            }

            @Override
            public void onError(FacebookException exception) {
                System.out.println("login Error");

            }
        });

        //create a new account
        TextView registerBtn = (TextView) findViewById(R.id.register_button);
        registerBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ((TextView) view).setTextColor(0xFFFFFFFF);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        ((TextView) view).setTextColor(ContextCompat.getColor(activity, R.color.accent));
                        break;
                    case MotionEvent.ACTION_UP:
                        ((TextView) view).setTextColor(ContextCompat.getColor(activity, R.color.accent));
                        break;
                }
                return false;
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, RegisterActivity.class);
                startActivity(intent);
            }
        });

        //set background image
        Picasso.with(this)
                .load(R.drawable.splashscreen)
                .fit()
                .into(background);


        //skip button
        TextView skipBtn = (TextView) findViewById(R.id.skip_button);
        skipBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ((TextView) view).setTextColor(ContextCompat.getColor(activity, R.color.accent)); // white
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        ((TextView) view).setTextColor(0xFFFFFFFF); // lightblack
                        break;
                    case MotionEvent.ACTION_UP:
                        ((TextView) view).setTextColor(0xFFFFFFFF); // lightblack
                        break;
                }
                return false;
            }
        });
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });


        //normal email password login
        final EditText emailView = (EditText) findViewById(R.id.email);
        final EditText passwordView = (EditText) findViewById(R.id.password);
        Button loginBtn = (Button) findViewById(R.id.login_button);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //login to server
                User.login(activity, emailView.getText().toString(), passwordView.getText().toString(), new User.onLoginListener() {
                    @Override
                    public void onLogin(String email) {
                        Intent intent = new Intent(activity, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }
        });


        //to generate key for facebook panel
        getFBKey();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Get FB Key. Used to obtain the facebook SHA code that you need to put in the facebook panel
     */
    public void getFBKey() {
        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", something);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
    }
}
