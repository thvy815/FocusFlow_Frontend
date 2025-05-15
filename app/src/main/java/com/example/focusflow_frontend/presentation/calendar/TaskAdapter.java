package com.example.focusflow_frontend.presentation.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Task;

import java.util.List;

public  class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{
    private List<Task> taskList;
    private OnTaskCheckedListener onTaskCheckedListener;

    public interface OnTaskCheckedListener {
        void onTaskChecked(Task task, boolean isChecked);
    }

    public TaskAdapter(List<Task> taskList, OnTaskCheckedListener listener) {
        this.taskList = taskList;
        this.onTaskCheckedListener = listener;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {

        private final TextView taskTitle;
        private final TextView tagName;
        private final TextView priorityName;
        private final CheckBox checkboxCompleted;

        // Lưu giữ các thành phần giao diện của từng item
        public TaskViewHolder(View itemView, OnTaskCheckedListener listener) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            tagName = itemView.findViewById(R.id.tagName);
            priorityName = itemView.findViewById(R.id.priorityName);
            checkboxCompleted = itemView.findViewById(R.id.checkboxCompleted);
        }

        // Gán dữ liệu của từng item
        public void bind(Task task) {
            // Task Title
            taskTitle.setText(task.getTitle());

            // Tag
            if (task.getTag() != null && !task.getTag().isEmpty()) {
                tagName.setText("#" + task.getTag());
                tagName.setVisibility(View.VISIBLE);
            } else {
                tagName.setVisibility(View.GONE);
            }

            // Priority
            String priorityText = null;
            switch (task.getPriority()) {
                case 1: priorityText = "Low"; break;
                case 2: priorityText = "Medium"; break;
                case 3: priorityText = "High"; break;
            }
            if (priorityText != null) {
                priorityName.setText(priorityText);
                priorityName.setVisibility(View.VISIBLE);
            } else {
                priorityName.setVisibility(View.GONE);
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
