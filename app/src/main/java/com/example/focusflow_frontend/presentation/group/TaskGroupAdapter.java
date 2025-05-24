package com.example.focusflow_frontend.presentation.group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskGroupAdapter extends RecyclerView.Adapter<TaskGroupAdapter.TaskViewHolder> {

    private List<Task> tasks; // Danh sách các task
    private final OnTaskCheckedChangeListener listener; // Lắng nghe thay đổi checkbox
    private final OnTaskClickListener clickListener;
    private final Map<Long, String> userMap = new HashMap<>(); // Map userId -> username

    // Interface cho sự kiện checkbox
    public interface OnTaskCheckedChangeListener {
        void onTaskCheckedChanged(Task task, boolean isChecked);
    }
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskGroupAdapter(List<Task> tasks, OnTaskCheckedChangeListener listener, OnTaskClickListener clickListener) {
        this.tasks = tasks;
        this.listener = listener;
        this.clickListener = clickListener;
    }

    // Cập nhật danh sách task mới
    public void setTaskList(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    // Gán danh sách người dùng để lấy tên từ id
    public void setUsers(List<User> users) {
        userMap.clear();
        for (User user : users) {
            userMap.put(user.getId(), user.getUsername());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_group, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        // Gán dữ liệu cho item
        holder.txtTitle.setText(task.getTitle());
        holder.txtUser.setText(userMap.getOrDefault(task.getUserId(), "Unknown"));
        holder.txtDeadline.setText(task.getDueDate());

        // Gán trạng thái checkbox và xử lý sự kiện
        holder.checkBox.setOnCheckedChangeListener(null); // Tránh gọi lại sự kiện cũ
        holder.checkBox.setChecked(task.isCompleted());
        holder.checkBox.setOnCheckedChangeListener((btn, isChecked) -> {
            task.setCompleted(isChecked);
            if (listener != null) listener.onTaskCheckedChanged(task, isChecked);
        });
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onTaskClick(task);
        });
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    // ViewHolder để ánh xạ các view trong item
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtUser, txtDeadline;
        CheckBox checkBox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.title);
            txtUser = itemView.findViewById(R.id.userName);
            txtDeadline = itemView.findViewById(R.id.deadline);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}
