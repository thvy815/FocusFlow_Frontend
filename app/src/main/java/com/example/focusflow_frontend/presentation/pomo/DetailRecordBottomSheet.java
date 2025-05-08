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

public class DetailRecordBottomSheet extends BottomSheetDialogFragment {
    int numeric = 0;

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
            }
        });

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_record, container, false);

// Set Title Text
        ViewUtils.setTitleText(view, R.id.detail_record_title, R.id.titleText, "Detail Record");

// Set Duration Text
        ViewUtils.setTitleText(view, R.id.duration, R.id.titleText, "Duration: ");
// Set Start Text
        ViewUtils.setTitleText(view, R.id.start_from_input, R.id.titleText, "Start from: ");
// Set End Text
        ViewUtils.setTitleText(view, R.id.end_at_input, R.id.titleText, "End at: ");
//Back click
        ViewUtils.backClick(this, view, R.id.detail_record_title, R.id.ic_back);
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
