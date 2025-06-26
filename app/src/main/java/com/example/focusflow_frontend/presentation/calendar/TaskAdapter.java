package com.example.focusflow_frontend.presentation.calendar;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.PorterDuff;

import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Task;

import java.util.List;

public  class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{
    private List<Task> taskList;
    private OnTaskCheckedListener onTaskCheckedListener;
    private OnTaskClickListener onTaskClickListener;
    private OnTaskLongClickListener onTaskLongClickListener;

    public interface OnTaskCheckedListener {
        void onTaskChecked(Task task, boolean isChecked);
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskLongClickListener {
        void onTaskLongClick(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnTaskCheckedListener listener, OnTaskClickListener clickListener, OnTaskLongClickListener longClickListener) {
        this.taskList = tasks;
        this.onTaskCheckedListener = listener;
        this.onTaskClickListener = clickListener;
        this.onTaskLongClickListener = longClickListener;
    }

    public void updateTaskInAdapter(Task updatedTask) {
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getId().equals(updatedTask.getId())) {
                taskList.set(i, updatedTask);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskTitle, tagName, priorityName;
        private final CheckBox checkboxCompleted;
        private final ImageView imageFlag;

        // Lưu giữ các thành phần giao diện của từng item
        public TaskViewHolder(View itemView, OnTaskCheckedListener listener) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            tagName = itemView.findViewById(R.id.tagName);
            priorityName = itemView.findViewById(R.id.priorityName);
            checkboxCompleted = itemView.findViewById(R.id.checkboxCompleted);
            imageFlag = itemView.findViewById(R.id.imageFlag);

            // Xử lý click vào item để chỉnh sửa task
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onTaskClickListener != null) {
                    onTaskClickListener.onTaskClick(taskList.get(position));
                }
            });

            // Xử lý long click vào item để hiện dialog
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onTaskLongClickListener != null) {
                    onTaskLongClickListener.onTaskLongClick(taskList.get(position));
                }
                return true;
            });
        }

        // Gán dữ liệu của từng item
        public void bind(Task task) {
            // Task Title
            taskTitle.setText(task.getTitle());

            // Tag
            if (task.getTag() == null || task.getTag().isEmpty() || task.getTag().equals("None")) {
                tagName.setVisibility(View.GONE);
            } else {
                tagName.setText("#" + task.getTag());
                tagName.setVisibility(View.VISIBLE);
            }

            // Priority
            String priorityText = null;
            int priorityColor = 0;
            switch (task.getPriority()) {
                case 1:
                    priorityText = "Low";
                    priorityColor = Color.parseColor("#4CAF50"); // xanh lá
                    break;
                case 2:
                    priorityText = "Medium";
                    priorityColor = Color.parseColor("#FF9800"); // cam
                    break;
                case 3:
                    priorityText = "High";
                    priorityColor = Color.parseColor("#F44336"); // đỏ
                    break;
            }
            if (priorityText != null) {
                priorityName.setText(priorityText);
                priorityName.setTextColor(priorityColor);
                priorityName.setVisibility(View.VISIBLE);
                // Đổi màu icon trong imageFlag
                imageFlag.setColorFilter(priorityColor, PorterDuff.Mode.SRC_IN);
                imageFlag.setVisibility(View.VISIBLE);
            } else {
                priorityName.setVisibility(View.GONE);
                imageFlag.setVisibility(View.GONE);
            }

            // Checkbox Completed
            checkboxCompleted.setOnCheckedChangeListener(null); // Ngăn trigger lại khi recycle
            checkboxCompleted.setChecked(task.isCompleted());

            checkboxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                if (onTaskCheckedListener != null) {
                    onTaskCheckedListener.onTaskChecked(task, isChecked);
                }
            });
        }
    }

    // Tạo view cho từng item
    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view, onTaskCheckedListener);
    }

    // Gán dữ liệu vào view
    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        holder.bind(taskList.get(position));
    }

    // Trả về số lượng item
    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
