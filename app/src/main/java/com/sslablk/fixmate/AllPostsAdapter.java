package com.sslablk.fixmate;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import com.sslablk.fixmate.PostImageAdapter;
import com.sslablk.fixmate.PostImageAdapter.OnImageClickListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.text.TextUtils;
import android.text.Layout;
import android.view.ViewTreeObserver;


import de.hdodenhof.circleimageview.CircleImageView;

public class AllPostsAdapter extends RecyclerView.Adapter<AllPostsAdapter.AllPostViewHolder> implements Filterable {

    private static final String TAG = "AllPostsAdapter";
    private static final int MAX_COLLAPSED_LINES = 4; // Max lines
    private final Context context;
    private List<AllPost> allPostList;
    private List<AllPost> allPostListFull;
    private OnAllPostInteractionListener listener;

    public interface OnAllPostInteractionListener {
        void onCommentClick(AllPost post);
        void onAuthorProfileClick(String authorId);
        void onPostImageClick(List<String> imageUrls, int clickedPosition);
    }

    public AllPostsAdapter(Context context, List<AllPost> allPostList, OnAllPostInteractionListener listener) {
        this.context = context;
        this.allPostList = allPostList;
        this.allPostListFull = new ArrayList<>(allPostList);
        this.listener = listener;
    }

    public void setAllPostList(List<AllPost> newAllPostList) {
        this.allPostList.clear();
        this.allPostList.addAll(newAllPostList);
        this.allPostListFull = new ArrayList<>(newAllPostList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AllPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_all_post, parent, false);
        return new AllPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllPostViewHolder holder, int position) {
        AllPost post = allPostList.get(position);

        if (post.getAuthorName() != null && post.getAuthorName().getUserName() != null) {
            holder.authorNameTextView.setText(post.getAuthorName().getUserName());
        } else {
            holder.authorNameTextView.setText("Unknown Author");
        }

        if (post.getAuthorProfilePic() != null && post.getAuthorProfilePic().getProfilePicUrl() != null && !post.getAuthorProfilePic().getProfilePicUrl().isEmpty()) {
            Glide.with(context)
                    .load(post.getAuthorProfilePic().getProfilePicUrl())
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(holder.authorProfilePicImageView);
        } else {
            holder.authorProfilePicImageView.setImageResource(R.drawable.profile);
        }

        holder.titleTextView.setText(post.getPostTitle());

        // Read More/Less Logic
        holder.descriptionTextView.setText(post.getPostDescription());
        holder.descriptionTextView.setMaxLines(Integer.MAX_VALUE);
        holder.descriptionTextView.setEllipsize(null);

        holder.descriptionTextView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                holder.descriptionTextView.getViewTreeObserver().removeOnPreDrawListener(this);

                Layout layout = holder.descriptionTextView.getLayout();
                if (layout != null) {
                    int lines = layout.getLineCount();
                    if (lines > MAX_COLLAPSED_LINES || layout.getEllipsisCount(lines - 1) > 0) {
                        holder.readMoreButton.setVisibility(View.VISIBLE);
                        if (post.isDescriptionExpanded()) {
                            holder.descriptionTextView.setMaxLines(Integer.MAX_VALUE);
                            holder.descriptionTextView.setEllipsize(null);
                            holder.readMoreButton.setText("Read Less");
                        } else {
                            holder.descriptionTextView.setMaxLines(MAX_COLLAPSED_LINES);
                            holder.descriptionTextView.setEllipsize(TextUtils.TruncateAt.END);
                            holder.readMoreButton.setText("Read More");
                        }
                    } else {
                        holder.readMoreButton.setVisibility(View.GONE);
                        holder.descriptionTextView.setMaxLines(Integer.MAX_VALUE);
                        holder.descriptionTextView.setEllipsize(null);
                    }
                }
                return true;
            }
        });

        holder.readMoreButton.setOnClickListener(v -> {
            post.setDescriptionExpanded(!post.isDescriptionExpanded());
            notifyItemChanged(holder.getAdapterPosition());
        });


        holder.dateTextView.setText(formatDate(post.getCreatedAt()));

        if (post.getPostImages() != null && !post.getPostImages().isEmpty()) {
            holder.postImagesViewPager.setVisibility(View.VISIBLE);
            PostImageAdapter imageAdapter = new PostImageAdapter(
                    context,
                    post.getPostImages(),
                    (imageUrls, clickedPosition) -> {
                        if (listener != null) {
                            listener.onPostImageClick(imageUrls, clickedPosition);
                        }
                    }
            );
            holder.postImagesViewPager.setAdapter(imageAdapter);
        } else {
            holder.postImagesViewPager.setVisibility(View.GONE);
        }

        holder.commentButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCommentClick(post);
            }
        });

        holder.authorNameTextView.setOnClickListener(v -> {
            if (listener != null && post.getPostAuthorId() != null) {
                listener.onAuthorProfileClick(post.getPostAuthorId());
            }
        });
        holder.authorProfilePicImageView.setOnClickListener(v -> {
            if (listener != null && post.getPostAuthorId() != null) {
                listener.onAuthorProfileClick(post.getPostAuthorId());
            }
        });

        holder.commentCountTextView.setText(String.valueOf(post.getPostCommentCount()));
        holder.reactCountTextView.setText(String.valueOf(post.getPostReact()));

        if (holder.reactButton != null) {
            holder.reactButton.setOnClickListener(v -> {
            });
        }
    }

    @Override
    public int getItemCount() {
        return allPostList.size();
    }

    public static class AllPostViewHolder extends RecyclerView.ViewHolder {
        CircleImageView authorProfilePicImageView;
        TextView authorNameTextView;
        ViewPager2 postImagesViewPager;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView readMoreButton;
        TextView dateTextView;
        ImageView commentButton;
        TextView commentCountTextView;
        TextView reactCountTextView;
        ImageView reactButton;

        public AllPostViewHolder(@NonNull View itemView) {
            super(itemView);
            authorProfilePicImageView = itemView.findViewById(R.id.author_profile_pic);
            authorNameTextView = itemView.findViewById(R.id.author_name);
            postImagesViewPager = itemView.findViewById(R.id.post_images_viewpager);

            titleTextView = itemView.findViewById(R.id.all_post_title);
            descriptionTextView = itemView.findViewById(R.id.all_post_description);
            readMoreButton = itemView.findViewById(R.id.read_more_button);
            dateTextView = itemView.findViewById(R.id.all_post_date);
            commentButton = itemView.findViewById(R.id.all_post_comment_button);
            commentCountTextView = itemView.findViewById(R.id.all_post_comment_count);
            reactButton = itemView.findViewById(R.id.all_post_react_button);
            reactCountTextView = itemView.findViewById(R.id.all_post_react_count);
        }
    }

    private String formatDate(String isoDateString) {
        Log.d(TAG, "Raw ISO Date String received: " + isoDateString);

        if (isoDateString == null || isoDateString.isEmpty()) {
            return "N/A Date";
        }

        String processedDateString = isoDateString;


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
            return "Parse Error Date";
        } catch (Exception e) {
            return "Error Date";
        }
    }

    @Override
    public Filter getFilter() {
        return allPostsFilter;
    }

    private Filter allPostsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<AllPost> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(allPostListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (AllPost item : allPostListFull) {
                    boolean matches = false;
                    if (item.getPostTitle() != null && item.getPostTitle().toLowerCase().contains(filterPattern)) {
                        matches = true;
                    } else if (item.getPostDescription() != null && item.getPostDescription().toLowerCase().contains(filterPattern)) {
                        matches = true;
                    } else if (item.getAuthorName() != null && item.getAuthorName().getUserName() != null && item.getAuthorName().getUserName().toLowerCase().contains(filterPattern)) {
                        matches = true;
                    }

                    if (matches) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            allPostList.clear();
            if (results.values instanceof List<?>) {
                allPostList.addAll((List<AllPost>) results.values);
            }
            notifyDataSetChanged();
        }
    };
}