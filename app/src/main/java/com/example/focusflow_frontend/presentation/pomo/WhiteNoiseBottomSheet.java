package com.example.focusflow_frontend.presentation.pomo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.utils.ViewUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class WhiteNoiseBottomSheet extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.white_noise, container, false);

        TextView tvDone = view.findViewById(R.id.tvDone);
        tvDone.setOnClickListener(v -> {
            ViewUtils.handleBackClick(this);
        });

        return view;
    }
}
