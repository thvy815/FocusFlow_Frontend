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
        viewModel.setTaskViewModel(taskViewModel);
        viewModel.setPomodoroViewModel(pomodoroViewModel);
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

        authViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                taskViewModel.fetchTasks(user.getId()); // Dùng lại hàm đã có!
            }
        });

        authViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                int userId = user.getId();
                taskViewModel.fetchTasks(userId); // ✅ gọi task
                pomodoroViewModel.fetchPomodorosByUser(requireContext(),user.getId()); // ✅ gọi pomo
            }
        });

        authViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                viewModel.setUserScore(user.getScore());
                updateUI();
            }
        });

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("MissionFragment", "onResume called");

        taskViewModel.getTaskList().observe(getViewLifecycleOwner(), tasks -> {
            Log.d("MissionFragment", "Task list updated: " + tasks.size());
            checkMissionsAndUpdate();
        });

        pomodoroViewModel.getPomodoroList().observe(getViewLifecycleOwner(), pomodoros -> {
            Log.d("MissionFragment", "Pomodoro list updated: " + pomodoros.size());
            checkMissionsAndUpdate();
        });


        updateUI();
    }

    private void checkMissionsAndUpdate() {
        Log.d("MissionCheck", "Checking mission completion...");

        if (viewModel.isMissionCompleted(0)) {
            Log.d("MissionCheck", "Mission 0 completed");
            viewModel.applyTaskStatus(0, true, 3);
        } else {
            Log.d("MissionCheck", "Mission 0 not completed");
        }

        if (viewModel.isMissionCompleted(1)) {
            Log.d("MissionCheck", "Mission 1 completed");
            viewModel.applyTaskStatus(1, true, 5);
        } else {
            Log.d("MissionCheck", "Mission 1 not completed");
        }

        if (viewModel.isMissionCompleted(2)) {
            Log.d("MissionCheck", "Mission 2 completed");
            viewModel.applyTaskStatus(2, true, 5);
        } else {
            Log.d("MissionCheck", "Mission 2 not completed");
        }

        updateUI();
    }




    private void updateUI() {

        int progress = viewModel.getProgress();
        int max = viewModel.getProgressMax();
        progressGrowth.setMax(max);
        progressGrowth.setProgress(progress);
        imgPet.setImageResource(viewModel.getCurrentPetImage());
        tvGrowthPoint.setText(progress + "/" + max);


        for (int i = 0; i < checkIcons.length; i++) {
            checkIcons[i].setColorFilter(
                    viewModel.isMissionCompleted(i)
                            ? getResources().getColor(R.color.orange)
                            : getResources().getColor(R.color.gray)
            );
        }
    }
}

