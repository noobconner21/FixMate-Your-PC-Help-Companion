package com.sslablk.fixmate;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PostResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<Post> data;

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

    public List<Post> getData() { // Changed return type
        return data;
    }

    public void setData(List<Post> data) { // Changed parameter type
        this.data = data;
    }
}