package com.example.focusflow_frontend.presentation.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;

public class ResetPasswordDialogFragment extends DialogFragment {
    private static final String ARG_TOKEN = "token";
    EditText etNewPassword, etConfirmPassword;
    Button btnReset;
    String token;
    private AuthViewModel authViewModel;

    public static ResetPasswordDialogFragment newInstance(String token) {
        ResetPasswordDialogFragment fragment = new ResetPasswordDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.activity_reset_password, null);

        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnReset = view.findViewById(R.id.btn_reset_password);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        String token = getArguments().getString(ARG_TOKEN);

        btnReset.setOnClickListener(v -> {
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.resetPassword(token, newPass);
        });

        authViewModel.resetPasswordResult.observe(this, success -> {
            if (success) {
                Toast.makeText(getContext(), "Password reset successful", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                Toast.makeText(getContext(), authViewModel.errorMessage.getValue(), Toast.LENGTH_SHORT).show();
            }
        });

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setCancelable(true)
                .create();
    }
}
