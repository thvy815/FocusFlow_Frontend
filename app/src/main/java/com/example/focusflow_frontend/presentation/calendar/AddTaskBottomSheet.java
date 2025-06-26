package com.example.focusflow_frontend.presentation.calendar;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.CtGroupUser;
import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;
import com.example.focusflow_frontend.data.viewmodel.TaskViewModel;
import com.example.focusflow_frontend.utils.TokenManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    private EditText editTitle, editDescription;
    private TextView nameTag, namePriority, nameDate, nameMember;
    private Button btnAddTask, btnCancel, btnDeleteTask;
    private ConstraintLayout btnSelectTag, btnSelectPriority, btnSelectDate, btnSelectMember;
    private String selectedTime = "None", selectedReminder = "None", selectedRepeat = "None";
    private int userId, memberIdSelected, newctId;
    private Group group;
    private TaskViewModel taskViewModel;
    private GroupViewModel groupViewModel;
    private AuthViewModel authViewModel;
    private Task editingTask = null;

    public interface OnTaskAddedListener {
        void onTaskAdded(Task task);
    }
    private OnTaskAddedListener listener;
    public void setOnTaskAddedListener(OnTaskAddedListener listener) {
        this.listener = listener;
    }

    public interface OnTaskUpdatedListener {
        void onTaskUpdated(Task updatedTask);
    }
    private OnTaskUpdatedListener updateListener;
    public void setOnTaskUpdatedListener(OnTaskUpdatedListener listener) {
        this.updateListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_task, container, false);

        userId = TokenManager.getUserId(requireContext());

        editTitle = view.findViewById(R.id.editTitle);
        editDescription = view.findViewById(R.id.editDescription);
        nameTag = view.findViewById(R.id.nameTag);
        namePriority = view.findViewById(R.id.namePriority);
        nameDate = view.findViewById(R.id.nameDate);
        nameMember = view.findViewById(R.id.nameMember);
        btnAddTask = view.findViewById(R.id.btnAddTask);
        btnDeleteTask = view.findViewById(R.id.btnDeleteTask);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSelectTag = view.findViewById(R.id.btn_select_tag);
        btnSelectPriority = view.findViewById(R.id.btn_select_priority);
        btnSelectDate = view.findViewById(R.id.btn_select_date);
        btnSelectMember = view.findViewById(R.id.btn_select_member);
        LinearLayout listMembers = view.findViewById(R.id.list_members);
        ScrollView scrollView = view.findViewById(R.id.scrollView);

        if (getArguments() != null) {
            // Nhận Task (nếu có)
            if (getArguments().containsKey("task")) {
                editingTask = (Task) getArguments().getSerializable("task");
            }
            // Nhận ngày được chọn từ CalendarFragment
            if (getArguments().containsKey("selected_date") && editingTask == null) {
                String selectedDate = getArguments().getString("selected_date");
                nameDate.setText(selectedDate);
            }
            // Group
            if (getArguments().containsKey("group")) {
                group = (Group) getArguments().getSerializable("group");
                btnSelectMember.setVisibility(View.VISIBLE);
            }
            else {
                btnSelectMember.setVisibility(View.GONE);
            }
        }

        // ViewModel
        taskViewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(TaskViewModel.class);
        groupViewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(GroupViewModel.class);
        authViewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(AuthViewModel.class);

        if (editingTask != null) {
            // Chế độ sửa
            btnAddTask.setText("Done");
            btnDeleteTask.setVisibility(View.VISIBLE);

            editTitle.setText(editingTask.getTitle());
            editDescription.setText(editingTask.getDescription());
            nameTag.setText(editingTask.getTag());
            namePriority.setText(convertPriorityToText(editingTask.getPriority()));
            nameDate.setText(editingTask.getDueDate());

            if (group != null) {
                int ctId = editingTask.getCtGroupId();

                groupViewModel.getCtByIdLiveData(ctId).observe(getViewLifecycleOwner(), ctGroupUser -> {
                    if (ctGroupUser != null) {
                        int userId = ctGroupUser.getUserId();

                        authViewModel.getUserByIdLiveData(userId).observe(getViewLifecycleOwner(), user -> {
                            if (user != null) {
                                nameMember.setText(user.getUsername());
                            } else {
                                nameMember.setText("Unknown");
                            }
                        });
                    } else {
                        nameMember.setText("Unknown");
                    }
                });
            }

            selectedTime = editingTask.getTime();
            selectedReminder = editingTask.getReminderStyle();
            selectedRepeat = editingTask.getRepeatStyle();
        } else {
            // Chế độ thêm mới
            btnAddTask.setText("Add");
            btnDeleteTask.setVisibility(View.GONE);
        }

        // Khi bấm nút Member: ẩn/hiện danh sách
        btnSelectMember.setOnClickListener(v -> {
            if (group != null) {
                // Toggle hiển thị nếu đã có data
                if (scrollView.getVisibility() == View.VISIBLE) {
                    scrollView.setVisibility(View.GONE);
                } else {
                    groupViewModel.fetchUsersInGroup(group.getId()); // Gọi API
                    scrollView.setVisibility(View.VISIBLE); // Hiện vùng chứa danh sách
                }
            }
        });

        groupViewModel.getUsersInGroup().observe(getViewLifecycleOwner(), users -> {
            listMembers.removeAllViews(); // Xóa cũ
            if (users != null && !users.isEmpty()) {
                for (User user : users) {
                    if (user.getId() != userId){
                        TextView tv = new TextView(getContext());
                        tv.setText(user.getUsername());
                        tv.setPadding(200, 10, 20, 10);
                        tv.setTextSize(18);
                        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.mpr1c_medium);
                        tv.setTypeface(typeface);

                        // Thêm sự kiện click để chọn user
                        tv.setOnClickListener(v1 -> {
                            nameMember.setText(user.getUsername());  // hoặc tạo 1 TextView nameMember để hiển thị
                            scrollView.setVisibility(View.GONE); // Ẩn danh sách sau khi chọn
                            memberIdSelected = user.getId();
                        });

                        listMembers.addView(tv);
                    }
                }
            } else {
                TextView noData = new TextView(getContext());
                noData.setText("No members found.");
                noData.setPadding(20, 10, 20, 10);
                listMembers.addView(noData);
            }
        });

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

            int priorityLevel = convertPriorityToLevel(priority);

            // Nếu có group (tức là task nhóm), cần lấy ct_id
            if (group != null) {
                // Gọi API lấy ctId → logic thêm task nằm trong callback
                groupViewModel.getCtIdLiveData().removeObservers(getViewLifecycleOwner());
                groupViewModel.getCtIdLiveData().observe(getViewLifecycleOwner(), ctId -> {
                    if (ctId == null) {
                        Toast.makeText(getContext(), "Không tìm thấy ct_id", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    newctId = ctId;

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
                        editingTask.setCtGroupId(newctId);

                        taskViewModel.updateTask(editingTask);
                        Toast.makeText(getContext(), "Task updated", Toast.LENGTH_SHORT).show();
                        if (updateListener != null) updateListener.onTaskUpdated(editingTask);
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
                        newTask.setCtGroupId(newctId);

                        taskViewModel.getCreatedTask().observe(getViewLifecycleOwner(), createdTask -> {
                            if (createdTask != null) {
                                Toast.makeText(getContext(), "Task added", Toast.LENGTH_SHORT).show();
                                if (listener != null) listener.onTaskAdded(createdTask); // đã có task_id
                                dismiss();
                            } else {
                                Toast.makeText(getContext(), "Add task failed", Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Gọi ViewModel để tạo task mới
                        taskViewModel.createTask(newTask);
                    }
                });

                // Gọi sau observe để không bỏ lỡ emit
                groupViewModel.fetchCtIdByUserAndGroup(memberIdSelected, group.getId());

            } else {
                // Task cá nhân (không cần ct_id)
                if (editingTask != null) {
                    editingTask.setTitle(title);
                    editingTask.setDescription(desc);
                    editingTask.setDueDate(date);
                    editingTask.setTime(selectedTime);
                    editingTask.setTag(tag);
                    editingTask.setPriority(priorityLevel);
                    editingTask.setReminderStyle(selectedReminder);
                    editingTask.setRepeatStyle(selectedRepeat);
                    editingTask.setCtGroupId(null);

                    taskViewModel.updateTask(editingTask);
                    Toast.makeText(getContext(), "Task updated", Toast.LENGTH_SHORT).show();
                    if (updateListener != null) updateListener.onTaskUpdated(editingTask);
                    dismiss();
                } else {
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
                    newTask.setCtGroupId(null);

                    taskViewModel.getCreatedTask().observe(getViewLifecycleOwner(), createdTask -> {
                        if (createdTask != null) {
                            Toast.makeText(getContext(), "Task added", Toast.LENGTH_SHORT).show();
                            if (listener != null) listener.onTaskAdded(createdTask); // đã có task_id
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Add task failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                    taskViewModel.createTask(newTask);
                }
            }
        });

        // Xử lý nút Delete
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
                                    if (updateListener != null) updateListener.onTaskUpdated(null); // Cập nhật lại danh sách sau khi xoá
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

