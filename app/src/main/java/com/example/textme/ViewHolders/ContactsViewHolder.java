package com.example.textme.ViewHolders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.textme.R;

public class ContactsViewHolder extends RecyclerView.ViewHolder {

   public TextView tvContactName;
   public TextView tvContactEmail;
   public ImageView contactImg;
   public ImageButton settingsContact;


    public ContactsViewHolder(@NonNull View itemView) {
        super(itemView);
        tvContactEmail = itemView.findViewById(R.id.tvUserEmail);
        tvContactName = itemView.findViewById(R.id.tvUserName);
        contactImg = itemView.findViewById(R.id.ciContactImage);
        settingsContact = itemView.findViewById(R.id.ibContactSetting);
    }
}
