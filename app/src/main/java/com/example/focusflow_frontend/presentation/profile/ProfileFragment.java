package com.example.focusflow_frontend.presentation.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.api.AvatarService;
import com.example.focusflow_frontend.data.api.ImageUploadService;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.presentation.main.MainActivity;
import com.example.focusflow_frontend.presentation.zalopay.ZaloPayBottomSheet;
import com.example.focusflow_frontend.utils.ApiClient;
import com.example.focusflow_frontend.utils.ImageApiClient;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProStatusCallback;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.*;

public class ProfileFragment extends Fragment {
    private AuthViewModel authViewModel;
    private ImageView avatarImage;
    private TextView usernameTextView;
    private Button btnUpgradePro;
    private ImageView btnSettings;

    private String fullname = "", username = "";

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    avatarImage.setImageURI(imageUri);
                    uploadImageToImageServer(imageUri);
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        avatarImage = view.findViewById(R.id.avatarImage);
        usernameTextView = view.findViewById(R.id.userName);
        btnUpgradePro = view.findViewById(R.id.btnUpgradePro);
        btnSettings = view.findViewById(R.id.btnSetting);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        authViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                fullname = user.getFullName() != null ? user.getFullName() : "";
                username = user.getUsername() != null ? user.getUsername() : "";
                usernameTextView.setText(fullname);

                String avatarUrl = user.getAvatarUrl();
                if (avatarUrl != null && avatarUrl.startsWith("url:")) {
                    if (getContext() != null) {
                        Glide.with(getContext())
                                .load(avatarUrl.substring(4))
                                .error(R.drawable.avatar1)
                                .into(avatarImage);
                    }
                } else {
                    avatarImage.setImageResource(R.drawable.avatar1);
                }
            }
        });

        authViewModel.getCurrentUser();

        avatarImage.setOnClickListener(v -> showImagePickDialog());

        ProUtils.isProValid(requireContext(), ApiClient.getRetrofit(requireContext()), new ProStatusCallback() {
            @Override
            public void onResult(boolean isProUser) {
                btnUpgradePro.setVisibility(isProUser ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                btnUpgradePro.setVisibility(View.VISIBLE);
            }
        });

        btnUpgradePro.setOnClickListener(v -> openZaloPay());
        btnSettings.setOnClickListener(v -> openSettings());

        return view;
    }

    private void showImagePickDialog() {
        new AlertDialog.Builder(requireContext())
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
            if (inputStream == null) return;

            byte[] imageBytes = getBytes(inputStream);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "avatar.jpg", requestFile);

            ImageApiClient.getImageRetrofit()
                    .create(ImageUploadService.class)
                    .uploadAvatar(body)
                    .enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String url = response.body().get("url");
                                if (url != null) {
                                    updateAvatar("url:" + url);
                                }
                            } else {
                                showToast("Failed to upload image");
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                            showToast("Connection error: " + t.getMessage());
                        }
                    });

        } catch (Exception e) {
            showToast("Failed to read image: " + e.getMessage());
        }
    }

    private void updateAvatar(String avatarUrl) {
        if (fullname == null || fullname.isEmpty() || username == null || username.isEmpty()) {
            if (authViewModel.getCurrentUserLiveData().getValue() != null) {
                fullname = authViewModel.getCurrentUserLiveData().getValue().getFullName();
                username = authViewModel.getCurrentUserLiveData().getValue().getUsername();
            }
        }

        authViewModel.updateUser(fullname, username, avatarUrl);
        authViewModel.fetchUserInfo(() -> showToast("Avatar updated successfully"));
    }

    private byte[] getBytes(InputStream inputStream) throws java.io.IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void showPredefinedAvatarDialog() {
        ImageApiClient.getImageRetrofit()
                .create(AvatarService.class)
                .getAvatarUrls()
                .enqueue(new Callback<List<String>>() {
                    @Override
                    public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            showAvatarGridDialog(response.body().toArray(new String[0]));
                        } else {
                            showToast("Failed to retrieve avatar list");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<String>> call, Throwable t) {
                        showToast("Network error: " + t.getMessage());
                    }
                });
    }

    private void showAvatarGridDialog(String[] avatarUrls) {
        GridView grid = new GridView(requireContext());
        grid.setNumColumns(3);
        grid.setAdapter(new UrlImageAdapter(requireContext(), avatarUrls));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Choose Avatar")
                .setView(grid)
                .setNegativeButton("Cancel", null)
                .create();

        grid.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUrl = "url:" + avatarUrls[position];
            if (getContext() != null) {
                Glide.with(getContext()).load(avatarUrls[position]).into(avatarImage);
            }
            updateAvatar(selectedUrl);
            dialog.dismiss();
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

    private void openSettings() {
        ProfileSettingBottomSheet sheet = new ProfileSettingBottomSheet();
        sheet.show(getParentFragmentManager(), "ProfileSettingBottomSheet");
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
