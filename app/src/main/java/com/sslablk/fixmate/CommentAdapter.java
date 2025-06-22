package com.sslablk.fixmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context context;
    private List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    public void setCommentList(List<Comment> newCommentList) {
        this.commentList = newCommentList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.commentorNameTextView.setText(comment.getCommentorName());
        holder.commentTextView.setText(comment.getCommentText());

        if (comment.getCommentorProfilePic() != null && !comment.getCommentorProfilePic().isEmpty()) {
            Glide.with(context)
                    .load(comment.getCommentorProfilePic())
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(holder.commentorProfilePicImageView);
        } else {
            holder.commentorProfilePicImageView.setImageResource(R.drawable.profile);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView commentorProfilePicImageView;
        TextView commentorNameTextView;
        TextView commentTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentorProfilePicImageView = itemView.findViewById(R.id.commentor_profile_pic);
            commentorNameTextView = itemView.findViewById(R.id.commentor_name);
            commentTextView = itemView.findViewById(R.id.comment_text);
        }
    }
}