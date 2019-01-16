package com.gmitmedia.ngelong;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.gmitmedia.helpers.EmptyRecyclerView;
import com.gmitmedia.helpers.EndlessRecyclerViewScrollListener;
import com.gmitmedia.helpers.GpsLocation;
import com.gmitmedia.helpers.Rate;
import com.gmitmedia.helpers.RecyclerViewPositionHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.gmitmedia.ngelong.SingleNewsArticleActivity.ITEM_KEY;

/**
 * Created by melvin on 08/09/2016.
 * Shows a full screen map with the place list in the bottom.
 */
public class MapFragment extends Fragment {
    Context context;

    //list
    private EmptyRecyclerView mRecyclerView;
    private EmptyRecyclerView.Adapter mAdapter;
    private EmptyRecyclerView.LayoutManager mLayoutManager;

    //map
    private GoogleMap map;
    List<Marker> markers = new ArrayList<Marker>();
    RecyclerViewPositionHelper posHelper;

    //all places
    Map<String, String> filterParams = new HashMap<String, String>();
    EndlessRecyclerViewScrollListener scrollListener;
    List<Place> places;
    Intent filterIntentData;
    BitmapDescriptor pin;
    double targetLat, targetLng;

    public final static int FILTER_REQUEST_CODE = 1547;

    public final static int LIST_INITIAL_LOAD = 10;
    public final static int LIST_INITIAL_LOAD_MORE_ONSCROLL = 5;

    GpsLocation gpsLocation;
    LinearLayout searcharea;
    Boolean stopLoading = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        gpsLocation = new GpsLocation(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mRecyclerView = (EmptyRecyclerView) rootView.findViewById(R.id.list);
        RelativeLayout empty = (RelativeLayout) rootView.findViewById(R.id.empty);
        mRecyclerView.setEmptyView(empty);
        searcharea = (LinearLayout) rootView.findViewById(R.id.searcharea);

        //set up map when it is loaded
        if (map == null) {
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;
                    map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            int i = (int) marker.getTag();
                            Intent intent = new Intent(context, SinglePlaceActivity.class);
                            intent.putExtra(SinglePlaceActivity.ITEM_KEY, places.get(i).id);
                            startActivity(intent);
                            return false;
                        }
                    });
                    gpsLocation.performIfPermissionGranted(new GpsLocation.OnPermissionGrantedListener() {
                        @Override
                        public void permissionGranted() {
                            Log.v("GPS", "granted");
                            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                map.setMyLocationEnabled(true);
                            }
                        }

                        @Override
                        public void permissionDenied() {

                        }
                    });


                }


            });
        }
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        context = getActivity();
        getActivity().setTitle(getString(R.string.app_name));

        //load pin image
        pin = BitmapDescriptorFactory.fromResource(R.drawable.pin);


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manage
        int spanCount = 1;
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        posHelper = new RecyclerViewPositionHelper(mRecyclerView);

        //handle when user scrolls more than the items on screen
        scrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager, spanCount) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                System.out.println("load more" + totalItemsCount);
                loadMore(totalItemsCount);
            }

            @Override
            public void onScroll(RecyclerView view, int dx, int dy) {
                if (places.size() > 0) {
                    Place p = places.get(posHelper.findFirstVisibleItemPosition());
                    gotoMap(p.gpslat, p.gpslng);
                }
            }
        };
        mRecyclerView.addOnScrollListener(scrollListener);

        //Redo search in a particular area button
        searcharea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterParams.put("currentlat", "" + map.getCameraPosition().target.latitude);
                filterParams.put("currentlng", "" + map.getCameraPosition().target.longitude);
                refresh();
            }
        });
        searcharea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        searcharea.setBackgroundColor(ContextCompat.getColor(context, R.color.primary));
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        searcharea.setBackgroundColor(ContextCompat.getColor(context, R.color.color_mapOverlay));
                        break;
                    case MotionEvent.ACTION_UP:
                        searcharea.setBackgroundColor(ContextCompat.getColor(context, R.color.color_mapOverlay));
                        break;
                }
                return false;
            }
        });

        //finally refresh places
        refresh();
    }

    /**
     * Refresh place list from server
     */
    public void refresh() {
        stopLoading = false;
        Place.loadMultiple(getActivity(), 0, LIST_INITIAL_LOAD, filterParams, false, new Place.onMultipleDownloadedListener() {
            @Override
            public void onDownloaded(List<Place> places) {
                setPlaces(places);

            }
        });
    }

    /**
     * Load more places from server
     *
     * @param first - start loading from this place
     */
    public void loadMore(int first) {
        if (!stopLoading)
            Place.loadMultiple(getActivity(), first, LIST_INITIAL_LOAD_MORE_ONSCROLL, filterParams, false, new Place.onMultipleDownloadedListener() {
                @Override
                public void onDownloaded(List<Place> places) {
                    if (places.size() == 0)
                        stopLoading = true;
                    //((MapPlaceAdapter) mAdapter).addItems(places);
                    addPlaces(places);
                    mRecyclerView.swapAdapter(mAdapter, false);
                }
            });
    }


    /**
     * Set place list
     *
     * @param places_loaded
     */
    public void setPlaces(final List<Place> places_loaded) {
        this.places = places_loaded;

        loadPlaces();

    }

    /**
     * Add more Places to list
     *
     * @param places_loaded
     */
    public void addPlaces(final List<Place> places_loaded) {
        this.places.addAll(places_loaded);

        loadPlaces();
    }

    /**
     * Load places from global array to map
     */
    public void loadPlaces() {

        if (getActivity() == null || !isAdded())
            return;

        //clear markers
        markers.clear();
        map.clear();

        //add new markers
        for (int i = 0; i < places.size(); i++) {
            LatLng coordinate = new LatLng(places.get(i).gpslat, places.get(i).gpslng);

            //add marker with no icon
            Marker marker;

            marker = map.addMarker(new MarkerOptions().position(coordinate).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("pin", 100))));
            marker.setTag(i);
            markers.add(marker);

            //load icon from server using the picasso library
            places.get(i).loadPinBitmap(context, i, new Place.PinLoadedListener() {
                @Override
                public void onLoaded(Bitmap bitmap, Place place, int i) {
                    markers.get(i).setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                }

                @Override
                public void onFailed(Place place, int i) {

                }
            });
        }

        //create list
        mAdapter = new MapPlaceAdapter(places, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //open ad. If ad not open attempt to open rate
                if (!((MainActivity) getActivity()).loadInterstitial()) {
                    if (!AskRate()) {
                        System.out.println("click: " + places.get(i).id + "  " + places.get(i).name);
                        Intent intent = new Intent(context, SinglePlaceActivity.class);
                        intent.putExtra(ITEM_KEY, places.get(i).id);
                        startActivity(intent);
                    }
                }
            }
        }, context);
        mRecyclerView.swapAdapter(mAdapter, false);
        scrollListener.resetState();

        //set marker to first item
        if (places.size() > 0) {
            Place p = places.get(0);
            gotoMap(p.gpslat, p.gpslng);
        }
    }


    /**
     * Resize map icon so that it is responsive
     *
     * @param iconName
     * @param width
     * @return
     */
    public Bitmap resizeMapIcons(String iconName, int width) {

        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", context.getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, (int) (((float) width / (float) imageBitmap.getWidth()) * imageBitmap.getHeight()), false);

    }


    /**
     * Animate map camera position to new lat/lng
     *
     * @param lat
     * @param lng
     */
    public void gotoMap(double lat, double lng) {
        if (map == null)
            return;

        //do not animate if already there
        if (!(lat == targetLat && lng == targetLng)) {
            targetLat = lat;
            targetLng = lng;

            //convert to LatLng
            LatLng coordinate = new LatLng(lat, lng);

            // Save current zoom
            float originalZoom = map.getCameraPosition().zoom;

            // Move temporarily camera zoom
            map.moveCamera(CameraUpdateFactory.zoomTo(13));

            //get display height
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            //offset
            Point pointInScreen = map.getProjection().toScreenLocation(coordinate);
            Point newPoint = new Point();
            newPoint.x = pointInScreen.x;
            newPoint.y = pointInScreen.y + (int) (size.y / 4.5f);
            LatLng newCenterLatLng = map.getProjection().fromScreenLocation(newPoint);

            // Restore original zoom
            map.moveCamera(CameraUpdateFactory.zoomTo(originalZoom));

            //animate
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(newCenterLatLng, 13));
        }
    }

    /**
     * Ask user to rate
     */
    public boolean AskRate() {
        return Rate.rateWithCounter(getActivity(), getResources().getInteger(R.integer.rate_shows_after_X_starts), getResources().getString(R.string.rate_title), getResources().getString(R.string.rate_text), getResources().getString(R.string.unable_to_reach_market), getResources().getString(R.string.Alert_accept), getResources().getString(R.string.Alert_cancel));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        gpsLocation.onActivityResult(requestCode, resultCode, data);

        //filter screen returned data
        if (requestCode == FILTER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                filterIntentData = data;
                //refresh list
                refresh();
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        gpsLocation.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.options_menu_map, menu);
        menu.findItem(R.id.search).setIcon(
                new IconicsDrawable(getContext())
                        .icon(FontAwesome.Icon.faw_search)
                        .color(ContextCompat.getColor(context, R.color.md_white_1000))
                        .sizeDp(18));

        //set map icon from FontAwsome
        menu.findItem(R.id.list).setIcon(
                new IconicsDrawable(context)
                        .icon(FontAwesome.Icon.faw_list)
                        .color(ContextCompat.getColor(context, R.color.md_white_1000))
                        .sizeDp(18));


        MenuItem item = menu.findItem(R.id.search);
        try {
            SearchView searchView = new SearchView(((MainActivity) context).getSupportActionBar().getThemedContext());
            MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
            MenuItemCompat.setActionView(item, searchView);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    System.out.println("load more search" + newText.toString());
                    filterParams.put("search", newText.toString());
                    refresh();
                    return false;
                }
            });
            searchView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }
            );
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle switch to map
        switch (item.getItemId()) {
            case R.id.list:
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.mainFragment, new SearchFragment(), "");
                ft.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
