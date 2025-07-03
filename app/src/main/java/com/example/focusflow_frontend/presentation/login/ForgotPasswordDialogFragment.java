package com.example.focusflow_frontend.presentation.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;

public class ForgotPasswordDialogFragment extends DialogFragment {
    private EditText emailEditText;
    private Button sendButton;
    private AuthViewModel authViewModel;
    private TextView messageTextView;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_forgot_password, null);

        emailEditText = view.findViewById(R.id.editTextEmail);
        Button sendButton = view.findViewById(R.id.buttonSend);
        messageTextView = view.findViewById(R.id.textViewMessage);

        // ViewModel
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        sendButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi API forgot-password
            authViewModel.sendForgotPasswordEmail(email);
        });

        authViewModel.forgotPasswordResult.observe(this, result -> {
            if (result != null && result) {
                messageTextView.setText("A password reset link has been sent to your email. Please check your inbox — the link will expire in 15 minutes.");
                messageTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                messageTextView.setText("Failed to send email. Please try again.");
                messageTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        });

        authViewModel.errorMessage.observe(this, message -> {
            if (message != null) {
                messageTextView.setText(message);
                messageTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        });

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setCancelable(true)
                .create();
    }
}

