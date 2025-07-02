package com.example.focusflow_frontend.presentation.chatbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.api.AiController;
import com.example.focusflow_frontend.data.model.ChatBox;
import com.example.focusflow_frontend.presentation.main.MainActivity;
import com.example.focusflow_frontend.presentation.zalopay.ZaloPayBottomSheet;
import com.example.focusflow_frontend.utils.ApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Callback;
import retrofit2.Call;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private EditText editTextMessage;
    private Button buttonSend;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatBox> messageList = new ArrayList<>();
    private AiController aiApiService;
    private int messageCount = 0;
    private final int MAX_FREE_MESSAGES = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox);

        // Thiết lập kích thước nhỏ nổi (giống dialog)
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }

        checkAndResetDailyLimit();

        SharedPreferences prefs = getSharedPreferences("chat_prefs", MODE_PRIVATE);
        messageCount = prefs.getInt("message_count", 0);
        Log.d("chatAI", "68");
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        recyclerView = findViewById(R.id.recyclerViewChat);

        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        aiApiService = ApiClient.getRetrofit(getApplication().getApplicationContext()).create(AiController.class);

        buttonSend.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();

            if (message.isEmpty()) return;

            Log.d("chatAI", "84");
            if (messageCount >= MAX_FREE_MESSAGES) {
                Log.d("chatAI", "86");
                showUpgradeDialog(this);  // Hiển thị popup quảng cáo
                return;
            }

            // 1. Hiển thị tin nhắn người dùng
            addMessage(message, true);
            Log.d("chatAI", "92");
            editTextMessage.setText("");

            // 2. Thêm message "Đang trả lời..."
            addMessage("Generating your response...", false);

            Log.d("chatAI", "97");
            // 3. Gọi AI
            callAI(message);
            Log.d("chatAI", "99");
            messageCount++;
            Log.d("chatAI", "100");
        });

    }
    private void checkAndResetDailyLimit() {
        SharedPreferences prefs = getSharedPreferences("chat_prefs", MODE_PRIVATE);
        String lastDate = prefs.getString("last_date", "");
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        if (!today.equals(lastDate)) {
            // Reset lại nếu là ngày mới
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("message_count", 0);
            editor.putString("last_date", today);
            editor.apply();
        }
    }
    public static void showUpgradeDialog(Activity activity) {
        if (activity == null || activity.isFinishing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_upgrade_pro, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Kiểm tra null trước khi gọi getWindow()
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();

        dialogView.findViewById(R.id.closeButton).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.tvNoThanks).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnUpgrade).setOnClickListener(v -> {
            dialog.dismiss();

            // Đảm bảo activity là AppCompatActivity trước khi ép kiểu
            if (activity instanceof AppCompatActivity) {
                ZaloPayBottomSheet sheet = new ZaloPayBottomSheet();
                sheet.setOnPlanSelectedListener((plan, amount) -> {
                    if (activity instanceof MainActivity) {
                        ((MainActivity) activity).createAndPayOrder(plan, amount);
                    }
                });
                sheet.show(((AppCompatActivity) activity).getSupportFragmentManager(), "ZaloPayBottomSheet");
            } else {
                Log.e("UpgradeDialog", "Activity is not AppCompatActivity");
            }
        });
    }

    private void addMessage(String text, boolean isUser) {
        messageList.add(new ChatBox(text, isUser));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    private void callAI(String prompt) {
        Map<String, String> body = new HashMap<>();
        body.put("message", prompt);

        aiApiService.chatWithAI(body).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                int lastIndex = messageList.size() - 1;
                if (lastIndex >= 0 && messageList.get(lastIndex).getContent().equals("Generating your response...")) {
                    messageList.remove(lastIndex);
                    chatAdapter.notifyItemRemoved(lastIndex);
                }

                if (response.isSuccessful() && response.body() != null) {
                    addMessage(response.body(), false);

                    aiApiService.incrementAiUsage().enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {}
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("ChatActivity", "Failed to update AI usage count: " + t.getMessage());
                        }
                    });

                    messageCount++;
                    SharedPreferences prefs = getSharedPreferences("chat_prefs", MODE_PRIVATE);
                    prefs.edit().putInt("message_count", messageCount).apply();
                } else {
                    addMessage("Lỗi phản hồi từ AI", false);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                int lastIndex = messageList.size() - 1;
                if (lastIndex >= 0 && messageList.get(lastIndex).getContent().equals("Generating your response...")) {
                    messageList.remove(lastIndex);
                    chatAdapter.notifyItemRemoved(lastIndex);
                }

                addMessage("Lỗi kết nối: " + t.getMessage(), false);
            }

        });
    }

}
