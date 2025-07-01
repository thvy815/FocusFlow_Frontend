package com.example.focusflow_frontend.presentation.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.presentation.main.MainActivity;
import com.example.focusflow_frontend.presentation.zalopay.ZaloPayBottomSheet;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProUtils;

import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;

    private AuthViewModel authViewModel;
    private ImageView avatarImage;
    private String savedAvatar;
    private String fullname = "";
    private String username = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        avatarImage = view.findViewById(R.id.avatarImage);
        TextView usernameTextView = view.findViewById(R.id.userName);
        LinearLayout achievementLayout = view.findViewById(R.id.achievementLayout);
        Button btnUpgradePro = view.findViewById(R.id.btnUpgradePro);
        ImageView btnSettings = view.findViewById(R.id.btnSetting);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getCurrentUser();
        authViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                fullname = user.getFullName();
                username = user.getUsername();
                usernameTextView.setText(fullname);

                // Hiển thị avatar
                if (user.getAvatarUrl() != null) {
                    String avatar = user.getAvatarUrl();
                    savedAvatar = avatar;
                    if (avatar.startsWith("uri:")) {
                        avatarImage.setImageURI(Uri.parse(avatar.substring(4)));
                    } else if (avatar.startsWith("res:")) {
                        avatarImage.setImageResource(Integer.parseInt(avatar.substring(4)));
                    }
                }
            }
        });

        // Avatar click
        avatarImage.setOnClickListener(v -> showImagePickDialog());

        List<Integer> earnedBadges = Arrays.asList(
                R.drawable.badge100,
                R.drawable.pomo3,
                R.drawable.pomo100
        );
        setUserBadges(achievementLayout, earnedBadges);

        // Nâng cấp Pro
        boolean isPro = ProUtils.isProValid(getContext());
        btnUpgradePro.setVisibility(isPro ? View.GONE : View.VISIBLE);
        btnUpgradePro.setOnClickListener(v -> openZaloPay());

        // Mở phần cài đặt
        btnSettings.setOnClickListener(v -> openSettings());

        setStreakAndScore(view);
        return view;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            savedAvatar = "uri:" + imageUri.toString();
            avatarImage.setImageURI(imageUri);
            avatarImage.setTag(imageUri);
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
            int resId = imageResIds[position];
            savedAvatar = "res:" + resId;
            avatarImage.setImageResource(resId);
            avatarImage.setTag(resId);
            authViewModel.updateUser(fullname, username, savedAvatar);
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
        FragmentManager fm = getParentFragmentManager();
        Fragment existing = fm.findFragmentByTag("ProfileSettingBottomSheet");
        if (existing != null && existing.isAdded()) {
            fm.beginTransaction().remove(existing).commit();
        }

        ProfileSettingBottomSheet sheet = new ProfileSettingBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putString("fullname", fullname);
        bundle.putString("username", username);
        if (savedAvatar != null) bundle.putString("savedAvatar", savedAvatar);

        if (authViewModel.getCurrentUserLiveData().getValue() != null) {
            bundle.putString("email", authViewModel.getCurrentUserLiveData().getValue().getEmail());
        }

        sheet.setArguments(bundle);
        sheet.show(fm, "ProfileSettingBottomSheet");
    }

    private void setUserBadges(LinearLayout layout, List<Integer> earnedBadges) {
        // Tất cả huy hiệu có thể có
        List<Integer> allBadges = Arrays.asList(
                R.drawable.badge1000,
                R.drawable.badge100,
                R.drawable.badge7,
                R.drawable.pomo3,
                R.drawable.pomo7,
                R.drawable.pomo100
        );
        layout.removeAllViews(); // Xoá huy hiệu cũ nếu có

        for (int badgeRes : allBadges) {
            ImageView img = new ImageView(getContext());
            img.setImageResource(badgeRes);

            // Layout params
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(10, 0, 10, 0);
            img.setLayoutParams(params);
            img.setAdjustViewBounds(true);
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // Nếu chưa đạt huy hiệu -> làm mờ đi
            if (!earnedBadges.contains(badgeRes)) {
                img.setAlpha(0.3f); // Làm mờ (30% độ đậm)
            } else {
                img.setAlpha(1.0f); // Hiển thị bình thường
            }
            layout.addView(img);
        }
    }
    private void setStreakAndScore(View view) {
        TextView streak = view.findViewById(R.id.streakValue);
        TextView score = view.findViewById(R.id.scoreValue);
        streak.setText("");  // Tuỳ bạn có dữ liệu hay không
        score.setText("");
    }
}
