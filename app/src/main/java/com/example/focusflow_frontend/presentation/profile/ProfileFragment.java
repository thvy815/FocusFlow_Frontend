package com.example.focusflow_frontend.presentation.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        LinearLayout achievementLayout = view.findViewById(R.id.achievementLayout);
        TextView usernameTextView = view.findViewById(R.id.userName);

        // Lấy username
        authViewModel.getCurrentUser();
        authViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                usernameTextView.setText(user.getFullName());
            }
        });

        setStreakAndScore(view);

        // Giả sử đây là danh sách badge người dùng đạt được, mới nhất nằm cuối
        List<Integer> userBadges = getUserBadges();
        achievementLayout.removeAllViews();

        int startIndex = Math.max(userBadges.size() - 3, 0);

        for (int i = startIndex; i < userBadges.size(); i++) {
            ImageView imageView = new ImageView(view.getContext());
            // Lấy drawable từ badge của user
            int badgeResId = userBadges.get(i);
            imageView.setImageResource(badgeResId);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(10, 0, 10, 0);
            imageView.setLayoutParams(params);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            achievementLayout.addView(imageView);
        }
        ImageView btnSettings = view.findViewById(R.id.btnSetting);
        btnSettings.setOnClickListener(v -> settingClick());

        ImageView avtClick = view.findViewById(R.id.avatarImage);

        avtClick.setOnClickListener(v->showImagePickDialog());

        Button btnUpgradePro = view.findViewById(R.id.btnUpgradePro);
        boolean isPro = ProUtils.isProValid(getContext());
        if (isPro) {
            btnUpgradePro.setVisibility(View.GONE); // Ẩn nếu đã Pro
        } else {
            btnUpgradePro.setVisibility(View.VISIBLE);
            btnUpgradePro.setOnClickListener(v -> {
                ZaloPayBottomSheet sheet = new ZaloPayBottomSheet();
                // Thiết lập callback khi người dùng chọn gói Pro
                sheet.setOnPlanSelectedListener((plan, amount) -> {
                    Log.d("ZaloPay", "Người dùng chọn gói: " + plan + ", amount: " + amount);

                    // Gọi hàm xử lý thanh toán từ MainActivity
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).createAndPayOrder(plan, amount);
                    }
                });

                // Hiển thị BottomSheet
                sheet.show(getParentFragmentManager(), "ZaloPayBottomSheet");
            });
        }
        Log.d("pro" , "onCreateView: isPro"+ isPro);
        return view;
    }
    private static final int PICK_IMAGE_REQUEST = 1;
    private void showImagePickDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn ảnh đại diện");

        builder.setItems(new CharSequence[]{"Chọn từ album", "Chọn từ mẫu có sẵn"}, (dialog, which) -> {
            if (which == 0) {
                openGallery();
            } else if (which == 1) {
                showPredefinedAvatarDialog(); // Hàm hiện tại của bạn
            }
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
            ImageView avatarImage = getView().findViewById(R.id.avatarImage);
            avatarImage.setImageURI(imageUri);
            avatarImage.setTag(imageUri); // Gán tag nếu bạn muốn dùng lại sau
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
            ImageView avatarImage = getView().findViewById(R.id.avatarImage);
            avatarImage.setImageResource(selectedResId);
            avatarImage.setTag(selectedResId);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void settingClick() {
        FragmentManager fragmentManager = getParentFragmentManager();
        Fragment existing = fragmentManager.findFragmentByTag("ProfileSettingBottomSheet");
        if (existing != null && existing.isAdded()) {
            fragmentManager.beginTransaction().remove(existing).commit();
        }

        ProfileSettingBottomSheet bottomSheet = new ProfileSettingBottomSheet();

        ImageView avatarImage = getView().findViewById(R.id.avatarImage);
        Bundle bundle = new Bundle();

        // Gửi avatar (nếu có)
        if (avatarImage.getTag() != null) {
            int imageResId = (int) avatarImage.getTag();
            bundle.putInt("imageResId", imageResId);
        }
        // Gửi thông tin user nếu đã có
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        if (authViewModel.getCurrentUserLiveData().getValue() != null) {
            String username = authViewModel.getCurrentUserLiveData().getValue().getUsername();
            String email = authViewModel.getCurrentUserLiveData().getValue().getEmail();

            bundle.putString("username", username);
            bundle.putString("email", email);
        }
        bottomSheet.setArguments(bundle);
        bottomSheet.show(fragmentManager, bottomSheet.getTag());
    }


    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private int[] imageResIds;

        public ImageAdapter(Context context, int[] imageResIds) {
            this.context = context;
            this.imageResIds = imageResIds;
        }

        @Override
        public int getCount() {
            return imageResIds.length;
        }

        @Override
        public Object getItem(int position) {
            return imageResIds[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                imageView = (ImageView) inflater.inflate(R.layout.item_image, parent, false);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(imageResIds[position]);
            return imageView;
        }
    }
    public List<Integer> getUserBadges() {
        return Arrays.asList(
                R.drawable.badge1000,
                R.drawable.badge100,
                R.drawable.badge7
        );
    }
    public void setStreakAndScore(View view){
        TextView streak = view.findViewById(R.id.streakValue);
        TextView score = view.findViewById(R.id.scoreValue);
        streak.setText("");
        score.setText("");
    }

}

