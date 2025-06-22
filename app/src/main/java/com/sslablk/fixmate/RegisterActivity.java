package com.sslablk.fixmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String API_URL = "API_URL_HERE";
    private static final String API_KEY = "X_API_KEY_HERE";

    TextInputEditText editTextFullName, editTextEmail, editTextPassword, editTextConfirmPassword;
    Button btnReg;
    TextView loginText;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    private Dialog progressDialog;
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        editTextFullName =  findViewById(R.id.fullName);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.confirmPassword);
        btnReg = findViewById(R.id.signUpBtn);
        loginText = findViewById(R.id.SignInNow);

        loginText.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnReg.setOnClickListener(view -> {
            String fullName = editTextFullName.getText().toString();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(fullName)){
                editTextFullName.setError("Name is Required.");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                editTextEmail.setError("Email is Required.");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                editTextPassword.setError("Password is Required.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                editTextConfirmPassword.setError("Passwords do not Match.");
                return;
            }

            showProgressBar();

            // --- Firebase User Creation ---
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userID = mAuth.getCurrentUser().getUid();
                            sendUserDataToApi(userID, fullName, email);

                        } else {
                            hideProgressBar();
                            Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void sendUserDataToApi(String userId, String fullName, String email) {

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
            jsonBody.put("name", fullName);
            jsonBody.put("email", email);
        } catch (JSONException e) {
            hideProgressBar();
            return;
        }

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);


        // --- UPDATED Request with API Key Header ---
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("x-api-key", API_KEY)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    hideProgressBar();
                    Toast.makeText(RegisterActivity.this, "Registration failed. Please check your connection.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                runOnUiThread(() -> {
                    hideProgressBar();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        boolean success = jsonResponse.getBoolean("success");
                        String message = jsonResponse.getString("message");
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();

                        if (success) {
                            storeUserDataInFirestore(userId, fullName, email);

                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(RegisterActivity.this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
                    }
                });

                response.close();
            }
        });
    }

    private void storeUserDataInFirestore(String userId, String fullName, String email) {
        DocumentReference documentReference = fStore.collection("users").document(userId);
        Map<String, Object> user = new HashMap<>();
        user.put("fName", fullName);
        user.put("email", email);
    }


    private void showProgressBar() {
        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.progress_bar_layout);
        progressDialog.setCancelable(false);
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        progressDialog.show();
    }

    private void hideProgressBar() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
