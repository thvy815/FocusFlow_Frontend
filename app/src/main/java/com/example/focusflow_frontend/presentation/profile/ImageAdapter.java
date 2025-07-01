package com.example.focusflow_frontend.presentation.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.focusflow_frontend.R;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private int[] imageResIds;

    public ImageAdapter(Context context, int[] imageResIds) {
        this.context = context;
        this.imageResIds = imageResIds;
    }

    @Override
    public int getCount() {
        return imageResIds.length;
    }

    @Override
    public Object getItem(int position) {
        return imageResIds[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            imageView = (ImageView) inflater.inflate(R.layout.item_image, parent, false);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(imageResIds[position]);
        return imageView;
    }
}