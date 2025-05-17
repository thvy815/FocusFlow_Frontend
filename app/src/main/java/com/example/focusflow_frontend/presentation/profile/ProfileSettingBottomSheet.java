package com.example.focusflow_frontend.presentation.profile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.presentation.login.SignInActivity;
import com.example.focusflow_frontend.utils.TokenManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ProfileSettingBottomSheet extends BottomSheetDialogFragment {

    private String imageURL;
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

        View view = inflater.inflate(R.layout.profile_setting, container, false);

        // Nhận dữ liệu từ Bundle

        // Hiển thị avatar
        View avt = view.findViewById(R.id.avtLayout);
        ImageView avatarImage = avt.findViewById(R.id.avatarImage);
        if (getArguments() != null) {
            int imageResId = getArguments().getInt("imageResId", -1);
            if (imageResId != -1) {
                avatarImage.setImageResource(imageResId);
            }
        }

        // Thiết lập các trường dữ liệu
        setTextLayout(view.findViewById(R.id.userName), "Username", "user123");
        setTextLayout(view.findViewById(R.id.emailRow), "Email", "william.he");
        setTextLayout(view.findViewById(R.id.google), "Google", "user123");
        setTextLayout(view.findViewById(R.id.android), "Android", "user123");

        // Gắn sự kiện click
        view.findViewById(R.id.tvDone).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.deleteAccount).setOnClickListener(v -> {
            Toast.makeText(getContext(), "loading Account deleted", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        view.findViewById(R.id.Logout).setOnClickListener(v -> {
            // Xóa dữ liệu đăng nhập
            TokenManager.clearToken(requireContext());
            TokenManager.clearUserId(requireContext());
            TokenManager.saveRememberMe(requireContext(), false);

            // Thông báo
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();

            // Chuyển về màn hình đăng nhập
            Intent intent = new Intent(requireContext(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear toàn bộ stack
            startActivity(intent);

            // Đóng BottomSheet
            dismiss();
        });

        return view;
    }

    private void setTextLayout(View view, String titleText, String contentText) {
        if (view == null) return;
        TextView title = view.findViewById(R.id.titleText);
        TextView content = view.findViewById(R.id.contentText);

        if (title != null) title.setText(titleText);
        if (content != null) content.setText(contentText);
    }
}
