package com.gmitmedia.ngelong;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsTextView;
import com.gmitmedia.helpers.AspectRatioImageView;
import com.gmitmedia.helpers.GpsLocation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Place Adapter to show place cards in list
 */
public class PlaceAdapter extends RecyclerView.Adapter<ViewHolder> {

    List<Place> places;
    Context context;
    private AdapterView.OnItemClickListener onItemClickListener;
    Typeface robotoMedium;
    GpsLocation gpslocation;

    String tag;

    PlaceAdapter(List<Place> places, GpsLocation gpslocation, AdapterView.OnItemClickListener onItemClickListener, Context context) {
        this.gpslocation = gpslocation;
        this.places = places;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
        robotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");
//
//        for (int i = 0; i < this.places.size(); i++)
//            Log.v("url", "init adapter Places:" + this.places.get(i).name);
//
//
//        double r = Math.random() * 100;
//        tag= ""+r;
//        Log.v("problem", "Init mAdapter r: " + tag);
    }

    /**
     * Add items to adapter
     *
     * @param places
     */
    public void addItems(List<Place> places) {

//        Log.v("problem", "add mAdapter r: " + tag);
//        for (int i = 0; i < this.places.size(); i++)
//            Log.v("url", "before adapter Places:" + this.places.get(i).name);
        this.places.addAll(places);

//        for (int i = 0; i < this.places.size(); i++)
//            Log.v("url", "after adapter Places:" + this.places.get(i).name);
    }


    /**
     * Holds the place screen elements to avoid creating them multiple times
     */
    public class PlaceViewHolder extends ViewHolder implements View.OnClickListener {
        CardView cv;
        TextView name, price, reviews, price_suffix, distance;
        AspectRatioImageView image;
        IconicsTextView rating;
        LinearLayout featured;

        PlaceViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view);
            image = (AspectRatioImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.name);
            price_suffix = (TextView) itemView.findViewById(R.id.price_suffix);
            price = (TextView) itemView.findViewById(R.id.price);
            rating = (IconicsTextView) itemView.findViewById(R.id.rating);
            reviews = (TextView) itemView.findViewById(R.id.reviews);
            distance = (TextView) itemView.findViewById(R.id.distance);
            featured = (LinearLayout) itemView.findViewById(R.id.featured);

            //set image on click listener
            image.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //passing the clicked position to the parent class
            onItemClickListener.onItemClick(null, view, getAdapterPosition(), view.getId());
        }
    }


    @Override
    public int getItemCount() {
        return places.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = null;

        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_1COLUMNS)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.place_card_1column, viewGroup, false);
        else if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.place_card_2column, viewGroup, false);

        RecyclerView.ViewHolder rvh = new PlaceViewHolder(v);
        return rvh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {

        if (i == 1)
            Log.v("url", "adapter Places:" + places.get(1).name);

        //set place name
        ((PlaceViewHolder) viewHolder).name.setTypeface(robotoMedium);
        ((PlaceViewHolder) viewHolder).name.setText(places.get(i).name);

        //set price and feature icons
        if (places.get(i).price > 0) {
            DecimalFormat dFormat = new DecimalFormat("####,###,###");
            ((PlaceViewHolder) viewHolder).price.setText(context.getString(R.string.currency) + dFormat.format(places.get(i).price));
            ((PlaceViewHolder) viewHolder).price.setVisibility(View.VISIBLE);
            ((PlaceViewHolder) viewHolder).price_suffix.setText(places.get(i).price_suffix);
            ((PlaceViewHolder) viewHolder).price_suffix.setVisibility(View.VISIBLE);
        } else {
            ((PlaceViewHolder) viewHolder).price.setVisibility(View.GONE);
            ((PlaceViewHolder) viewHolder).price_suffix.setVisibility(View.GONE);
        }
        if (places.get(i).featured == 1) {
            ((PlaceViewHolder) viewHolder).featured.setVisibility(View.VISIBLE);
        } else {
            ((PlaceViewHolder) viewHolder).featured.setVisibility(View.GONE);
        }

        String rating = "";
        if (places.get(i).totalratings > 0) {
            for (int s = 0; s <= places.get(i).avgrating - 1; s++) {
                rating += "{faw-star}";
            }
        } else {
            for (int s = 0; s <= Configurations.DEFAULT_PLACE_RATING - 1; s++) {
                rating += "{faw-star}";
            }
        }

        ((PlaceViewHolder) viewHolder).rating.setText(rating);

        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_1COLUMNS) {
            ((PlaceViewHolder) viewHolder).reviews.setText("(" + places.get(i).totalratings + " " + context.getString(R.string.place_card_ratings) + ")");
        } else {
            ((PlaceViewHolder) viewHolder).reviews.setText("(" + places.get(i).totalratings + ")");
        }
        DecimalFormat distanceFormat = new DecimalFormat("####,###,###.##");
        if (gpslocation.hasPermission()) {
            ((PlaceViewHolder) viewHolder).distance.setText("" + distanceFormat.format((gpslocation.getDistanceToLocation(places.get(i).gpslat, places.get(i).gpslng) / 1000)) + "Km");
        } else
            ((PlaceViewHolder) viewHolder).distance.setText("");

        //set image as box when in 2 column mode
        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS) {
            ((PlaceViewHolder) viewHolder).image.setBox(true);
        }

        //load place image with picasso
        if (places.get(i).imageUrl != null) {
            RequestCreator r = Picasso.with(context).load(places.get(i).imageUrl[0]).placeholder(R.drawable.loading);
            if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS) {
                r.fit().centerCrop();
            }
            r.into(((PlaceViewHolder) viewHolder).image);
        }

        //set stats
        // ((ReviewViewHolder) viewHolder).stats.setText("{faw-eye} " + places.get(i).viewed + "  {faw-star} " + places.get(i).favorited);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}