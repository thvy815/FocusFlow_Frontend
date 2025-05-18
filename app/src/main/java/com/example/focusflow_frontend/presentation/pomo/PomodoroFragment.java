package com.example.focusflow_frontend.presentation.pomo;

import static android.content.Context.VIBRATOR_MANAGER_SERVICE;
import static android.view.View.VISIBLE;

import static androidx.core.content.ContextCompat.getSystemService;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.aigestudio.wheelpicker.WheelPicker;
import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PomodoroFragment extends Fragment {
    private TextView timerText;
//    private CircleTimerView circleView;
    private CountDownTimer countDownTimer;
    long totalTime = 25*60*1000;
    long timeLeft = totalTime;
    private boolean isPaused = false;
    long startTime, endTime, pauseTime;
    private int selectedMinute = 25;
    private boolean isStarted = false;
    private WheelPicker wheelPicker;
    private WhiteNoisePlayer whiteNoisePlayer = new WhiteNoisePlayer();
    private WhiteNoiseBottomSheet bottomSheet = new WhiteNoiseBottomSheet(whiteNoisePlayer);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pomodoro, container, false);

        timerText = view.findViewById(R.id.timer_text);
        wheelPicker = view.findViewById(R.id.wheelPicker);

        wheelPicker.setVisibility(View.INVISIBLE);
        wheelPicker.setVisibleItemCount(3);
        Typeface customTypeface = ResourcesCompat.getFont(requireContext(), R.font.mpr1c_bold);
        wheelPicker.setTypeface(customTypeface);

//        // Khởi tạo các view
//        circleView = view.findViewById(R.id.circleView);

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
        imvSchedule.setOnClickListener(v -> focusStatisticClick());

        timerText.setOnClickListener(v-> showTimePickerDialog());


        View.OnClickListener openWhiteNoiseListener = v -> {
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        };

        imvVolume1.setOnClickListener(openWhiteNoiseListener);
        imvVolume2.setOnClickListener(openWhiteNoiseListener);

        return view;
    }

// Chuyển trang:


    public void focusStatisticClick() {
        FocusStatisticsBottomSheet statsSheet = new FocusStatisticsBottomSheet();
        statsSheet.show(getParentFragmentManager(), statsSheet.getTag());
    }

//Bấm thay đổi phút POMO
    private void showTimePickerDialog() {
    if (isStarted) {
        return;
    }
    //Hien thi wheelPicker
    wheelPicker.setVisibility(View.VISIBLE);
    timerText.setVisibility(View.INVISIBLE);

    // Lấy giá trị hiện tại từ timerText và xác định chỉ số của WheelPicker
    String currentTime = timerText.getText().toString();
    int currentMinute = Integer.parseInt(currentTime.split(":")[0]);

    // Tạo danh sách các lựa chọn thời gian
    List<String> timeOptions = new ArrayList<>();
    for (int i = 1; i <= 60; i++) {
        timeOptions.add(i + ":00");
    }

    wheelPicker.setData(timeOptions);

    wheelPicker.setSelectedItemPosition(timeOptions.indexOf(currentMinute + ":00"));

    wheelPicker.setOnItemSelectedListener((picker, data, position) -> {
        String selectedText = (String) data;
        selectedMinute = Integer.parseInt(selectedText.split(":")[0]);

        updateTimerDisplay(selectedMinute);
        totalTime = selectedMinute * 60 * 1000;
        timeLeft = totalTime;
    });

    // Ẩn WheelPicker sau khi người dùng chọn thời gian và hiển thị lại timerText
    wheelPicker.setOnItemSelectedListener((picker, data, position) -> {
        String selectedText = (String) data;
        selectedMinute = Integer.parseInt(selectedText.split(":")[0]);

        // Cập nhật thời gian
        updateTimerDisplay(selectedMinute);
        totalTime = selectedMinute * 60 * 1000;
        timeLeft = totalTime;

        // Ẩn WheelPicker sau khi chọn và hiển thị lại timerText
        wheelPicker.setVisibility(View.GONE);
        timerText.setVisibility(View.VISIBLE);  // Hiển thị lại timerText
        Toast.makeText(getContext(), "Đặt thời gian: " + selectedMinute + " phút", Toast.LENGTH_SHORT).show();
    });
}


    private void updateTimerDisplay(int minutes) {
        TextView timerText = getView().findViewById(R.id.timer_text);
        timerText.setText(String.format(Locale.getDefault(), "%02d:00", minutes));
    }

// Bấm nút START:
    public void startClick() {
        if (wheelPicker.getVisibility() == View.VISIBLE) {
            Toast.makeText(getContext(), "Vui lòng chọn thời gian trước khi bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }
        startTime = System.currentTimeMillis();
        pauseTime = startTime;

        ConstraintLayout layout = getView().findViewById(R.id.pomo_layout);

        Button startButton = getView().findViewById(R.id.start_button);
        LinearLayout afterStart = getView().findViewById(R.id.afterStart);
        TextView timerText = getView().findViewById(R.id.timer_text);
        ImageView play_icon = getView().findViewById(R.id.play_icon);

        int parentWidth = layout.getWidth();
        int parentHeight = layout.getHeight();

        int textWidth = timerText.getWidth();
        int textHeight = timerText.getHeight();

        // Tính khoảng cách cần di chuyển để đến giữa
        float targetX = (parentWidth - textWidth) / 2f - timerText.getX();
        float targetY = (parentHeight - textHeight) / 2f - timerText.getY();

        timerText.animate()
                .translationXBy(targetX)
                .translationYBy(targetY)
                .setDuration(500)
                .start();

        afterStart.animate()
                .translationXBy(targetX)
                .translationYBy((targetY + 20))
                .start();
        play_icon.animate()
                .translationXBy(targetX)
                .translationYBy((targetY + 20))
                .start();

        float currentSizePx = timerText.getTextSize(); // already in pixels
        float targetSizePx = 80;

        ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(timerText, "textSize", currentSizePx, targetSizePx);
//        scaleAnimator.setDuration(600);
        scaleAnimator.start();

        startButton.setVisibility(View.GONE);
        afterStart.setVisibility(VISIBLE);
        startTimer();
    }
    public void startTimer() {
        if (isPaused) return;
        isStarted = true;
        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                int minutes = (int) (timeLeft / 1000) / 60;
                int seconds = (int) (timeLeft / 1000) % 60;

                // Cập nhật thời gian hiển thị trên TextView
                timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                Toast.makeText(getContext(), "Time's up!", Toast.LENGTH_SHORT).show();
                timerText.setText(getString(R.string.timeOut));
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

// Bấm nút PAUSE:
    public void pauseClick() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            endTime = System.currentTimeMillis();
            long focusDuration = endTime - pauseTime;
            saveFocusToServer(pauseTime, endTime, focusDuration);
        }
        whiteNoisePlayer.stopWhiteNoise();
        isPaused = true;


        getView().findViewById(R.id.play_icon).setVisibility(VISIBLE);
        getView().findViewById(R.id.paused_icon).setVisibility(View.INVISIBLE);
    }

// Bấm nút PLAY:
    public void playClick() {
        isPaused = false;
        onResume();

        getView().findViewById(R.id.play_icon).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.paused_icon).setVisibility(VISIBLE);
    }

// Bấm nút STOP:
    public void stopClick() {
        int minTime = 20 * 60 * 1000;
        if (timeLeft > minTime) {
            showDialogLessThan5min();
        } else {
            showDialogMoreThan5min();
        }
    }

//Bấm QUIT:
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

        builder.setPositiveButton("QUIT", (dialog, which) -> {
            restartFragment();
        });
        builder.create().show();
    }
    private void restartFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new PomodoroFragment())
                .commit();
        whiteNoisePlayer.stopWhiteNoise();
    }

    private void saveFocusToServer(long startTime, long endTime, long duration) {
        // TODO: Gửi dữ liệu lên server
    }

    private void saveRecordToServer(long startTime, long endTime, long duration) {
        // TODO: Gửi dữ liệu tổng hợp lên server
    }



}