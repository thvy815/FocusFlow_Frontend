package com.example.focusflow_frontend.presentation.calendar;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import com.example.focusflow_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateBottomSheet extends BottomSheetDialogFragment {

    // Interface nội bộ để gửi dữ liệu về AddTaskBottomSheet
    public interface OnDateSelectedListener {
        void onDateInfoSelected(String date, String time, String reminder, String repeat);
    }

    private OnDateSelectedListener listener;
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View sheetView = inflater.inflate(R.layout.bottom_sheet_date, container, false);

        CalendarView calendarView = sheetView.findViewById(R.id.calendarView);
        ConstraintLayout btnSelectTime = sheetView.findViewById(R.id.btnSelectTime);
        TextView nameTime = sheetView.findViewById(R.id.nameTime);
        TextView nameReminder = sheetView.findViewById(R.id.nameReminder);
        TextView nameRepeat = sheetView.findViewById(R.id.nameRepeat);

        // CalendarView
        final long[] selectedDate = {System.currentTimeMillis()};
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate[0] = calendar.getTimeInMillis();
        });

        // Time Picker
        btnSelectTime.setOnClickListener(view -> {
            Calendar now = Calendar.getInstance();
            TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                    (tp, hour, minute) -> nameTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute)),
                    now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
            timePicker.show();
        });

        // Reminder
        sheetView.findViewById(R.id.btnSelectReminder).setOnClickListener(view ->
                showOptionDialog("Select Reminder", new String[]{"None", "5 minutes before", "15 minutes before", "1 hour before"}, nameReminder)
        );

        // Repeat
        sheetView.findViewById(R.id.btnSelectRepeat).setOnClickListener(view ->
                showOptionDialog("Select Repeat", new String[]{"None", "Daily", "Weekly", "Monthly"}, nameRepeat)
        );

        // Done
        sheetView.findViewById(R.id.btnDone).setOnClickListener(v -> {
            String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(selectedDate[0]));
            String time = nameTime.getText().toString();
            String reminder = nameReminder.getText().toString();
            String repeat = nameRepeat.getText().toString();

            if (listener != null) {
                listener.onDateInfoSelected(dateStr, time, reminder, repeat);
            }
            dismiss();
        });

        // Cancel
        sheetView.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());

        return sheetView;
    }

    private void showOptionDialog(String title, String[] options, TextView targetView) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(options, (dialog, which) -> targetView.setText(options[which]))
                .show();
    }
}
