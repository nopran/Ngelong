package com.gmitmedia.ngelong;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.gmitmedia.helpers.OnSwipeTouchListener;
import com.squareup.picasso.Picasso;

/**
 * This activity shows images in full-screen
 */
public class FullScreenImage extends AppCompatActivity {

    ImageView imageView;
    Activity activity;
    Handler handler;
    int slideshow_seconds = 2;
    int slideshow_current_image = 0;
    String[] imageUrl;
    Runnable slideshowRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        this.activity = this;

        //only landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //get imageview
        imageView = (ImageView) findViewById(R.id.image);
        imageView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onClick() {
                //next image
                slideshow_current_image++;
                if (slideshow_current_image >= imageUrl.length)
                    slideshow_current_image = 0;

                //switch image
                Picasso.with(activity)
                        .load(imageUrl[slideshow_current_image])
                        .fit()
                        .placeholder(R.drawable.loading)
                        .into(imageView);

                restartSlideTimer();
            }

            @Override
            public void onSwipeLeft() {
                //next image
                slideshow_current_image--;
                if (slideshow_current_image < 0)
                    slideshow_current_image = imageUrl.length - 1;

                //switch image
                Picasso.with(activity)
                        .load(imageUrl[slideshow_current_image])
                        .fit()
                        .placeholder(R.drawable.loading)
                        .into(imageView);

                restartSlideTimer();

            }

            @Override
            public void onSwipeRight() {

                //next image
                slideshow_current_image++;
                if (slideshow_current_image >= imageUrl.length)
                    slideshow_current_image = 0;

                //switch image
                Picasso.with(activity)
                        .load(imageUrl[slideshow_current_image])
                        .fit()
                        .placeholder(R.drawable.loading)
                        .into(imageView);

                restartSlideTimer();
            }
        });


        //get article imageUrls
        Bundle b = this.getIntent().getExtras();
        imageUrl = b.getStringArray("imageUrl");
        slideshow_seconds = b.getInt("slideshow_seconds");


        //set slideshow timer
        slideshowRunnable = new Runnable() {
            @Override
            public void run() {
                //next image
                slideshow_current_image++;
                if (slideshow_current_image >= imageUrl.length)
                    slideshow_current_image = 0;

                //switch image
                Picasso.with(activity)
                        .load(imageUrl[slideshow_current_image])
                        .fit()
                        .placeholder(R.drawable.loading)
                        .into(imageView);

                //delay
                handler.postDelayed(this, slideshow_seconds * 1000);
            }
        };
        handler = new Handler();
        handler.postDelayed(slideshowRunnable, 0);

    }

    /**
     * The slideshow timer is reset every time the user scrolls
     */
    public void restartSlideTimer() {
        handler.removeCallbacks(slideshowRunnable);
        handler.postDelayed(slideshowRunnable, slideshow_seconds * 1000);
    }

}
