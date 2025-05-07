package com.example.focusflow_frontend.presentation.pomo;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focusflow_frontend.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
//import com.github.mikephil.charting.components.XAxis;
//import com.github.mikephil.charting.data.Entry;
//import com.github.mikephil.charting.data.LineData;
//import com.github.mikephil.charting.data.LineDataSet;
//import com.github.mikephil.charting.data.PieData;
//import com.github.mikephil.charting.data.PieDataSet;
//import com.github.mikephil.charting.data.PieEntry;
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
//import java.util.ArrayList;

public class FocusStatistics extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus_statistics);

        paintDetailsChart();
        paintTrendChart();

//        loadData();
    }

//    LineChart lineChart;
//    PieChart pieChart;
//    int numTodayPomo = 0;
//    int numToTalPomo = 0;
//    int numTotalTime = 0;
//    int latestDuration = 0;
//
//    String latestDayPomo = "Apr 6   20:48 - 20:55";

    public void paintTrendChart() {
//        lineChart = findViewById(R.id.Trendchart);
//        int[] numPomoDay = new int[7];
//
//        //Lấy dữ liệu từ DB: chưa làm
//        for (int i = 0; i < 7; i++)
//            numPomoDay[i] = i + 2;
//        ArrayList<Entry> entries = new ArrayList<>();
//        entries.add(new Entry(0, numPomoDay[0]));
//        entries.add(new Entry(1, numPomoDay[1]));
//        entries.add(new Entry(2, numPomoDay[2]));
//        entries.add(new Entry(3, numPomoDay[3]));
//        entries.add(new Entry(4, numPomoDay[4]));
//        entries.add(new Entry(5, numPomoDay[5]));
//        entries.add(new Entry(6, numPomoDay[6]));
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
//        // Tên ngày trong tuần
//        final String[] days = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
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
    }

    public void paintDetailsChart() {
//        pieChart = findViewById(R.id.DetailsChart);
//
//        //Dữ liệu từ DB: chưa làm
//        int numPomo = 2;
//        int totalMinutes = 4;
//
//        if (numPomo == 0) {
//            pieChart.clear();
//            pieChart.setNoDataText("No data");
//            pieChart.setNoDataTextColor(Color.GRAY);
//            pieChart.invalidate();
//        } else {
//            // Tạo dữ liệu biểu đồ
//            ArrayList<PieEntry> entries = new ArrayList<>();
//            entries.add(new PieEntry(totalMinutes));
//
//            PieDataSet dataSet = new PieDataSet(entries, "");
//            dataSet.setColors(Color.BLUE); // Xanh dương
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

        }
    }
