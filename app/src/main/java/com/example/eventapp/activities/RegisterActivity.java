package com.example.eventapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventapp.R;
import com.example.eventapp.api.ApiService;
import com.example.eventapp.models.AuthResponse;
import com.example.eventapp.models.RegisterRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.eventapp.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton registerButton;
    private TextView loginLink;
    private ProgressBar progressBar;

    @Inject
    ApiService apiService;

    @Inject
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);

        // Set up click listeners
        registerButton.setOnClickListener(v -> handleRegister());
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegister() {
        // Reset errors
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);

        // Get values
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Log registration attempt (remove in production)
        Log.d(TAG, "Attempting registration for email: " + email);

        // Validate input
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Name is required");
            focusView = nameEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            focusView = emailEditText;
            cancel = true;
        } else if (!isValidEmail(email)) {
            emailLayout.setError("Enter a valid email address");
            focusView = emailEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            focusView = passwordEditText;
            cancel = true;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            focusView = passwordEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            createAccount(name, email, password);
        }
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void createAccount(String name, String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        
        RegisterRequest request = new RegisterRequest(name, email, password);
        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Registration successful
                    Log.d(TAG, "Registration successful for email: " + email);
                    Toast.makeText(RegisterActivity.this, 
                        "Registration successful! Please login.", 
                        Toast.LENGTH_LONG).show();
                    sessionManager.saveToken(response.body().getToken());
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    // Registration failed
                    String errorMessage = response.body() != null ? response.body().getMessage() : 
                                        "Registration failed. Please try again.";
                    Log.w(TAG, "Registration failed: " + errorMessage);
                    emailLayout.setError(errorMessage);
                    emailEditText.requestFocus();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                showProgress(false);
                Log.e(TAG, "Registration error", t);
                Toast.makeText(RegisterActivity.this,
                    "Network error: " + t.getMessage(),
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!show);
        loginLink.setEnabled(!show);
    }
} 