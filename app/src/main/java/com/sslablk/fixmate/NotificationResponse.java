package com.sslablk.fixmate;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NotificationResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<AppNotification> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<AppNotification> getData() {
        return data;
    }

    public void setData(List<AppNotification> data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}