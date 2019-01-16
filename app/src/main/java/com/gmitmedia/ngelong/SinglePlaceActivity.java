package com.gmitmedia.ngelong;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Dimension;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecloud.pulltozoomview.PullToZoomScrollViewEx;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;
import com.mikepenz.iconics.view.IconicsTextView;
import com.gmitmedia.helpers.BillingHelper;
import com.gmitmedia.helpers.EmptyRecyclerView;
import com.gmitmedia.helpers.FontManager;
import com.gmitmedia.helpers.SlideShowAdapter;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

import static com.gmitmedia.helpers.FontManager.FONTAWESOME;

/**
 * This activity shows a single place
 */
public class SinglePlaceActivity extends AppCompatActivity {

    public final static int LIST_INITIAL_LOAD = 4;
    public final static int LIST_INITIAL_LOAD_MORE_ONSCROLL = 5;

    PullToZoomScrollViewEx scrollView;
    IconicsImageView favoriteBtn;
    Place place;
    Context context;
    FragmentActivity activity;
    TextView priceView, priceSuffix;
    //IconicsTextView photosView;
    IconicsTextView addressView, phoneView, emailView, phoneButton, addressButton, emailButton;
    TextView addressTitleView, phoneTitleView, emailTitleView;
    RelativeLayout addressLayout, phoneLayout, emailLayout;
    WebView descriptionView;
    private GoogleMap map;
    private Marker marker;
    RelativeLayout price_view;
    Button dealBtn;
    LinearLayout review_layout;

    //slideshow
    int slideshow_current_image = 0;
    ViewPager mPager;
    CircleIndicator indicator;

    public static String ITEM_KEY = "item_key";
    int ItemId;

    Typeface robotoMedium;

    List<Review> reviews;
    EditText my_review;
    static TextView[] stars = new TextView[6];

    private RecyclerView mRecyclerView;
    private EmptyRecyclerView.Adapter mAdapter;
    private EmptyRecyclerView.LayoutManager mLayoutManager;

    NativeExpressAdView ad;
    LinearLayout BackgroundLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_place);
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

        robotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");


        //get scrollview
        scrollView = (PullToZoomScrollViewEx) findViewById(R.id.scroll_view);

        //set zoom, content and header view
        View headView = LayoutInflater.from(this).inflate(R.layout.place_head_view, null, false);
        View zoomView = LayoutInflater.from(this).inflate(R.layout.place_zoom_view, null, false);
        View contentView = LayoutInflater.from(this).inflate(R.layout.place_content_view, null, false);
        scrollView.setHeaderView(headView);
        scrollView.setZoomView(zoomView);
        scrollView.setScrollContentView(contentView);

        //set aspect ratio of header image
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        int mScreenWidth = localDisplayMetrics.widthPixels;
        LinearLayout.LayoutParams localObject = new LinearLayout.LayoutParams(mScreenWidth, (int) (9.0F * (mScreenWidth / 16.0F)));
        scrollView.setHeaderLayoutParams(localObject);

        //get place image element
        mPager = (ViewPager) zoomView.findViewById(R.id.pager);
        indicator = (CircleIndicator) zoomView.findViewById(R.id.indicator);

        //set favorite button
        favoriteBtn = (IconicsImageView) headView.findViewById(R.id.favorite);
        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (place.isFavorite(context)) {
                    favoriteBtn.setIcon("faw_star_o");
                    place.setFavorite(context, false);
                } else {
                    favoriteBtn.setIcon("faw_star");
                    place.setFavorite(context, true);
                }
            }
        });

        //get place prices elements
        price_view = (RelativeLayout) contentView.findViewById(R.id.price_view);
        priceView = (TextView) contentView.findViewById(R.id.price);
        priceSuffix = (TextView) contentView.findViewById(R.id.price_suffix);
        dealBtn = (Button) contentView.findViewById(R.id.view_deal_button);

        //get place status and photo counter
        //photosView = (IconicsTextView) headView.findViewById(R.id.photos);


        //set description view
        descriptionView = (WebView) contentView.findViewById(R.id.descriptionView);
        descriptionView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
            }
        });
        //enable javascript
        descriptionView.getSettings().setJavaScriptEnabled(true);

        //favorites and address views
        addressView = (IconicsTextView) contentView.findViewById(R.id.addressView);
        phoneView = (IconicsTextView) contentView.findViewById(R.id.phoneView);
        emailView = (IconicsTextView) contentView.findViewById(R.id.emailView);
        addressTitleView = (TextView) contentView.findViewById(R.id.addressTitleView);
        phoneTitleView = (TextView) contentView.findViewById(R.id.phoneTitleView);
        emailTitleView = (TextView) contentView.findViewById(R.id.emailTitleView);
        phoneButton = (IconicsTextView) contentView.findViewById(R.id.phoneButton);
        addressButton = (IconicsTextView) contentView.findViewById(R.id.addressButton);
        emailButton = (IconicsTextView) contentView.findViewById(R.id.emailButton);
        phoneLayout = (RelativeLayout) contentView.findViewById(R.id.phoneLayout);
        addressLayout = (RelativeLayout) contentView.findViewById(R.id.addressLayout);
        emailLayout = (RelativeLayout) contentView.findViewById(R.id.emailLayout);

        //remove reviews if user system is disabled
        review_layout = (LinearLayout) findViewById(R.id.review_layout);
        if (!Configurations.ENABLE_USER_SYSTEM) {
            review_layout.setVisibility(View.GONE);
        }

        //rating
        //star container
        LinearLayout starContainer = (LinearLayout) findViewById(R.id.star_container);
        Typeface iconFont = FontManager.getTypeface(activity, FONTAWESOME);

        //star margin
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, activity.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(margin, margin, margin, margin);

        //add stars
        for (int i = 0; i < 5; i++) {
            stars[i] = new TextView(activity);
            stars[i].setText(activity.getString(R.string.reviews_fa_star_o));
            stars[i].setTypeface(iconFont);
            stars[i].setTextSize(Dimension.SP, 40);
            stars[i].setLayoutParams(llp);
            stars[i].setTextColor(ContextCompat.getColor(context, R.color.accent));
            stars[i].setTag(i);
            stars[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (User.isUserLoggedInElseTry(context)) {
                        int rating = (int) view.getTag() + 1;
                        setRating(rating);
                        //submit review
                        Rating.addRating(activity, rating, place.id, new Rating.onDoneListener() {
                            @Override
                            public void onDone() {
                                refreshMyRating();
                            }

                            @Override
                            public void onError() {
                            }
                        });
                    }
                }
            });
            starContainer.addView(stars[i]);
        }


        //map
        initialiseMap();


        //out of rotation
        if (savedInstanceState != null) {
            ItemId = savedInstanceState.getInt(ITEM_KEY);
        } else {
            //get place id from intent (from deep link or prev menu)
            ItemId = Place.getIdFromIntent(getIntent(), savedInstanceState);
            System.out.println("place id:" + ItemId);
        }

        //correct id?
        if (ItemId < 0) {
            finish();
            return;
        }

        //load place
        Place.loadSingle(this, ItemId, new Place.onDownloadedListener() {
            @Override
            public void onDownloaded(Place propertyLocal) {
                propertyLocal.viewed(activity);
                place = propertyLocal;
                setTitle(place.name);
                descriptionView.loadData(Functions.HTMLTemplate(place.text), "text/html; charset=utf-8", "utf-8");
                DecimalFormat dFormat = new DecimalFormat("####,###,###");
                if (place.price > 0) {
                    priceView.setText(getString(R.string.currency) + dFormat.format(place.price));
                    priceSuffix.setText(place.price_suffix);
                    dealBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(place.deal_link));
                            if (browserIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(browserIntent);
                            }
                        }
                    });
                    priceView.setVisibility(View.VISIBLE);
                    priceSuffix.setVisibility(View.VISIBLE);
                    price_view.setVisibility(View.VISIBLE);
                } else {
                    priceView.setVisibility(View.GONE);
                    priceSuffix.setVisibility(View.GONE);
                    price_view.setVisibility(View.GONE);
                }

                //set address, email and phone
                if (place.address.length() > 0) {
                    addressView.setText(place.address);
                    addressView.setVisibility(View.VISIBLE);
                    addressTitleView.setVisibility(View.VISIBLE);
                    addressButton.setVisibility(View.VISIBLE);
                    addressLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + place.gpslat + "," + place.gpslng + " (" + place.name + ")"));
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        }
                    });
                    addressLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                addressLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_list_background));
                            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                                addressLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_card_background));
                            }
                            return false;
                        }
                    });
                }
                if (place.email.length() > 0) {
                    emailView.setText(place.email);
                    emailView.setVisibility(View.VISIBLE);
                    emailTitleView.setVisibility(View.VISIBLE);
                    emailButton.setVisibility(View.VISIBLE);
                    emailLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("message/rfc822");
                            i.putExtra(Intent.EXTRA_EMAIL, new String[]{place.email});
                            i.putExtra(Intent.EXTRA_SUBJECT, place.name);
                            try {
                                startActivity(Intent.createChooser(i, getResources().getString(R.string.email_send_email)));
                            } catch (android.content.ActivityNotFoundException ex) {
                                Toast.makeText(activity, getResources().getString(R.string.email_no_email), Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                    emailLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {

                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                emailLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_list_background));
                            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                                emailLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_card_background));
                            }
                            return false;
                        }
                    });
                }
                if (place.telephone.length() > 0) {
                    phoneView.setText(place.telephone);
                    phoneView.setVisibility(View.VISIBLE);
                    phoneTitleView.setVisibility(View.VISIBLE);
                    phoneButton.setVisibility(View.VISIBLE);
                    phoneLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                            callIntent.setData(Uri.parse("tel://" + place.telephone));
                            startActivity(callIntent);
                        }
                    });
                    phoneLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {

                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                phoneLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_list_background));
                            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                                phoneLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_card_background));
                            }
                            return false;
                        }
                    });
                }

                //map coordinates
                setMapCoordinates(place.gpslat, place.gpslng);

                //set fav button
                refreshFavoriteBtn();

                //set image
                if (place.imageUrl != null) {
                    mPager.setAdapter(new SlideShowAdapter(context, place.imageUrl, R.drawable.loading, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openFullScreenImage();
                        }
                    }));
                    indicator.setViewPager(mPager);
                }


                refreshReviews();
                refreshMyReview();
                refreshMyRating();

                //photos count
                // photosView.setText(String.format(getResources().getString(R.string.photos_count), "" + place.imageUrl.length));
            }
        });


        // Auto start of viewpager
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (place != null) {
                    if (place.imageUrl != null) {
                        if (slideshow_current_image == place.imageUrl.length) {
                            slideshow_current_image = 0;
                        }
                        mPager.setCurrentItem(slideshow_current_image++, true);
                    }
                }
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, Configurations.SLIDESHOW_TIME_SECONDS * 2000, Configurations.SLIDESHOW_TIME_SECONDS * 1000);


        //reviews title
        TextView reviews_title = (TextView) contentView.findViewById(R.id.reviews_title);
        reviews_title.setTypeface(robotoMedium);

        //reviews RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);


        //load more reviews
        final TextView loadMoreBtn = (TextView) findViewById(R.id.load_more_button);
        loadMoreBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ((TextView) view).setTextColor(0xFFFFFFFF); // lightblack
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        ((TextView) view).setTextColor(ContextCompat.getColor(activity, R.color.accent)); // white
                        break;
                    case MotionEvent.ACTION_UP:
                        ((TextView) view).setTextColor(ContextCompat.getColor(activity, R.color.accent)); // white
                        break;
                }
                return false;
            }
        });
        loadMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMoreReviews(mAdapter.getItemCount());
            }
        });


        //My review
        my_review = (EditText) findViewById(R.id.my_review);
        //submit review
        final TextView reviewSubmitBtn = (TextView) findViewById(R.id.review_submit);
        reviewSubmitBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ((TextView) view).setTextColor(0xFFFFFFFF); // lightblack
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        ((TextView) view).setTextColor(ContextCompat.getColor(activity, R.color.accent)); // white
                        break;
                    case MotionEvent.ACTION_UP:
                        ((TextView) view).setTextColor(ContextCompat.getColor(activity, R.color.accent)); // white
                        break;
                }
                return false;
            }
        });
        reviewSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (User.isUserLoggedInElseTry(context)) {
                    reviewSubmitBtn.setEnabled(false);
                    reviewSubmitBtn.setTextColor(ContextCompat.getColor(activity, R.color.colorDivider));
                    //submit review
                    Review.addReview(activity, my_review.getText().toString(), place.id, new Review.onDoneListener() {
                        @Override
                        public void onDone() {
                            reviewSubmitBtn.setTextColor(ContextCompat.getColor(activity, R.color.accent));
                            reviewSubmitBtn.setEnabled(true);
                            refreshReviews();
                        }

                        @Override
                        public void onError() {
                            reviewSubmitBtn.setTextColor(ContextCompat.getColor(activity, R.color.accent));
                            reviewSubmitBtn.setEnabled(true);
                        }
                    });
                }
            }
        });


//        //Native Advert
//        NativeExpressAdView adView = (NativeExpressAdView)findViewById(R.id.adView);
//        AdRequest request = new AdRequest.Builder().build();
//        adView.loadAd(request);

        //Admob Banner and Interstitial Advert
        BackgroundLayout = (LinearLayout) findViewById(R.id.background_layout);
        ad = (NativeExpressAdView) findViewById(R.id.adView);
        if (!BillingHelper.isPremium(context)) {

            AdRequest adRequest = new AdRequest.Builder().build();

            if (getResources().getString(R.string.native_ad).length() > 1) {
                ad.loadAd(adRequest);
            } else {
                if (ad != null)
                    ad.destroy();
                if (BackgroundLayout != null)
                    BackgroundLayout.removeView(ad);
            }
        } else {
            //destroy banner ad
            if (ad != null)
                ad.destroy();
            if (BackgroundLayout != null)
                BackgroundLayout.removeView(ad);
        }

    }


    public void refreshMyReview() {
        if (User.isUserLoggedIn(context)) {
            //get my review
            Review.loadSingle(activity, place.id, new Review.onDownloadedListener() {
                @Override
                public void onDownloaded(Review item) {
                    if (item.hasreview)
                        my_review.setText(item.text);
                }
            });
        }
    }

    public void refreshMyRating() {
        if (User.isUserLoggedIn(context)) {
            //get my review
            Rating.loadSingle(activity, place.id, new Rating.onDownloadedListener() {
                @Override
                public void onDownloaded(Rating item) {
                    if (item.hasrating)
                        setRating((int) item.rating);
                }
            });
        }
    }

    private void setRating(int rating) {
        rating = rating - 1;
        for (int i = 0; i <= rating; i++) {
            stars[i].setText(activity.getString(R.string.reviews_fa_star));
        }
        if (rating != 5) {
            for (int i = rating + 1; i < 5; i++) {
                stars[i].setText(activity.getString(R.string.reviews_fa_star_o));
            }
        }
    }

    protected void onSaveInstanceState(Bundle onOrientChange) {
        super.onSaveInstanceState(onOrientChange);
        onOrientChange.putInt(ITEM_KEY, ItemId);
    }


    public void refreshReviews() {

        Review.loadMultiple(activity, 0, LIST_INITIAL_LOAD, "" + place.id, new Review.onMultipleDownloadedListener() {
            @Override
            public void onDownloaded(List<Review> items) {
                setReviews(items);
            }
        });
    }

    /**
     * Load more articles from server
     *
     * @param first - start loading from this article
     */
    public void loadMoreReviews(int first) {
        Review.loadMultiple(activity, first, LIST_INITIAL_LOAD_MORE_ONSCROLL, "" + place.id, new Review.onMultipleDownloadedListener() {
            @Override
            public void onDownloaded(List<Review> items) {
                ((ReviewAdapter) mAdapter).addItems(items);
                mRecyclerView.swapAdapter(mAdapter, false);
            }
        });
    }


    public void setReviews(final List<Review> reviews_loaded) {
        this.reviews = reviews_loaded;
        mAdapter = new ReviewAdapter(reviews, context);
        mRecyclerView.swapAdapter(mAdapter, false);
    }

    /**
     * Open images in full screen mode
     */
    public void openFullScreenImage() {
        Bundle b = new Bundle();
        b.putStringArray("imageUrl", place.imageUrl);
        b.putInt("slideshow_seconds", Configurations.SLIDESHOW_TIME_SECONDS);
        Intent i = new Intent(context, FullScreenImage.class);
        i.putExtras(b);
        startActivity(i);
    }


    /**
     * Set the map coordinates
     *
     * @param lat
     * @param lng
     */
    public void setMapCoordinates(Double lat, double lng) {
        if (map == null)
            return;
        LatLng coordinate = new LatLng(lat, lng);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(coordinate)      // Sets the center of the map to Mountain View
                .zoom(13)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        marker = map.addMarker(new MarkerOptions().position(coordinate));
    }


    /**
     * Initialise mini map
     */
    private void initialiseMap() {
        if (map != null) {
            return;
        }
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + place.gpslat + "," + place.gpslng + " (" + place.name + ")"));
                        startActivity(intent);
                    }
                });
            }
        });

        // Initialize map options. For example:
        // mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }


    /**
     * Refresh Favorite button (star).
     * Checks state from local preferences
     */
    public void refreshFavoriteBtn() {
        if (place.isFavorite(context)) {
            favoriteBtn.setIcon("faw_star");
        } else {
            favoriteBtn.setIcon("faw_star_o");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        initialiseMap();
        descriptionView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        descriptionView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        descriptionView.destroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.single_place_menu, menu);

        //set share icon from FontAwsome
        menu.findItem(R.id.share).setIcon(
                new IconicsDrawable(context)
                        .icon(FontAwesome.Icon.faw_share_alt)
                        .color(ContextCompat.getColor(context, R.color.md_white_1000))
                        .sizeDp(18));
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle share
        switch (item.getItemId()) {
            case R.id.share:
                if (place != null)
                    place.share(activity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
