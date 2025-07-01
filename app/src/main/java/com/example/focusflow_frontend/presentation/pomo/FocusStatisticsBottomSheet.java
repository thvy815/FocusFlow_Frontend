package com.example.focusflow_frontend.presentation.pomo;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Pomodoro;
import com.example.focusflow_frontend.data.viewmodel.PomodoroViewModel;
import com.example.focusflow_frontend.utils.ViewUtils;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FocusStatisticsBottomSheet extends BottomSheetDialogFragment {

    private LineChart trendChart;
    private PieChart  pieChart;
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
       userId = getArguments() != null ? getArguments().getInt("userId", -1) : -1;
// Set Title Text
        ViewUtils.setTitleText(view, R.id.focus_statistics_title, R.id.titleText, "Focus Statistics");
        //Set record
        viewModel.fetchLatestPomodoro(requireContext(), userId);
        viewModel.getLatestPomodoro().observe(getViewLifecycleOwner(), latestPomodoro -> {
            if (latestPomodoro != null) {
                LatestPomo(latestPomodoro);
            }
        });
        updatePomodoroCounts();
        trendChart = view.findViewById(R.id.trendChart);
        paintTrendChart(trendChart);

        pieChart = view.findViewById(R.id.DetailsChart);
        pieChart.post(() -> {
            viewModel.getAllPomodoro(requireContext(), userId);
            showTodayPieChart(pieChart);
        });
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
    private void updatePomodoroCounts() {
        viewModel.fetchPomodorosByUser(requireContext(), userId); // đảm bảo có dữ liệu

        viewModel.getPomodoroList().observe(getViewLifecycleOwner(), pomodoros -> {
            if (pomodoros == null) return;

            int totalPomo = pomodoros.size(); // Tổng số Pomodoro

            // Tính số Pomodoro của hôm nay
            int todayPomo = 0;
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            for (Pomodoro p : pomodoros) {
                if (p.getStartAt() != null && p.getStartAt().startsWith(today)) {
                    todayPomo++;
                }
            }

            // Gán text vào 2 TextView
            TextView todayTextView = requireView().findViewById(R.id.todayValue);
            TextView totalTextView = requireView().findViewById(R.id.totalValue);

            todayTextView.setText(String.valueOf(todayPomo));
            totalTextView.setText(String.valueOf(totalPomo));
        });
    }


    public void addRecordClick() {
        AddRecordBottomSheet addRecordBottomSheet = new AddRecordBottomSheet();
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        addRecordBottomSheet.setArguments(args);
        addRecordBottomSheet.show(getParentFragmentManager(), "add_record");
        addRecordBottomSheet.setOnDismissListener(() -> {
            // Gọi lại toàn bộ hàm setup lại giao diện (vẽ chart, load dữ liệu,...)
            if (getView() != null) {
                View root = getView();
                updatePomodoroCounts();                    // Cập nhật số lượng
                paintTrendChart((LineChart) root.findViewById(R.id.trendChart)); // Vẽ trend chart
                showTodayPieChart((PieChart) root.findViewById(R.id.DetailsChart)); // Pie chart
                viewModel.fetchLatestPomodoro(requireContext(), userId);           // Cập nhật latest
            }
        });


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
            if (durationMap == null) return;

            Map<String, Integer> fullWeekMap = new LinkedHashMap<>();

            // Tính ngày Chủ Nhật đầu tuần hiện tại
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            List<String> labels = new ArrayList<>();
            // Tạo 7 ngày (Chủ nhật đến Thứ bảy)
            for (int i = 0; i < 7; i++) {
                String dateStr = sdf.format(calendar.getTime());
                fullWeekMap.put(dateStr, 0);  // mặc định 0 phút
                labels.add(dateStr.substring(5));  // chỉ lấy MM-dd để hiển thị
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Gộp dữ liệu từ durationMap vào fullWeekMap
            for (Map.Entry<String, Integer> entry : durationMap.entrySet()) {
                if (fullWeekMap.containsKey(entry.getKey())) {
                    fullWeekMap.put(entry.getKey(), entry.getValue()); // phút
                }
            }
            // Tạo entry để vẽ chart
            List<Entry> entries = new ArrayList<>();
            int index = 0;
            for (Integer value : fullWeekMap.values()) {
                entries.add(new Entry(index++, value));
            }

            LineDataSet dataSet = new LineDataSet(entries, "Pomodoro Minutes/Day");
            dataSet.setColor(Color.BLUE);
            dataSet.setCircleColor(Color.BLUE);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(Color.CYAN);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
            xAxis.setLabelRotationAngle(-25f);
            xAxis.setLabelCount(labels.size(), true);

            lineChart.getAxisLeft().setAxisMinimum(0f);
            lineChart.getAxisLeft().setGranularity(10f);
            lineChart.getAxisRight().setEnabled(false);

            lineChart.getLegend().setEnabled(true);
            lineChart.getDescription().setEnabled(false);
            lineChart.animateX(1000);
            lineChart.invalidate();
        });

        // Gọi để load dữ liệu pomodoro → trigger calculateWeeklyDurations
        viewModel.fetchPomodorosByUser(requireContext(), userId);
    }
    private void showTodayPieChart(PieChart pieChart) {
        viewModel.fetchPomodorosByUser(requireContext(), userId); // 1. Trigger trước

        viewModel.getPomodoroList().observe(getViewLifecycleOwner(), pomodoros -> {
            if (pomodoros == null) return;

            // 2. Gọi fetchTaskNames khi chắc chắn có pomodoros
            viewModel.fetchTaskNames(requireContext(), pomodoros);

            // 3. Quan sát TaskMap
            viewModel.getTaskNameMapLiveData().observe(getViewLifecycleOwner(), taskNameMap -> {
                if (taskNameMap == null) return;

                // 4. Lọc hôm nay
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                List<Pomodoro> todayList = new ArrayList<>();
                for (Pomodoro p : pomodoros) {
                    if (p.getStartAt() != null && p.getStartAt().startsWith(today)) {
                        todayList.add(p);
                    }
                }

                // 5. Gom thời lượng theo tên task
                Map<String, Integer> taskDurationMap = new HashMap<>();
                for (Pomodoro p : todayList) {
                    String taskName = taskNameMap.getOrDefault(p.getTaskId(), "Another");
                    int minutes = (int) (p.getTotalTime() / 1000 / 60);
                    taskDurationMap.put(taskName, taskDurationMap.getOrDefault(taskName, 0) + minutes);
                }

                // 6. Tạo entries
                List<PieEntry> entries = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : taskDurationMap.entrySet()) {
                    if (entry.getValue() > 0) {
                        entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                    }
                }

                if (entries.isEmpty()) {
                    pieChart.clear();
                    pieChart.setNoDataText("No focus sessions today.");
                    return;
                }
                // 7. Vẽ chart
                PieDataSet dataSet = new PieDataSet(entries, "Today's Pomodoro Tasks");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                dataSet.setValueTextSize(14f);
                dataSet.setValueTextColor(Color.WHITE);
                // ✅ Custom hiển thị phần trăm với 1–2 chữ số thập phân
                dataSet.setValueFormatter(new ValueFormatter() {
                    private final DecimalFormat format = new DecimalFormat("##0.0#"); // 1–2 chữ số thập phân

                    @Override
                    public String getFormattedValue(float value) {
                        return format.format(value) + " %";
                    }
                });

                PieData pieData = new PieData(dataSet);
                pieChart.setData(pieData);

                // Thiết lập phần trăm
                pieChart.setUsePercentValues(true);
                pieChart.setDrawHoleEnabled(false);
                pieChart.getDescription().setEnabled(false);
                pieChart.getLegend().setEnabled(true);
                pieChart.animateY(1000);
                pieChart.invalidate();
            });
        });
    }
    public void LatestPomo(Pomodoro latest) {
        if (latest != null) {
            String startTime = latest.getStartAt();
            String endTime = latest.getEndAt();

            TextView txtStartTime = getView().findViewById(R.id.txtStartTime);
            TextView txtEndTime = getView().findViewById(R.id.txtEndTime);
            TextView txtDuration = getView().findViewById(R.id.duration);
            TextView txtTask = getView().findViewById(R.id.taskName);

            // Hiển thị thời gian
            try {
                if (startTime != null && endTime != null) {
                    LocalDateTime start = LocalDateTime.parse(startTime);
                    LocalDateTime end = LocalDateTime.parse(endTime);

                    DateTimeFormatter full = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    String displayTime;
                    displayTime = start.format(full) + "\n" + end.format(full);

                    txtStartTime.setText(displayTime);
                } else {
                    txtStartTime.setText("Không rõ");
                }
            } catch (Exception e) {
                txtStartTime.setText("Không rõ");
                e.printStackTrace();
            }

            txtEndTime.setText("");
            txtDuration.setText(latest.getTotalTime() / 60 / 1000 + " min");

            // Hiển thị tên task từ ViewModel
            viewModel.getTaskNameMapLiveData().observe(getViewLifecycleOwner(), taskNameMap -> {
                if (taskNameMap != null) {
                    String taskName = taskNameMap.get(latest.getTaskId());
                    txtTask.setText(taskName != null ? taskName : "No task");
                } else {
                    txtTask.setText("No task");
                }
            });

        } else {
            System.out.println("Chưa có dữ liệu Pomodoro");
        }
    }


}
