package com.example.focusflow_frontend.presentation.pomo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.utils.ViewUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddRecordBottomSheet extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_record, container, false);

// Set Title Text
        View textLayout = view.findViewById(R.id.add_record_title);
        ViewUtils.setTitleText(textLayout,"Add Record");

        return view;
    }
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
