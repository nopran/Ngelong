package com.gmitmedia.ngelong;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;



public class ProfileActivity extends AppCompatActivity {
    EditText usernameView;
    FragmentActivity activity;
    Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        this.activity = this;

        //setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                finish();
            }
        });


        //get resources
        usernameView = (EditText) findViewById(R.id.username);
        final EditText passwordView = (EditText) findViewById(R.id.password);
        final EditText confirm_passwordView = (EditText) findViewById(R.id.confirm_password);
        saveBtn = (Button) findViewById(R.id.save_button);

        //save button pressed
        saveBtn.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {
                saveBtn.setEnabled(false);
                Functions.setTint(saveBtn.getBackground(),ContextCompat.getColor(activity, R.color.colorDivider));
                User.setCurrentUser(activity, usernameView.getText().toString(), passwordView.getText().toString(), confirm_passwordView.getText().toString(), new User.onDoneListener() {
                    @Override
                    public void onDone() {
                        refresh();
                        saveBtn.setEnabled(true);
                        Functions.setTint(saveBtn.getBackground(),ContextCompat.getColor(activity, R.color.accent));
                    }

                    @Override
                    public void onError() {
                        saveBtn.setEnabled(true);
                        Functions.setTint(saveBtn.getBackground(),ContextCompat.getColor(activity, R.color.accent));
                    }
                });
            }
        });

        //get user data
        refresh();

    }

    /**
     * Refreshes user data in form
     */
    public void refresh() {
        User.getCurrentUser(activity, new User.onUserListener() {
            @Override
            public void onUser(User user) {
                usernameView.setText(user.username);
            }
        });
    }

}
