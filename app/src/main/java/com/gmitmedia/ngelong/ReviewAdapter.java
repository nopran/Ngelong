package com.gmitmedia.ngelong;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Property Adapter to show place cards in list
 */
public class ReviewAdapter extends RecyclerView.Adapter<ViewHolder> {

    List<Review> reviews;
    Context context;
    Typeface robotoRegular;

    ReviewAdapter(List<Review> reviews, Context context) {
        this.reviews = reviews;
        this.context = context;
        robotoRegular = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
    }

    /**
     * Add items to adapter
     *
     * @param reviews
     */
    public void addItems(List<Review> reviews) {
        this.reviews.addAll(reviews);
    }


    /**
     * Holds the review screen elements to avoid creating them multiple times
     */
    public class ReviewViewHolder extends ViewHolder {
        TextView username, text;

        ReviewViewHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username);
            text = (TextView) itemView.findViewById(R.id.text);
        }
    }


    @Override
    public int getItemCount() {
        return reviews.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = null;
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.review_item, viewGroup, false);
        ViewHolder rvh = new ReviewViewHolder(v);
        return rvh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder propertyViewHolder, final int i) {
        //set review title
        ((ReviewViewHolder) propertyViewHolder).username.setTypeface(robotoRegular);
        ((ReviewViewHolder) propertyViewHolder).username.setText(reviews.get(i).username);

        //set review text
        ((ReviewViewHolder) propertyViewHolder).text.setTypeface(robotoRegular);
        ((ReviewViewHolder) propertyViewHolder).text.setText(reviews.get(i).text);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}