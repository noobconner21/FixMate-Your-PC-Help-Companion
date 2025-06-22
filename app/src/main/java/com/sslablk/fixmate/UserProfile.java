package com.sslablk.fixmate;

import com.google.gson.annotations.SerializedName;

public class UserProfile {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("user_email")
    private String userEmail;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("profile_pic")
    private String profilePicUrl;

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getCreatedAt() { return createdAt; }
    public String getProfilePicUrl() { return profilePicUrl; }
}
