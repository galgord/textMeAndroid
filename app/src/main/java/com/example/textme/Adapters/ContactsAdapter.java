package com.example.textme.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.textme.ChatActivity;
import com.example.textme.Dialogs.ContactSettingsFragment;
import com.example.textme.Models.User;
import com.example.textme.R;
import com.example.textme.ViewHolders.ContactsViewHolder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsViewHolder> {

    private List<User> contacts;
    private Context context;
    private FragmentManager fragmentManager;

    public ContactsAdapter(List<User> contacts,Context ctx,FragmentManager fm) {
        this.contacts = contacts;
        context = ctx;
        fragmentManager = fm;
    }

    public ContactsAdapter() {
    }

    public List<User> getContacts() {
        return contacts;
    }

    public void setContacts(List<User> contacts) {
        this.contacts = contacts;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item,parent, false);
        return new ContactsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {
                holder.tvContactName.setText(contacts.get(position).getDisplayName());
                holder.tvContactEmail.setText(contacts.get(position).getEmail());

                StorageReference ref = FirebaseStorage.getInstance().getReference().child("users_profile_images").child(contacts.get(position).getId());
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                       Glide.with(context).load(uri).centerCrop().placeholder(R.drawable.user_placeholder).into(holder.contactImg);
                    }
                });

                holder.itemView.setOnClickListener((v)->{
                    Intent i = new Intent(context, ChatActivity.class);
                    i.putExtra("user",contacts.get(position));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                });

                holder.settingsContact.setOnClickListener((v)->{
                    DialogFragment dialogFragment = new ContactSettingsFragment();
                    dialogFragment.show(fragmentManager,"settings");
                });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}
