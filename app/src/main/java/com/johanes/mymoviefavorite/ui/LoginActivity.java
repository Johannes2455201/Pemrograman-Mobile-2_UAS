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

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SessionManager.isLoggedIn(this)) {
            startMain();
            return;
        }
        setContentView(R.layout.activity_login);

        emailLayout = findViewById(R.id.layoutEmail);
        passwordLayout = findViewById(R.id.layoutPassword);
        emailInput = findViewById(R.id.editEmail);
        passwordInput = findViewById(R.id.editPassword);
        MaterialButton loginButton = findViewById(R.id.buttonLogin);
        MaterialButton registerButton = findViewById(R.id.buttonGoRegister);

        loginButton.setOnClickListener(v -> attemptLogin());
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        clearErrors();

        String email = getText(emailInput);
        String password = getText(passwordInput);

        boolean valid = true;
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

        UserDataSource.login(email, password, new UserDataSource.AuthCallback() {
            @Override
            public void onSuccess(String name, String email) {
                SessionManager.saveSession(LoginActivity.this, name, email);
                startMain();
            }

            @Override
            public void onError(String message) {
                String text = TextUtils.isEmpty(message)
                        ? getString(R.string.error_login_failed)
                        : message;
                Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT).show();
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
        emailLayout.setError(null);
        passwordLayout.setError(null);
    }

    private String getText(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
