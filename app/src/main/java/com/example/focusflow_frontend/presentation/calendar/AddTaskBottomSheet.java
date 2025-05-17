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
    private Button btnAddTask, btnCancel, btnDeleteTask;
    private ConstraintLayout btnSelectTag, btnSelectPriority, btnSelectDate;
    private String selectedTime = "", selectedReminder = "", selectedRepeat = "";
    private int userId;
    private TaskViewModel taskViewModel;
    private Task editingTask = null;

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
        btnDeleteTask = view.findViewById(R.id.btnDeleteTask);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSelectTag = view.findViewById(R.id.btn_select_tag);
        btnSelectPriority = view.findViewById(R.id.btn_select_priority);
        btnSelectDate = view.findViewById(R.id.btn_select_date);

        // Nhận Task từ arguments
        if (getArguments() != null && getArguments().containsKey("task")) {
            editingTask = (Task) getArguments().getSerializable("task");
        }

        // ViewModel
        taskViewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(TaskViewModel.class);

        // Xử lý nút Add (Edit)
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
            int priorityLevel = convertPriorityToLevel(priority);

            if (editingTask != null) {
                // Chỉnh sửa task
                editingTask.setTitle(title);
                editingTask.setDescription(desc);
                editingTask.setDueDate(date);
                editingTask.setTime(selectedTime);
                editingTask.setTag(tag);
                editingTask.setPriority(priorityLevel);
                editingTask.setReminderStyle(selectedReminder);
                editingTask.setRepeatStyle(selectedRepeat);

                taskViewModel.updateTask(editingTask);
                Toast.makeText(getContext(), "Task updated", Toast.LENGTH_SHORT).show();
                dismiss();
            }
            else {
                // Thêm task mới
                Task newTask = new Task();
                newTask.setId(null);
                newTask.setUserId(userId);
                newTask.setTitle(title);
                newTask.setDescription(desc);
                newTask.setDueDate(date);
                newTask.setTime(selectedTime);
                newTask.setTag(tag);
                newTask.setPriority(priorityLevel);
                newTask.setReminderStyle(selectedReminder);
                newTask.setRepeatStyle(selectedRepeat);

                taskViewModel.getTaskCreatedSuccess().observe(getViewLifecycleOwner(), success -> {
                    if (success != null && success) {
                        if (listener != null) listener.onTaskAdded(); // Thông báo task đã được thêm thành công
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Add task failed, please try again", Toast.LENGTH_SHORT).show();
                    }
                });

                // Gọi ViewModel để tạo task mới
                taskViewModel.createTask(newTask);
            }
        });

        // Xử lý nút Delete
        if (editingTask != null) {
            // Chế độ sửa
            btnAddTask.setText("Done");
            btnDeleteTask.setVisibility(View.VISIBLE);

            editTitle.setText(editingTask.getTitle());
            editDescription.setText(editingTask.getDescription());
            nameTag.setText(editingTask.getTag());
            namePriority.setText(convertPriorityToText(editingTask.getPriority()));
            nameDate.setText(editingTask.getDueDate());

            selectedTime = editingTask.getTime();
            selectedReminder = editingTask.getReminderStyle();
            selectedRepeat = editingTask.getRepeatStyle();
        } else {
            // Chế độ thêm mới
            btnAddTask.setText("Add");
            btnDeleteTask.setVisibility(View.GONE);
        }
        btnDeleteTask.setOnClickListener(v -> {
            if (editingTask != null) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            taskViewModel.deleteTask(editingTask.getId());
                            taskViewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), success -> {
                                if (success != null && success) {
                                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                                    if (listener != null) listener.onTaskAdded(); // Cập nhật lại danh sách sau khi xoá
                                    dismiss();
                                } else {
                                    Toast.makeText(getContext(), "Delete failed, try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
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

    private int convertPriorityToLevel(String priority) {
        switch (priority.toLowerCase()) {
            case "low": return 1;
            case "medium": return 2;
            case "high": return 3;
            default: return 0;
        }
    }

    private String convertPriorityToText(int level) {
        switch (level) {
            case 1: return "Low";
            case 2: return "Medium";
            case 3: return "High";
            default: return "None";
        }
    }

    private void showDateBottomSheet() {
        DateBottomSheet dateSheet = new DateBottomSheet();

        // Truyền dữ liệu task đã chọn vào dateSheet
        dateSheet.setInitialDateTime(
                nameDate.getText().toString(),
                selectedTime,
                selectedReminder,
                selectedRepeat
        );

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

