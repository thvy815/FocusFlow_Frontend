package com.example.focusflow_frontend.presentation.pomo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.presentation.pomo.CircleTimerView;
import com.example.focusflow_frontend.utils.ViewUtils;

import java.lang.reflect.Field;
import java.util.Locale;

public class PomodoroFragment extends Fragment {

    private TextView timerText;
    private CircleTimerView circleView;
    private CountDownTimer countDownTimer;
    long totalTime, timeLeft;
    int minutes = -1, seconds = -1;
    private boolean isPaused = false;
    long beginTime;
    long startTime, endTime, pauseTime;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pomodoro, container, false);

        // Khởi tạo các view
        circleView = view.findViewById(R.id.circleView);

        Button startButton = view.findViewById(R.id.start_button);
        ImageView playButton = view.findViewById(R.id.play_icon);
        ImageView pauseButton = view.findViewById(R.id.paused_icon);
        ImageView stopButton = view.findViewById(R.id.stop_icon);
        ImageView imvVolume1 = view.findViewById(R.id.volume_ic);
        ImageView imvSchedule = view.findViewById(R.id.clock_icon);
        ImageView imvVolume2 = view.findViewById(R.id.volume_icon);

        startButton.setOnClickListener(v -> startClick());
        playButton.setOnClickListener(v -> playClick());
        pauseButton.setOnClickListener(v -> pauseClick());
        stopButton.setOnClickListener(v -> stopClick());

        // Gán thêm sự kiện chuyển màn
        imvVolume1.setOnClickListener(v -> NoiseClick());
        imvVolume2.setOnClickListener(v -> NoiseClick());
        imvSchedule.setOnClickListener(v -> focusStatisticClick());

        NumberPicker minutePicker = view.findViewById(R.id.minute_picker);
        NumberPicker secondPicker = view.findViewById(R.id.second_picker);

// Set min-max values
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);

// Optional: set default to 25:00
        minutePicker.setValue(25);
        secondPicker.setValue(0);

        int[] minute = new int[1];
        int[] second = new int[1];

        minutePicker.setOnClickListener(v -> {
            NumberClick(view, R.id.minute_picker, minute);
            minutes = minute[0];
        });

        minutePicker.setOnClickListener(v -> {
            NumberClick(view, R.id.minute_picker, second);
            seconds = second[0];
        });

//        if (minutes == -1 && seconds == -1)
//        {
//            minutes = minutePicker.getValue();
//
//        }


        minutePicker.setFormatter(value -> String.format(Locale.getDefault(), "%02d", value));
        secondPicker.setFormatter(value -> String.format(Locale.getDefault(), "%02d", value));

        return view;
    }

    public void NumberClick(View view, int NumberId, int[] Values){
        NumberPicker c = view.findViewById(NumberId);
        Values[0] = c.getValue();
    }
    public void NoiseClick() {
        WhiteNoiseBottomSheet statsSheet = new WhiteNoiseBottomSheet();
        statsSheet.show(getParentFragmentManager(), statsSheet.getTag());
    }

    public void focusStatisticClick() {
        FocusStatisticsBottomSheet statsSheet = new FocusStatisticsBottomSheet();
        statsSheet.show(getParentFragmentManager(), statsSheet.getTag());
    }

    public void startClick() {
        startTime = System.currentTimeMillis();
        pauseTime = startTime;

        Button startButton = getView().findViewById(R.id.start_button);
        LinearLayout afterStart = getView().findViewById(R.id.afterStart);

        startButton.setVisibility(View.GONE);
        afterStart.setVisibility(View.VISIBLE);
        startTimer();
    }

    public void startTimer() {
        if (isPaused) return;
        NumberPicker minutePicker = getView().findViewById(R.id.minute_picker);
        NumberPicker secondPicker = getView().findViewById(R.id.second_picker);

        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = minutePicker.getValue();
                int seconds = secondPicker.getValue();
                timeLeft = (minutes * 60 + seconds) * 1000L;

                // Update timer text
                timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                // Update progress circle
                float progress = 1f - (float) timeLeft / totalTime;
                circleView.setProgress(progress);
            }

            @Override
            public void onFinish() {
                timerText.setText(getString(R.string.timeOut));
                circleView.setProgress(1f);
            }
        }.start();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (timeLeft < totalTime && !isPaused) {
            startTimer();
            pauseTime = System.currentTimeMillis();
        }
    }

//    private void removeNumberPickerDivider(NumberPicker numberPicker) {
//        try {
//            Field dividerField = NumberPicker.class.getDeclaredField("mSelectionDivider");
//            dividerField.setAccessible(true);
//            dividerField.set(numberPicker, null); // Xóa drawable
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void pauseClick() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            endTime = System.currentTimeMillis();
            long focusDuration = endTime - pauseTime;
            saveFocusToServer(pauseTime, endTime, focusDuration);
        }

        isPaused = true;


        getView().findViewById(R.id.play_icon).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.paused_icon).setVisibility(View.INVISIBLE);
    }

    public void playClick() {
        isPaused = false;
        onResume();

        timerText.setVisibility(View.VISIBLE);
        getView().findViewById(R.id.play_icon).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.paused_icon).setVisibility(View.VISIBLE);
    }

    public void stopClick() {
        int minTime = 20 * 60 * 1000;
        if (timeLeft > minTime) {
            showDialogLessThan5min();
        } else {
            showDialogMoreThan5min();
        }
    }

    private void showDialogLessThan5min() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Abandon This Focus?");
        builder.setMessage("The record can't be saved because the focus duration is less than 5 mins.");
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setPositiveButton("QUIT", (dialog, which) -> restartFragment());
        builder.create().show();
    }

    private void showDialogMoreThan5min() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("End the Pomo in Advance?");
        builder.setMessage("The Pomo is on-going. Do you want to save the record");
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.setNeutralButton("Save", (dialog, which) -> {
            endTime = System.currentTimeMillis();
            long focusDuration = endTime - pauseTime;
            long pomoDuration = endTime - startTime;

            saveFocusToServer(startTime, endTime, focusDuration);
            saveRecordToServer(startTime, endTime, pomoDuration);
            restartFragment();
        });

        builder.setPositiveButton("QUIT", (dialog, which) -> restartFragment());
        builder.create().show();
    }

    private void restartFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new PomodoroFragment())
                .commit();
    }

    private void saveFocusToServer(long startTime, long endTime, long duration) {
        // TODO: Gửi dữ liệu lên server
    }

    private void saveRecordToServer(long startTime, long endTime, long duration) {
        // TODO: Gửi dữ liệu tổng hợp lên server
    }
}
