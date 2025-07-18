package com.example.focusflow_frontend.presentation.pomo;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private int userId;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.focus_record, container, false);
        userId = getArguments() != null ? getArguments().getInt("userId", -1) : -1;
        Log.d("FocusRecord", "userId = " + userId);
        if (userId == -1) {
            Toast.makeText(getContext(), "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
        }

        // Set Title Text
        ViewUtils.setTitleText(view, R.id.focus_record_title, R.id.titleText, "Focus Record");

        // Add Button Click
        TextView btnAdd = view.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> addRecordClick());

        // Back Click
        ViewUtils.backClick(this, view, R.id.focus_record_title, R.id.ic_back);

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.focusRecyclerView);
        adapter = new FocusRecordAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // ViewModel setup
        viewModel = new ViewModelProvider(this).get(PomodoroViewModel.class);
        observeData();
        loadData();

        // Handle item click
        adapter.setOnItemClickListener(detail -> {
            DetailRecordBottomSheet statsSheet = new DetailRecordBottomSheet();
            Bundle args = new Bundle();
            args.putInt("pomodoroId", detail.getId()); // ✅ Sửa: truyền đúng ID
            args.putInt("userId", userId);
            statsSheet.setArguments(args);

            // Refresh khi Detail đóng
            statsSheet.setOnDismissListener(() -> loadData());

            statsSheet.show(getParentFragmentManager(), statsSheet.getTag());
        });

        setCancelable(false);
        return view;
    }

    private void observeData() {
        viewModel.getPomodoroList().observe(getViewLifecycleOwner(), pomodoros -> {
            if (pomodoros != null) {
                adapter.setRecords(pomodoros);
                viewModel.fetchTaskNames(requireContext(), pomodoros);
            }
        });

        viewModel.getTaskNameMapLiveData().observe(getViewLifecycleOwner(), taskNameMap -> {
            adapter.setTaskNameMap(taskNameMap);
        });
    }

    public void addRecordClick() {
        AddRecordBottomSheet addSheet = new AddRecordBottomSheet();
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        addSheet.setArguments(args);

        // Refresh khi Add đóng
        addSheet.setOnDismissListener(() -> loadData());

        addSheet.show(getParentFragmentManager(), addSheet.getTag());
    }

    private void loadData() {
        viewModel.getAllPomodoro(requireContext(), userId);
    }
}
