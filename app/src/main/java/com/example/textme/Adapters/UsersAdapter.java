package com.example.textme.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.textme.Models.Friend;
import com.example.textme.Models.User;
import com.example.textme.R;
import com.example.textme.ViewHolders.UserViewHolder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private List<User> users;
    private List<String> friendsIds;
    private Context ctx;

    public UsersAdapter(List<User> users, Context ctx,List<String> friendsIds) {
        this.users = users;
        this.ctx = ctx;
        this.friendsIds = friendsIds;
    }

    public UsersAdapter() {
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Context getCtx() {
        return ctx;
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }

    public List<String> getFriendsIds() {
        return friendsIds;
    }

    public void setFriendsIds(List<String> friendsIds) {
        this.friendsIds = friendsIds;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item,parent,false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            holder.tvName.setText(users.get(position).getDisplayName());
            holder.tvEmail.setText(users.get(position).getEmail());

        StorageReference ref = FirebaseStorage.getInstance().getReference().child("users_profile_images").child(users.get(position).getId());
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ctx).load(uri).centerCrop().placeholder(R.drawable.user_placeholder).into(holder.userImage);
            }
        });
            for(int i = 0; i < friendsIds.size(); i++){
                if(users.get(position).getId().matches(friendsIds.get(i))){
                    holder.addFriendBtn.setVisibility(View.GONE);
                }
            }



            holder.addFriendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFriend(new Friend(users.get(position).getId().toString(),true,false),new Friend(FirebaseAuth.getInstance().getCurrentUser().getUid(),true,false),holder.addFriendBtn);
                }
            });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void addFriend(Friend friend, Friend client,ImageButton button){
        FirebaseDatabase.getInstance().getReference().child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(friend.uid).setValue(friend).addOnSuccessListener((s->{
            FirebaseDatabase.getInstance().getReference().child("Friends").child(friend.uid).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(client).addOnSuccessListener((success ->{
                friendsIds.add(friend.uid);
                transtion(button);
            })).addOnFailureListener(f->{

            });
        })).addOnFailureListener((fail->{

        }));
    }

    private void transtion(ImageButton button){
        Drawable[] layers = new Drawable[2];
        layers[0] = ctx.getResources().getDrawable(R.drawable.add);
        layers[1] = ctx.getResources().getDrawable(R.drawable.verified);
        TransitionDrawable transition = new TransitionDrawable(layers);
        transition.setCrossFadeEnabled(true);
        button.setImageDrawable(transition);
        transition.startTransition(500 /*animation duration*/);
        button.postDelayed(()->{
            Animation animation = new AlphaAnimation(1.0f,0.0f);
            Toast.makeText(ctx, "Contact Successfully Added", Toast.LENGTH_SHORT).show();
            animation.setDuration(1000);
            button.startAnimation(animation);
            button.postDelayed(this::notifyDataSetChanged,1000);
        },1000);
    }
}
