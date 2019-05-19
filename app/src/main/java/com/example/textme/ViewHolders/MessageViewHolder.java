package com.example.textme.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;

import com.example.textme.R;


public class MessageViewHolder extends RecyclerView.ViewHolder {
    public TextView tvMessageBody,tvMessageSender;
    public CardView mContainer;
    public ConstraintLayout clMessage;
    public ImageView ivMessageImage;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);

        tvMessageBody = itemView.findViewById(R.id.messageBody);
        tvMessageSender = itemView.findViewById(R.id.messageSender);

        mContainer = itemView.findViewById(R.id.messageContainer);
        clMessage = itemView.findViewById(R.id.messageConst);
        ivMessageImage = itemView.findViewById(R.id.messageImage);

    }
}
