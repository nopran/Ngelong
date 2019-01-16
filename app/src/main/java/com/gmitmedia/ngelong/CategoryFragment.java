package com.gmitmedia.ngelong;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.gmitmedia.helpers.EmptyRecyclerView;

import java.util.List;

/**
 * Created by melvin on 08/09/2016.
 * A fragment which displays a list of all the categories in text and image format.
 */
public class CategoryFragment extends Fragment {
    Context context;
    private EmptyRecyclerView mRecyclerView;
    private EmptyRecyclerView.Adapter mAdapter;
    private EmptyRecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout swipeLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

        //set title
        getActivity().setTitle(getString(R.string.categories_page_title));

        //set RecycleView
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Swipe to Refresh
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        refresh();
    }


    /**
     * Reloads categories from server and shows them on screen
     */
    public void refresh() {
        Category.loadCategories(context, "", new Category.onCategoriesDownloadedListener() {
            @Override
            public void onCategoriesDownloaded(List<Category> categories) {
                swipeLayout.setRefreshing(false);
                setCategories(categories);
            }
        });
    }


    /**
     * Shows loaded categories on screen
     *
     * @param categories
     */
    public void setCategories(final List<Category> categories) {
        mAdapter = new CategoryAdapter(categories, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle b = new Bundle();
                b.putInt("Category_id", categories.get(i).id);
                Fragment f = new PlacesInCategoryFragment();
                f.setArguments(b);
                changeFragment(f);

            }
        }, context);
        mRecyclerView.setAdapter(mAdapter);
    }


    /**
     * Change parent fragment. Replace itself.
     * @param  - Fragment to open up.
     */
    public void changeFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainFragment, fragment);
        //transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
//        inflater.inflate(R.menu.main_menu, menu);
//
//        //set map icon from FontAwsome
//        menu.findItem(R.id.search).setIcon(
//                new IconicsDrawable(context)
//                        .icon(FontAwesome.Icon.faw_search)
//                        .color(ContextCompat.getColor(context, R.color.md_white_1000))
//                        .sizeDp(18));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle share
        switch (item.getItemId()) {
//            case R.id.search:
//                final FragmentTransaction ft = getFragmentManager().beginTransaction();
//                ft.replace(R.id.mainFragment, new SearchFragment(), "");
//                ft.commit();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
