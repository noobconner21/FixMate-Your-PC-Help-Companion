package com.sslablk.fixmate;
import com.google.gson.annotations.SerializedName;
public class AppNotification {
    @SerializedName("notification_id") private String notificationId;
    @SerializedName("content") private String content;
    @SerializedName("commentor_profile_pic") private String commentorProfilePic;
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCommentorProfilePic() { return commentorProfilePic; }
    public void setCommentorProfilePic(String commentorProfilePic) { this.commentorProfilePic = commentorProfilePic; }
}