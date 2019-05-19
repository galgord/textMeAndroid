package com.example.textme.ViewHolders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.textme.R;

public class UserViewHolder extends RecyclerView.ViewHolder {

   public TextView tvName;
   public TextView tvEmail;
   public ImageView userImage;
   public ImageButton addFriendBtn;

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);

        tvName = itemView.findViewById(R.id.tvUserName);
        tvEmail = itemView.findViewById(R.id.tvUserEmail);
        userImage = itemView.findViewById(R.id.ciContactImage);
        addFriendBtn = itemView.findViewById(R.id.addFriendButton);

    }
}
