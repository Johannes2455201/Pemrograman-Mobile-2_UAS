package com.johanes.mymoviefavorite.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.johanes.mymoviefavorite.R;
import com.johanes.mymoviefavorite.data.SessionManager;
import com.johanes.mymoviefavorite.data.UserDataSource;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameLayout = findViewById(R.id.layoutName);
        emailLayout = findViewById(R.id.layoutEmail);
        passwordLayout = findViewById(R.id.layoutPassword);
        nameInput = findViewById(R.id.editName);
        emailInput = findViewById(R.id.editEmail);
        passwordInput = findViewById(R.id.editPassword);
        MaterialButton registerButton = findViewById(R.id.buttonRegister);
        MaterialButton loginButton = findViewById(R.id.buttonGoLogin);

        registerButton.setOnClickListener(v -> attemptRegister());
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegister() {
        clearErrors();

        String name = getText(nameInput);
        String email = getText(emailInput);
        String password = getText(passwordInput);

        boolean valid = true;
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        if (!valid) {
            return;
        }

        UserDataSource.register(name, email, password, new UserDataSource.AuthCallback() {
            @Override
            public void onSuccess(String name, String email) {
                SessionManager.saveSession(RegisterActivity.this, name, email);
                startMain();
            }

            @Override
            public void onError(String message) {
                String text = TextUtils.isEmpty(message)
                        ? getString(R.string.error_register_failed)
                        : message;
                Toast.makeText(RegisterActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void clearErrors() {
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
    }

    private String getText(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
