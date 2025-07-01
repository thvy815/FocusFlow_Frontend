package com.example.focusflow_frontend.presentation.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.presentation.login.SignInActivity;
import com.example.focusflow_frontend.presentation.main.MainActivity;
import com.example.focusflow_frontend.presentation.zalopay.ZaloPayBottomSheet;
import com.example.focusflow_frontend.utils.TokenManager;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ProfileSettingBottomSheet extends BottomSheetDialogFragment {
    private AuthViewModel authViewModel;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private String savedAvatar;
    private String username, email, fullname;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_setting, container, false);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        View avt = view.findViewById(R.id.avtLayout);
        ImageView avatarImage = avt.findViewById(R.id.avatarImage);
        if (getArguments() != null && getArguments().containsKey("savedAvatar")) {
            String savedAvatar = getArguments().getString("savedAvatar");
            if (savedAvatar != null) {
                if (savedAvatar.startsWith("uri:")) {
                    avatarImage.setImageURI(Uri.parse(savedAvatar.substring(4)));
                } else if (savedAvatar.startsWith("res:")) {
                    avatarImage.setImageResource(Integer.parseInt(savedAvatar.substring(4)));
                }
            }
        }

        avt.setOnClickListener(v -> showImagePickDialog());

        username = getArguments() != null ? getArguments().getString("username", "") : "";
        email = getArguments() != null ? getArguments().getString("email", "") : "";
        fullname = getArguments() != null ? getArguments().getString("fullname", "") : "";

        View fullNameView = view.findViewById(R.id.fullName);
        View userNameView = view.findViewById(R.id.userName);
        setTextLayout(fullNameView, "Fullname", fullname);
        setTextLayout(userNameView, "Username", username);
        setTextLayout(view.findViewById(R.id.emailRow), "Email", email);

        fullNameView.setOnClickListener(v -> {
            showEditDialog("Fullname", fullname, newValue -> {
                fullname = newValue;
                authViewModel.updateUser(fullname, username, savedAvatar);
                setTextLayout(fullNameView, "Fullname", newValue);
            });
        });

        userNameView.setOnClickListener(v -> {
            showEditDialog("Username", username, newValue -> {
                username = newValue;
                authViewModel.updateUser(fullname, username, savedAvatar);
                setTextLayout(userNameView, "Username", newValue);
            });
        });


        TextView upgrade = view.findViewById(R.id.upgradeButton);
        TextView upgradeDesc = view.findViewById(R.id.upgradeDesc);
        boolean isPro = ProUtils.isProValid(getContext());
        if (isPro) {
            upgradeDesc.setText("You're using Pro version");
            upgrade.setVisibility(View.GONE);
        } else {
            upgrade.setVisibility(View.VISIBLE);
            upgrade.setOnClickListener(v -> {
                ZaloPayBottomSheet sheet = new ZaloPayBottomSheet();
                sheet.setOnPlanSelectedListener((plan, amount) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).createAndPayOrder(plan, amount);
                    }
                });
                sheet.show(getParentFragmentManager(), "ZaloPayBottomSheet");
            });
        }

        view.findViewById(R.id.tvDone).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.deleteAccount).setOnClickListener(v -> authViewModel.deleteCurrentUser());
        authViewModel.deleteResult.observe(getViewLifecycleOwner(), isDeleted -> {
            if (Boolean.TRUE.equals(isDeleted)) {
                Toast.makeText(getContext(), "Your account has been deleted", Toast.LENGTH_SHORT).show();
                handleLogoutAndRedirect();
            }
        });
        view.findViewById(R.id.Logout).setOnClickListener(v -> {
            handleLogoutAndRedirect();
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
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

    private void showEditDialog(String field, String oldValue, OnValueUpdatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chỉnh sửa " + field);

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_text, null);
        EditText editText = dialogView.findViewById(R.id.editText);
        editText.setText(oldValue);
        editText.setSelection(oldValue.length()); // đưa con trỏ về cuối
        builder.setView(dialogView);

        builder.setNegativeButton("Hủy", null);
        builder.setPositiveButton("Lưu", null); // Tạm thời để null để xử lý riêng

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            TextView saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveBtn.setText("Lưu");

            saveBtn.setOnClickListener(v -> {
                String newValue = editText.getText().toString().trim();
                if (!newValue.isEmpty() && !newValue.equals(oldValue)) {
                    listener.onValueUpdated(newValue);
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Không có thay đổi", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
    public interface OnValueUpdatedListener {
        void onValueUpdated(String newValue);
    }
    private void showImagePickDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn ảnh đại diện");
        builder.setItems(new CharSequence[]{"Chọn từ album", "Chọn từ mẫu có sẵn"}, (dialog, which) -> {
            if (which == 0) openGallery();
            else showPredefinedAvatarDialog();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            View avt = getView().findViewById(R.id.avtLayout);
            ImageView avatarImage = avt.findViewById(R.id.avatarImage);
            avatarImage.setImageURI(imageUri);
            avatarImage.setTag(imageUri);
            savedAvatar = "uri:" + imageUri.toString();
            authViewModel.updateUser(fullname, username, savedAvatar);
        }
    }

    private void showPredefinedAvatarDialog() {
        final int[] imageResIds = {
                R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3, R.drawable.avatar4
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn từ mẫu có sẵn");
        GridView gridView = new GridView(getContext());
        gridView.setNumColumns(3);
        gridView.setAdapter(new ImageAdapter(getContext(), imageResIds));
        builder.setView(gridView);
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            int selectedResId = imageResIds[position];
            View avt = getView().findViewById(R.id.avtLayout);
            ImageView avatarImage = avt.findViewById(R.id.avatarImage);
            avatarImage.setImageResource(selectedResId);
            avatarImage.setTag(selectedResId);
            savedAvatar = "res:" + selectedResId;
            authViewModel.updateUser(fullname, username, savedAvatar);
            dialog.dismiss();
        });
        dialog.show();
    }
    private void handleLogoutAndRedirect() {
        TokenManager.clearToken(requireContext());
        ProUtils.clearProStatus(getContext());
        TokenManager.clearUserId(requireContext());
        TokenManager.saveRememberMe(requireContext(), false);
        Intent intent = new Intent(requireContext(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        dismiss();
    }
}
