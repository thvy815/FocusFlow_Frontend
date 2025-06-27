package com.example.focusflow_frontend.presentation.zalopay;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.api.AppInfo;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import vn.zalopay.sdk.*;

public class ZaloPayBottomSheet extends BottomSheetDialogFragment {
    private RadioGroup planGroup;
    private Button btnPay;
    public interface OnPlanSelectedListener {
        void onPlanSelected(String planName, String amount);
    }
    private OnPlanSelectedListener listener;

    public void setOnPlanSelectedListener(OnPlanSelectedListener listener) {
        this.listener = listener;
    }

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
            bottomSheet.setOnTouchListener((v, event) -> true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_zalopay, container, false);
        Log.d("ZaloPayDebug", "onCreateView STARTED");
        planGroup = view.findViewById(R.id.planGroup);
        btnPay = view.findViewById(R.id.btnPay);

        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        btnPay.setOnClickListener(v -> {
            int selectedId = planGroup.getCheckedRadioButtonId();
            final String amount, plan;

            if (selectedId == R.id.plan1) {
                amount = "29000"; plan = "1 tháng";
            } else if (selectedId == R.id.plan3) {
                amount = "79000"; plan = "3 tháng";
            } else if (selectedId == R.id.plan6) {
                amount = "149000"; plan = "6 tháng";
            } else if (selectedId == R.id.plan12) {
                amount = "279000"; plan = "12 tháng";
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn gói!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onPlanSelected(plan, amount);
            }

            dismiss();
        });

        return view;
    }
}
