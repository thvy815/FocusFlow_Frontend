package com.example.focusflow_frontend.presentation.streak;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Pomodoro;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.data.viewmodel.MissionViewModel;
import com.example.focusflow_frontend.data.viewmodel.PomodoroViewModel;
import com.example.focusflow_frontend.data.viewmodel.TaskViewModel;
import com.example.focusflow_frontend.utils.TokenManager;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MissionFragment extends Fragment {
    private ImageView imgPet;
    private TextView  tvGrowthPoint;
    private ProgressBar progressGrowth;
    private ImageButton btnNextPet, btnPrevPet;
    private ImageView[] checkIcons;
    private MissionViewModel viewModel;
    private TaskViewModel taskViewModel;
    private PomodoroViewModel pomodoroViewModel;
    private SimpleDateFormat fullFormat;
    private SimpleDateFormat dateFormat;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_mission, container, false);

        fullFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        viewModel = new ViewModelProvider(this).get(MissionViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        pomodoroViewModel = new ViewModelProvider(requireActivity()).get(PomodoroViewModel.class);
        AuthViewModel authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        viewModel.setAuthViewModel(authViewModel);

        viewModel.init(requireContext());

        // Bind views
        imgPet = view.findViewById(R.id.imgPet);
        tvGrowthPoint = view.findViewById(R.id.tvGrowthPoint);
        progressGrowth = view.findViewById(R.id.progressGrowth);
        btnNextPet = view.findViewById(R.id.btnNextPet);
        btnPrevPet = view.findViewById(R.id.btnPrevPet);
        checkIcons = new ImageView[]{
                view.findViewById(R.id.imgCheck1),
                view.findViewById(R.id.imgCheck2),
                view.findViewById(R.id.imgCheck3)
        };

        // Navigation buttons
        btnNextPet.setOnClickListener(v -> {
            viewModel.nextPet();
            updateUI();
        });
        btnPrevPet.setOnClickListener(v -> {
            viewModel.prevPet();
            updateUI();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        int completedTasks = countCompletedTasksToday(taskViewModel);
        int finishedPomodoros = countFinishedPomodorosToday(pomodoroViewModel);
        int tasksForTomorrow = countTasksForTomorrow(taskViewModel);

        if (completedTasks >= 1) viewModel.applyTaskStatus(0, true, 10);
        if (finishedPomodoros >= 1) viewModel.applyTaskStatus(1, true, 10);
        if (tasksForTomorrow >= 3) viewModel.applyTaskStatus(2, true, 10);

        updateUI();
    }

    private int countCompletedTasksToday(TaskViewModel taskViewModel) {
        List<Task> tasks = taskViewModel.getTaskList().getValue();
        if (tasks == null) return 0;

        String today = dateFormat.format(new Date());
        int count = 0;

        for (Task task : tasks) {
            if (task.getDueDate() != null && task.getDueDate().equals(today)
                    && Boolean.TRUE.equals(task.isCompleted())) {
                count++;
            }
        }
        return count;
    }

    private int countFinishedPomodorosToday(PomodoroViewModel pomoVM) {
        List<Pomodoro> pomodoros = pomoVM.getPomodoroList().getValue();
        if (pomodoros == null) return 0;

        String today = dateFormat.format(new Date());
        int count = 0;

        for (Pomodoro p : pomodoros) {
            try {
                Date startDate = fullFormat.parse(p.getStartAt());
                String startDateStr = dateFormat.format(startDate);
                if (today.equals(startDateStr)) {
                    count++;
                }
            } catch (Exception e) {
                Log.w("MissionFragment", "Lỗi parse pomo date: " + p.getStartAt());
            }
        }
        return count;
    }

    private int countTasksForTomorrow(TaskViewModel taskViewModel) {
        List<Task> tasks = taskViewModel.getTaskList().getValue();
        if (tasks == null) return 0;

        // Định dạng ngày mai dưới dạng dd/MM/yyyy
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        String tomorrow = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());

        int count = 0;
        for (Task task : tasks) {
            if (tomorrow.equals(task.getDueDate())) {
                count++;
            }
        }
        return count;
    }

    private void updateUI() {

        int progress = viewModel.getProgress();
        int max = viewModel.getProgressMax();
        progressGrowth.setMax(max);
        progressGrowth.setProgress(progress);
        tvGrowthPoint.setText(progress + "/" + max);

        for (int i = 0; i < checkIcons.length; i++) {
            checkIcons[i].setColorFilter(
                    viewModel.isTaskCompleted(i)
                            ? getResources().getColor(R.color.orange)
                            : getResources().getColor(R.color.gray)
            );
        }
    }
}

