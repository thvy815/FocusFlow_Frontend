package com.example.focusflow_frontend.presentation.pomo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Pomodoro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FocusRecordAdapter extends RecyclerView.Adapter<FocusRecordAdapter.FocusRecordViewHolder> {

    private List<Pomodoro> recordList = new ArrayList<>();

    public void setRecords(List<Pomodoro> records) {
        this.recordList = records;
        notifyDataSetChanged();
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
        holder.bind(detail);
    }

    @Override
    public int getItemCount() {
        return recordList != null ? recordList.size() : 0;
    }

    public static class FocusRecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtStartTime, txtEndTime, txtDuration;

        public FocusRecordViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            txtStartTime = itemView.findViewById(R.id.txtStartTime);
            txtEndTime = itemView.findViewById(R.id.txtEndTime);
            txtDuration = itemView.findViewById(R.id.duration);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick((Pomodoro) itemView.getTag());
                }
            });
        }

        public void bind(Pomodoro detail) {
            txtStartTime.setText(detail.getStartAt());
            txtEndTime.setText(detail.getEndAt());
            txtDuration.setText(detail.getTotalTime()/60/1000 + "min");
        }
    }

    public Pomodoro LatestPomo(){
        Pomodoro latestDetail = null;
        if (recordList != null && !recordList.isEmpty()) {
            latestDetail = Collections.max(recordList, new Comparator<Pomodoro>() {
                @Override
                public int compare(Pomodoro o1, Pomodoro o2) {
                    return o1.getStartAt().compareTo(o2.getStartAt());
                }
            });
        }
        return latestDetail;
    }
}
