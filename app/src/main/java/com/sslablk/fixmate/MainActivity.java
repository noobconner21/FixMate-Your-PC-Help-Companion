package com.sslablk.fixmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "X_API_KEY_HERE";
    private static final String TAG = "MainActivity";

    private MaterialToolbar mainToolbar;
    private CircleImageView toolbarProfileImage;
    private LoadingDialog loadingDialog;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingDialog = new LoadingDialog(this);
        mainToolbar = findViewById(R.id.main_toolbar);
        toolbarProfileImage = findViewById(R.id.toolbar_profile_image);
        bottomNav = findViewById(R.id.bottom_navigation);

        toolbarProfileImage.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        bottomNav.setOnItemSelectedListener(item -> {
            loadingDialog.show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    mainToolbar.setVisibility(View.VISIBLE);
                } else {
                    mainToolbar.setVisibility(View.GONE);
                    if (itemId == R.id.nav_post) {
                        selectedFragment = new PostFragment();
                    } else if (itemId == R.id.nav_add) {
                        selectedFragment = new AddFragment();
                    } else if (itemId == R.id.nav_notification) {
                        selectedFragment = new NotificationFragment();
                    } else if (itemId == R.id.nav_map) {
                        selectedFragment = new MapFragment();
                    }
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }

                loadingDialog.dismiss();

            }, 500);

            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        fetchToolbarProfileImage();
    }

    private void fetchToolbarProfileImage() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<GetUserProfileResponse> call = apiService.getUserProfile(API_KEY, userId);

        call.enqueue(new Callback<GetUserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetUserProfileResponse> call, @NonNull Response<GetUserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserProfile user = response.body().getData();
                    if (user != null && user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                        Glide.with(MainActivity.this)
                                .load(user.getProfilePicUrl())
                                .placeholder(R.drawable.profile)
                                .error(R.drawable.profile)
                                .into(toolbarProfileImage);
                    }
                } else {
                    String errorBodyString = "Unknown error";
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetUserProfileResponse> call, @NonNull Throwable t) {
            }
        });
    }
}
