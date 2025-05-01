package com.example.focusflow_frontend.presentation.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.api.UserService;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.utils.ApiClient;
import retrofit2.*;

public class SignInActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    Button btnSignIn;
    UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtEmail = findViewById(R.id.et_email);
        edtPassword = findViewById(R.id.et_password);
        btnSignIn = findViewById(R.id.btn_sign_in);

        TextView tvToSignUp = findViewById(R.id.tv_sign_up);
        tvToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        userService = ApiClient.getRetrofit().create(UserService.class);

        btnSignIn.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            userService.getUser(email).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().getPassword().equals(password)) {
                            Toast.makeText(SignInActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignInActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignInActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(SignInActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
