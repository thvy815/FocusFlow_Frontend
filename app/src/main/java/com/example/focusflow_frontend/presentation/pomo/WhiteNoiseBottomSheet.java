package com.example.focusflow_frontend.presentation.pomo;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.media.MediaPlayer;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.utils.ApiClient;
import com.example.focusflow_frontend.utils.ViewUtils;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProStatusCallback;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import retrofit2.Retrofit;

public class WhiteNoiseBottomSheet extends BottomSheetDialogFragment {
    private WhiteNoisePlayer whiteNoisePlayer;

    public WhiteNoiseBottomSheet(WhiteNoisePlayer player) {
        this.whiteNoisePlayer = player;}


        @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(layoutParams);

                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(false);
            behavior.setHideable(false);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheet.setOnTouchListener((v, event) -> true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.white_noise, container, false);

        TextView tvDone = view.findViewById(R.id.tvDone);
        tvDone.setOnClickListener(v -> {
            ViewUtils.handleBackClick(this);
        });
        ViewUtils.setWhiteNoise(view, R.id.Row1, R.id.noise1, R.drawable.volume_off);
        ViewUtils.setWhiteNoise(view, R.id.Row1, R.id.noise2, R.drawable.clock);
        ViewUtils.setWhiteNoise(view, R.id.Row1, R.id.noise3, R.drawable.fan);

        ViewUtils.setWhiteNoise(view, R.id.Row2, R.id.noise1, R.drawable.rain);
        ViewUtils.setWhiteNoise(view, R.id.Row2, R.id.noise2, R.drawable.underwater);
        ViewUtils.setWhiteNoise(view, R.id.Row2, R.id.noise3, 0);


        ViewUtils.stopVolume(view, R.id.Row1, R.id.noise1, whiteNoisePlayer);
        ViewUtils.setVolume(view, R.id.Row1, R.id.noise2, R.raw.clock_sound, whiteNoisePlayer);
        ViewUtils.setVolume(view, R.id.Row1, R.id.noise3, R.raw.fan_sound, whiteNoisePlayer);

        Retrofit retrofit = ApiClient.getRetrofit(requireContext());
        ProUtils.isProValid(requireContext(), retrofit, new ProStatusCallback() {
            @Override
            public void onResult(boolean isProUser) {
                if (isProUser) {
                    ViewUtils.setVolume(view, R.id.Row2, R.id.noise1, R.raw.rain_sound, whiteNoisePlayer);
                    ViewUtils.setVolume(view, R.id.Row2, R.id.noise2, R.raw.underwater_sound, whiteNoisePlayer);
                } else {
                    ViewUtils.stopVolumeWithDialog(WhiteNoiseBottomSheet.this, view, R.id.Row2, R.id.noise1, whiteNoisePlayer, false);
                    ViewUtils.stopVolumeWithDialog(WhiteNoiseBottomSheet.this, view, R.id.Row2, R.id.noise2, whiteNoisePlayer, false);
                }
            }
            @Override
            public void onError(String message) {
                Log.e("WhiteNoiseBottomSheet", "Pro check failed: " + message);
            }
        });

        return view;
    }
}
