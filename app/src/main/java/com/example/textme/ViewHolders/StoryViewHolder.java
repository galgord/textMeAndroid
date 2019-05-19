package com.example.textme.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.textme.R;

public class StoryViewHolder extends RecyclerView.ViewHolder {
    public ImageView ivStory;
    public TextView tvStoryTitle;
    public  TextView tvAddStory;

    public StoryViewHolder(@NonNull View itemView) {
        super(itemView);
        ivStory = itemView.findViewById(R.id.storyImage);
        tvStoryTitle = itemView.findViewById(R.id.storyUser);
        tvAddStory = itemView.findViewById(R.id.tvFirstStory);
    }
}
