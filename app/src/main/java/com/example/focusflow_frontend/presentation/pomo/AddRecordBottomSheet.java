package com.example.focusflow_frontend.presentation.pomo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.viewmodel.PomodoroViewModel;
import com.example.focusflow_frontend.utils.ViewUtils;
import com.google.android.material.bottomsheet.*;

import java.time.*;
import java.util.*;

public class AddRecordBottomSheet extends BottomSheetDialogFragment {

    private int userId;
    private PomodoroViewModel viewModel;
    private List<Task> taskList = new ArrayList<>();
    private Runnable onDismissListener;

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
        viewModel = new ViewModelProvider(this).get(PomodoroViewModel.class);

        dialog.setOnShowListener(dInterface -> {
            FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(bottomSheet.getLayoutParams());

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
        ViewUtils.setTitleText(view, R.id.add_record_title, R.id.titleText, "Add Record");

        Spinner spinnerTask = view.findViewById(R.id.spinnerTask);
        TextView tvStartTime = view.findViewById(R.id.tvStartTime);
        TextView tvEndTime = view.findViewById(R.id.tvEndTime);
        TextView tvStartDate = view.findViewById(R.id.tvStartDate);
        TextView tvEndDate = view.findViewById(R.id.tvEndDate);
        TextView btnSave = view.findViewById(R.id.start_button);

        Calendar now = Calendar.getInstance();
        String today = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));

        tvStartDate.setText(today);
        tvEndDate.setText(today);

        // Load Task List
        viewModel.getTaskListLiveData().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks == null) return;
            taskList = tasks;

            List<String> taskTitles = new ArrayList<>();
            taskTitles.add("-- No Task --"); // null option

            for (Task t : tasks) {
                taskTitles.add(t.getTitle());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, taskTitles);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTask.setAdapter(adapter);
            spinnerTask.setSelection(0);
        });

        viewModel.fetchTasks(requireContext());

        // Pickers
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));
        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvEndDate.setOnClickListener(v -> showDatePicker(tvEndDate));

        // Save
        btnSave.setOnClickListener(v -> saveRecord(view, spinnerTask, tvStartDate, tvStartTime, tvEndDate, tvEndTime));

        // Back click
        ViewUtils.backClick(this, view, R.id.add_record_title, R.id.ic_back);

        return view;
    }

    private void showTimePicker(TextView target) {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hour, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d:00", hour, minute);
            target.setText(time);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void showDatePicker(TextView target) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, y, m, d) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
            target.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveRecord(View root, Spinner spinner, TextView startDateView, TextView startTimeView, TextView endDateView, TextView endTimeView) {
        try {
            String startDateStr = startDateView.getText().toString();
            String endDateStr = endDateView.getText().toString();
            String startTimeStr = startTimeView.getText().toString();
            String endTimeStr = endTimeView.getText().toString();

            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalTime endTime = LocalTime.parse(endTimeStr);

            long startMillis = startDate.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endMillis = endDate.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            if (endMillis <= startMillis) {
                Toast.makeText(getContext(), "End time must be after start time", Toast.LENGTH_SHORT).show();
                return;
            }

            long duration = endMillis - startMillis;

            Integer taskId = null;
            int pos = spinner.getSelectedItemPosition();
            if (pos > 0 && pos - 1 < taskList.size()) {
                taskId = taskList.get(pos-1).getId();
            }

            Log.d("AddRecord", "Creating Pomodoro: userId=" + userId + ", taskId=" + taskId
                    + ", startMillis=" + startMillis + ", endMillis=" + endMillis
                    + ", totalTime=" + duration);

            viewModel.createPomodorofull(
                    getContext(),
                    userId,
                    taskId,
                    startMillis,
                    endMillis,
                    startDate, // dùng ngày bắt đầu làm ngày hiển thị
                    duration,
                    false
            );
            Toast.makeText(getContext(), "Save successful", Toast.LENGTH_SHORT).show();
            dismiss();
        } catch (Exception e) {
            Log.e("AddRecord", "Lỗi khi tạo Pomodoro: ", e);
            Toast.makeText(getContext(), "Lỗi định dạng dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }
}
