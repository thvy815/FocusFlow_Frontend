package com.example.focusflow_frontend.presentation.chatbox;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.api.AiController;
import com.example.focusflow_frontend.data.model.ChatBox;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.viewmodel.AiUsagesViewModel;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.presentation.zalopay.ZaloPayBottomSheet;
import com.example.focusflow_frontend.utils.ApiClient;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProStatusCallback;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProUtils;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private EditText editTextMessage;
    private Button buttonSend;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatBox> messageList = new ArrayList<>();
    private AiController aiApiService;
    private AiUsagesViewModel viewModel;

    private int messageCount = 0;
    private final int MAX_FREE_MESSAGES = 5;
    private SharedPreferences prefs;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox);

        // Thu nhỏ giao diện như dialog
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
        prefs = getSharedPreferences("chat_prefs", MODE_PRIVATE);
        checkAndResetDailyLimit();
        // Ánh xạ View
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        recyclerView = findViewById(R.id.recyclerViewChat);

        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        aiApiService = ApiClient.getRetrofit(getApplicationContext()).create(AiController.class);
        viewModel = new ViewModelProvider(this).get(AiUsagesViewModel.class);
        Log.d("ChatActivity", "ViewModel initialized");
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getCurrentUserLiveData().observe(this, user -> {
            this.currentUser = user;
        });
        authViewModel.getCurrentUser();
        buttonSend.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();
            if (message.isEmpty()) return;

            if (currentUser == null) {
                addMessage("Không thể kiểm tra trạng thái tài khoản", false);
                return;
            }
            ProUtils.isProValid(this, ApiClient.getRetrofit(this), new ProStatusCallback () {
                @Override
                public void onResult(boolean isProUser) {
                    if (isProUser) {
                        sendAiMessage(message);
                    } else {
                        authViewModel.fetchUserInfo(() -> {
                            User updatedUser = authViewModel.getCurrentUserLiveData().getValue();
                            if (updatedUser != null && updatedUser.getAiUsageCount() < MAX_FREE_MESSAGES) {
                                sendAiMessage(message);
                            } else {
                                showUpgradeDialog();
                            }
                        });
                    }
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(ChatActivity.this, "Không thể kiểm tra Pro: " + message, Toast.LENGTH_SHORT).show();
                    showUpgradeDialog(); // fallback
                }
            });

        });
        ImageView closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> finish());
    }
    private AuthViewModel authViewModel;

    private void sendAiMessage(String message) {
        buttonSend.setEnabled(false);
        addMessage(message, true);
        editTextMessage.setText("");
        addMessage("Generating your response...", false);
        callAI(message);
    }

    private void checkAndResetDailyLimit() {
        String lastDate = prefs.getString("last_date", "");
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        if (!today.equals(lastDate)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("message_count", 0);
            editor.putString("last_date", today);
            editor.apply();
        }

        messageCount = prefs.getInt("message_count", 0);
    }

    private void saveMessageCount() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("message_count", messageCount);
        editor.apply();
    }
    private void resetMessageCount() {
        messageCount = 0;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("message_count", 0);
        editor.apply();
    }

    private void callAI(String prompt) {
        Map<String, String> body = new HashMap<>();
        body.put("message", prompt);

        aiApiService.chatWithAI(body).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                removeLoadingMessage();

                if (response.isSuccessful() && response.body() != null) {
                    addMessage(response.body(), false);
                    viewModel.incrementAiUsage(getApplicationContext(), 1);
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                    } catch (Exception e) {
                        errorBody = "Error reading errorBody: " + e.getMessage();
                    }
                    addMessage("Lỗi phản hồi từ AI.\nStatus: " + response.code() + "\nError: " + errorBody, false);
                }

                buttonSend.setEnabled(true);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                removeLoadingMessage();
                addMessage("Lỗi kết nối: " + t.getMessage(), false);
                buttonSend.setEnabled(true);
            }
        });
    }

    private void removeLoadingMessage() {
        if (!messageList.isEmpty() && "Generating your response...".equals(messageList.get(messageList.size() - 1).getContent())) {
            messageList.remove(messageList.size() - 1);
            chatAdapter.notifyItemRemoved(messageList.size());
        }
    }

    private void addMessage(String text, boolean isUser) {
        messageList.add(new ChatBox(text, isUser));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    public void showUpgradeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_upgrade_pro, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        dialogView.findViewById(R.id.closeButton).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.tvNoThanks).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnUpgrade).setOnClickListener(v -> {
            dialog.dismiss();
            ZaloPayBottomSheet sheet = new ZaloPayBottomSheet();
            sheet.show(getSupportFragmentManager(), "ZaloPayBottomSheet");
        });
    }
}
