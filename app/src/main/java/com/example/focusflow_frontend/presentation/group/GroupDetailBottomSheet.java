package com.example.focusflow_frontend.presentation.group;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;
import com.example.focusflow_frontend.data.viewmodel.TaskViewModel;
import com.example.focusflow_frontend.presentation.calendar.AddTaskBottomSheet;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_GROUP= "group";
    private static final String ARG_USER= "user";
    private Group group;
    private User user;
    private GroupViewModel viewModel;
    private TaskViewModel taskViewModel;
    private AuthViewModel userViewModel;
    private TaskGroupAdapter adapter;
    private List<Task> allTasks = new ArrayList<>();

//lấy tham số của group ở đây
    public static GroupDetailBottomSheet newInstance(Group group, User user) {
        GroupDetailBottomSheet fragment = new GroupDetailBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_GROUP, group);
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(dlg -> {
            FrameLayout bottomSheet = dialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet
            );
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheet.setOnTouchListener((v, e) -> true); // Disable swipe down
            }
        });
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        FrameLayout bottomSheet = dialog.findViewById(
                com.google.android.material.R.id.design_bottom_sheet
        );
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(false);
            behavior.setHideable(false);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.bottom_sheet_group_detail, container, false);

        TextView groupNameTextView = view.findViewById(R.id.group_name);
        if (getArguments() != null) {
            group = (Group) getArguments().getSerializable(ARG_GROUP);
            user = (User) getArguments().getSerializable(ARG_USER);
            // Set group name
            groupNameTextView.setText(group.getGroupName());

        }
        // Setup ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        setupRecycleView(view);

        // // Gọi API để lấy task theo groupId
        taskViewModel.fetchTasksByGroup(group.getId());

        // Observe danh sách task ban đầu
        taskViewModel.getGroupTaskList().observe(getViewLifecycleOwner(), tasks -> {
            if (allTasks.isEmpty()) {
                allTasks = new ArrayList<>(tasks); // Lưu bản gốc
            }
            adapter.setTaskList(tasks);
        });

        viewModel.filterTask().observe(getViewLifecycleOwner(), filtered -> {
            adapter.setTaskList(filtered);
        });
        // Setup tìm kiếm
        setupSearchBar(view);
        viewModel.getRequestSearchFocus().observe(getViewLifecycleOwner(), focus -> {
            if (focus != null && focus) {
                EditText searchInput = view.findViewById(R.id.etSearchTask);
                searchInput.requestFocus();
                viewModel.requestFocusOnSearch(false);
            }
        });
        //Menu:
        ImageView imMenu = view.findViewById(R.id.menuGroup);
        imMenu.setOnClickListener(v->openMenu(viewModel));
        //Add Task
        ImageView addBtn = view.findViewById(R.id.btnAdd);
        addBtn.setOnClickListener(v->addTask());
        //Tro lai group
        ImageView imBack = view.findViewById(R.id.btnBack);
        imBack.setOnClickListener(v -> {dismiss();});
        //Giai tan

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.clearFilteredTasks(allTasks); // Bạn tạo thêm hàm này trong ViewModel
    }

    private void setupRecycleView(View view){
        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskGroupAdapter(
                new ArrayList<>(),
                (task, isChecked) -> task.setCompleted(isChecked), // listener checkbox
                task -> showEditTaskBottomSheet(task),
                userViewModel,
                viewModel,
                getViewLifecycleOwner()
        );

        recyclerView.setAdapter(adapter);
    }

    private void showEditTaskBottomSheet(Task task) {
        AddTaskBottomSheet sheet = new AddTaskBottomSheet();

        Bundle args = new Bundle();
        args.putSerializable("task", task);
        args.putSerializable("group", group);
        sheet.setArguments(args);

        // Khi task được update xong
        sheet.setOnTaskUpdatedListener(updatedTask -> {
            adapter.updateTaskInAdapter(updatedTask);
        });

        sheet.show(getParentFragmentManager(), "EditTaskBottomSheet");
    }

    private void setupSearchBar(View view) {
        EditText searchInput = view.findViewById(R.id.etSearchTask);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchTask(s.toString(), allTasks);
            }
        });
    }

    //Mo menu:
    private void openMenu(GroupViewModel viewModel){
        Bundle args = new Bundle();
        args.putSerializable("group", group);
        args.putSerializable("user",user);

        GroupMenuBottomSheet menuSheet = new GroupMenuBottomSheet();
        menuSheet.setArguments(args);
        menuSheet.show(getParentFragmentManager(), menuSheet.getTag());
        menuSheet.setOnLeaveGroupListener(() -> {
            dismiss();
        });
        viewModel.setGroupRemoved(group.getId());
    }

    //Thuc hien addTask
    private void addTask(){
        AddTaskBottomSheet sheet = new AddTaskBottomSheet();

        // Truyền thêm groupId và userId nếu cần cho tạo task
        Bundle args = new Bundle();
        args.putSerializable("group", group);
        sheet.setArguments(args);

        sheet.setOnTaskAddedListener(newTask -> {
            adapter.addTaskToAdapter(newTask); // thêm task vào danh sách hiện tại
            allTasks.add(newTask);             // cập nhật danh sách gốc
        });

        sheet.show(getParentFragmentManager(), "AddTaskBottomSheet");
    }
}
