package com.example.focusflow_frontend.presentation.group;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_GROUP_NAME = "group_name";
    private GroupViewModel viewModel;
    private TaskGroupAdapter adapter;
    private List<Task> allTasks = new ArrayList<>();
//lấy tham số của group ở đây
    public static GroupDetailBottomSheet newInstance(Group group) {
        GroupDetailBottomSheet fragment = new GroupDetailBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_NAME, group.getGroup_name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(dlg -> {
            FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
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
        FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
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
        View view = inflater.inflate(R.layout.bottom_sheet_group_detail, container, false);

        // Setup ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);

        // Set group name
        TextView groupNameTextView = view.findViewById(R.id.group_name);
        groupNameTextView.setText(getArguments() != null ? getArguments().getString(ARG_GROUP_NAME, "") : "");

        setupRecycleView(view);

        // Observe danh sách task
        viewModel.getTaskList().observe(getViewLifecycleOwner(), tasks -> {
            allTasks = tasks;
            adapter.setTaskList(tasks);
        });

        viewModel.filterTask().observe(getViewLifecycleOwner(), filtered -> {
            adapter.setTaskList(filtered);
        });
        // Setup tìm kiếm
        setupSearchBar(view);
        //Menu:
        ImageView imMenu = view.findViewById(R.id.menuGroup);
        imMenu.setOnClickListener(v->{

        });
        //Tro lai group
        ImageView imBack = view.findViewById(R.id.btnBack);
        imBack.setOnClickListener(v -> {dismiss();});

        return view;
    }
    private void setupRecycleView(View view){
        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskGroupAdapter(
                new ArrayList<>(),
                (task, isChecked) -> task.setCompleted(isChecked),
                task -> {
                    Toast.makeText(getContext(), "Nhớ thêm Task vào đây nha để chuyển layout Detail Task", Toast.LENGTH_SHORT).show();
                }
        );
        recyclerView.setAdapter(adapter);

        // Gán người dùng mẫu (sau này có thể đổi thành từ API)
        adapter.setUsers(generateSampleUsers());
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

    private List<User> generateSampleUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("101", "Alice", "103"));
        users.add(new User("102", "Bob", "1"));
        users.add(new User("103", "Charlie", "1"));
        return users;
    }
}
