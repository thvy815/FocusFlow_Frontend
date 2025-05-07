package com.example.focusflow_frontend.presentation.pomo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.focusflow_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class WhiteNoiseBottomSheet extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.white_noise, container, false);
    }
}
