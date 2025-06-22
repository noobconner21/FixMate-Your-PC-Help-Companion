package com.sslablk.fixmate;

import com.google.gson.annotations.SerializedName;

public class AuthorProfilePic {
    @SerializedName("profile_pic")
    private String profilePicUrl;

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}