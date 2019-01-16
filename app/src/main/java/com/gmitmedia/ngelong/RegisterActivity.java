package com.gmitmedia.ngelong;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import static com.gmitmedia.ngelong.Configurations.SHOW_SPLASH_SCREEN_BACKGROUND_IMAGE;

public class RegisterActivity extends AppCompatActivity {

    FragmentActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ImageView background = (ImageView) findViewById(R.id.background);
        activity = this;

        //switch background image
        Picasso.with(this)
                .load(R.drawable.splashscreen)
                .fit()
                .into(background);
        if (SHOW_SPLASH_SCREEN_BACKGROUND_IMAGE)
            background.setVisibility(View.VISIBLE);


        //load resources
        final EditText emailView = (EditText) findViewById(R.id.email);
        final EditText usernameView = (EditText) findViewById(R.id.username);
        final EditText passwordView = (EditText) findViewById(R.id.password);
        Button registerBtn = (Button) findViewById(R.id.register_button);

        //register button pressed
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //login to server
                User.register(activity, usernameView.getText().toString(), passwordView.getText().toString(), emailView.getText().toString(), new User.onLoginListener() {

                    @Override
                    public void onLogin(String email) {
                        Intent intent = new Intent(activity, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }
        });


    }


}
