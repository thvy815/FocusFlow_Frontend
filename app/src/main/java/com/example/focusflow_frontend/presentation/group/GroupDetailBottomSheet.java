package com.example.focusflow_frontend.presentation.group;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    private boolean checkPermission() {
        if (user == null || group == null || user.getId() != group.getLeaderId()) {
            Toast.makeText(getContext(), "This action is only available to the group leader", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.bottom_sheet_group_detail, container, false);

        TextView groupNameTextView = view.findViewById(R.id.group_name);

        // Setup ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        if (getArguments() != null) {
            group = (Group) getArguments().getSerializable(ARG_GROUP);
            user = (User) getArguments().getSerializable(ARG_USER);
            // Set group name
            groupNameTextView.setText(group.getGroupName());

            allTasks.clear();                         // Xóa task nhóm cũ nếu có
            viewModel.clearAssignedUsersCache();      // Xóa user nhóm cũ
            connectWebSocket(group.getId());          // Kết nối WebSocket nhóm mới
            taskViewModel.fetchTasksByGroup(group.getId()); // Gọi API task mới
        }

        setupRecycleView(view);

        // Observe danh sách task ban đầu
        taskViewModel.getGroupTaskList().observe(getViewLifecycleOwner(), tasks -> {
            List<Task> sorted = sortTasksByDueDate(new ArrayList<>(tasks));
            if (allTasks.isEmpty()) {
                allTasks = new ArrayList<>(sorted);
            }
            adapter.setTaskList(sorted);
        });

        viewModel.filterTask().observe(getViewLifecycleOwner(), filtered -> {
            adapter.setTaskList(sortTasksByDueDate(new ArrayList<>(filtered)));
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
        addBtn.setOnClickListener(v -> {
            if (!checkPermission()) return;
            addTask();
        });
        //Tro lai group
        ImageView imBack = view.findViewById(R.id.btnBack);
        imBack.setOnClickListener(v -> {dismiss();});

        setupSwipeToRefresh(view);

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

    private List<Task> sortTasksByDueDate(List<Task> tasks) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        tasks.sort((t1, t2) -> {
            try {
                Date date1 = sdf.parse(t1.getDueDate());
                Date date2 = sdf.parse(t2.getDueDate());
                return date1.compareTo(date2); // tăng dần
            } catch (ParseException | NullPointerException e) {
                return 0; // nếu có lỗi khi parse thì giữ nguyên vị trí
            }
        });

        return tasks;
    }

    private void setupSwipeToRefresh(View view) {
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.orange, R.color.teal, R.color.blue
        );

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (group != null) {
                taskViewModel.fetchTasksByGroup(group.getId());

                // Delay để đảm bảo loading smooth
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                }, 800);
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    private void setupRecycleView(View view){
        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskGroupAdapter(
                new ArrayList<>(),
                (task, isChecked) -> {
                    boolean isLeader = user.getId() == group.getLeaderId();

                    boolean isAssigned = false;
                    if (task.getAssignedUsers() != null) {
                        for (User u : task.getAssignedUsers()) {
                            if (u.getId() == user.getId()) {
                                isAssigned = true;
                                break;
                            }
                        }
                    }

                    if (isLeader || isAssigned) {
                        task.setCompleted(isChecked);
                        taskViewModel.updateTask(new TaskGroupRequest(task, null));
                    } else {
                        Toast.makeText(getContext(), "⚠️ Only assigned users or leader can complete this task", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(adapter.getTaskIndexById(task.getId())); // reset lại
                    }
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
                    subscribeToGroupTopic(groupId);           // để cập nhật task UI
                    subscribeToUserTopic(user.getId());       // để hiện notification
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
            Log.d("WS-Group", "📨 Received message: " + topicMessage.getPayload());

            requireActivity().runOnUiThread(() -> {
                // 🔁 Chỉ cập nhật UI, không hiện notification ở đây
                taskViewModel.fetchTasksByGroup(group.getId());
            });
        }, throwable -> {
            Log.e("WS-Group", "❌ Error subscribing to topic", throwable);
        });
    }

    private void subscribeToUserTopic(int userId) {
        stompClient.topic("/topic/user/" + userId).subscribe(topicMessage -> {
            Log.d("WS-User", "📨 Received message: " + topicMessage.getPayload());

            requireActivity().runOnUiThread(() -> {
                try {
                    JSONObject json = new JSONObject(topicMessage.getPayload());
                    String type = json.optString("type", "");
                    String taskTitle = json.optString("title", "New Task");

                    if ("created".equals(type)) {
                        showNotification("📌 New Group Task", taskTitle);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }, throwable -> {
            Log.e("WS-User", "❌ Error subscribing to user topic", throwable);
        });
    }


    private void showNotification(String title, String message) {
        Context context = getContext();
        if (context == null) return;

        // ⚠️ Kiểm tra quyền thông báo nếu Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // 👉 Không có quyền, không gửi notification
                Log.w("Notification", "⚠️ Missing POST_NOTIFICATIONS permission");
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_channel")
                .setSmallIcon(R.drawable.ic_noti) // dùng icon của bạn
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify((int) System.currentTimeMillis(), builder.build()); // ID random để không bị ghi đè
    }
}