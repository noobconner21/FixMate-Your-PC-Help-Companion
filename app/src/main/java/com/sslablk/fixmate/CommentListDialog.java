package com.sslablk.fixmate;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap; // For Map<String, String> body
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentListDialog extends Dialog {

    private static final String API_KEY = "X_API_KEY_HERE";
    private static final String TAG = "CommentListDialog";

    private String postId;
    private RecyclerView commentsRecyclerView;
    private TextView emptyCommentsTextView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private LoadingDialog loadingDialog;
    private EditText commentEditText;
    private ImageView sendCommentButton;

    public CommentListDialog(@NonNull Context context, String postId) {
        super(context);
        this.postId = postId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_comment_list);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lp);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setGravity(Gravity.BOTTOM);

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        commentsRecyclerView = findViewById(R.id.comments_recyclerview);
        emptyCommentsTextView = findViewById(R.id.empty_comments_text_view);
        ImageView closeButton = findViewById(R.id.close_comments_button);
        commentEditText = findViewById(R.id.comment_edit_text);
        sendCommentButton = findViewById(R.id.send_comment_button);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new CommentAdapter(getContext(), commentList);
        commentsRecyclerView.setAdapter(commentAdapter);
        loadingDialog = new LoadingDialog(getContext());
        closeButton.setOnClickListener(v -> dismiss());
        sendCommentButton.setOnClickListener(v -> addComment());
        fetchComments();
    }

    private void fetchComments() {
        if (postId == null || postId.isEmpty()) {
            emptyCommentsTextView.setText("Invalid Post ID.");
            emptyCommentsTextView.setVisibility(View.VISIBLE);
            commentsRecyclerView.setVisibility(View.GONE);
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        showLoadingDialog();

        Call<CommentResponse> call = apiService.getCommentsByPostId(API_KEY, postId);

        call.enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(@NonNull Call<CommentResponse> call, @NonNull Response<CommentResponse> response) {
                hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    commentList.clear();
                    if (response.body().getData() != null) {
                        commentList.addAll(response.body().getData());
                    }
                    commentAdapter.notifyDataSetChanged();

                    if (commentList.isEmpty()) {
                        emptyCommentsTextView.setText("No comments yet. Be the first to comment!");
                        emptyCommentsTextView.setVisibility(View.VISIBLE);
                        commentsRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyCommentsTextView.setVisibility(View.GONE);
                        commentsRecyclerView.setVisibility(View.VISIBLE);
                        commentsRecyclerView.scrollToPosition(commentList.size() - 1);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load comments: " + response.message(), Toast.LENGTH_SHORT).show();
                    emptyCommentsTextView.setText("Failed to load comments.");
                    emptyCommentsTextView.setVisibility(View.VISIBLE);
                    commentsRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommentResponse> call, @NonNull Throwable t) {
                hideLoadingDialog();
                Toast.makeText(getContext(), "Network error loading comments.", Toast.LENGTH_SHORT).show();
                emptyCommentsTextView.setText("Network error loading comments.");
                emptyCommentsTextView.setVisibility(View.VISIBLE);
                commentsRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    //  Method to add a comment
    private void addComment() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to comment.", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentContent = commentEditText.getText().toString().trim();
        if (TextUtils.isEmpty(commentContent)) {
            Toast.makeText(getContext(), "Comment cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentorId = currentUser.getUid();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        showLoadingDialog();

        Map<String, String> body = new HashMap<>();
        body.put("commentor_id", commentorId);
        body.put("comment_content", commentContent);

        Call<AddCommentResponse> call = apiService.addCommentToPost(API_KEY, postId, body);

        call.enqueue(new Callback<AddCommentResponse>() {
            @Override
            public void onResponse(@NonNull Call<AddCommentResponse> call, @NonNull Response<AddCommentResponse> response) {
                hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    commentEditText.setText("");
                    hideKeyboard(commentEditText);

                    // After successful comment, re-fetch all comments to update the list
                    fetchComments();
                } else {
                    String errorMessage = "Failed to add comment.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMessage = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            errorMessage = "Error: " + response.errorBody().string();
                        } catch (Exception e) {
                        }
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddCommentResponse> call, @NonNull Throwable t) {
                hideLoadingDialog();
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
    public void dismiss() {
        super.dismiss();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}