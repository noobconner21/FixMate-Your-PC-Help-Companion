package com.sslablk.fixmate;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime; // For API 26+
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.time.format.DateTimeParseException;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private static final String TAG = "PostAdapter";
    private final Context context;
    private List<Post> postList;
    private OnPostInteractionListener listener;

    public interface OnPostInteractionListener {
        void onCommentClick(Post post);
        void onOptionsClick(Post post, View anchorView);
    }

    public PostAdapter(Context context, List<Post> postList, OnPostInteractionListener listener) {
        this.context = context;
        this.postList = postList;
        this.listener = listener;
    }

    // Method to update data
    public void setPostList(List<Post> newPostList) {
        this.postList = newPostList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.titleTextView.setText(post.getPostTitle());
        holder.descriptionTextView.setText(post.getPostDescription());
        holder.dateTextView.setText(formatDate(post.getCreatedAt()));
        holder.commentCountTextView.setText(String.valueOf(post.getPostCommentCount()));


        if (post.getPostImages() != null && !post.getPostImages().isEmpty()) {
            Glide.with(context)
                    .load(post.getPostImages().get(0))
                    .placeholder(R.drawable.image_icon)
                    .error(R.drawable.image_icon)
                    .centerCrop()
                    .into(holder.postImageView);
            holder.postImageView.setVisibility(View.VISIBLE);
        } else {
            holder.postImageView.setVisibility(View.GONE);
        }


        holder.commentButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCommentClick(post);
            }
        });

        holder.optionsButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOptionsClick(post, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView dateTextView;
        ImageView commentButton;
        TextView commentCountTextView;
        ImageView optionsButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.post_image);
            titleTextView = itemView.findViewById(R.id.post_title);
            descriptionTextView = itemView.findViewById(R.id.post_description);
            dateTextView = itemView.findViewById(R.id.post_date);
            commentButton = itemView.findViewById(R.id.comment_button);
            commentCountTextView = itemView.findViewById(R.id.comment_count);
            optionsButton = itemView.findViewById(R.id.options_button);
        }
    }

    // Date formatting helper
    private String formatDate(String isoDateString) {
        Log.d(TAG, "Raw ISO Date String received: " + isoDateString);

        if (isoDateString == null || isoDateString.isEmpty()) {
            return "N/A Date";
        }

        String processedDateString = isoDateString;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(isoDateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM dd,yyyy 'at' hh:mm a", Locale.getDefault());
                return odt.format(outputFormatter);
            } catch (DateTimeParseException e) {
                Log.e(TAG, "DateTimeParseException (API 26+) during date formatting for: '" + isoDateString + "'", e);
            }
        }

        try {
            int dotIndex = processedDateString.indexOf('.');
            if (dotIndex != -1) {
                int plusMinusZIndex = -1;
                for (int i = dotIndex + 1; i < processedDateString.length(); i++) {
                    char c = processedDateString.charAt(i);
                    if (c == '+' || c == '-' || c == 'Z') {
                        plusMinusZIndex = i;
                        break;
                    }
                }
                if (plusMinusZIndex != -1) {
                    String fractionalSecondsPart = processedDateString.substring(dotIndex + 1, plusMinusZIndex);
                    if (fractionalSecondsPart.length() > 3) {
                        processedDateString = processedDateString.substring(0, dotIndex + 1 + 3) +
                                processedDateString.substring(plusMinusZIndex);
                    }
                }
            }

            if (processedDateString.matches(".*[+-]\\d{2}:\\d{2}$")) {
                int lastColonIndex = processedDateString.lastIndexOf(':');
                processedDateString = processedDateString.substring(0, lastColonIndex) +
                        processedDateString.substring(lastColonIndex + 1);
            }
            else if (processedDateString.endsWith("Z")) {
                processedDateString = processedDateString.replace("Z", "+0000");
            }

            Log.d(TAG, "Processed Date String for SimpleDateFormat: " + processedDateString);

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = inputFormat.parse(processedDateString);

            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd,yyyy 'at' hh:mm a", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getDefault());

            return outputFormat.format(date);

        } catch (ParseException e) {
            Log.e(TAG, "SimpleDateFormat ParseException for: '" + isoDateString + "' Processed: '" + processedDateString + "'", e);
            return "Parse Error Date";
        } catch (Exception e) {
            Log.e(TAG, "Generic Exception in formatDate for: '" + isoDateString + "'", e);
            return "Error Date";
        }
    }
}