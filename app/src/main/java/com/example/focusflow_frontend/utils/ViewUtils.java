package com.example.focusflow_frontend.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.focusflow_frontend.R;

public class ViewUtils {

    // Set Title Text
    public static void setTitleText(View rootView, int rootId, int titleText, String text) {
        if (rootView == null) return;

        View textLayout = rootView.findViewById(rootId);
        if (textLayout != null) {
            TextView title = textLayout.findViewById(titleText);
            if (title != null)
                title.setText(text);
        }
    }

    // Set Content Text
    public static void setContentText(View rootView, int rootId, int contentText, String text) {
        if (rootView == null) return;

        View textLayout = rootView.findViewById(rootId);
        if (textLayout != null) {
            TextView title = textLayout.findViewById(contentText);
            if (title != null)
                title.setText(text);
        }
    }

    public static void backClick(androidx.fragment.app.Fragment fragment, View rootView, int rootId, int backIcon){
        if (rootView != null){
            View textLayout = rootView.findViewById(rootId);
            if (textLayout != null){
                ImageView ic_back = textLayout.findViewById(backIcon);

                ic_back.setOnClickListener(v -> {
                    handleBackClick(fragment);
                });
            }
        }
    }
    public static void handleBackClick(androidx.fragment.app.Fragment fragment) {
        if (fragment instanceof androidx.fragment.app.DialogFragment) {
            ((androidx.fragment.app.DialogFragment) fragment).dismiss();
        } else {
            fragment.requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        }
    }



}