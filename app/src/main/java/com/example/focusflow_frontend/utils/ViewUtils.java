package com.example.focusflow_frontend.utils;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.presentation.pomo.WhiteNoisePlayer;

import java.util.List;

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

    //Set white_noise:
    public static void setWhiteNoise(View rootView, int rootId, int noiseId, int imageVoice){
        if (rootView == null) return;
        View NoiseLayout = rootView.findViewById(rootId);
        if (NoiseLayout != null){
            ImageView noise = NoiseLayout.findViewById(noiseId);
            if (noise != null)
                noise.setImageResource(imageVoice);
        }
    }

    public static void setVolume(View rootView, int rootId, int noiseId, int id, WhiteNoisePlayer whiteNoisePlayer) {
        if (rootView == null || whiteNoisePlayer == null) return;
        View noiseLayout = rootView.findViewById(rootId);
        if (noiseLayout != null) {
            ImageView noise = noiseLayout.findViewById(noiseId);
            if (noise != null) {
                Context context = rootView.getContext();
                noise.setOnClickListener(v -> {
                    if (context != null) {
                        whiteNoisePlayer.startWhiteNoise(context, id);
                    }
                });
            }
        }
    }

    public static void stopVolume(View rootView, int rootId, int noiseId, WhiteNoisePlayer whiteNoisePlayer) {
        if (rootView == null || whiteNoisePlayer == null) return;
        View noiseLayout = rootView.findViewById(rootId);
        if (noiseLayout != null) {
            ImageView noise = noiseLayout.findViewById(noiseId);
            if (noise != null) {
                noise.setOnClickListener(v -> {
                        whiteNoisePlayer.stopWhiteNoise();
                });
            }
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