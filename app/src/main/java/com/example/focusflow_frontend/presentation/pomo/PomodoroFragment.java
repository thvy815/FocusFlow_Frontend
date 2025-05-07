package com.example.focusflow_frontend.presentation.pomo;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.presentation.pomo.CircleTimerView;
import com.example.focusflow_frontend.utils.ViewUtils;

import java.util.Locale;

public class PomodoroFragment extends Fragment {

    private TextView timerText;
    private CircleTimerView circleView;
    private CountDownTimer countDownTimer;
    final long totalTime = 25 * 60 * 1000; // 25 phút
    long timeLeft = totalTime;
    private boolean isPaused = false;
    long beginTime;
    long startTime, endTime, pauseTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pomodoro, container, false);

        // Khởi tạo các view
        timerText = view.findViewById(R.id.timer_text);
        circleView = view.findViewById(R.id.circleView);

        Button startButton = view.findViewById(R.id.start_button);
        ImageView playButton = view.findViewById(R.id.play_icon);
        ImageView pauseButton = view.findViewById(R.id.paused_icon);
        ImageView imvPaused = view.findViewById(R.id.imv_paused);
        ImageView stopButton = view.findViewById(R.id.stop_icon);

        startButton.setOnClickListener(v -> startClick());
        playButton.setOnClickListener(v -> playClick());
        pauseButton.setOnClickListener(v -> pauseClick());
        stopButton.setOnClickListener(v -> stopClick());

        // Gán thêm sự kiện chuyển màn
        view.findViewById(R.id.volume_icon).setOnClickListener(v -> whiteNoiseClick());
        view.findViewById(R.id.clock_icon).setOnClickListener(v -> focusStatisticClick());

        return view;
    }

    public void whiteNoiseClick() {
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
        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                int minutes = (int) (timeLeft / 1000) / 60;
                int seconds = (int) (timeLeft / 1000) % 60;

                timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
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

    public void pauseClick() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            endTime = System.currentTimeMillis();
            long focusDuration = endTime - pauseTime;
            saveFocusToServer(pauseTime, endTime, focusDuration);
        }

        isPaused = true;

        timerText.setVisibility(View.INVISIBLE);
        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.mpr1c_bold);
        timerText.setTypeface(typeface);

        getView().findViewById(R.id.play_icon).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.paused_icon).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.imv_paused).setVisibility(View.VISIBLE);
    }

    public void playClick() {
        isPaused = false;
        onResume();

        timerText.setVisibility(View.VISIBLE);
        getView().findViewById(R.id.imv_paused).setVisibility(View.GONE);
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
