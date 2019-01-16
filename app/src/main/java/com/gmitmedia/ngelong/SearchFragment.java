package com.gmitmedia.ngelong;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.gmitmedia.helpers.EmptyRecyclerView;
import com.gmitmedia.helpers.EndlessRecyclerViewScrollListener;
import com.gmitmedia.helpers.GpsLocation;
import com.gmitmedia.helpers.Rate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gmitmedia.ngelong.R.id.map;

/**
 * Created by melvin on 08/09/2016.
 * A fragment that shows a list of places and provides features for the user to search and filter.
 */
public class SearchFragment extends Fragment {
    public final static int LIST_INITIAL_LOAD = 8;
    public final static int LIST_INITIAL_LOAD_MORE_ONSCROLL = 5;

    Context context;
    static EmptyRecyclerView mRecyclerView;
    static EmptyRecyclerView.Adapter mAdapter;
    EmptyRecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout swipeLayout;
    static Map<String, String> filterParams = new HashMap<String, String>();
    EndlessRecyclerViewScrollListener scrollListener;
    List<Place> places;
    GpsLocation gpsLocation;


    public static String MODE_KEY = "mode_key";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        gpsLocation = new GpsLocation(getActivity());
        getGpsPermission();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        mRecyclerView = (EmptyRecyclerView) rootView.findViewById(R.id.list);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeToRefresh);
        RelativeLayout empty = (RelativeLayout) rootView.findViewById(R.id.empty);
        mRecyclerView.setEmptyView(empty);

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        context = getActivity();
        getActivity().setTitle(getString(R.string.app_name));
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);


        // use a linear layout manage
        int spanCount = 1;
        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_1COLUMNS) {
            mLayoutManager = new LinearLayoutManager(context);

        } else if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS) {
            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            spanCount = ((StaggeredGridLayoutManager) mLayoutManager).getSpanCount();
        }
        mRecyclerView.setLayoutManager(mLayoutManager);


        //handle when user scrolls more than the items on screen
        scrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager, spanCount) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list

                System.out.println("load more" + totalItemsCount);
               // Log.v("problem", "load more Recycleview r: " + mRecyclerView.getTag());
                loadMore(totalItemsCount);
            }

            @Override
            public void onScroll(RecyclerView view, int dx, int dy) {

            }
        };
        mRecyclerView.addOnScrollListener(scrollListener);

        // Swipe to Refresh
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // refreshes the WebView
                refresh();
            }
        });


        //filterParams = new HashMap<String, String>();
        filterParams.put("search", "");
        refresh();

        //double r = Math.random() * 100;
       // mRecyclerView.setTag(r);
      //  Log.v("problem", "Init Recycleview r: " + mRecyclerView.getTag());


        //close keyboard
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    /**
     * Refresh places list from server
     */
    public void refresh() {
        Place.loadMultiple(getActivity(), 0, LIST_INITIAL_LOAD, filterParams, false, new Place.onMultipleDownloadedListener() {
            @Override
            public void onDownloaded(List<Place> items) {
                swipeLayout.setRefreshing(false);
                setPlaces(items);
            }
        });
    }

    /**
     * Load more places from server
     *
     * @param first - start loading from this position
     */
    public void loadMore(int first) {

        Place.loadMultiple(getActivity(), first, LIST_INITIAL_LOAD_MORE_ONSCROLL, filterParams, false, new Place.onMultipleDownloadedListener() {
            @Override
            public void onDownloaded(List<Place> items) {
                swipeLayout.setRefreshing(false);
                ((PlaceAdapter) mAdapter).addItems(items);
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
        mAdapter = new PlaceAdapter(places, gpsLocation, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //open ad. If ad not open attempt to open rate
                if (!((MainActivity) getActivity()).loadInterstitial()) {
                    if (!AskRate()) {
                        // System.out.println("click: " + places.get(i).id + "  " + places.get(i).name);
                        Intent intent = new Intent(context, SinglePlaceActivity.class);
                        intent.putExtra(SinglePlaceActivity.ITEM_KEY, places.get(i).id);
                        startActivity(intent);
                    }
                }
            }
        }, context);
//        if (places_loaded.size() > 0)
//            Log.v("url", "Places:" + ((PlaceAdapter)mAdapter).places.get(1).name);
//        else{
//            Log.v("url", "Places: less than one");
//        }


        mRecyclerView.swapAdapter(mAdapter, false);
        scrollListener.resetState();
    }


    /**
     * Ge permission to use GPS from user
     */
    public void getGpsPermission() {
        gpsLocation.performIfPermissionGranted(new GpsLocation.OnPermissionGrantedListener() {
            @Override
            public void permissionGranted() {
                gpsLocation.getCurrentLocation(new GpsLocation.OnLocationListener() {
                    @Override
                    public void location(Location location, boolean refresh) {
                        if (refresh)
                            refresh();
                    }
                });

            }

            @Override
            public void permissionDenied() {

            }
        });
    }

    /**
     * Get ids of all places
     *
     * @return ids
     */
    public int[] getIds() {
        int[] ids = new int[places.size()];
        for (int i = 0; i < places.size(); i++) {
            ids[i] = places.get(i).id;
        }
        return ids;
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
        inflater.inflate(R.menu.options_menu, menu);
        menu.findItem(R.id.search).setIcon(
                new IconicsDrawable(getContext())
                        .icon(FontAwesome.Icon.faw_search)
                        .color(ContextCompat.getColor(context, R.color.md_white_1000))
                        .sizeDp(18));

        //set map icon from FontAwsome
        menu.findItem(map).setIcon(
                new IconicsDrawable(context)
                        .icon(FontAwesome.Icon.faw_map)
                        .color(ContextCompat.getColor(context, R.color.md_white_1000))
                        .sizeDp(18));
        menu.findItem(R.id.sort).setIcon(
                new IconicsDrawable(context)
                        .icon(FontAwesome.Icon.faw_sort_amount_asc)
                        .color(ContextCompat.getColor(context, R.color.md_white_1000))
                        .sizeDp(18));


        //search
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
        //be careful- References to filterParams are of the first initialised filterParams
        // Handle switch to map
        switch (item.getItemId()) {
            case R.id.map:
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.mainFragment, new MapFragment(), "");
                ft.commit();
                return true;
            case R.id.sort_distance:
                gpsLocation.getCurrentLocation(new GpsLocation.OnLocationListener() {
                    @Override
                    public void location(Location location, boolean refresh) {
                        if (location != null) {
                            filterParams.put("currentlat", "" + location.getLatitude());
                            filterParams.put("currentlng", "" + location.getLongitude());
                            filterParams.remove("sort");
                            refresh();
                        }

                    }
                });
                return true;
            case R.id.sort_rating:
                filterParams.remove("currentlat");
                filterParams.remove("currentlng");
                filterParams.put("sort", "avgrating");
                refresh();
             //   Log.v("problem", "sort Recycleview r: " + mRecyclerView.getTag());
                return true;
            case R.id.sort_recent:
                filterParams.remove("currentlat");
                filterParams.remove("currentlng");
                filterParams.remove("sort");
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
