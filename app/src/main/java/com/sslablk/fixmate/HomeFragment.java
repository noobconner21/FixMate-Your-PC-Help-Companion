package com.sslablk.fixmate;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sslablk.fixmate.AllPostsAdapter;
import com.sslablk.fixmate.CommentListDialog;
import com.sslablk.fixmate.FullScreenImageDialog;
import com.sslablk.fixmate.PostImageAdapter;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements AllPostsAdapter.OnAllPostInteractionListener {

    private static final String API_KEY = "X_API_KEY_HERE";
    private static final String TAG = "HomeFragment";

    private RecyclerView allPostsRecyclerView;
    private TextView emptyStateTextView;
    private AllPostsAdapter allPostsAdapter;
    private List<AllPost> allPostList = new ArrayList<>();
    private LoadingDialog loadingDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TextInputEditText searchEditText;
    private MaterialButton searchButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allPostsRecyclerView = view.findViewById(R.id.all_posts_recyclerview);
        emptyStateTextView = view.findViewById(R.id.empty_state_all_posts_text);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);

        allPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        allPostsAdapter = new AllPostsAdapter(requireContext(), allPostList, this);
        allPostsRecyclerView.setAdapter(allPostsAdapter);

        loadingDialog = new LoadingDialog(requireContext());

        swipeRefreshLayout.setOnRefreshListener(() -> {
            searchEditText.setText("");
            fetchAllPosts();
        });

        searchButton.setOnClickListener(v -> performSearch());

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    event != null && event.getAction() == android.view.KeyEvent.ACTION_DOWN && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER) {
                performSearch();
                return true;
            }
            return false;
        });

        fetchAllPosts();
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        allPostsAdapter.getFilter().filter(query);

        View currentFocus = requireActivity().getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    private void fetchAllPosts() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        if (!swipeRefreshLayout.isRefreshing()) {
            showLoadingDialog();
        }

        Call<AllPostsResponse> call = apiService.getAllPosts(API_KEY);

        call.enqueue(new Callback<AllPostsResponse>() {
            @Override
            public void onResponse(@NonNull Call<AllPostsResponse> call, @NonNull Response<AllPostsResponse> response) {
                hideLoadingDialog();
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    AllPostsResponse allPostsResponse = response.body();
                    if (allPostsResponse.isSuccess() && allPostsResponse.getData() != null) {
                        allPostsAdapter.setAllPostList(allPostsResponse.getData());
                        updateEmptyStateVisibility(allPostsAdapter.getItemCount(), "No posts available at the moment.");

                    } else {
                        Toast.makeText(getContext(), allPostsResponse.getMessage() != null ? allPostsResponse.getMessage() : "Failed to fetch all posts.", Toast.LENGTH_LONG).show();
                        updateEmptyStateVisibility(0, allPostsResponse.getMessage() != null ? allPostsResponse.getMessage() : "No posts found.");
                    }
                } else {
                    Toast.makeText(getContext(), "Error: " + response.code() + " " + response.message(), Toast.LENGTH_LONG).show();
                    updateEmptyStateVisibility(0, "Failed to load posts. Error: " + response.code());

                }
            }

            @Override
            public void onFailure(@NonNull Call<AllPostsResponse> call, @NonNull Throwable t) {
                hideLoadingDialog();
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyStateVisibility(0, "Network error: " + t.getMessage());
            }
        });
    }

    private void updateEmptyStateVisibility(int itemCount, String defaultMessage) {
        if (itemCount == 0) {
            String currentQuery = searchEditText.getText().toString().trim();
            if (!currentQuery.isEmpty()) {
                emptyStateTextView.setText("No posts found matching '" + currentQuery + "'.");
            } else {
                emptyStateTextView.setText(defaultMessage);
            }
            emptyStateTextView.setVisibility(View.VISIBLE);
            allPostsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.GONE);
            allPostsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCommentClick(AllPost post) {
        if (post.getPostId() != null && !post.getPostId().isEmpty()) {
            CommentListDialog commentDialog = new CommentListDialog(requireContext(), post.getPostId());
            commentDialog.show();
        } else {
            Toast.makeText(getContext(), "Cannot fetch comments: Invalid post ID.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAuthorProfileClick(String authorId) {
        Toast.makeText(getContext(), "View profile of author ID: " + authorId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPostImageClick(List<String> imageUrls, int clickedPosition) {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String imageUrlToDisplay = imageUrls.get(clickedPosition);

            if (imageUrlToDisplay != null && !imageUrlToDisplay.startsWith("http")) {
                if (imageUrlToDisplay.startsWith("/")) {
                    imageUrlToDisplay = ApiClient.BASE_URL + imageUrlToDisplay.substring(1);
                } else {
                    imageUrlToDisplay = ApiClient.BASE_URL + imageUrlToDisplay;
                }
            }

            FullScreenImageDialog dialog = new FullScreenImageDialog(requireContext(), imageUrlToDisplay);
            dialog.show();
        }
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
        if (searchEditText != null) {
            searchEditText.setText("");
        }
        fetchAllPosts();
    }
}