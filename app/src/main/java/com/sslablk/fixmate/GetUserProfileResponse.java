package com.sslablk.fixmate;

import com.google.gson.annotations.SerializedName;

public class GetUserProfileResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private UserProfile data;

    public String getMessage() { return message; }
    public boolean isSuccess() { return success; }
    public UserProfile getData() { return data; }
}
