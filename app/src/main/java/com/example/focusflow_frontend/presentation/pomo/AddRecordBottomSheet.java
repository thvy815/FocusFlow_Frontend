package com.example.focusflow_frontend.presentation.pomo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.viewmodel.PomodoroViewModel;
import com.example.focusflow_frontend.utils.ViewUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class AddRecordBottomSheet extends BottomSheetDialogFragment {

    private  int userId;
    private PomodoroViewModel pomodoroViewModel;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        userId = getArguments() != null ? getArguments().getInt("userId", 1) : 1;

        pomodoroViewModel = new ViewModelProvider(this).get(PomodoroViewModel.class);

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

        return dialog;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_record, container, false);

// Set Title Text
        ViewUtils.setTitleText(view, R.id.add_record_title, R.id.titleText, "Add Record");

        Spinner spinnerTask = view.findViewById(R.id.spinnerTask);
        TextView tvStartTime = view.findViewById(R.id.tvStartTime);
        TextView tvEndTime = view.findViewById(R.id.tvEndTime);
        TextView btnSaveRecord = view.findViewById(R.id.btnSave);

// Giả sử bạn có list các Task name
        List<String> taskNames = Arrays.asList("17", "2", "3"); // Hoặc lấy từ ViewModel
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, taskNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTask.setAdapter(adapter);

// Xử lý chọn giờ
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));

        TextView tvDate = view.findViewById(R.id.tvDate);

// Khởi tạo ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        tvDate.setText(String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)));

// Hiện DatePicker khi click vào tvDate
        tvDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (DatePicker view1, int selectedYear, int selectedMonth, int selectedDay) -> {
                        // Cập nhật TextView ngày sau khi chọn
                        tvDate.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay));
                    }, year, month, day);

            datePickerDialog.show();
        });
//Save Record:
        btnSaveRecord.setOnClickListener(v -> saveRecord());
//Back click
        ViewUtils.backClick(this, view, R.id.add_record_title, R.id.ic_back);
        return view;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showTimePicker(TextView targetView) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    String timeStr = String.format("%02d:%02d:00", hourOfDay, minute);
                    targetView.setText(timeStr);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveRecord(){
        pomodoroViewModel.getLastCreatedPomodoro().observe(getViewLifecycleOwner(), pomodoro -> {
            if (pomodoro != null) {
                Toast.makeText(getContext(), "Pomodoro created with ID: " + pomodoro.getId(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to create Pomodoro", Toast.LENGTH_SHORT).show();
            }
        });

        Spinner spinnerTask = getView().findViewById(R.id.spinnerTask);
        String taskId = spinnerTask.getSelectedItem().toString();

        // Lấy thời gian bắt đầu và kết thúc
        TextView tvStartTime = getView().findViewById(R.id.tvStartTime);
        TextView tvEndTime = getView().findViewById(R.id.tvEndTime);
        TextView tvDate = getView().findViewById(R.id.tvDate);

        String dueDate = tvDate.getText().toString();

        LocalTime startTime = LocalTime.parse(tvStartTime.getText().toString());
        LocalTime endTime = LocalTime.parse(tvEndTime.getText().toString());

        long totalTime = endTime.toSecondOfDay() - startTime.toSecondOfDay();
        pomodoroViewModel.createPomodorofull(getContext(), userId, Integer.parseInt(taskId), startTime.toSecondOfDay(),
                endTime.toSecondOfDay(), LocalDate.parse(dueDate), totalTime, false);
    }

}
