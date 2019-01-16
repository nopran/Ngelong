package com.gmitmedia.helpers;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gmitmedia.ngelong.R;
import com.squareup.picasso.Picasso;

public class SlideShowAdapter extends PagerAdapter {

    private String[] imageUrls;
    private LayoutInflater inflater;
    private Context context;
    int defaultImageRes;
    View.OnClickListener clicklistener;

    public SlideShowAdapter(Context context, String[] imageUrls, int defaultImageRes, View.OnClickListener clicklistener) {
        this.context = context;
        this.imageUrls = imageUrls;
        inflater = LayoutInflater.from(context);
        this.defaultImageRes = defaultImageRes;
        this.clicklistener = clicklistener;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return imageUrls.length;
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View myImageLayout = inflater.inflate(R.layout.slide, view, false);
        ImageView myImage = (ImageView) myImageLayout.findViewById(R.id.image);
        if (clicklistener != null)
            myImage.setOnClickListener(clicklistener);

        //switch image
        Picasso.with(context)
                .load(imageUrls[position])
                .fit()
                .placeholder(defaultImageRes)
                .into(myImage);

        view.addView(myImageLayout, 0);
        return myImageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}