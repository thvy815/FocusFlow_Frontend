package com.example.focusflow_frontend.presentation.chatbox;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.ChatBox;

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
    private AiApiService aiApiService;
    private int messageCount = 0;
    private final int MAX_FREE_MESSAGES = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox);
        checkAndResetDailyLimit();

        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        recyclerView = findViewById(R.id.recyclerViewChat);

        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        aiApiService = RetrofitClient.getClient().create(AiApiService.class);

        buttonSend.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();

            if (message.isEmpty()) return;

            if (messageCount >= MAX_FREE_MESSAGES) {
                showUpgradeDialog();  // Hiển thị popup quảng cáo
                return;
            }

            addMessage(message, true);
            editTextMessage.setText("");
            callAI(message);
            messageCount++;
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

    private void showUpgradeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Upgrade to Pro")
                .setMessage("You have reached the limit of 5 free messages.\nUpgrade to the Pro version to continue chatting without limits.")
                .setPositiveButton("Upgrade", (dialog, which) -> {
                    // TODO: Redirect to upgrade page or open another activity
                })
                .setNegativeButton("Later", null)
                .show();
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
                if (response.isSuccessful() && response.body() != null) {
                    addMessage(response.body(), false);
                } else {
                    addMessage("Lỗi phản hồi từ AI.", false);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                addMessage("Lỗi kết nối: " + t.getMessage(), false);
            }
        });
    }
}

