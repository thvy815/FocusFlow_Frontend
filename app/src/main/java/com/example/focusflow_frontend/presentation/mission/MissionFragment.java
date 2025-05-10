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

import com.example.focusflow_frontend.R;

public class MissionFragment extends Fragment {

    private ImageView imgPet;
    private TextView tvPetName, tvGrowthPoint;
    private ProgressBar progressGrowth;
    private ImageView[] checkIcons;
    private TextView petName;
    private ImageButton editName;
    private EditText editInput;
    private Button buttonApplyEdit;

    private int currentPetIndex = 0;
    private int currentLevel = 0;
    private int currentGrowth = 0;

    private final int[] petImages = {
            R.drawable.ic_pet_level1,
            R.drawable.ic_pet_level2,
            R.drawable.ic_pet_level3,
            R.drawable.ic_pet_level4,
            R.drawable.ic_pet_level5
    };

    private final int[] levelThresholds = {10, 20, 100, 200, 500};
    private final boolean[] taskCompleted = {false, false, false};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mission, container, false);

        imgPet = view.findViewById(R.id.imgPet);
        tvPetName = view.findViewById(R.id.tvPetName);
        tvGrowthPoint = view.findViewById(R.id.tvGrowthPoint);
        progressGrowth = view.findViewById(R.id.progressGrowth);
        petName = view.findViewById(R.id.tvPetName);
        editName = view.findViewById(R.id.button_edit);
        editInput= view.findViewById(R.id.edit_input);
        buttonApplyEdit = view.findViewById(R.id.buttonApplyEdit);

        checkIcons = new ImageView[]{
                view.findViewById(R.id.imgCheck1),
                view.findViewById(R.id.imgCheck2),
                view.findViewById(R.id.imgCheck3)
        };

        // Nut chuyen pet
        view.findViewById(R.id.btnNextPet).setOnClickListener(v -> {
            currentPetIndex = (currentPetIndex + 1) % petImages.length;
            updateUI();
        });

        view.findViewById(R.id.btnPrevPet).setOnClickListener(v -> {
            currentPetIndex = (currentPetIndex - 1 + petImages.length) % petImages.length;
            updateUI();
        });

        // nut nhiem vu
        view.findViewById(R.id.btnTask1).setOnClickListener(v -> completeTask(0, 1));
        view.findViewById(R.id.btnTask2).setOnClickListener(v -> completeTask(1, 3));
        view.findViewById(R.id.btnTask3).setOnClickListener(v -> completeTask(2, 3));

        updateUI();

        editName.setOnClickListener(v -> {
            editInput.setVisibility(View.VISIBLE);
            buttonApplyEdit.setVisibility(View.VISIBLE);
            editInput.setText(petName.getText());
        });
        buttonApplyEdit.setOnClickListener(v -> {
            String newText = editInput.getText().toString();
            if (!newText.isEmpty()) {
                petName.setText(newText);
            }
            editInput.setVisibility(View.GONE);
            buttonApplyEdit.setVisibility(View.GONE);
        });
        return view;
    }

    private void completeTask(int taskIndex, int point) {
        if (!taskCompleted[taskIndex]) {
            taskCompleted[taskIndex] = true;
            currentGrowth += point;
            updateUI();
        }
    }

    private void updateUI() {
        // update anh pet
        imgPet.setImageResource(petImages[currentPetIndex]);
        tvPetName.setText("Pet " + (currentPetIndex + 1));

        // update level
        while (currentLevel < levelThresholds.length && currentGrowth >= levelThresholds[currentLevel]) {
            currentLevel++;
        }

        // update progress
        int max = currentLevel < levelThresholds.length ? levelThresholds[currentLevel] : levelThresholds[levelThresholds.length - 1];
        int prevMax = currentLevel == 0 ? 0 : levelThresholds[currentLevel - 1];
        int progress = currentGrowth - prevMax;

        progressGrowth.setMax(max - prevMax);
        progressGrowth.setProgress(progress);
        tvGrowthPoint.setText(progress + "/" + (max - prevMax));

        // update icon mission
        for (int i = 0; i < checkIcons.length; i++) {
            checkIcons[i].setColorFilter(taskCompleted[i] ?
                    getResources().getColor(R.color.orange) :
                    getResources().getColor(R.color.gray)
            );
        }
    }
}
