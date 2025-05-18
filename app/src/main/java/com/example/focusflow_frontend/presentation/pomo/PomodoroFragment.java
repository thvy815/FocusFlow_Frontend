package com.example.focusflow_frontend.presentation.pomo;

import static android.view.View.VISIBLE;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.aigestudio.wheelpicker.WheelPicker;
import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Pomodoro;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PomodoroFragment extends Fragment {
    private TextView timerText;
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
    private PomodoroViewModel pomodoroViewModel;
    private Pomodoro currPomodoro;
    private int userId,taskId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pomodoro, container, false);

        pomodoroViewModel = new ViewModelProvider(this).get(PomodoroViewModel.class);

        timerText = view.findViewById(R.id.timer_text);
        wheelPicker = view.findViewById(R.id.wheelPicker);

        wheelPicker.setVisibility(View.INVISIBLE);
        wheelPicker.setVisibleItemCount(3);
        Typeface customTypeface = ResourcesCompat.getFont(requireContext(), R.font.mpr1c_bold);
        wheelPicker.setTypeface(customTypeface);

        Button startButton = view.findViewById(R.id.start_button);
        ImageView playButton = view.findViewById(R.id.play_icon);
        ImageView pauseButton = view.findViewById(R.id.paused_icon);
        ImageView stopButton = view.findViewById(R.id.stop_icon);
        ImageView imvVolume1 = view.findViewById(R.id.volume_ic);
        ImageView imvSchedule = view.findViewById(R.id.clock_icon);
        ImageView imvVolume2 = view.findViewById(R.id.volume_icon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startButton.setOnClickListener(v -> startClick());
        }
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
        Bundle args = new Bundle();
        if (currPomodoro != null)
            args.putInt("userId", userId);
        else
            args.putInt("userId", -1);
        statsSheet.setArguments(args);
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startClick() {
        if (wheelPicker.getVisibility() == View.VISIBLE) {
            Toast.makeText(getContext(), "Vui lòng chọn thời gian trước khi bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }
        startTime = System.currentTimeMillis();
        pauseTime = startTime;
        LocalDate dueDate = LocalDate.now();;

        animateTimerToCenter();
        SavePomodoro(startTime,1,17,dueDate);

        getView().findViewById(R.id.start_button).setVisibility(View.GONE);
        getView().findViewById(R.id.afterStart).setVisibility(VISIBLE);
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

                endTime = System.currentTimeMillis();
                Time en = new Time(endTime);

                long focusDuration = endTime - pauseTime;
                long pomoDuration = endTime - startTime;

                currPomodoro.setEndAt(en.toString());
                currPomodoro.setTotalTime(pomoDuration);

                int pomoId = currPomodoro.getId();
                userId = currPomodoro.getUserId();
                taskId = currPomodoro.getTaskId();

                pomodoroViewModel.createPomodoroDetail(getContext(), userId, taskId, pomoId, pauseTime, endTime, focusDuration);
                pomodoroViewModel.updatePomodoro(getContext(), currPomodoro);




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
            int pomoId = currPomodoro.getId();
            userId = currPomodoro.getUserId();
            taskId = currPomodoro.getTaskId();
            pomodoroViewModel.createPomodoroDetail(getContext(), userId, taskId, pomoId, pauseTime, endTime, focusDuration);
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
        endTime = System.currentTimeMillis();
        long minTime = endTime - startTime;
        if (minTime < 5*60*1000) {
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

            Time en = new Time(endTime);

            currPomodoro.setEndAt(en.toString());
            currPomodoro.setTotalTime(pomoDuration);

            int pomoId = currPomodoro.getId();
            userId = currPomodoro.getUserId();
            taskId = currPomodoro.getTaskId();

            pomodoroViewModel.createPomodoroDetail(getContext(), userId, taskId, pomoId, pauseTime, endTime, focusDuration);
            pomodoroViewModel.updatePomodoro(getContext(), currPomodoro);

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

    private void animateTimerToCenter() {
        ConstraintLayout layout = getView().findViewById(R.id.pomo_layout);

        Button startButton = getView().findViewById(R.id.start_button);
        LinearLayout afterStart = getView().findViewById(R.id.afterStart);
        TextView timerText = getView().findViewById(R.id.timer_text);
        ImageView play_icon = getView().findViewById(R.id.play_icon);

        // Lấy kích thước cha và các view
        int parentWidth = layout.getWidth();
        int parentHeight = layout.getHeight();

        int textWidth = timerText.getWidth();
        int textHeight = timerText.getHeight();

        // Tính khoảng cách di chuyển đến tâm
        float targetX = (parentWidth - textWidth) / 2f - timerText.getX();
        float targetY = (parentHeight - textHeight) / 2f - timerText.getY();

        // Di chuyển timerText về giữa
        timerText.animate()
                .translationXBy(targetX)
                .translationYBy(targetY)
                .setDuration(500)
                .start();

        // Di chuyển afterStart và playIcon theo
        afterStart.animate()
                .translationXBy(targetX)
                .translationYBy(targetY + 20)
                .setDuration(500)
                .start();

        play_icon.animate()
                .translationXBy(targetX)
                .translationYBy(targetY + 20)
                .setDuration(500)
                .start();

        // Thu/phóng kích thước chữ timerText từ currentSizePx đến 80px
        float currentSizePx = timerText.getTextSize();
        float targetSizePx = 80f;

        ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(timerText, "textSize", currentSizePx, targetSizePx);
        scaleAnimator.setDuration(500);
        scaleAnimator.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void SavePomodoro(long startTime, int userId, int taskId, LocalDate dueDate){
        pomodoroViewModel.getLastCreatedPomodoro().observe(getViewLifecycleOwner(), pomodoro -> {
            if (pomodoro != null) {
                currPomodoro = pomodoro;
                Toast.makeText(getContext(), "Pomodoro created with ID: " + pomodoro.getId(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to create Pomodoro", Toast.LENGTH_SHORT).show();
            }
        });
        pomodoroViewModel.createPomodoro(getContext(), userId, taskId, startTime, dueDate, false);
    }





}