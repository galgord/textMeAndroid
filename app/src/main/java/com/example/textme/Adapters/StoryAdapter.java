package com.example.textme.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.textme.Dialogs.addCollectionDialog;
import com.example.textme.Models.Story;
import com.example.textme.R;
import com.example.textme.ViewHolders.ContactsViewHolder;
import com.example.textme.ViewHolders.StoryViewHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryViewHolder> {

    List<Story> stories;
    Context ctx;
    String userId;
    FragmentManager fragmentManager;
    List<String> storyImages = new ArrayList<>();

    public StoryAdapter(List<Story> stories, Context ctx,String userId,FragmentManager fragmentManager) {
        this.stories = stories;
        this.ctx = ctx;
        this.userId = userId;
        this.fragmentManager = fragmentManager;
    }

    public StoryAdapter() {
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_item,parent, false);
        return new StoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
                if(userId.matches(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    if(position == 0) {
                        holder.tvAddStory.setVisibility(View.VISIBLE);
                        holder.ivStory.setVisibility(View.GONE);
                        holder.tvStoryTitle.setVisibility(View.GONE);
                        holder.itemView.setOnClickListener((v) -> {
                            DialogFragment dialogFragment = new addCollectionDialog();
                            dialogFragment.show(fragmentManager, null);
                        });
                    } else {
                        holder.tvAddStory.setVisibility(View.GONE);
                        holder.ivStory.setVisibility(View.VISIBLE);
                        holder.tvStoryTitle.setVisibility(View.VISIBLE);
                        holder.tvStoryTitle.setText(stories.get(position - 1).getUserName());
                        FirebaseStorage.getInstance().getReference().child("Collections").child(userId).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if(task.isSuccessful()){
                                            Glide.with(ctx).load(task.toString()).into(holder.ivStory);
                                        }
                                    }
                                });
                    }
                } else {
                    if(stories != null) {
                        holder.tvStoryTitle.setText(stories.get(position).getUserName());
                    }
                }

    }

    @Override
    public int getItemCount() {
        if(userId.matches(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return stories == null ? 1 : stories.size() + 1;
        } else {
            return stories == null ? 0: stories.size();
        }
    }
}
