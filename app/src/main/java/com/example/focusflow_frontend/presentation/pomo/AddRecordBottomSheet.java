package com.example.focusflow_frontend.presentation.pomo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.viewmodel.PomodoroViewModel;
import com.example.focusflow_frontend.utils.ViewUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class AddRecordBottomSheet extends BottomSheetDialogFragment {

    private int userId;
    private PomodoroViewModel pomodoroViewModel;
    private Runnable onDismissListener;

    private List<Task> taskList;  // lưu danh sách task lấy từ ViewModel

    public void setOnDismissListener(Runnable listener) {
        this.onDismissListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) onDismissListener.run();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        userId = getArguments() != null ? getArguments().getInt("userId", -1) : -1;

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
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView btnSaveRecord = view.findViewById(R.id.start_button);

        // Khởi tạo ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        tvDate.setText(String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)));

        // Lấy danh sách Task từ ViewModel
        pomodoroViewModel.getTaskListLiveData().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                taskList = tasks;
                List<String> taskTitles = new ArrayList<>();

                // Thêm placeholder vào đầu danh sách
                taskTitles.add("-- Select Task --");

                for (Task t : tasks) {
                    taskTitles.add(t.getTitle());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, taskTitles);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTask.setAdapter(adapter);

                // Đặt spinner chọn placeholder mặc định
                spinnerTask.setSelection(0);
            }
        });
        // Yêu cầu ViewModel load Task
        pomodoroViewModel.fetchTasks(requireContext());

        // Xử lý chọn giờ
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));

        // Hiện DatePicker khi click vào tvDate
        tvDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (DatePicker view1, int selectedYear, int selectedMonth, int selectedDay) -> {
                        tvDate.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay));
                    }, year, month, day);

            datePickerDialog.show();
        });

        // Save Record:
        btnSaveRecord.setOnClickListener(v -> saveRecord());

        // Back click
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
    private void saveRecord() {
        Spinner spinnerTask = getView().findViewById(R.id.spinnerTask);
        int selectedPosition = spinnerTask.getSelectedItemPosition();

        Integer taskId = null;
        if (taskList != null && selectedPosition >= 0 && selectedPosition < taskList.size()) {
            taskId = taskList.get(selectedPosition).getId();
        }

        TextView tvStartTime = getView().findViewById(R.id.tvStartTime);
        TextView tvEndTime = getView().findViewById(R.id.tvEndTime);
        TextView tvDate = getView().findViewById(R.id.tvDate);

        String startStr = tvStartTime.getText().toString();
        String endStr = tvEndTime.getText().toString();
        String dueDateStr = tvDate.getText().toString();

        try {
            LocalDate date = LocalDate.parse(dueDateStr);
            LocalTime startTime = LocalTime.parse(startStr);
            LocalTime endTime = LocalTime.parse(endStr);

            ZoneId zoneId = ZoneId.systemDefault();
            long startMillis = date.atTime(startTime).atZone(zoneId).toInstant().toEpochMilli();
            long endMillis = date.atTime(endTime).atZone(zoneId).toInstant().toEpochMilli();
            long totalTime = endMillis - startMillis;

            if (totalTime <= 0) {
                Toast.makeText(getContext(), "End time must be after start time", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("AddRecord", "Creating Pomodoro: userId=" + userId + ", taskId=" + taskId
                    + ", startMillis=" + startMillis + ", endMillis=" + endMillis
                    + ", totalTime=" + totalTime);

            // Gửi taskId có thể null
            int check = pomodoroViewModel.createPomodorofull(
                    getContext(),
                    userId,
                    taskId,
                    startMillis,
                    endMillis,
                    date,
                    totalTime,
                    false
            );
                Toast.makeText(getContext(), "Save successful", Toast.LENGTH_SHORT).show();
                dismiss();
        } catch (Exception e) {
            Log.e("AddRecord", "Error parsing time or creating record: ", e);
            Toast.makeText(getContext(), "Lỗi định dạng thời gian hoặc dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }


}
