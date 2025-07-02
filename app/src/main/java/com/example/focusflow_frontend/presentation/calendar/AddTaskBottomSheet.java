package com.example.focusflow_frontend.presentation.calendar;

import android.app.AlertDialog;
import android.graphics.Color;
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
import com.example.focusflow_frontend.data.model.TaskGroupRequest;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;
import com.example.focusflow_frontend.data.viewmodel.TaskViewModel;
import com.example.focusflow_frontend.utils.TokenManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    private EditText editTitle, editDescription;
    private TextView nameTag, namePriority, nameDate;
    private Button btnAddTask, btnCancel, btnDeleteTask;
    private ConstraintLayout btnSelectTag, btnSelectPriority, btnSelectDate, btnSelectMember;
    private String selectedTime = "None", selectedReminder = "None", selectedRepeat = "None";
    private int userId;
    private List<Integer> selectedMemberIds = new ArrayList<>();
    private Group group;
    private TaskViewModel taskViewModel;
    private GroupViewModel groupViewModel;
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

    public interface OnTaskDeletedListener {
        void onTaskDeleted(int deletedTaskId);
    }
    private OnTaskDeletedListener deleteListener;
    public void setOnTaskDeletedListener(OnTaskDeletedListener listener) {
        this.deleteListener = listener;
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
        btnAddTask = view.findViewById(R.id.btnAddTask);
        btnDeleteTask = view.findViewById(R.id.btnDeleteTask);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSelectTag = view.findViewById(R.id.btn_select_tag);
        btnSelectPriority = view.findViewById(R.id.btn_select_priority);
        btnSelectDate = view.findViewById(R.id.btn_select_date);
        btnSelectMember = view.findViewById(R.id.btn_select_member);
        LinearLayout listMembers = view.findViewById(R.id.list_members);
        ScrollView scrollView = view.findViewById(R.id.scrollView);

        // ViewModel
        taskViewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(TaskViewModel.class);
        groupViewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(GroupViewModel.class);

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
                scrollView.setVisibility(View.VISIBLE); // <- Hiện luôn danh sách nếu là nhóm
                groupViewModel.fetchUsersInGroup(group.getId()); // <- Gọi API lấy member nhóm
            }
            else {
                btnSelectMember.setVisibility(View.GONE);
                scrollView.setVisibility(View.GONE);
            }
        }

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

            if (group != null) {
                // Khởi tạo biến chứa dữ liệu
                List<User>[] groupMembers = new List[]{new ArrayList<>()};
                List<User>[] assignedUsers = new List[]{new ArrayList<>()};

                // Observer 1: thành viên trong nhóm
                groupViewModel.getUsersInGroup().observe(getViewLifecycleOwner(), users -> {
                    if (users != null && !users.isEmpty()) {
                        groupMembers[0] = users;

                        // Nếu đã có assignedUsers thì render
                        if (!assignedUsers[0].isEmpty()) {
                            updateSelectedIdsAndRender(groupMembers[0], assignedUsers[0], listMembers);
                        }
                    } else {
                        listMembers.removeAllViews();
                        TextView noData = new TextView(getContext());
                        noData.setText("No members found.");
                        noData.setPadding(20, 10, 20, 10);
                        listMembers.addView(noData);
                    }
                });

                // Observer 2: thành viên được assign task
                groupViewModel.refreshAssignedUsersOfTask(editingTask.getId());
                groupViewModel.getAssignedUsersLiveData(editingTask.getId()).observe(getViewLifecycleOwner(), assigned -> {
                    if (assigned != null) {
                        assignedUsers[0] = assigned;
                    } else {
                        assignedUsers[0] = new ArrayList<>();
                    }

                    // Nếu đã có groupMembers thì render
                    if (!groupMembers[0].isEmpty()) {
                        updateSelectedIdsAndRender(groupMembers[0], assignedUsers[0], listMembers);
                    }
                });
            }
        } else {
            if (group != null) {
                groupViewModel.getUsersInGroup().observe(getViewLifecycleOwner(), users -> {
                    if (users != null && !users.isEmpty()) {
                        selectedMemberIds.clear(); // chưa chọn ai cả
                        renderUserViews(users, selectedMemberIds, listMembers);
                    } else {
                        listMembers.removeAllViews();
                        TextView noData = new TextView(getContext());
                        noData.setText("No members found.");
                        noData.setPadding(20, 10, 20, 10);
                        listMembers.addView(noData);
                    }
                });
            }

            // Chế độ thêm mới
            btnAddTask.setText("Add");
            btnDeleteTask.setVisibility(View.GONE);
        }

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

            // Chỉnh sửa task
            if (editingTask != null) {
                editingTask.setTitle(title);
                editingTask.setDescription(desc);
                editingTask.setDueDate(date);
                editingTask.setTime(selectedTime);
                editingTask.setTag(tag);
                editingTask.setPriority(priorityLevel);
                editingTask.setReminderStyle(selectedReminder);
                editingTask.setRepeatStyle(selectedRepeat);

                if (group != null) {
                    // Là task nhóm → cập nhật lại ct_ids
                    groupViewModel.getCtIdsForGroupMembers(selectedMemberIds, group.getId());

                    groupViewModel.getCtIdLiveData().observe(getViewLifecycleOwner(), ctIds -> {
                        if (ctIds == null || ctIds.isEmpty()) {
                            Toast.makeText(getContext(), "Không tìm thấy ct_id", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        TaskGroupRequest request = new TaskGroupRequest(editingTask, ctIds); // request có taskId
                        taskViewModel.updateTask(request); // gửi lên BE

                        taskViewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
                            if (Boolean.TRUE.equals(success)) {
                                // Cập nhật lại user assigned
                                groupViewModel.refreshAssignedUsersOfTask(editingTask.getId());
                                Toast.makeText(getContext(), "Task updated", Toast.LENGTH_SHORT).show();
                                if (updateListener != null) updateListener.onTaskUpdated(editingTask);
                                dismiss();
                            } else {
                                Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                } else {
                    // Task cá nhân → chỉ cần update task
                    TaskGroupRequest request = new TaskGroupRequest(editingTask, null);
                    taskViewModel.updateTask(request);
                    Toast.makeText(getContext(), "Task updated", Toast.LENGTH_SHORT).show();
                    if (updateListener != null) updateListener.onTaskUpdated(editingTask);
                    dismiss();
                }

                return;
            }

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

            if (group != null) {
                if (selectedMemberIds.isEmpty()) {
                    Toast.makeText(getContext(), "Please select at least one member", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Lấy ct_ids trước → xử lý sau trong observer
                groupViewModel.getCtIdsForGroupMembers(selectedMemberIds, group.getId());

                // Gọi API lấy ctIds → Sau khi nhận được mới gọi tạo Task
                groupViewModel.getCtIdLiveData().observe(getViewLifecycleOwner(), ctIds -> {
                    if (ctIds == null || ctIds.isEmpty()) {
                        Toast.makeText(getContext(), "Không tìm thấy ct_id", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    TaskGroupRequest request = new TaskGroupRequest(newTask, ctIds);
                    taskViewModel.createTask(request);

                    taskViewModel.getCreatedTask().observe(getViewLifecycleOwner(), createdTask -> {
                        if (createdTask != null) {
                            Toast.makeText(getContext(), "Task added", Toast.LENGTH_SHORT).show();
                            if (listener != null) listener.onTaskAdded(createdTask);
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Add task failed", Toast.LENGTH_SHORT).show();
                        }
                    });


                });
            } else {
                // Task cá nhân
                TaskGroupRequest request = new TaskGroupRequest(newTask, null);
                taskViewModel.createTask(request);

                taskViewModel.getCreatedTask().observe(getViewLifecycleOwner(), createdTask -> {
                    if (createdTask != null) {
                        Toast.makeText(getContext(), "Task added", Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onTaskAdded(createdTask);
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Add task failed", Toast.LENGTH_SHORT).show();
                    }
                });
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
                                    if (deleteListener != null) deleteListener.onTaskDeleted(editingTask.getId()); // Cập nhật lại danh sách sau khi xoá
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

    private void updateSelectedIdsAndRender(List<User> users, List<User> assignedUsers, LinearLayout container) {
        selectedMemberIds.clear();
        for (User user : assignedUsers) {
            selectedMemberIds.add(user.getId());
        }
        renderUserViews(users, selectedMemberIds, container);
    }

    private void renderUserViews(List<User> users, List<Integer> selectedIds, LinearLayout container) {
        container.removeAllViews();
        for (User user : users) {
            TextView tv = new TextView(getContext());
            tv.setText(user.getUsername());
            tv.setPadding(200, 10, 20, 10);
            tv.setTextSize(18);
            Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.mpr1c_medium);
            tv.setTypeface(typeface);

            if (selectedIds.contains(user.getId())) {
                tv.setBackgroundColor(Color.parseColor("#BDBDBD"));
            }

            tv.setOnClickListener(v -> {
                if (selectedIds.contains(user.getId())) {
                    selectedIds.remove(Integer.valueOf(user.getId()));
                    tv.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    selectedIds.add(user.getId());
                    tv.setBackgroundColor(Color.parseColor("#BDBDBD"));
                }
            });

            container.addView(tv);
        }
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

