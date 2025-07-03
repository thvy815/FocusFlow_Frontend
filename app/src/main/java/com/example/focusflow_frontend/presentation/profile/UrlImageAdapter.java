package com.example.focusflow_frontend.presentation.profile;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class UrlImageAdapter extends BaseAdapter {
    private final Context context;
    private final String[] imageUrls;

    public UrlImageAdapter(Context context, String[] imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    public int getCount() {
        return imageUrls.length;
    }

    @Override
    public Object getItem(int position) {
        return imageUrls[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView image = new ImageView(context);
        Glide.with(context).load(imageUrls[position]).into(image);
        image.setLayoutParams(new GridView.LayoutParams(250, 250)); // Kích thước ảnh
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return image;
    }
}
