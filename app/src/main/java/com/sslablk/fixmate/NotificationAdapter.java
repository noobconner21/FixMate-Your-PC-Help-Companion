package com.sslablk.fixmate;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;


import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private static final String TAG = "NotificationAdapter";
    private final Context context;
    private List<AppNotification> notificationList;
    private OnNotificationClickListener listener;


    public interface OnNotificationClickListener {
        void onNotificationClick(AppNotification notification);
    }

    public NotificationAdapter(Context context, List<AppNotification> notificationList, OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    public void setNotificationList(List<AppNotification> newNotificationList) {
        this.notificationList = newNotificationList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        AppNotification notification = notificationList.get(position);

        if (holder.commentorProfilePic == null) {
            Log.e(TAG, "ERROR: commentorProfilePic ImageView is NULL for notification position: " + position);
        } else if (notification.getCommentorProfilePic() != null && !notification.getCommentorProfilePic().isEmpty()) {
            Glide.with(context)
                    .load(notification.getCommentorProfilePic())
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(holder.commentorProfilePic);
        } else {
            holder.commentorProfilePic.setImageResource(R.drawable.profile);
        }

        if (holder.notificationMessage == null) {
            Log.e(TAG, "ERROR: notificationMessage TextView is NULL for notification position: " + position);
        } else {
            holder.notificationMessage.setText(notification.getContent());
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        CircleImageView commentorProfilePic;
        TextView notificationMessage;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            commentorProfilePic = itemView.findViewById(R.id.notification_commentor_profile_pic);
            notificationMessage = itemView.findViewById(R.id.notification_message);
        }
    }
}