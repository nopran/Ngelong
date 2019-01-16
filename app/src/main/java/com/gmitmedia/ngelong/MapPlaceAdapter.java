package com.gmitmedia.ngelong;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsTextView;
import com.gmitmedia.helpers.AspectRatioImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Property Adapter to show place cards in list
 */
public class MapPlaceAdapter extends RecyclerView.Adapter<ViewHolder> {

    List<Place> places;
    Context context;
    private AdapterView.OnItemClickListener onItemClickListener;
    Typeface robotoMedium;

    MapPlaceAdapter(List<Place> places, AdapterView.OnItemClickListener onItemClickListener, Context context) {
        this.places = places;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
        robotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");
    }

    public void addItems(List<Place> places) {
        this.places.addAll(places);
    }


    /**
     * Holds the place screen elements to avoid creating them multiple times
     */
    public class PropertyViewHolder extends ViewHolder implements View.OnClickListener {
        CardView cv;
        TextView name, price, reviews, price_suffix, distance;
        AspectRatioImageView image;
        IconicsTextView rating;


        PropertyViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view);
            image = (AspectRatioImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.name);
            // price_suffix = (TextView) itemView.findViewById(R.id.price_suffix);
            //  price = (TextView) itemView.findViewById(R.id.price);
            rating = (IconicsTextView) itemView.findViewById(R.id.rating);
            reviews = (TextView) itemView.findViewById(R.id.reviews);
            //  distance = (TextView) itemView.findViewById(R.id.distance);

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
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.place_card_map, viewGroup, false);
        ViewHolder rvh = new PropertyViewHolder(v);
        return rvh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder propertyViewHolder, final int i) {
        //set place name
        ((PropertyViewHolder) propertyViewHolder).name.setTypeface(robotoMedium);
        ((PropertyViewHolder) propertyViewHolder).name.setText(places.get(i).name);
        DecimalFormat dFormat = new DecimalFormat("####,###,###");
        // ((ReviewViewHolder) propertyViewHolder).price.setText(context.getString(R.string.currency) + dFormat.format(places.get(i).saleprice));


        //load place image with picasso
        if (places.get(i).imageUrl != null) {
            RequestCreator r = Picasso.with(context).load(places.get(i).imageUrl[0]).placeholder(R.drawable.loading);
            r.into(((PropertyViewHolder) propertyViewHolder).image);
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
        ((PropertyViewHolder) propertyViewHolder).rating.setText(rating);

        ((PropertyViewHolder) propertyViewHolder).reviews.setText("(" + places.get(i).totalratings + " Ratings)");

        //((PlaceViewHolder) propertyViewHolder).distance.setText("5km");
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}