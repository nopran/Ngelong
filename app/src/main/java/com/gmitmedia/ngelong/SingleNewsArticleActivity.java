package com.gmitmedia.ngelong;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * This activity shows a single article
 */
public class SingleNewsArticleActivity extends AppCompatActivity {
    Context context;
    Activity activity;

    public static String ITEM_KEY = "item_key", POSITION = "position", ITEM_LIST_KEY = "item_list_key", TITLE = "title_key";

    int ItemId;
    int[] ItemIds;
    int position = 0;
    String title = null;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_article);
        this.context = this;
        this.activity = this;

        //only portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

        //out of rotation
        if (savedInstanceState != null) {
            ItemId = savedInstanceState.getInt(SingleNewsArticleActivity.ITEM_KEY);
        } else {
            //get article id from intent (from deep link or prev menu)
            ItemId = NewsArticle.getIdFromIntent(getIntent(), savedInstanceState);
            System.out.println("article id:" + ItemId);
        }

        //correct id?
        if (ItemId < 0) {
            //check if there's an id list
            if (savedInstanceState == null) {
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    ItemIds = extras.getIntArray(ITEM_LIST_KEY);
                    position = extras.getInt(POSITION, 0);
                    title = extras.getString(TITLE, null);
                }
            } else {
                if (savedInstanceState.containsKey(ITEM_LIST_KEY)) {
                    ItemIds = (int[]) savedInstanceState.getSerializable(ITEM_LIST_KEY);
                    position = (int) savedInstanceState.getSerializable(POSITION);
                    title = (String) savedInstanceState.getSerializable(TITLE);
                }
            }

            //both Item id and Item id list are empty.
            if (ItemIds == null) {
                finish();
                return;
            }
        }


        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(position);

        //set title
        setTitle(getString(R.string.back));
        if (title != null) {
            if (title.length() > 0)
                setTitle(title);
        }
    }

    protected void onSaveInstanceState(Bundle onOrientChange) {
        super.onSaveInstanceState(onOrientChange);
        onOrientChange.putInt(ITEM_KEY, ItemId);
        onOrientChange.putIntArray(ITEM_LIST_KEY, ItemIds);
        onOrientChange.putInt(POSITION, position);
    }

    public boolean isSingleItem() {
        return (ItemId >= 0);
    }


    /**
     * View pager adapter for swipe
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            int id = 0;
            if (isSingleItem())
                id = ItemId;
            else
                id = ItemIds[position];

            SingleNewsArticleFragment f = new SingleNewsArticleFragment();
            Bundle bdl = new Bundle(1);
            bdl.putInt(ITEM_KEY, id);
            f.setArguments(bdl);
            return f;
        }

        @Override
        public int getCount() {
            if (isSingleItem())
                return 1;
            else
                return ItemIds.length;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
