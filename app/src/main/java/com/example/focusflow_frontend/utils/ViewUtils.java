package com.example.focusflow_frontend.utils;

import android.view.View;
import android.widget.TextView;

import com.example.focusflow_frontend.R;

public class ViewUtils {
    public static void setTitleText(View view, String titleText) {
        if (view == null) return;

        TextView title = view.findViewById(R.id.titleText);

        if (title != null) title.setText(titleText);
    }
}