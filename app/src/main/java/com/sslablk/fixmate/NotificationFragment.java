package com.sslablk.fixmate;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private static final String API_KEY = "X_API_KEY_HERE";
    private static final String TAG = "NotificationFragment";

    private RecyclerView notificationsRecyclerView;
    private TextView emptyStateTextView;
    private NotificationAdapter notificationAdapter;
    private List<AppNotification> notificationList = new ArrayList<>();
    private LoadingDialog loadingDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationsRecyclerView = view.findViewById(R.id.notifications_recyclerview);
        emptyStateTextView = view.findViewById(R.id.empty_notifications_text_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_notification_layout);

        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationAdapter = new NotificationAdapter(requireContext(), notificationList, this);
        notificationsRecyclerView.setAdapter(notificationAdapter);

        loadingDialog = new LoadingDialog(requireContext());

        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchNotifications();
        });

        fetchNotifications();
    }

    private void fetchNotifications() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to view notifications.", Toast.LENGTH_SHORT).show();
            emptyStateTextView.setText("Please log in to view notifications.");
            emptyStateTextView.setVisibility(View.VISIBLE);
            notificationsRecyclerView.setVisibility(View.GONE);
            return;
        }

        String userId = currentUser.getUid();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        if (!swipeRefreshLayout.isRefreshing()) {
            showLoadingDialog();
        }

        Call<NotificationResponse> call = apiService.getNotificationsByUserId(API_KEY, userId);

        call.enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(@NonNull Call<NotificationResponse> call, @NonNull Response<NotificationResponse> response) {
                hideLoadingDialog();
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    NotificationResponse notificationResponse = response.body();
                    if (notificationResponse.isSuccess() && notificationResponse.getData() != null) {
                        notificationList.clear();
                        notificationList.addAll(notificationResponse.getData());
                        notificationAdapter.notifyDataSetChanged();

                        if (notificationList.isEmpty()) {
                            emptyStateTextView.setText("No new notifications.");
                            notificationsRecyclerView.setVisibility(View.GONE);
                            emptyStateTextView.setVisibility(View.VISIBLE);
                        } else {
                            emptyStateTextView.setVisibility(View.GONE);
                            notificationsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(getContext(), notificationResponse.getMessage() != null ? notificationResponse.getMessage() : "Failed to fetch notifications.", Toast.LENGTH_LONG).show();
                        emptyStateTextView.setText(notificationResponse.getMessage() != null ? notificationResponse.getMessage() : "No notifications found.");
                        notificationsRecyclerView.setVisibility(View.GONE);
                        emptyStateTextView.setVisibility(View.VISIBLE);
                        Log.e(TAG, "API Response: Success=false or data is null. Message: " + notificationResponse.getMessage());
                    }
                } else {
                    Toast.makeText(getContext(), "Error: " + response.code() + " " + response.message(), Toast.LENGTH_LONG).show();
                    emptyStateTextView.setText("Failed to load notifications. Error: " + response.code());
                    notificationsRecyclerView.setVisibility(View.GONE);
                    emptyStateTextView.setVisibility(View.VISIBLE);
                    Log.e(TAG, "HTTP Error: " + response.code() + " " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error Body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<NotificationResponse> call, @NonNull Throwable t) {
                hideLoadingDialog();
                swipeRefreshLayout.setRefreshing(false);

                Log.e(TAG, "API call failed", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                emptyStateTextView.setText("Network error: " + t.getMessage());
                notificationsRecyclerView.setVisibility(View.GONE);
                emptyStateTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onNotificationClick(AppNotification notification) {
//        Toast.makeText(getContext(), "Notification clicked: " + notification.getPostTitle(), Toast.LENGTH_SHORT).show();
    }

    private void showLoadingDialog() {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchNotifications();
    }
}