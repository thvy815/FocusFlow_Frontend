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
import com.example.focusflow_frontend.data.viewmodel.MissionViewModel;
import com.example.focusflow_frontend.utils.TokenManager;

public class MissionFragment extends Fragment {

    private ImageView imgPet;
    private TextView  tvGrowthPoint;
    private ProgressBar progressGrowth;
    private ImageButton btnNextPet, btnPrevPet;
    private ImageView[] checkIcons;

    private MissionViewModel viewModel;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_mission, container, false);

        viewModel = new ViewModelProvider(this).get(MissionViewModel.class);
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


        // Lấy userId từ token và gọi API để cập nhật điểm số, Pet
        userId = TokenManager.getUserId(requireContext());
        viewModel.fetchScoreFromBackend(requireContext(), userId, this::updateUI);

        return view;
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

