package com.example.focusflow_frontend.presentation.pomo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.utils.ViewUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FocusRecordBottomSheet extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.focus_record, container, false);

        // Set Title Text
        ViewUtils.setTitleText(view, R.id.focus_record_title, R.id.titleText, "Focus Record");

        TextView btnAdd = view.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecordClick();
            }
        });

        //Back click
        ViewUtils.backClick(this, view, R.id.focus_record_title, R.id.ic_back);

        return view;
    }

    public void addRecordClick() {
        AddRecordBottomSheet statsSheet = new AddRecordBottomSheet();
        statsSheet.show(getParentFragmentManager(), statsSheet.getTag());
    }
}
