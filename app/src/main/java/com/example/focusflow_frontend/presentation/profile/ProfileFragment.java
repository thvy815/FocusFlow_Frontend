package com.example.focusflow_frontend.presentation.profile;

import static java.lang.Math.log;

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

import com.bumptech.glide.Glide;
import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Streak;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.data.viewmodel.PomodoroViewModel;
import com.example.focusflow_frontend.data.viewmodel.StreakViewModel;
import com.example.focusflow_frontend.presentation.main.MainActivity;
import com.example.focusflow_frontend.presentation.zalopay.ZaloPayBottomSheet;
import com.example.focusflow_frontend.utils.TokenManager;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProUtils;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.*;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;

    private AuthViewModel authViewModel;
    private ImageView avatarImage;
    private String savedAvatar;
    private String fullname = "", username = "";
    private int totalPomodoro = 0, maxStreak = 0;
    private boolean isPomodoroReady = false, isStreakReady = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        avatarImage = view.findViewById(R.id.avatarImage);
        TextView usernameTextView = view.findViewById(R.id.userName);
        LinearLayout achievementLayout = view.findViewById(R.id.achievementLayout);
        Button btnUpgradePro = view.findViewById(R.id.btnUpgradePro);
        ImageView btnSettings = view.findViewById(R.id.btnSetting);

        // Auth
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getCurrentUser();
        authViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                fullname = user.getFullName();
                username = user.getUsername();
                usernameTextView.setText(fullname);

                if (user.getAvatarUrl() != null) {
                    savedAvatar = user.getAvatarUrl();
                    if (savedAvatar.startsWith("res:")) {
                        avatarImage.setImageResource(Integer.parseInt(savedAvatar.substring(4)));
                    } else if (savedAvatar.startsWith("url:")) {
                        String imageUrl = savedAvatar.substring(4);
                        Glide.with(getContext()).load(imageUrl).into(avatarImage);
                    }
                }
            }
        });

        // Pomodoro
        PomodoroViewModel pomodoroViewModel = new ViewModelProvider(this).get(PomodoroViewModel.class);
        int userId = TokenManager.getUserId(getContext());
        pomodoroViewModel.getAllPomodoro(getContext(), userId);
        pomodoroViewModel.getPomodoroList().observe(getViewLifecycleOwner(), pomodoros -> {
            if (pomodoros != null) {
                totalPomodoro = pomodoros.size();
                isPomodoroReady = true;
                tryShowBadges(achievementLayout);
            }
        });

        // Streak
        StreakViewModel streakViewModel = new ViewModelProvider(this).get(StreakViewModel.class);
        streakViewModel.getStreakByUser(userId, new StreakViewModel.StreakCallback() {
            @Override
            public void onSuccess(Streak streak) {
                maxStreak = streak.getMaxStreak();
                isStreakReady = true;
                tryShowBadges(achievementLayout);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("STREAK_ERROR", errorMessage);
                Toast.makeText(getContext(), "Failed to load streak: " + errorMessage, Toast.LENGTH_SHORT).show();
                isStreakReady = true;
                tryShowBadges(achievementLayout);
            }
        });

        // Avatar
        avatarImage.setOnClickListener(v -> showImagePickDialog());

        // Upgrade Pro
        btnUpgradePro.setVisibility(ProUtils.isProValid(getContext()) ? View.GONE : View.VISIBLE);
        btnUpgradePro.setOnClickListener(v -> openZaloPay());

        // Settings
        btnSettings.setOnClickListener(v -> openSettings());

        // Optional: Streak & score
        setStreakAndScore(view);

        return view;
    }

    private void tryShowBadges(LinearLayout layout) {
        if (isPomodoroReady && isStreakReady) {
            List<Integer> earnedBadges = getEarnedBadges(totalPomodoro, maxStreak);
            Map<Integer, String> descriptions = getBadgeDescriptions(totalPomodoro, maxStreak);
            setUserBadges(layout, earnedBadges, descriptions);
        }
    }

    private List<Integer> getEarnedBadges(int pomodoroCount, int streakCount) {
        List<Integer> badges = new ArrayList<>();
        if (pomodoroCount >= 3) badges.add(R.drawable.pomo3);
        if (pomodoroCount >= 7) badges.add(R.drawable.pomo7);
        if (pomodoroCount >= 100) badges.add(R.drawable.pomo100);
        if (streakCount >= 7) badges.add(R.drawable.badge7);
        if (streakCount >= 100) badges.add(R.drawable.badge100);
        if (streakCount >= 1000) badges.add(R.drawable.badge1000);
        return badges;
    }

    private Map<Integer, String> getBadgeDescriptions(int pomodoroCount, int streakCount) {
        Map<Integer, String> map = new HashMap<>();
        map.put(R.drawable.pomo3, pomodoroCount >= 3 ? "Congratulations! You have completed 3 Pomodoros." : "Complete 3 Pomodoros to earn this badge.");
        map.put(R.drawable.pomo7, pomodoroCount >= 7 ? "Awesome! You have finished 7 Pomodoros." : "Complete 7 Pomodoros to unlock this badge.");
        map.put(R.drawable.pomo100, pomodoroCount >= 100 ? "Incredible! You reached 100 Pomodoros." : "Complete 100 Pomodoros to get this badge.");
        map.put(R.drawable.badge7, streakCount >= 7 ? "Nice! You maintained a 7-day streak." : "Reach a 7-day streak to earn this badge.");
        map.put(R.drawable.badge100, streakCount >= 100 ? "Fantastic! You achieved a 100-day streak." : "Maintain a 100-day streak to earn this badge.");
        map.put(R.drawable.badge1000, streakCount >= 1000 ? "Legendary! You completed a 1000-day streak." : "Reach a 1000-day streak to unlock this badge.");
        return map;
    }

    private void setUserBadges(LinearLayout layout, List<Integer> earned, Map<Integer, String> descriptions) {
        List<Integer> allBadges = Arrays.asList(
                R.drawable.pomo3, R.drawable.pomo7, R.drawable.pomo100,
                R.drawable.badge7, R.drawable.badge100, R.drawable.badge1000
        );

        layout.removeAllViews();
        for (int badgeRes : allBadges) {
            ImageView img = new ImageView(getContext());
            img.setImageResource(badgeRes);
            img.setAlpha(earned.contains(badgeRes) ? 1.0f : 0.3f);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(10, 0, 10, 0);
            img.setLayoutParams(params);
            img.setAdjustViewBounds(true);
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);

            img.setOnClickListener(v -> showBadgeInfoDialog(badgeRes, descriptions.getOrDefault(badgeRes, "No description available.")));
            layout.addView(img);
        }
    }
    private void showBadgeInfoDialog(int badgeResId, String description) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_badge_info, null);

        ImageView badgeImage = dialogView.findViewById(R.id.badgeImage);
        TextView badgeDescription = dialogView.findViewById(R.id.badgeDescription);
        ImageView closeButton = dialogView.findViewById(R.id.closeButton); // Nút đóng

        badgeImage.setImageResource(badgeResId);
        badgeDescription.setText(description);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                .setView(dialogView)
                .create();

        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Nền bo góc
        dialog.show();
    }

    private void showImagePickDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Choose Avatar")
                .setItems(new CharSequence[]{"From Gallery", "Predefined Avatars"}, (dialog, which) -> {
                    if (which == 0) openGallery();
                    else showPredefinedAvatarDialog();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            // Hiển thị trước
            avatarImage.setImageURI(imageUri);
            Log.d("truoc storage", "loi");
            // Tạo reference đến Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("avatars/" + UUID.randomUUID().toString() + ".jpg");
            Log.d("Image", "onActivityResult: ");
            // Upload ảnh
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Lấy URL sau khi upload thành công
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            savedAvatar = "url:" + downloadUrl;

                            // Cập nhật avatar vào ViewModel hoặc server của bạn
                            authViewModel.updateUser(fullname, username, savedAvatar);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Upload avatar thất bại", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void showPredefinedAvatarDialog() {
        final int[] avatars = {R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3, R.drawable.avatar4};
        GridView grid = new GridView(getContext());
        grid.setNumColumns(3);
        grid.setAdapter(new ImageAdapter(getContext(), avatars));

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Select Avatar")
                .setView(grid)
                .setNegativeButton("Cancel", null)
                .create();

        grid.setOnItemClickListener((parent, view, position, id) -> {
            int resId = avatars[position];
            savedAvatar = "res:" + resId;
            avatarImage.setImageResource(resId);
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

    private void setStreakAndScore(View view) {
        ((TextView) view.findViewById(R.id.streakValue)).setText(""); // optional: bạn có thể đặt dữ liệu thật
        ((TextView) view.findViewById(R.id.scoreValue)).setText("");
    }
}
