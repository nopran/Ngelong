package com.gmitmedia.ngelong;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.gmitmedia.helpers.EmptyRecyclerView;
import com.gmitmedia.helpers.EndlessRecyclerViewScrollListener;
import com.gmitmedia.helpers.GpsLocation;

import java.util.List;

/**
 * Created by melvin on 06/06/2017.
 * Shows a list of Places that belong to a category
 */
public class PlacesInCategoryFragment extends Fragment {
    public final static int LIST_INITIAL_LOAD = 10;
    public final static int LIST_INITIAL_LOAD_MORE_ONSCROLL = 5;

    Context context;
    private EmptyRecyclerView mRecyclerView;
    private EmptyRecyclerView.Adapter mAdapter;
    private EmptyRecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout swipeLayout;
    int categoryId = 0;
    EndlessRecyclerViewScrollListener scrollListener;
    List<Place> places;
    GpsLocation gpsLocation;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        gpsLocation = new GpsLocation(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);
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

        //set RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
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
                loadMore(totalItemsCount);
            }

            @Override
            public void onScroll(RecyclerView view, int dx, int dy) {

            }
        };
        mRecyclerView.addOnScrollListener(scrollListener);


        //get category Id from
        categoryId = getArguments().getInt("Category_id", 0);

        //load category from server
        Category.getCategoryName(context, categoryId, new Category.onNameFoundListener() {
            @Override
            public void onNameFound(String name) {
                getActivity().setTitle(name);
            }
        });

        // Swipe to Refresh
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        swipeLayout.setRefreshing(true);
        // refresh();
        getGpsPermission();
    }

    /**
     * Refresh places list from server
     */
    public void refresh() {
        Place.loadMultiple(getActivity(), 0, LIST_INITIAL_LOAD, "", "" + categoryId, new Place.onMultipleDownloadedListener() {
            @Override
            public void onDownloaded(List<Place> places) {
                swipeLayout.setRefreshing(false);
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
        Place.loadMultiple(getActivity(), first, LIST_INITIAL_LOAD_MORE_ONSCROLL, "", "" + categoryId, new Place.onMultipleDownloadedListener() {
            @Override
            public void onDownloaded(List<Place> places) {
                swipeLayout.setRefreshing(false);
                ((PlaceAdapter) mAdapter).addItems(places);
                mRecyclerView.swapAdapter(mAdapter, false);
            }
        });
    }


    /**
     * Show places to screen
     *
     * @param places_loaded - list of places to show
     */
    public void setPlaces(final List<Place> places_loaded) {
        this.places = places_loaded;
        mAdapter = new PlaceAdapter(places, gpsLocation, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //open in new activity on click
                Intent intent = new Intent(context, SinglePlaceActivity.class);
                intent.putExtra(SinglePlaceActivity.ITEM_KEY, places.get(i).id);
                startActivity(intent);
            }
        }, context);
        mRecyclerView.swapAdapter(mAdapter, false);
        scrollListener.resetState();
    }

    /**
     * Get the ids of all places
     *
     * @return
     */
    public int[] getIds() {
        int[] ids = new int[places.size()];
        for (int i = 0; i < places.size(); i++) {
            ids[i] = places.get(i).id;
        }
        return ids;
    }


    /**
     * Get permission to use GPS from user
     */
    public void getGpsPermission() {
        gpsLocation.performIfPermissionGranted(new GpsLocation.OnPermissionGrantedListener() {
            @Override
            public void permissionGranted() {
                gpsLocation.getCurrentLocation(new GpsLocation.OnLocationListener() {
                    @Override
                    public void location(Location location, boolean refresh) {
                        // if (refresh)
                        refresh();
                    }
                });
            }

            @Override
            public void permissionDenied() {
                refresh();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //clear options menu
        menu.clear();

        //re-initialise menu
        inflater.inflate(R.menu.options_menu_categories, menu);

        //set search icon using FontAwesome
        menu.findItem(R.id.search).setIcon(
                new IconicsDrawable(getContext())
                        .icon(FontAwesome.Icon.faw_search)
                        .color(ContextCompat.getColor(context, R.color.md_white_1000))
                        .sizeDp(18));

        //set search feature
        MenuItem item = menu.findItem(R.id.search);
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
                //Search on server when text changed
                Place.loadMultiple(getActivity(), 0, 1000, newText, "" + categoryId, new Place.onMultipleDownloadedListener() {
                    @Override
                    public void onDownloaded(List<Place> places) {
                        setPlaces(places);
                    }
                });
                return false;
            }
        });
    }
}
