package com.sslablk.fixmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

public class PostImagePagerAdapter extends RecyclerView.Adapter<PostImagePagerAdapter.PostImageViewHolder> {

    private final Context context;
    private final List<String> imageUrls;
    private OnImageClickListener listener;
    public interface OnImageClickListener {
        void onImageClick(List<String> imageUrls, int clickedPosition);
    }

    public PostImagePagerAdapter(Context context, List<String> imageUrls, OnImageClickListener listener) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_image, parent, false);
        return new PostImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostImageViewHolder holder, int position) {
        String rawImageUrl = imageUrls.get(position);
        String finalImageUrl = rawImageUrl;

        if (finalImageUrl != null && !finalImageUrl.startsWith("http")) {
            if (finalImageUrl.startsWith("/")) {
                finalImageUrl = ApiClient.BASE_URL + finalImageUrl.substring(1);
            } else {
                finalImageUrl = ApiClient.BASE_URL + finalImageUrl;
            }
        }

        Glide.with(context)
                .load(finalImageUrl)
                .placeholder(R.drawable.image_icon)
                .error(R.drawable.image_icon)
                .centerCrop()
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(imageUrls, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class PostImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PostImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.post_single_image);
        }
    }
}