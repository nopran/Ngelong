package com.gmitmedia.ngelong;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
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
import com.gmitmedia.helpers.Rate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by melvin on 08/09/2016.
 * A fragment that shows a list of articles and provides features for the user to search and filter articles.
 */
public class NewsFragment extends Fragment {
    Context context;
    private EmptyRecyclerView mRecyclerView;
    private EmptyRecyclerView.Adapter mAdapter;
    private EmptyRecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout swipeLayout;
    Map<String, String> filterParams = new HashMap<String, String>();
    EndlessRecyclerViewScrollListener scrollListener;
    List<NewsArticle> newsArticles;

    public final static int LIST_INITIAL_LOAD = 10;
    public final static int LIST_INITIAL_LOAD_MORE_ONSCROLL = 5;

    public static String MODE_KEY = "mode_key";
    public static int RECENT = 0, TOP = 1;
    int mode = RECENT;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        getActivity().setTitle(getString(R.string.news));
        Log.v("create", "create Tab");

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manage
        int spanCount = 1;
        mLayoutManager = new LinearLayoutManager(context);
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

        // Swipe to Refresh
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // refreshes the WebView
                refresh();
            }
        });


        //get item
        if (getArguments() != null) {
            if (getArguments().containsKey(MODE_KEY))
                mode = getArguments().getInt(MODE_KEY, RECENT);
        }

        //filterParams = new HashMap<String, String>();
        filterParams.put("search", "");
        refresh();

        //close keyboard
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


    }


    /**
     * Refresh article list from server
     */
    public void refresh() {

        NewsArticle.loadMultiple(getActivity(), 0, LIST_INITIAL_LOAD, filterParams, (mode == TOP), new NewsArticle.onMultipleDownloadedListener() {
            @Override
            public void onDownloaded(List<NewsArticle> items) {
                swipeLayout.setRefreshing(false);
                setNewsArticles(items);
            }
        });
    }

    /**
     * Load more articles from server
     *
     * @param first - start loading from this article
     */
    public void loadMore(int first) {
        NewsArticle.loadMultiple(getActivity(), first, LIST_INITIAL_LOAD_MORE_ONSCROLL, filterParams, (mode == TOP), new NewsArticle.onMultipleDownloadedListener() {
            @Override
            public void onDownloaded(List<NewsArticle> items) {
                swipeLayout.setRefreshing(false);
                ((NewsArticlesAdapter) mAdapter).addItems(items);
                mRecyclerView.swapAdapter(mAdapter, false);
            }
        });
    }


    /**
     * Set article list
     *
     * @param articles_loaded
     */
    public void setNewsArticles(final List<NewsArticle> articles_loaded) {
        this.newsArticles = articles_loaded;
        mAdapter = new NewsArticlesAdapter(newsArticles, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //open ad. If ad not open attempt to open rate
                if (!((MainActivity) getActivity()).loadInterstitial()) {
                    if (!AskRate()) {
                        System.out.println("click: " + newsArticles.get(i).id + "  " + newsArticles.get(i).name);
                        Intent intent = new Intent(context, SingleNewsArticleActivity.class);
                        intent.putExtra(SingleNewsArticleActivity.ITEM_LIST_KEY, getIds());
                        intent.putExtra(SingleNewsArticleActivity.POSITION, i);
                        intent.putExtra(SingleNewsArticleActivity.TITLE, getString(R.string.news));
                        startActivity(intent);
                    }
                }
            }
        }, context);
        mRecyclerView.swapAdapter(mAdapter, false);
        scrollListener.resetState();

    }

    /**
     * Get ids of all articles
     *
     * @return
     */
    public int[] getIds() {
        int[] ids = new int[newsArticles.size()];
        for (int i = 0; i < newsArticles.size(); i++) {
            ids[i] = newsArticles.get(i).id;
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


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.search_news_menu, menu);
        menu.findItem(R.id.search).setIcon(
                new IconicsDrawable(getContext())
                        .icon(FontAwesome.Icon.faw_search)
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


}
