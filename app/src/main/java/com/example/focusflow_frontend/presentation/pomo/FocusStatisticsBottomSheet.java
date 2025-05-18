package com.example.focusflow_frontend.presentation.pomo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.utils.ViewUtils;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FocusStatisticsBottomSheet extends BottomSheetDialogFragment {

    private LineChart trendChart;
    private HorizontalBarChart detailChart;

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
        View view = inflater.inflate(R.layout.focus_statistics, container, false);

// Set Title Text
        ViewUtils.setTitleText(view, R.id.focus_statistics_title, R.id.titleText, "Focus Statistics");

//Chuyen trang Add Record
        ImageView btnAdd = view.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecordClick();
            }
        });

//Chuyển trang Focus Record
        GridLayout gridLayout = view.findViewById(R.id.FocusRecord);
        gridLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                focusRecordClick();
            }
        });

//Back click
        ViewUtils.backClick(this, view, R.id.focus_statistics_title, R.id.ic_back);

        trendChart = view.findViewById(R.id.Trendchart);
        detailChart = view.findViewById(R.id.DetailsChart);
//
//        paintTrendChart();
//        paintDetailsChart();

        return view;
    }

    public void addRecordClick() {
        AddRecordBottomSheet statsSheet = new AddRecordBottomSheet();
        statsSheet.show(getParentFragmentManager(), statsSheet.getTag());
    }

    public void focusRecordClick(){
        FocusRecordBottomSheet statsSheet = new FocusRecordBottomSheet();
        statsSheet.show(getParentFragmentManager(), statsSheet.getTag());
    }
}
