package com.sslablk.fixmate;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AllPost {

    @SerializedName("post_id")
    private String postId;

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

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("author_name")
    private AuthorName authorName;

    @SerializedName("author_profile_pic")
    private AuthorProfilePic authorProfilePic;
    private transient boolean isDescriptionExpanded = false;

    public String getPostId() { return postId; }
    public String getPostTitle() { return postTitle; }
    public String getPostDescription() { return postDescription; }
    public int getPostCommentCount() { return postCommentCount; }
    public int getPostReact() { return postReact; }
    public String getPostAuthorId() { return postAuthorId; }
    public List<String> getPostImages() { return postImages; }
    public String getCreatedAt() { return createdAt; }
    public AuthorName getAuthorName() { return authorName; }
    public AuthorProfilePic getAuthorProfilePic() { return authorProfilePic; }

    public boolean isDescriptionExpanded() {
        return isDescriptionExpanded;
    }

    public void setDescriptionExpanded(boolean descriptionExpanded) {
        isDescriptionExpanded = descriptionExpanded;
    }


    // Setters
    public void setPostId(String postId) { this.postId = postId; }
    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }
    public void setPostDescription(String postDescription) { this.postDescription = postDescription; }
    public void setPostCommentCount(int postCommentCount) { this.postCommentCount = postCommentCount; }
    public void setPostReact(int postReact) { this.postReact = postReact; }
    public void setPostAuthorId(String postAuthorId) { this.postAuthorId = postAuthorId; }
    public void setPostImages(List<String> postImages) { this.postImages = postImages; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setAuthorName(AuthorName authorName) { this.authorName = authorName; }
    public void setAuthorProfilePic(AuthorProfilePic authorProfilePic) { this.authorProfilePic = authorProfilePic; }
}