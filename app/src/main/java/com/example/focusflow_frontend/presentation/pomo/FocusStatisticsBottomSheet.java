package com.example.focusflow_frontend.presentation.pomo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.utils.ViewUtils;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FocusStatisticsBottomSheet extends BottomSheetDialogFragment {

    private LineChart trendChart;
    private HorizontalBarChart detailChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.focus_statistics, container, false);

        trendChart = view.findViewById(R.id.Trendchart);
        detailChart = view.findViewById(R.id.DetailsChart);
//
//        paintTrendChart();
//        paintDetailsChart();

// Set Title Text
        ViewUtils.setTitleText(view, R.id.focus_statistics_title, R.id.titleText, "Focus Statistics");

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
        statsSheet.show(getParentFragmentManager(), statsSheet.getTag());
    }

    public void focusRecordClick(){
        FocusRecordBottomSheet statsSheet = new FocusRecordBottomSheet();
        statsSheet.show(getParentFragmentManager(), statsSheet.getTag());
    }
// Paint chart
//    private void paintTrendChart() {
//        int[] numPomoDay = new int[7];
//        for (int i = 0; i < 7; i++)
//            numPomoDay[i] = i + 2;
//
//        ArrayList<Entry> entries = new ArrayList<>();
//        for (int i = 0; i < 7; i++) {
//            entries.add(new Entry(i, numPomoDay[i]));
//        }
//
//        LineDataSet dataSet = new LineDataSet(entries, "Pomodoros");
//        dataSet.setDrawValues(false);
//        dataSet.setDrawCircles(true);
//        dataSet.setDrawCircleHole(false);
//        dataSet.setLineWidth(2f);
//
//        LineData lineData = new LineData(dataSet);
//        lineChart.setData(lineData);
//
//        String[] days = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f);
//        xAxis.setDrawGridLines(false);
//
//        lineChart.getAxisRight().setEnabled(false);
//        lineChart.getDescription().setEnabled(false);
//        lineChart.getLegend().setEnabled(false);
//        lineChart.invalidate();
//    }
//
//    private void paintDetailsChart() {
//        int numPomo = 2;
//        int totalMinutes = 4;
//
//        if (numPomo == 0) {
//            pieChart.clear();
//            pieChart.setNoDataText("No data");
//            pieChart.setNoDataTextColor(Color.GRAY);
//            pieChart.invalidate();
//        } else {
//            ArrayList<PieEntry> entries = new ArrayList<>();
//            entries.add(new PieEntry(totalMinutes));
//
//            PieDataSet dataSet = new PieDataSet(entries, "");
//            dataSet.setColors(Color.BLUE);
//            dataSet.setValueTextSize(4f);
//            dataSet.setValueTextColor(Color.WHITE);
//            dataSet.setDrawValues(false);
//            dataSet.setSliceSpace(0.5f);
//
//            PieData data = new PieData(dataSet);
//
//            pieChart.setData(data);
//            pieChart.setDrawEntryLabels(false);
//            pieChart.setUsePercentValues(false);
//            pieChart.setCenterText(" " + totalMinutes);
//            pieChart.setCenterTextSize(20f);
//            pieChart.setCenterTextColor(Color.GRAY);
//            pieChart.setDrawHoleEnabled(true);
//            pieChart.setHoleRadius(85f);
//            pieChart.setTransparentCircleRadius(90f);
//            pieChart.setDrawRoundedSlices(true);
//            pieChart.getDescription().setEnabled(false);
//            pieChart.getLegend().setEnabled(false);
//            pieChart.invalidate();
//        }
//    }
}
