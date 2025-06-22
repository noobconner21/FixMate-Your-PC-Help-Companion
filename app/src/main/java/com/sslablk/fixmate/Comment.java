package com.sslablk.fixmate;

import com.google.gson.annotations.SerializedName;

public class Comment {
    @SerializedName("commentor_name")
    private String commentorName;

    @SerializedName("commentor_profile_pic")
    private String commentorProfilePic;

    @SerializedName("comment_id")
    private String commentId;

    @SerializedName("comment")
    private String commentText;

    // Getters and Setters
    public String getCommentorName() {
        return commentorName;
    }

    public void setCommentorName(String commentorName) {
        this.commentorName = commentorName;
    }

    public String getCommentorProfilePic() {
        return commentorProfilePic;
    }

    public void setCommentorProfilePic(String commentorProfilePic) {
        this.commentorProfilePic = commentorProfilePic;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
}