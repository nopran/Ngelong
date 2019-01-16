package com.gmitmedia.ngelong;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.gmitmedia.helpers.EmptyRecyclerView;
import com.gmitmedia.helpers.GpsLocation;

import java.util.List;

/**
 * Created by melvin on 08/09/2016.
 * <p>
 * Shows a list of the bookmarked Places. The bookmarked Places are stored by id locally in preferences.
 * The content is however obtained from server.
 */
public class BookmarkFragment extends Fragment {
    Context context;
    private EmptyRecyclerView mRecyclerView;
    private EmptyRecyclerView.Adapter mAdapter;
    private EmptyRecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout swipeLayout;
    RelativeLayout empty;
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
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeToRefresh);
        mRecyclerView = (EmptyRecyclerView) rootView.findViewById(R.id.list);
        empty = (RelativeLayout) rootView.findViewById(R.id.empty);
        mRecyclerView.setEmptyView(empty);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();

        //set title
        getActivity().setTitle(getString(R.string.Bookmark_page_title));

        //set RecyclerView
        mRecyclerView.setHasFixedSize(true);

        int spanCount = 1;
        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_1COLUMNS) {
            mLayoutManager = new LinearLayoutManager(context);

        } else if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS) {
            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            spanCount = ((StaggeredGridLayoutManager) mLayoutManager).getSpanCount();
        }
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Swipe to Refresh
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        //refresh();
        getGpsPermission();
    }

    /**
     * Refresh Bookmarked Places.
     */
    public void refresh() {
        Place.getFavorites(getActivity(), new Place.onMultipleDownloadedListener() {
            @Override
            public void onDownloaded(List<Place> place) {
                swipeLayout.setRefreshing(false);
                setPlaces(place);
            }
        });
    }


    /**
     * Show places on screen after refresh
     *
     * @param places
     */
    public void setPlaces(final List<Place> places) {
        mAdapter = new PlaceAdapter(places, gpsLocation, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, SinglePlaceActivity.class);
                intent.putExtra(SinglePlaceActivity.ITEM_KEY, places.get(i).id);
                startActivity(intent);
            }
        }, context);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Obtain GPS permission from user
     */
    public void getGpsPermission() {
        gpsLocation.performIfPermissionGranted(new GpsLocation.OnPermissionGrantedListener() {
            @Override
            public void permissionGranted() {
                Log.v("GPS", "granted");
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        gpsLocation.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        gpsLocation.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

    }

}
