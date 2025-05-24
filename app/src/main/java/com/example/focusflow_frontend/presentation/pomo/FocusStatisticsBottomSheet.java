package com.example.focusflow_frontend.presentation.pomo;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.viewmodel.PomodoroViewModel;
import com.example.focusflow_frontend.utils.ViewUtils;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FocusStatisticsBottomSheet extends BottomSheetDialogFragment {

    private LineChart trendChart;
    private HorizontalBarChart detailChart;
    private  int userId;
    private PomodoroViewModel viewModel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PomodoroViewModel.class);

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

       userId = getArguments() != null ? getArguments().getInt("userId", 1) : 1;


// Set Title Text
        ViewUtils.setTitleText(view, R.id.focus_statistics_title, R.id.titleText, "Focus Statistics");

        LineChart lineChart = view.findViewById(R.id.trendChart);
        paintTrendChart(lineChart);

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

        return view;
    }

    public void addRecordClick() {
        AddRecordBottomSheet statsSheet = new AddRecordBottomSheet();
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        statsSheet.setArguments(args);
        statsSheet.show(getParentFragmentManager(), statsSheet.getTag());
    }

    public void focusRecordClick(){
        FocusRecordBottomSheet statsSheet = new FocusRecordBottomSheet();
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        statsSheet.setArguments(args);
        statsSheet.show(getParentFragmentManager(), statsSheet.getTag());
    }

    public void paintTrendChart(LineChart lineChart) {
        viewModel.getDailyDurationMap().observe(getViewLifecycleOwner(), durationMap -> {
            if (durationMap == null || durationMap.isEmpty()) return;

            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            int index = 0;

            for (Map.Entry<String, Integer> entry : durationMap.entrySet()) {
                entries.add(new Entry(index, entry.getValue()));
                labels.add(entry.getKey().substring(5)); // lấy "MM-dd" cho gọn
                index++;
            }

            LineDataSet dataSet = new LineDataSet(entries, "Pomodoro Minutes/Day");
            dataSet.setColor(Color.BLUE);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(Color.CYAN);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // làm mượt đường cong

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);

            // Cấu hình trục X
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
            xAxis.setLabelRotationAngle(-45f);

            lineChart.getAxisRight().setEnabled(false);
            lineChart.getDescription().setText("Thống kê Pomodoro trong tuần");
            lineChart.animateX(1000);
            lineChart.invalidate(); // Vẽ lại
        });

        // Chỉ gọi 1 lần khi bắt đầu
        viewModel.fetchPomodorosByUser(requireContext(), userId);
    }

}
