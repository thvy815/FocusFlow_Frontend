package com.example.focusflow_frontend.presentation.pomo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.viewmodel.PomodoroViewModel;
import com.example.focusflow_frontend.utils.ViewUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FocusRecordBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private FocusRecordAdapter adapter;
    private PomodoroViewModel viewModel;
    private int userId, pomodoroId;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(layoutParams);

                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                bottomSheet.setOnTouchListener((v, event) -> true);
            }
        });
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(false);
            behavior.setHideable(false);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.focus_record, container, false);
        userId = getArguments() != null ? getArguments().getInt("userId", 1) : 1;

        if (userId == -1)
        {
            Toast.makeText(getContext(),"Không có thông tin người dùng",Toast.LENGTH_SHORT);
        }
        // Set Title Text
        ViewUtils.setTitleText(view, R.id.focus_record_title, R.id.titleText, "Focus Record");

        TextView btnAdd = view.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecordClick();
            }
        });
        setCancelable(false);
        //Back click
        ViewUtils.backClick(this, view, R.id.focus_record_title, R.id.ic_back);

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.focusRecyclerView);
        adapter = new FocusRecordAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(PomodoroViewModel.class);
        observeData();
        viewModel.getAllPomodoro(getContext(), userId);

        adapter.setOnItemClickListener(detail -> {
            DetailRecordBottomSheet statsSheet = new DetailRecordBottomSheet();
            Bundle args = new Bundle();
            args.putInt("pomodoroId", pomodoroId);
            statsSheet.setArguments(args);
            statsSheet.show(getParentFragmentManager(), statsSheet.getTag());
        });

        setCancelable(false);

        return view;
    }

    private void observeData() {
        viewModel.getPomodoroList().observe(getViewLifecycleOwner(), details -> {
            if (details != null) adapter.setRecords(details);
        });
    }
    public void addRecordClick() {
        AddRecordBottomSheet statsSheet = new AddRecordBottomSheet();
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        statsSheet.setArguments(args);
        statsSheet.show(getParentFragmentManager(), statsSheet.getTag());
    }
}
