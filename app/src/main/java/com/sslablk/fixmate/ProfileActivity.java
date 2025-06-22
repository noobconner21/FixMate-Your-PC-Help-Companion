package com.sslablk.fixmate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String API_KEY = "X_API_KEY_HERE";
    private static final String TAG = "ProfileActivity";
    private static final String GITHUB_PROFILE_URL = "https://github.com/noobconner21";

    private CircleImageView profileImageView;
    private TextView userNameTextView, userEmailTextView;
    private LoadingDialog loadingDialog;
    private UserProfile currentUserProfile;

    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;
    private Uri selectedImageUri = null;
    private CircleImageView dialogProfileImage;

    private TextView developerOption;
    private TextView aboutOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        if (dialogProfileImage != null) {
                            Glide.with(this).load(uri).into(dialogProfileImage);
                        }
                    }
                });

        loadingDialog = new LoadingDialog(this);
        profileImageView = findViewById(R.id.profile_image);
        userNameTextView = findViewById(R.id.user_name);
        userEmailTextView = findViewById(R.id.user_email);

        MaterialToolbar toolbar = findViewById(R.id.profile_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton editProfileButton = findViewById(R.id.edit_profile_button);
        editProfileButton.setOnClickListener(v -> showEditProfileDialog());

        TextView logoutOption = findViewById(R.id.logout_option);
        logoutOption.setOnClickListener(v -> logoutUser());

        developerOption = findViewById(R.id.developer);
        developerOption.setOnClickListener(v -> openDeveloperGitHub());

        aboutOption = findViewById(R.id.about_option);
        aboutOption.setOnClickListener(v -> showAboutDialog());


        fetchUserProfile();
    }

    private void fetchUserProfile() {
        loadingDialog.show();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            loadingDialog.dismiss();
            finish();
            return;
        }
        String userId = currentUser.getUid();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<GetUserProfileResponse> call = apiService.getUserProfile(API_KEY, userId);

        call.enqueue(new Callback<GetUserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetUserProfileResponse> call, @NonNull Response<GetUserProfileResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentUserProfile = response.body().getData();
                    populateUI(currentUserProfile);
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to fetch profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetUserProfileResponse> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(UserProfile user) {
        if (user == null) return;

        userNameTextView.setText(user.getUserName());
        userEmailTextView.setText(user.getUserEmail());

        Glide.with(this)
                .load(user.getProfilePicUrl())
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(profileImageView);
    }

    private void showEditProfileDialog() {
        if (currentUserProfile == null) {
            Toast.makeText(this, "Profile data not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user_profile, null);
        builder.setView(dialogView);

        dialogProfileImage = dialogView.findViewById(R.id.edit_profile_image);
        TextView changePhotoText = dialogView.findViewById(R.id.change_photo_text);
        TextInputEditText editUserName = dialogView.findViewById(R.id.edit_user_name);
        Button saveButton = dialogView.findViewById(R.id.save_changes_button);

        editUserName.setText(currentUserProfile.getUserName());
        Glide.with(this)
                .load(currentUserProfile.getProfilePicUrl())
                .placeholder(R.drawable.profile)
                .into(dialogProfileImage);

        final AlertDialog dialog = builder.create();

        changePhotoText.setOnClickListener(v ->
                photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );

        saveButton.setOnClickListener(v -> {
            String newName = editUserName.getText().toString().trim();
            if (TextUtils.isEmpty(newName)) {
                Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            performProfileUpdate(newName);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void performProfileUpdate(String newName) {
        loadingDialog.show();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            loadingDialog.dismiss();
            return;
        }

        RequestBody userIdPart = RequestBody.create(currentUser.getUid(), MediaType.parse("text/plain"));
        RequestBody namePart = RequestBody.create(newName, MediaType.parse("text/plain"));
        MultipartBody.Part imagePart;

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image to update.", Toast.LENGTH_SHORT).show();
            loadingDialog.dismiss();
            return;
        }

        try {
            imagePart = prepareFilePart("image", selectedImageUri);
        } catch (Exception e) {
            loadingDialog.dismiss();
            Toast.makeText(this, "Error preparing image.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<UpdateUserProfileResponse> call = apiService.updateUserProfile(API_KEY, userIdPart, namePart, imagePart);

        call.enqueue(new Callback<UpdateUserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UpdateUserProfileResponse> call, @NonNull Response<UpdateUserProfileResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    fetchUserProfile();
                    updateToolbarImageInMainActivity(response.body().getData().getProfilePicUrl());
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdateUserProfileResponse> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateToolbarImageInMainActivity(String imageUrl) {
        Intent intent = new Intent("com.sslablk.fixmate.PROFILE_UPDATED");
        intent.putExtra("new_image_url", imageUrl);
        sendBroadcast(intent);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) throws Exception {
        File file = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
        try (InputStream inputStream = getContentResolver().openInputStream(fileUri);
             OutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        }

        RequestBody requestFile = RequestBody.create(file,
                MediaType.parse(Objects.requireNonNull(getContentResolver().getType(fileUri)))
        );
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(ProfileActivity.this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openDeveloperGitHub() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_PROFILE_URL));
        try {
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(this, "No browser found to open the link.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to show a simple About dialog
    private void showAboutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.profile_about_option)
                .setMessage(R.string.about)
                .setPositiveButton("OK", null) // "OK" button to dismiss
                .create(); // Create the dialog

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
}