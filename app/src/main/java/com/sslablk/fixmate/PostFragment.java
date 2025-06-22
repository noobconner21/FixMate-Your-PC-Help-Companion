package com.sslablk.fixmate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostFragment extends Fragment implements PostAdapter.OnPostInteractionListener {

    private static final String API_KEY = "X_API_KEY_HERE";
    private static final String TAG = "PostFragment";

    private RecyclerView postsRecyclerView;
    private TextView emptyStateTextView;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private LoadingDialog loadingDialog;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postsRecyclerView = view.findViewById(R.id.posts_recyclerview);
        emptyStateTextView = view.findViewById(R.id.empty_state_text);

        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new PostAdapter(requireContext(), postList, this);
        postsRecyclerView.setAdapter(postAdapter);

        loadingDialog = new LoadingDialog(requireContext());

        fetchUserPosts();
    }

    private void fetchUserPosts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to view posts.", Toast.LENGTH_SHORT).show();
            emptyStateTextView.setText("Please log in to view posts.");
            emptyStateTextView.setVisibility(View.VISIBLE);
            postsRecyclerView.setVisibility(View.GONE);
            return;
        }

        String userId = currentUser.getUid();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        showLoadingDialog();

        Call<PostResponse> call = apiService.getPostsByUserId(API_KEY, userId);

        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(@NonNull Call<PostResponse> call, @NonNull Response<PostResponse> response) {
                hideLoadingDialog();

                if (response.isSuccessful() && response.body() != null) {
                    PostResponse postResponse = response.body();
                    if (postResponse.isSuccess() && postResponse.getData() != null) {
                        postList.clear();
                        postList.addAll(postResponse.getData());
                        postAdapter.notifyDataSetChanged();

                        if (postList.isEmpty()) {
                            emptyStateTextView.setText("You haven't created any posts yet.");
                            emptyStateTextView.setVisibility(View.VISIBLE);
                            postsRecyclerView.setVisibility(View.GONE);
                        } else {
                            emptyStateTextView.setVisibility(View.GONE);
                            postsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(getContext(), postResponse.getMessage() != null ? postResponse.getMessage() : "Failed to fetch posts.", Toast.LENGTH_LONG).show();
                        emptyStateTextView.setText(postResponse.getMessage() != null ? postResponse.getMessage() : "Cannot find posts related to this user.");
                        emptyStateTextView.setVisibility(View.VISIBLE);
                        postsRecyclerView.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(getContext(), "Error: " + response.code() + " " + response.message(), Toast.LENGTH_LONG).show();
                    emptyStateTextView.setText("Failed to load posts. Error: " + response.code());
                    emptyStateTextView.setVisibility(View.VISIBLE);
                    postsRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PostResponse> call, @NonNull Throwable t) {
                hideLoadingDialog();
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                emptyStateTextView.setText("Network error: " + t.getMessage());
                emptyStateTextView.setVisibility(View.VISIBLE);
                postsRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    // --- OnPostInteractionListener Implementation ---
    @Override
    public void onCommentClick(Post post) {
        if (post.getPostId() != null && !post.getPostId().isEmpty()) {
            CommentListDialog commentDialog = new CommentListDialog(requireContext(), post.getPostId());
            commentDialog.show();
        } else {
            Toast.makeText(getContext(), "Cannot fetch comments: Invalid post ID.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onOptionsClick(Post post, View anchorView) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && post.getPostAuthorId().equals(currentUser.getUid())) {
            showPostOptionsMenu(post, anchorView);
        } else {
            Toast.makeText(getContext(), "You are not the author of this post.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPostOptionsMenu(Post post, View anchorView) {
        PopupMenu popup = new PopupMenu(getContext(), anchorView);
        popup.getMenuInflater().inflate(R.menu.post_options_menu, popup.getMenu());

        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceShowIcon.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error forcing icons to show in PopupMenu: " + e.getMessage());
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_edit_post) {
                    showEditPostDialog(post);
                    return true;
                } else if (id == R.id.action_delete_post) {
                    showDeleteConfirmationDialog(post);
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void showDeleteConfirmationDialog(Post post) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deletePost(post);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deletePost(Post post) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        showLoadingDialog();

        Call<DeletePostResponse> call = apiService.deletePost(API_KEY, post.getPostId());

        call.enqueue(new Callback<DeletePostResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeletePostResponse> call, @NonNull Response<DeletePostResponse> response) {
                hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    postList.remove(post);
                    postAdapter.notifyDataSetChanged();
                    if (postList.isEmpty()) {
                        emptyStateTextView.setText("You haven't created any posts yet.");
                        emptyStateTextView.setVisibility(View.VISIBLE);
                        postsRecyclerView.setVisibility(View.GONE);
                    }
                } else {
                    String errorMessage = "Failed to delete post.";
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeletePostResponse> call, @NonNull Throwable t) {
                hideLoadingDialog();
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditPostDialog(Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_post, null);
        builder.setView(dialogView);

        EditText editTitle = dialogView.findViewById(R.id.edit_post_title);
        EditText editDescription = dialogView.findViewById(R.id.edit_post_description);

        editTitle.setText(post.getPostTitle());
        editDescription.setText(post.getPostDescription());

        builder.setTitle("Edit Post")
                .setPositiveButton("Update", (dialog, id) -> {
                    String newTitle = editTitle.getText().toString().trim();
                    String newDescription = editDescription.getText().toString().trim();

                    if (TextUtils.isEmpty(newTitle) || TextUtils.isEmpty(newDescription)) {
                        Toast.makeText(getContext(), "Title and description cannot be empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newTitle.equals(post.getPostTitle()) || !newDescription.equals(post.getPostDescription())) {
                        updatePost(post.getPostId(), newTitle, newDescription);
                    } else {
                        Toast.makeText(getContext(), "No changes to update.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }

    private void updatePost(String postId, String newTitle, String newDescription) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        showLoadingDialog();

        Map<String, String> body = new HashMap<>();
        body.put("title", newTitle);
        body.put("description", newDescription);

        Call<UpdatePostResponse> call = apiService.updatePost(API_KEY, postId, body);

        call.enqueue(new Callback<UpdatePostResponse>() {
            @Override
            public void onResponse(@NonNull Call<UpdatePostResponse> call, @NonNull Response<UpdatePostResponse> response) {
                hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    fetchUserPosts();
                } else {
                    String errorMessage = "Failed to update post.";
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdatePostResponse> call, @NonNull Throwable t) {
                hideLoadingDialog();
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        fetchUserPosts();
    }
}