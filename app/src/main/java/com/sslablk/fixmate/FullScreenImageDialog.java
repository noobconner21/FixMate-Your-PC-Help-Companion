package com.sslablk.fixmate;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

public class FullScreenImageDialog extends Dialog {

    private String imageUrl;
    private ImageView fullScreenImageView;

    public FullScreenImageDialog(@NonNull Context context, String imageUrl) {
        super(context);
        this.imageUrl = imageUrl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_full_screen_image);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        getWindow().setGravity(Gravity.CENTER);

        fullScreenImageView = findViewById(R.id.full_screen_image_view);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .fitCenter()
                    .placeholder(R.drawable.image_icon)
                    .error(R.drawable.image_icon)
                    .into(fullScreenImageView);
        } else {
            fullScreenImageView.setImageResource(R.drawable.image_icon);
        }

        fullScreenImageView.setOnClickListener(v -> dismiss());
    }
}