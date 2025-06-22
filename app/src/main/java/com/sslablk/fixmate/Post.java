package com.sslablk.fixmate;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Post {

    @SerializedName("post_id")
    private String postId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("post_title")
    private String postTitle;

    @SerializedName("post_description")
    private String postDescription;

    @SerializedName("post_comment_count")
    private int postCommentCount;

    @SerializedName("post_react")
    private int postReact;

    @SerializedName("post_author_id")
    private String postAuthorId;

    @SerializedName("post_images")
    private List<String> postImages;

    public String getPostId() { return postId; }
    public String getCreatedAt() { return createdAt; }
    public String getPostTitle() { return postTitle; }
    public String getPostDescription() { return postDescription; }
    public int getPostCommentCount() { return postCommentCount; }
    public int getPostReact() { return postReact; }
    public String getPostAuthorId() { return postAuthorId; }
    public List<String> getPostImages() { return postImages; }
}
