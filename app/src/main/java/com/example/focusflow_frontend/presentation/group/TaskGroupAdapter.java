package com.example.focusflow_frontend.presentation.group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;

import java.util.List;

public class TaskGroupAdapter extends RecyclerView.Adapter<TaskGroupAdapter.TaskViewHolder> {
    private List<Task> tasks; // Danh sách các task
    private final OnTaskCheckedChangeListener listener; // Lắng nghe thay đổi checkbox
    private OnTaskClickListener clickListener;
    private final AuthViewModel authViewModel;
    private final GroupViewModel groupViewModel;
    private final LifecycleOwner lifecycleOwner;

    // Interface cho sự kiện checkbox
    public interface OnTaskCheckedChangeListener {
        void onTaskCheckedChanged(Task task, boolean isChecked);
    }
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }
    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.clickListener = listener;
    }

    public TaskGroupAdapter(List<Task> tasks,
                            OnTaskCheckedChangeListener listener,
                            OnTaskClickListener clickListener,
                            AuthViewModel authViewModel,
                            GroupViewModel groupViewModel,
                            LifecycleOwner lifecycleOwner) {
        this.tasks = tasks;
        this.listener = listener;
        this.clickListener = clickListener;
        this.authViewModel = authViewModel;
        this.groupViewModel = groupViewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    // Cập nhật danh sách task mới
    public void setTaskList(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public void updateTaskInAdapter(Task updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void addTaskToAdapter(Task task) {
        tasks.add(task); // thêm vào cuối danh sách
        notifyItemInserted(tasks.size() - 1);
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
        holder.txtDeadline.setText(task.getDueDate());

        // Gán trạng thái checkbox và xử lý sự kiện
        holder.checkBox.setOnCheckedChangeListener(null); // Tránh gọi lại sự kiện cũ
        holder.checkBox.setChecked(task.isCompleted());
        holder.checkBox.setOnCheckedChangeListener((btn, isChecked) -> {
            task.setCompleted(isChecked);
            if (listener != null) listener.onTaskCheckedChanged(task, isChecked);
        });

        // Sự kiện click item
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTaskClick(tasks.get(holder.getAdapterPosition()));
            }
        });

        // Lấy ctId từ task → lấy userId từ ct → lấy username từ user
        int ctId = task.getCtGroupId();

        groupViewModel.getCtByIdLiveData(ctId).observe(lifecycleOwner, ctGroupUser -> {
            if (ctGroupUser != null) {
                int userId = ctGroupUser.getUserId();

                authViewModel.getUserByIdLiveData(userId).observe(lifecycleOwner, user -> {
                    if (user != null) {
                        holder.txtUser.setText(user.getUsername());
                    } else {
                        holder.txtUser.setText("Unknown");
                    }
                });
            } else {
                holder.txtUser.setText("Unknown");
            }
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
