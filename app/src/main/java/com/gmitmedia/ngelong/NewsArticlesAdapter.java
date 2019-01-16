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

import com.mikepenz.iconics.view.IconicsImageView;
import com.gmitmedia.helpers.AspectRatioImageView;
import com.gmitmedia.helpers.PrettyTime;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.List;

/**
 * NewsArticle Adapter to show article cards in list
 */
public class NewsArticlesAdapter extends RecyclerView.Adapter<ViewHolder> {

    List<NewsArticle> newsArticles;
    Context context;
    private AdapterView.OnItemClickListener onItemClickListener;
    Typeface robotoMedium, robotoRegular;

    NewsArticlesAdapter(List<NewsArticle> newsArticles, AdapterView.OnItemClickListener onItemClickListener, Context context) {
        this.newsArticles = newsArticles;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
        robotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");
        robotoRegular = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
    }

    /**
     * Add items to adapter
     *
     * @param newsArticles
     */
    public void addItems(List<NewsArticle> newsArticles) {
        this.newsArticles.addAll(newsArticles);
    }


    /**
     * Holds the news screen elements to avoid creating them multiple times
     */
    public class HeadlineNewsViewHolder extends ViewHolder implements View.OnClickListener {
        CardView cv;
        TextView title, submission_date;
        AspectRatioImageView image;
        IconicsImageView video;

        HeadlineNewsViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view);
            image = (AspectRatioImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.title);
            video = (IconicsImageView) itemView.findViewById(R.id.video);
            submission_date = (TextView) itemView.findViewById(R.id.submission_date);


            //set image on click listener
            cv.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //passing the clicked position to the parent class
            onItemClickListener.onItemClick(null, view, getAdapterPosition(), view.getId());
        }
    }

    /**
     * Holds the news screen elements to avoid creating them multiple times
     */
    public class NewsViewHolder extends ViewHolder implements View.OnClickListener {
        CardView cv;
        TextView title, submission_date;
        AspectRatioImageView image;
        IconicsImageView video;

        NewsViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view);
            image = (AspectRatioImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.title);
            video = (IconicsImageView) itemView.findViewById(R.id.video);
            submission_date = (TextView) itemView.findViewById(R.id.submission_date);


            //set image on click listener
            cv.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //passing the clicked position to the parent class
            onItemClickListener.onItemClick(null, view, getAdapterPosition(), view.getId());
        }
    }


    @Override
    public int getItemCount() {
        return newsArticles.size();
    }

    @Override
    public int getItemViewType(int position) {
        return newsArticles.get(position).is_headline;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = null;

        if (viewType == NewsArticle.HEADLINE) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_card_headline_1columns, viewGroup, false);

            RecyclerView.ViewHolder rvh = new HeadlineNewsViewHolder(v);
            return rvh;
        } else {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_card_not_headline_1column, viewGroup, false);
            RecyclerView.ViewHolder rvh = new NewsViewHolder(v);
            return rvh;
        }

    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        if (newsArticles.get(i).is_headline == NewsArticle.HEADLINE) {

            //set news name
            ((HeadlineNewsViewHolder) viewHolder).title.setTypeface(robotoRegular);
            ((HeadlineNewsViewHolder) viewHolder).title.setText(newsArticles.get(i).name);

            //date
            String DateToStr = PrettyTime.getTimeAgo(newsArticles.get(i).submission_date, "yyyy-MM-dd HH:mm:ss", context);
            ((HeadlineNewsViewHolder) viewHolder).submission_date.setText(DateToStr);


            //video
            if (newsArticles.get(i).has_video == 1) {
                ((HeadlineNewsViewHolder) viewHolder).video.setVisibility(View.VISIBLE);
            } else {
                ((HeadlineNewsViewHolder) viewHolder).video.setVisibility(View.GONE);
            }

            //load places image with picasso
            RequestCreator r;
            if (newsArticles.get(i).imageUrl != null && newsArticles.get(i).imageUrl.length > 0 && newsArticles.get(i).imageUrl[0] != null && newsArticles.get(i).imageUrl[0].length() > 0)
                r = Picasso.with(context).load(newsArticles.get(i).imageUrl[0]).placeholder(R.drawable.loading);
            else {
                r = Picasso.with(context).load(R.drawable.loading);
            }
            r.into(((HeadlineNewsViewHolder) viewHolder).image);


        } else {
            //set news name
            ((NewsViewHolder) viewHolder).title.setTypeface(robotoRegular);
            ((NewsViewHolder) viewHolder).title.setText(newsArticles.get(i).name);

            //date
            String DateToStr = PrettyTime.getTimeAgo(newsArticles.get(i).submission_date, "yyyy-MM-dd HH:mm:ss", context);
            ((NewsViewHolder) viewHolder).submission_date.setText(DateToStr);

            //video
            if (newsArticles.get(i).has_video == 1) {
                ((NewsViewHolder) viewHolder).video.setVisibility(View.VISIBLE);
            } else {
                ((NewsViewHolder) viewHolder).video.setVisibility(View.GONE);
            }

            //set image as box when in 2 column mode
            ((NewsViewHolder) viewHolder).image.setBox(true);


            //load places image with picasso
            RequestCreator r;
            if (newsArticles.get(i).imageUrl != null && newsArticles.get(i).imageUrl.length > 0 && newsArticles.get(i).imageUrl[0] != null && newsArticles.get(i).imageUrl[0].length() > 0)
                r = Picasso.with(context).load(newsArticles.get(i).imageUrl[0]).placeholder(R.drawable.loading);
            else {
                r = Picasso.with(context).load(R.drawable.loading);
                System.out.println("empty");
            }
            r.fit().centerCrop();
            r.into(((NewsViewHolder) viewHolder).image);

        }


    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}