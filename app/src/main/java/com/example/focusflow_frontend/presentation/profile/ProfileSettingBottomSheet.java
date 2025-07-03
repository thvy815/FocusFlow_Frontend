package com.example.focusflow_frontend.presentation.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.api.AvatarService;
import com.example.focusflow_frontend.data.api.ImageUploadService;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.presentation.login.SignInActivity;
import com.example.focusflow_frontend.presentation.main.MainActivity;
import com.example.focusflow_frontend.presentation.zalopay.ZaloPayBottomSheet;
import com.example.focusflow_frontend.utils.ApiClient;
import com.example.focusflow_frontend.utils.ImageApiClient;
import com.example.focusflow_frontend.utils.TokenManager;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProStatusCallback;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileSettingBottomSheet extends BottomSheetDialogFragment {
    private AuthViewModel authViewModel;
    private String savedAvatar;
    private String username, email, fullname;
    private ImageView avatarImage;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) uploadImageToImageServer(imageUri);
                }
            }
    );

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.requestLayout();
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_setting, container, false);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        View avt = view.findViewById(R.id.avtLayout);
        avatarImage = view.findViewById(R.id.avatarImage);

        authViewModel.getCurrentUser();
        authViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                fullname = user.getFullName();
                username = user.getUsername();
                email = user.getEmail();
                savedAvatar = user.getAvatarUrl();
                updateAvatarView(savedAvatar);
                setTextLayout(view.findViewById(R.id.fullName), "Fullname", fullname);
                setTextLayout(view.findViewById(R.id.userName), "Username", username);
                setTextLayout(view.findViewById(R.id.emailRow), "Email", email);
            }
        });

        avt.setOnClickListener(v -> showAvatarOptions());

        view.findViewById(R.id.fullName).setOnClickListener(v -> {
            showEditDialog("Fullname", fullname, newValue -> {
                fullname = newValue;
                updateUserAndUI(view);
            });
        });

        view.findViewById(R.id.userName).setOnClickListener(v -> {
            showEditDialog("Username", username, newValue -> {
                username = newValue;
                updateUserAndUI(view);
            });
        });

        TextView upgrade = view.findViewById(R.id.upgradeButton);
        TextView upgradeDesc = view.findViewById(R.id.upgradeDesc);

        ProUtils.isProValid(getContext(), ApiClient.getRetrofit(requireContext()), new ProStatusCallback() {
            @Override
            public void onResult(boolean isPro) {
                if (isPro) {
                    upgradeDesc.setText("You're using Pro version");
                    upgrade.setVisibility(View.GONE);
                } else {
                    upgrade.setVisibility(View.VISIBLE);
                    upgrade.setOnClickListener(v -> openZaloPay());
                }
            }

            @Override
            public void onError(String message) {
                upgrade.setVisibility(View.VISIBLE);
            }
        });

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
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
    private void showAvatarOptions() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Choose Avatar")
                .setItems(new CharSequence[]{"From Gallery", "Select from Templates"}, (dialog, which) -> {
                    if (which == 0) openGallery();
                    else showPredefinedAvatarDialog();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void uploadImageToImageServer(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = getBytes(inputStream);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "avatar.jpg", requestFile);

            ImageApiClient.getImageRetrofit().create(ImageUploadService.class).uploadAvatar(body)
                    .enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                savedAvatar = "url:" + response.body().get("url");
                                updateUserAndUI(getView());
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                            Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private void showPredefinedAvatarDialog() {
        ImageApiClient.getImageRetrofit()
                .create(AvatarService.class)
                .getAvatarUrls()
                .enqueue(new Callback<List<String>>() {
                    @Override
                    public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            GridView grid = new GridView(getContext());
                            grid.setNumColumns(3);
                            String[] urls = response.body().toArray(new String[0]);
                            grid.setAdapter(new UrlImageAdapter(getContext(), urls));

                            AlertDialog dialog = new AlertDialog.Builder(getContext())
                                    .setTitle("Choose Avatar")
                                    .setView(grid)
                                    .setNegativeButton("Cancel", null)
                                    .create();

                            grid.setOnItemClickListener((parent, view, position, id) -> {
                                savedAvatar = "url:" + urls[position];
                                updateUserAndUI(getView());
                                dialog.dismiss();
                            });

                            dialog.show();
                        } else {
                            Toast.makeText(getContext(), "Failed to load image list", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<String>> call, Throwable t) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserAndUI(View view) {
        authViewModel.updateUser(fullname, username, savedAvatar);
        setTextLayout(view.findViewById(R.id.fullName), "Fullname", fullname);
        setTextLayout(view.findViewById(R.id.userName), "Username", username);
        updateAvatarView(savedAvatar);
    }

    private void updateAvatarView(String avatarUrl) {
        if (avatarUrl != null && avatarUrl.startsWith("url:")) {
            Glide.with(requireContext()).load(avatarUrl.substring(4)).into(avatarImage);
        } else {
            avatarImage.setImageResource(R.drawable.avatar1);
        }
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
        builder.setTitle("Edit " + field);

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_text, null);
        EditText editText = dialogView.findViewById(R.id.editText);
        editText.setText(oldValue);
        editText.setSelection(oldValue.length());
        builder.setView(dialogView);

        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Save", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Save");
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newValue = editText.getText().toString().trim();
                if (!newValue.equals(oldValue) && !newValue.isEmpty()) {
                    listener.onValueUpdated(newValue);
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "No changes made", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void openZaloPay() {
        ZaloPayBottomSheet sheet = new ZaloPayBottomSheet();
        sheet.setOnPlanSelectedListener((plan, amount) -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).createAndPayOrder(plan, amount);
            }
        });
        sheet.show(getParentFragmentManager(), "ZaloPayBottomSheet");
    }

    private void handleLogoutAndRedirect() {
        TokenManager.clearToken(requireContext());
        ProUtils.clearProStatus(requireContext());
        TokenManager.clearUserId(requireContext());
        TokenManager.saveRememberMe(requireContext(), false);
        Intent intent = new Intent(requireContext(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        dismiss();
    }

    public interface OnValueUpdatedListener {
        void onValueUpdated(String newValue);
    }
}
