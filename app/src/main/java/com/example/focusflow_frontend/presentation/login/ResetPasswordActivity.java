package com.example.focusflow_frontend.presentation.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;

public class ResetPasswordActivity extends AppCompatActivity {
    EditText etNewPassword, etConfirmPassword;
    Button btnReset;
    String token;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etNewPassword = findViewById(R.id.et_new_password);
        btnReset = findViewById(R.id.btn_reset_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        // ViewModel
        authViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(AuthViewModel.class);

        Uri data = getIntent().getData();
        if (data != null && data.getQueryParameter("token") != null) {
            token = data.getQueryParameter("token");
            Log.d("ResetPassword", "Token from deep link: " + token);
        } else {
            token = getIntent().getStringExtra("token"); // fallback nếu mở app thủ công
        }

        btnReset.setOnClickListener(v -> {
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill in both password fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }


            authViewModel.resetPassword(token, newPass);
        });

        authViewModel.resetPasswordResult.observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ResetPasswordActivity.this, SignUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(this, authViewModel.errorMessage.getValue(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
