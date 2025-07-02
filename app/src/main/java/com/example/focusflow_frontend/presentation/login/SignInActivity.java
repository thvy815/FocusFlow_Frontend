package com.example.focusflow_frontend.presentation.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.ProStatusResponse;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.presentation.main.MainActivity;
import com.example.focusflow_frontend.utils.ApiClient;
import com.example.focusflow_frontend.utils.TokenManager;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    CheckBox cbRememberMe;
    Button btnSignIn;
    AuthViewModel viewModel;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("410988307717-aitcia4ljjebo74lhehmj7t9ol6tuhmo.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Nếu token còn thời hạn thì cho phép vào app luôn (không cần đăng nhập)
        if (TokenManager.isRememberMe(this) && TokenManager.getToken(this) != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_sign_in);

        edtEmail = findViewById(R.id.et_email);
        edtPassword = findViewById(R.id.et_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        btnSignIn = findViewById(R.id.btn_sign_in);
        LinearLayout googleLogin = findViewById(R.id.btnGoogleLogin);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);

        // Forgot Password
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Sign in Google
        googleLogin.setOnClickListener(view -> {
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        });

        // Chuyển hướng Sign Up
        TextView tvToSignUp = findViewById(R.id.tv_sign_up);
        tvToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // ViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Sign In
        btnSignIn.setOnClickListener(v -> {
            String usernameOrEmail = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            boolean rememberMe = cbRememberMe.isChecked();
            viewModel.signIn(usernameOrEmail, password, rememberMe);
        });

        // Dữ liệu đăng nhập đúng
        viewModel.signInResult.observe(this, result -> {
            TokenManager.saveToken(this, result.getToken());
            TokenManager.saveUserId(this, result.getUserId());

            // Lưu thêm vào SharedPreferences nếu chưa có
            SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
            prefs.edit().putString("token", result.getToken()).apply();

            // Nếu rememberMe, lưu thêm flag
            if (cbRememberMe.isChecked()) {
                TokenManager.saveRememberMe(this, true);
            } else {
                TokenManager.saveRememberMe(this, false);
            }

            Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
            // Reset Retrofit để sử dụng token mới
            ApiClient.resetRetrofit();
            // Gọi API kiểm tra trạng thái Pro sau khi đăng nhập thành công
            ApiClient.getProController(this).getProStatus().enqueue(new Callback<ProStatusResponse>() {
                @Override
                public void onResponse(Call<ProStatusResponse> call, Response<ProStatusResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ProStatusResponse status = response.body();

                        if (status.isPro() && System.currentTimeMillis() < status.getExpireTime()) {
                            ProUtils.saveProStatus(SignInActivity.this, status.getPlanName(), status.getExpireTime());
                            Log.d("ProStatus", "Đã lưu trạng thái Pro: " + status.getPlanName());
                        } else {
                            ProUtils.clearProStatus(SignInActivity.this);
                            Log.d("ProStatus", "Không phải Pro hoặc đã hết hạn");
                        }
                    } else {
                        Log.e("ProStatus", "Lỗi khi gọi /api/pro/status: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ProStatusResponse> call, Throwable t) {
                    Log.e("ProStatus", "Lỗi mạng khi gọi /api/pro/status", t);
                }
            });

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // Dữ liệu đăng nhập sai
        viewModel.errorMessage.observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                viewModel.signInWithGoogle(idToken);
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In thất bại", Toast.LENGTH_SHORT).show();
                Log.e("GoogleSignIn", "Lỗi: " + e.getStatusCode(), e);
            }
        }
    }
}
