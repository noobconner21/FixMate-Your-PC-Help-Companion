package com.sslablk.fixmate;
import com.google.gson.annotations.SerializedName;
public class NotificationDetails {
    @SerializedName("notification_id") private String notificationId;
    @SerializedName("related_post_id") private String relatedPostId;
    @SerializedName("from_who") private String fromWho;
    @SerializedName("isRead") private boolean isRead;
    @SerializedName("created_at") private String createdAt;
    @SerializedName("post_author_id") private String postAuthorId;
    // Getters and Setters for all fields
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public String getRelatedPostId() { return relatedPostId; }
    public void setRelatedPostId(String relatedPostId) { this.relatedPostId = relatedPostId; }
    public String getFromWho() { return fromWho; }
    public void setFromWho(String fromWho) { this.fromWho = fromWho; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getPostAuthorId() { return postAuthorId; }
    public void setPostAuthorId(String postAuthorId) { this.postAuthorId = postAuthorId; }
}