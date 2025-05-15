package com.example.focusflow_frontend.presentation.calendar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.viewmodel.TaskViewModel;
import com.example.focusflow_frontend.utils.TokenManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    private EditText editTitle, editDescription;
    private TextView nameTag, namePriority, nameDate;
    private Button btnAddTask, btnCancel;
    private ConstraintLayout btnSelectTag, btnSelectPriority, btnSelectDate;
    private String selectedTime = "", selectedReminder = "", selectedRepeat = "";
    private int userId;
    private TaskViewModel taskViewModel;

    public interface OnTaskAddedListener {
        void onTaskAdded();
    }

    private OnTaskAddedListener listener;
    public void setOnTaskAddedListener(OnTaskAddedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_task, container, false);

        editTitle = view.findViewById(R.id.editTitle);
        editDescription = view.findViewById(R.id.editDescription);
        nameTag = view.findViewById(R.id.nameTag);
        namePriority = view.findViewById(R.id.namePriority);
        nameDate = view.findViewById(R.id.nameDate);
        btnAddTask = view.findViewById(R.id.btnAddTask);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSelectTag = view.findViewById(R.id.btn_select_tag);
        btnSelectPriority = view.findViewById(R.id.btn_select_priority);
        btnSelectDate = view.findViewById(R.id.btn_select_date);

        // ViewModel
        taskViewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(TaskViewModel.class);

        taskViewModel.getTaskCreatedSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                if (listener != null) {
                    listener.onTaskAdded(); // Thông báo task đã được thêm thành công
                }
                dismiss();
            } else if (success != null && !success) {
                Toast.makeText(getContext(), "Add task failed, please try again", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút Add
        btnAddTask.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String desc = editDescription.getText().toString().trim();
            String date = nameDate.getText().toString();
            String tag = nameTag.getText().toString();
            String priority = namePriority.getText().toString();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            userId = TokenManager.getUserId(requireContext());

            int priorityLevel;
            switch (priority.toLowerCase()) {
                case "low": priorityLevel = 1; break;
                case "medium": priorityLevel = 2; break;
                case "high": priorityLevel = 3; break;
                default: priorityLevel = 0; break;
            }

            Task newTask = new Task();
            newTask.setUserId(userId);
            newTask.setTitle(title);
            newTask.setDescription(desc);
            newTask.setDueDate(date);
            newTask.setTime(selectedTime);
            newTask.setTag(tag);
            newTask.setPriority(priorityLevel);
            newTask.setReminderStyle(selectedReminder);
            newTask.setRepeatStyle(selectedRepeat);

            // Gọi ViewModel để tạo task mới
            taskViewModel.createTask(newTask);
        });

        // Xử lý nút Cancel
        btnCancel.setOnClickListener(v -> dismiss());

        // Mở chọn Tag
        btnSelectTag.setOnClickListener(v -> {
            String[] options = {"None", "Study", "Work", "Entertainment", "Other"};
            showOptionDialog("Select Tag", options, nameTag);
        });

        // Mở chọn Priority
        btnSelectPriority.setOnClickListener(v -> {
            String[] options = {"None", "Low", "Medium", "High"};
            showOptionDialog("Select Priority", options, namePriority);
        });

        // Mở chọn Date
        btnSelectDate.setOnClickListener(v -> showDateBottomSheet());

        return view;
    }

    private void showDateBottomSheet() {
        DateBottomSheet dateSheet = new DateBottomSheet();
        dateSheet.setOnDateSelectedListener((date, time, reminder, repeat) -> {
            nameDate.setText(date);
            selectedTime = time;
            selectedReminder = reminder;
            selectedRepeat = repeat;
        });
        dateSheet.show(getParentFragmentManager(), "DateBottomSheet");
    }

    private void showOptionDialog(String title, String[] options, TextView targetView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title);
        builder.setItems(options, (dialog, which) -> targetView.setText(options[which]));
        builder.show();
    }
}

