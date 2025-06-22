package com.sslablk.fixmate;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AllPostsResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<AllPost> data; // AllPost objects

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<AllPost> getData() {
        return data;
    }

    public void setData(List<AllPost> data) {
        this.data = data;
    }
}