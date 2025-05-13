package com.example.focusflow_frontend.presentation.mission;

import android.os.Bundle;
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

public class MissionFragment extends Fragment {

    private ImageView imgPet;
    private TextView tvPetName, tvGrowthPoint;
    private ProgressBar progressGrowth;
    private ImageButton btnNextPet, btnPrevPet;
    private ImageView[] checkIcons;
    private ImageButton buttonEdit;
    private EditText editInput;
    private Button buttonApplyEdit;

    private MissionViewModel viewModel;

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
        tvPetName = view.findViewById(R.id.tvPetName);
        tvGrowthPoint = view.findViewById(R.id.tvGrowthPoint);
        progressGrowth = view.findViewById(R.id.progressGrowth);
        btnNextPet = view.findViewById(R.id.btnNextPet);
        btnPrevPet = view.findViewById(R.id.btnPrevPet);
        checkIcons = new ImageView[]{
                view.findViewById(R.id.imgCheck1),
                view.findViewById(R.id.imgCheck2),
                view.findViewById(R.id.imgCheck3)
        };
        buttonEdit = view.findViewById(R.id.button_edit);
        editInput = view.findViewById(R.id.edit_input);
        buttonApplyEdit = view.findViewById(R.id.buttonApplyEdit);

        // Navigation buttons
        btnNextPet.setOnClickListener(v -> { viewModel.nextPet(); updateUI(); });
        btnPrevPet.setOnClickListener(v -> { viewModel.prevPet(); updateUI(); });

        // Rename pet
        buttonEdit.setOnClickListener(v -> {
            editInput.setVisibility(View.VISIBLE);
            buttonApplyEdit.setVisibility(View.VISIBLE);
            editInput.setText(viewModel.getPetName());
        });
        buttonApplyEdit.setOnClickListener(v -> {
            String newName = editInput.getText().toString().trim();
            if (!newName.isEmpty()) {
                viewModel.renamePet(newName);
            }
            editInput.setVisibility(View.GONE);
            buttonApplyEdit.setVisibility(View.GONE);
            updateUI();
        });

        // Initial UI
        updateUI();
        return view;
    }

    //Ham de check hoan thanh task
    public void syncTaskStatus(int taskIndex, boolean isCompleted, int point) {
        viewModel.applyTaskStatus(taskIndex, isCompleted, point);
        updateUI();
    }

    private void updateUI() {
        imgPet.setImageResource(viewModel.getPetImageRes());
        tvPetName.setText(viewModel.getPetName());

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