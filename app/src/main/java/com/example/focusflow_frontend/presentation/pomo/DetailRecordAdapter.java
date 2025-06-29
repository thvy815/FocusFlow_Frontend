package com.example.focusflow_frontend.presentation.pomo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Pomodoro;
import com.example.focusflow_frontend.data.model.PomodoroDetail;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DetailRecordAdapter extends RecyclerView.Adapter<DetailRecordAdapter.DetailRecordViewHolder> {

    private List<PomodoroDetail> recordList = new ArrayList<>();

    public void setRecords(List<PomodoroDetail> records) {
        this.recordList = records;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DetailRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail_record, parent, false);
        return new DetailRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailRecordViewHolder holder, int position) {
        PomodoroDetail detail = recordList.get(position);
        holder.bind(detail);
    }

    @Override
    public int getItemCount() {
        return recordList != null ? recordList.size() : 0;
    }

    public static class DetailRecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtStartTime, txtEndTime, txtDuration;

        public DetailRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            txtStartTime = itemView.findViewById(R.id.txtStartTime);
            txtEndTime = itemView.findViewById(R.id.txtEndTime);
            txtDuration = itemView.findViewById(R.id.duration);
        }

        public void bind(PomodoroDetail detail) {
            try {
                String startAtRaw = detail.getStartAt();
                String endAtRaw = detail.getEndAt();

                // Giả sử định dạng chuỗi từ BE là ISO-8601: "2024-06-28T10:20:00"
                LocalDateTime start = LocalDateTime.parse(startAtRaw);
                LocalDateTime end = LocalDateTime.parse(endAtRaw);

                DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
                DateTimeFormatter fullFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                String startDisplay, endDisplay;

                if (start.toLocalDate().equals(end.toLocalDate())) {
                    // Cùng ngày → chỉ hiển thị giờ
                    startDisplay = start.format(timeFormat);
                    endDisplay = end.format(timeFormat);
                } else {
                    // Khác ngày → hiển thị đầy đủ
                    startDisplay = start.format(fullFormat);
                    endDisplay = end.format(fullFormat);
                }

                txtStartTime.setText(startDisplay);
                txtEndTime.setText(endDisplay);

                // Tính duration
                long durationMillis = detail.getTotalTime(); // Giả sử totalTime tính bằng millis
                long durationMin = durationMillis / 1000 / 60;
                txtDuration.setText(durationMin + " min");

            } catch (DateTimeParseException e) {
                txtStartTime.setText("Không rõ");
                txtEndTime.setText("Không rõ");
                txtDuration.setText("?");
            }
        }
    }
}
