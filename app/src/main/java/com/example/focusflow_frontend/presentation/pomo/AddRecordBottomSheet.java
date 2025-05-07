package com.example.focusflow_frontend.presentation.pomo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.utils.ViewUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddRecordBottomSheet extends BottomSheetDialogFragment {
    int numeric = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_record, container, false);

// Set Title Text
        ViewUtils.setTitleText(view, R.id.add_record_title, R.id.titleText, "Add Record");

//Increase/Decrease Pomo
        View numeric_Pomo = view.findViewById(R.id.numeric_pomo_input);

        ImageView imvAdd = numeric_Pomo.findViewById(R.id.add_num);
        TextView tvNumeric = numeric_Pomo.findViewById(R.id.tv_numeric);
        ImageView imvSub = numeric_Pomo.findViewById(R.id.sub_num);

        imvAdd.setOnClickListener(v -> {
            numeric++;
            tvNumeric.setText(String.valueOf(numeric));
        });

        imvSub.setOnClickListener(v -> {
            if (numeric > 0) {
                numeric--;
                tvNumeric.setText(String.valueOf(numeric));
            }
        });

// Set Task Text
        ViewUtils.setTitleText(view, R.id.et_task_input, R.id.titleText, "Task: ");
// Set Start Text
        ViewUtils.setTitleText(view, R.id.start_from_input, R.id.titleText, "Start from: ");
// Set End Text
        ViewUtils.setTitleText(view, R.id.end_at_input, R.id.titleText, "End at: ");
//Back click
        ViewUtils.backClick(this, view, R.id.add_record_title, R.id.ic_back);
        return view;
    }

    public void saveRecord(){}


//    Đẩy fragment lên full màn hình (chưa làm được)
//    @Override
//    public int getTheme() {
//        return R.style.BottomSheetFullScreen;
//    }
//
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
//        dialog.setOnShowListener(dialogInterface -> {
//            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
//            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//
//            if (bottomSheet != null) {
//                // Đổi sang set chiều cao MATCH_PARENT
//                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
//                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
//                bottomSheet.setLayoutParams(layoutParams);
//            }
//        });
//
//        return dialog;
//    }


}
