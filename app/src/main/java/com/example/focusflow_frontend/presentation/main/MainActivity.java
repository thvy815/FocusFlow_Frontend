package com.example.focusflow_frontend.presentation.main;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.api.CreateOrder;
import com.example.focusflow_frontend.data.model.ProUpgradeRequest;
import com.example.focusflow_frontend.presentation.calendar.CalendarFragment;
import com.example.focusflow_frontend.presentation.chatbox.ChatActivity;
import com.example.focusflow_frontend.presentation.group.GroupFragment;
import com.example.focusflow_frontend.presentation.pomo.PomodoroFragment;
import com.example.focusflow_frontend.presentation.profile.ProfileFragment;
import com.example.focusflow_frontend.presentation.streak.StreakFragment;
import com.example.focusflow_frontend.utils.ApiClient;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FrameLayout assistiveButton;
    private final Handler fadeHandler = new Handler(Looper.getMainLooper());
    private Runnable fadeRunnable;
    private static final int REQUEST_POST_NOTIFICATIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        assistiveButton = findViewById(R.id.assistive_button);

        // Assistive Touch
        enableDragAndSnap(assistiveButton);

        // Notification
        createNotificationChannel();
        requestNotificationPermissionIfNeeded();
        requestExactAlarmPermissionIfNeeded();

        // Default fragment
        loadFragment(new CalendarFragment());

        // Chọn menu dưới
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (item.getItemId() == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
            } else if (item.getItemId() == R.id.nav_pomodoro) {
                selectedFragment = new PomodoroFragment();
            } else if (item.getItemId() == R.id.nav_streak) {
                selectedFragment = new StreakFragment();
            } else if (item.getItemId() == R.id.nav_group) {
                selectedFragment = new GroupFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (intent.getData() != null && "demozpdk".equals(intent.getData().getScheme())) {
            ZaloPaySDK.getInstance().onResult(intent);  // <-- Bắt buộc
            Log.d("ZaloPay", "✅ onNewIntent gọi onResult thành công");
        }
    }
    public void createAndPayOrder(String plan, String amount) {
        new Thread(() -> {
            try {
                CreateOrder orderApi = new CreateOrder();
                JSONObject data = orderApi.createOrder(amount);

                String code = data.getString("returncode");
                if (code.equals("1")) {
                    String zpToken = data.getString("zptranstoken");

                    runOnUiThread(() -> ZaloPaySDK.getInstance().payOrder(
                            MainActivity.this, zpToken, "demozpdk://app", new PayOrderListener() {
                                @Override
                                public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                                    runOnUiThread(() -> {
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Nâng cấp thành công")
                                                .setMessage("Bạn đã nâng cấp thành công gói " + plan)
                                                .setPositiveButton("OK", null)
                                                .show();

                                        long expireTime = System.currentTimeMillis() + ProUtils.getDurationInMillis(plan);
                                        ProUtils.saveProStatus(MainActivity.this, plan, expireTime);

                                        ProUpgradeRequest request = new ProUpgradeRequest(plan, expireTime);
                                        ApiClient.getProController(MainActivity.this).upgradePro(request)
                                                .enqueue(new Callback<Void>() {
                                                    @Override
                                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                                        Log.d("ProUpgrade", "Lưu Pro thành công");
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Void> call, Throwable t) {
                                                        Log.e("ProUpgrade", "API lỗi", t);
                                                    }
                                                });
                                    });
                                }

                                @Override
                                public void onPaymentCanceled(String zpTransToken, String appTransID) {
                                    runOnUiThread(() ->
                                            Toast.makeText(MainActivity.this, "Bạn đã hủy thanh toán", Toast.LENGTH_SHORT).show());
                                }

                                @Override
                                public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                                    runOnUiThread(() ->
                                            Toast.makeText(MainActivity.this, "Lỗi thanh toán: " + zaloPayError.toString(), Toast.LENGTH_SHORT).show());
                                }
                            }));
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Tạo đơn hàng thất bại!", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Lỗi tạo đơn hàng!", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }


    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "task_channel";
            CharSequence name = "TaskChannel";
            String description = "Kênh thông báo nhắc task";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_POST_NOTIFICATIONS
                );
            }
        }
    }

    private void requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent); // Người dùng sẽ vào cài đặt cấp quyền
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền thông báo.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền thông báo để nhận nhắc nhở.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableDragAndSnap(View view) {
        final float[] dX = new float[1];
        final float[] dY = new float[1];
        final int[] lastAction = new int[1];

        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dX[0] = v.getX() - event.getRawX();
                    dY[0] = v.getY() - event.getRawY();
                    lastAction[0] = MotionEvent.ACTION_DOWN;
                    cancelFadeOut(view); // Hủy mờ khi chạm
                    return true;

                case MotionEvent.ACTION_MOVE:
                    v.setX(event.getRawX() + dX[0]);
                    v.setY(event.getRawY() + dY[0]);
                    lastAction[0] = MotionEvent.ACTION_MOVE;
                    return true;

                case MotionEvent.ACTION_UP:
                    if (lastAction[0] == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(this, ChatActivity.class);
                        startActivity(intent);
                    } else {
                        snapToEdge(v);        // Tự động neo vào cạnh
                        scheduleFadeOut(v);   // Tự mờ sau khi buông tay
                    }
                    return true;

                default:
                    return false;
            }
        });
    }

    private void snapToEdge(View view) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float screenWidth = metrics.widthPixels;
        float middle = screenWidth / 2f;
        float currentX = view.getX();

        float targetX = currentX <= middle ? 0 : screenWidth - view.getWidth();

        view.animate().x(targetX).setDuration(300).start();
    }

    private void scheduleFadeOut(View view) {
        fadeRunnable = () -> view.animate().alpha(0.3f).setDuration(500).start();
        fadeHandler.postDelayed(fadeRunnable, 3000); // Mờ sau 3 giây
    }

    private void cancelFadeOut(View view) {
        fadeHandler.removeCallbacks(fadeRunnable);
        view.animate().alpha(1f).setDuration(200).start();
    }
}