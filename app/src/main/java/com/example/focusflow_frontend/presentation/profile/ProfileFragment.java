package com.example.focusflow_frontend.presentation.profile;

import android.content.Context;
import android.content.SharedPreferences;
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
                // Lấy username
                usernameTextView.setText(user.getUsername());
            }
        });

        setStreakAndCore(view);

        // Giả sử đây là danh sách badge người dùng đạt được, mới nhất nằm cuối
        List<Integer> userBadges = getUserBadges(); // Trả về danh sách drawable resource ID

        achievementLayout.removeAllViews();

// Lấy 3 badge cuối cùng, nếu chưa đủ thì lấy hết
        int startIndex = Math.max(userBadges.size() - 3, 0);

        for (int i = startIndex; i < userBadges.size(); i++) {
            ImageView imageView = new ImageView(view.getContext());

            // Lấy drawable từ badge của user
            int badgeResId = userBadges.get(i);
            imageView.setImageResource(badgeResId);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(10, 0, 10, 0);
            imageView.setLayoutParams(params);

            achievementLayout.addView(imageView);
        }


        ImageView btnSettings = view.findViewById(R.id.btnSetting);
        btnSettings.setOnClickListener(v -> settingClick());
        ImageView avtClick = view.findViewById(R.id.avatarImage);
        avtClick.setOnClickListener(v->editAvtClick());

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
    public void editAvtClick() {

        showImagePickDialog();
    }

    private void showImagePickDialog() {
        final int[] imageResIds = {
                R.drawable.avatar1,
                R.drawable.avatar2,
                R.drawable.avatar3,
                R.drawable.avatar4
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn ảnh đại diện");

        GridView gridView = new GridView(getContext());
        gridView.setNumColumns(3);
        gridView.setAdapter(new ImageAdapter(getContext(), imageResIds));

        // Biến lưu tạm ID ảnh được chọn
        final int[] selectedImageResId = { -1 };

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            // Khi người dùng chọn ảnh, lưu ID tạm
            selectedImageResId[0] = imageResIds[position];
        });

        builder.setView(gridView);

        // Nút Hủy: không làm gì, dialog đóng
        builder.setNegativeButton("Hủy", null);

        // Nút OK: cập nhật avatar nếu có ảnh được chọn
        builder.setPositiveButton("OK", (dialog, which) -> {
            if (selectedImageResId[0] != -1) {
                ImageView avtImageView = getView().findViewById(R.id.avatarImage);
                avtImageView.setImageResource(selectedImageResId[0]);
                avtImageView.setTag(selectedImageResId[0]);
            }
        });

        builder.show();
    }

    private void settingClick() {

        ImageView avatarImage = getView().findViewById(R.id.avatarImage);

        FragmentManager fragmentManager = getParentFragmentManager();
        Fragment existing = fragmentManager.findFragmentByTag("ProfileSettingBottomSheet");
        if (existing != null && existing.isAdded()) {
            fragmentManager.beginTransaction().remove(existing).commit();
        }

        // Tạo instance của BottomSheet
        ProfileSettingBottomSheet bottomSheet = new ProfileSettingBottomSheet();

        avatarImage = getView().findViewById(R.id.avatarImage);

        Bundle bundle = new Bundle();
        if (avatarImage.getTag() != null) {
            int imageResId = (int) avatarImage.getTag(); // Lấy lại ID từ tag
            bundle.putInt("imageResId", imageResId); // Truyền theo kiểu int
        }
        bottomSheet.setArguments(bundle);
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
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
        // Tạo danh sách tĩnh các badge (drawable resource)
        return Arrays.asList(
                R.drawable.badge1000,
                R.drawable.badge100,
                R.drawable.badge7
        );
    }

    public void setStreakAndCore(View view){
        TextView streak = view.findViewById(R.id.streakValue);
        TextView core = view.findViewById(R.id.core_Value);
        streak.setText("189 profile fragment");
        core.setText("tuong tu o tren");
    }

}

