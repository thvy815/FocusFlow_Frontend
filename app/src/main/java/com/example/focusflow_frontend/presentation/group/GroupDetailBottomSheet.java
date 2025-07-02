package com.example.focusflow_frontend.presentation.group;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.model.TaskGroupRequest;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;
import com.example.focusflow_frontend.data.viewmodel.TaskViewModel;
import com.example.focusflow_frontend.presentation.calendar.AddTaskBottomSheet;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class GroupDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_GROUP= "group";
    private static final String ARG_USER= "user";
    private Group group;
    private User user;
    private GroupViewModel viewModel;
    private TaskViewModel taskViewModel;
    private TaskGroupAdapter adapter;
    private List<Task> allTasks = new ArrayList<>();
    private StompClient stompClient;

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
        viewModel.clearFilteredTasks(allTasks); // Bạn tạo thêm hàm này trong ViewModel4
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.disconnect();
        }
    }

    private void setupRecycleView(View view){
        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskGroupAdapter(
                new ArrayList<>(),
                (task, isChecked) -> {
                    task.setCompleted(isChecked);
                    taskViewModel.updateTask(new TaskGroupRequest(task, null));
                }, // listener checkbox
                task -> showEditTaskBottomSheet(task),
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
            // Gọi lại API để lấy avt assigned users mới
            viewModel.refreshAssignedUsersOfTask(updatedTask.getId());

            // Tìm vị trí task trong adapter
            int index = adapter.getTaskIndexById(updatedTask.getId());
            if (index != -1) {
                adapter.updateTaskInAdapter(updatedTask);
                adapter.notifyItemChanged(index);
            }
        });

        sheet.setOnTaskDeletedListener(deletedTaskId -> {
            // Xóa task khỏi adapter nếu có
            adapter.removeTaskFromAdapter(deletedTaskId);
        });

        // Kết nối WebSocket để nhận task mới theo thời gian thực
        connectWebSocket(group.getId());

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
        args.putSerializable("user", user);
        sheet.setArguments(args);

        sheet.setOnTaskAddedListener(newTask -> {
            adapter.addTaskToAdapter(newTask); // thêm task vào danh sách hiện tại
            allTasks.add(newTask);             // cập nhật danh sách gốc
        });

        sheet.show(getParentFragmentManager(), "AddTaskBottomSheet");
    }

    private void connectWebSocket(int groupId) {
        String websocketUrl = "ws://10.0.2.2:8080/ws/websocket"; // thay bằng IP backend thật
        Log.d("WebSocket", "🌐 Connecting to: " + websocketUrl);
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, websocketUrl);
        stompClient.connect();

        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d("WS", "✅ WebSocket connected successfully");
                    subscribeToGroupTopic(groupId);
                    break;
                case ERROR:
                    Log.e("WS", "❌ WebSocket connection error", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.d("WS", "WebSocket closed");
                    break;
            }
        }, throwable -> {
            Log.e("WS", "❌ WebSocket lifecycle exception", throwable);
        });
    }

    private void subscribeToGroupTopic(int groupId) {
        stompClient.topic("/topic/group/" + groupId).subscribe(topicMessage -> {
            Log.d("WS", "📨 Received message: " + topicMessage.getPayload());
            requireActivity().runOnUiThread(() -> {
                taskViewModel.fetchTasksByGroup(group.getId());
            });
        }, throwable -> {
            Log.e("WS", "❌ Error subscribing to topic", throwable);
        });
    }
}