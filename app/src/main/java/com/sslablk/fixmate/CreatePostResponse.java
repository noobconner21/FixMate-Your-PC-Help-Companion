package com.sslablk.fixmate;

import com.google.gson.annotations.SerializedName;

public class CreatePostResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private boolean success;
    @SerializedName("data")
    private Post data;

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public Post getData() {
        return data;
    }
}
