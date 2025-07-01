package com.example.focusflow_frontend.presentation.pomo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Pomodoro;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FocusRecordAdapter extends RecyclerView.Adapter<FocusRecordAdapter.FocusRecordViewHolder> {

    private List<Pomodoro> recordList = new ArrayList<>();
    private Map<Integer, String> taskNameMap = new HashMap<>();

    public void setRecords(List<Pomodoro> records) {
        if (records != null) {
            Collections.sort(records, new Comparator<Pomodoro>() {
                @Override
                public int compare(Pomodoro o1, Pomodoro o2) {
                    // Sắp xếp giảm dần (mới nhất trước)
                    return o2.getStartAt().compareTo(o1.getStartAt());
                }
            });
        }
        this.recordList = records != null ? records : new ArrayList<>();
        notifyDataSetChanged();
    }

    public Map<Integer, String> getTaskNameMap() {
        return taskNameMap;
    }

    public void setTaskNameMap(Map<Integer, String> taskNameMap) {
        this.taskNameMap = taskNameMap;
        notifyDataSetChanged();
    }
    public Pomodoro getLatestPomodoro() {
        if (recordList == null || recordList.isEmpty()) return null;
        return recordList.get(0);
    }
    public interface OnItemClickListener {
        void onItemClick(Pomodoro pomodoro);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public FocusRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_focus_record, parent, false);
        return new FocusRecordViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FocusRecordViewHolder holder, int position) {
        Pomodoro detail = recordList.get(position);
        holder.bind(detail, taskNameMap, listener);  // ✅ truyền map vào
        holder.itemView.setTag(detail);
    }

    @Override
    public int getItemCount() {
        return recordList != null ? recordList.size() : 0;
    }


    public static class FocusRecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtStartTime, txtEndTime, txtDuration, txtTask;

        public FocusRecordViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            txtStartTime = itemView.findViewById(R.id.txtStartTime);
            txtEndTime = itemView.findViewById(R.id.txtEndTime);
            txtDuration = itemView.findViewById(R.id.duration);
            txtTask = itemView.findViewById(R.id.taskName);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick((Pomodoro) itemView.getTag());
                }
            });
        }
        public void bind(Pomodoro detail, Map<Integer, String> taskNameMap, OnItemClickListener listener) {
            // Đặt dữ liệu text
            String taskName = taskNameMap != null ?
                    taskNameMap.getOrDefault(detail.getTaskId(), "No task") :
                    "No task";

            txtTask.setText(taskName);

            try {
                LocalDateTime start = LocalDateTime.parse(detail.getStartAt());
                LocalDateTime end = LocalDateTime.parse(detail.getEndAt());

                DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy " +
                        "HH:mm:ss");

                String displayTime;
                displayTime = start.format(fullFormatter) + "\n" + end.format(fullFormatter);

                txtStartTime.setText(displayTime);
            } catch (Exception e) {
                txtStartTime.setText("Không rõ");
            }

            long duration = detail.getTotalTime() / 60 / 1000;
            txtDuration.setText(duration + " min");

            txtEndTime.setText(""); // không cần hiển thị riêng end time nữa

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(detail);
            });
        }

    }
}
