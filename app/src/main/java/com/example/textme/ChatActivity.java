package com.example.textme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.textme.Dialogs.AttachDialog;
import com.example.textme.Dialogs.ImagePreviewDialog;
import com.example.textme.Models.Message;
import com.example.textme.Models.User;
import com.example.textme.ViewHolders.ContactsViewHolder;
import com.example.textme.ViewHolders.MessageViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    public static final int TAKE_PHOTO_REQUEST = 2;

    //Views
    RecyclerView rvChat;
    EditText etMessage;
    ImageButton sendBtn;
    ImageButton attachBtn;
    ConstraintSet constraintSet;

    User contact;

    //Firebase Stuff
    FirebaseRecyclerOptions options;
    FirebaseRecyclerAdapter adapter;
    FirebaseUser client;
    StorageReference storageReference;
    DatabaseReference ref;
    Uri mImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        findViews(); // Finding Views

        // Getting Contact Object
        contact = getIntent().getParcelableExtra("user");

        getSupportActionBar().setTitle(contact.getDisplayName());
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setOnClickListener((v)->{
            startActivity(new Intent(this,ProfileActivity.class).putExtra("id",contact.getId()));
        });


        // Getting Chat Ref
        if(contact.getId().compareTo(FirebaseAuth.getInstance().getCurrentUser().getUid()) < 0){
            ref = FirebaseDatabase.getInstance().getReference().child("Chats").child(contact.getId()+FirebaseAuth.getInstance().getCurrentUser().getUid());
        } else {
            ref = FirebaseDatabase.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()+ contact.getId());
        }

        if(contact.getId().compareTo(FirebaseAuth.getInstance().getCurrentUser().getUid()) < 0){
            storageReference = FirebaseStorage.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(contact.getId()+FirebaseAuth.getInstance().getCurrentUser().getUid());
        } else {
            storageReference = FirebaseStorage.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()+ contact.getId());
        }

        //Fetching Messages
        FetchMessages(contact);

        // SendBar Buttons Click Listners
        sendBtn.setOnClickListener((v)->{
            sendMessage(etMessage.getText().toString()); // Sending Message
            etMessage.setText(""); // Resseting Chat Input
        });
        attachBtn.setOnClickListener(v->{
            DialogFragment dialogFragment = new AttachDialog();
            dialogFragment.show(getSupportFragmentManager(),"attach");
        });

    }



    // Method Finding Views
    private void findViews() {
        rvChat = findViewById(R.id.recyclerView);
        sendBtn = findViewById(R.id.sendMessageBtn);
        etMessage = findViewById(R.id.etMessage);
        client = FirebaseAuth.getInstance().getCurrentUser();
        attachBtn = findViewById(R.id.attachToMessageBtn);
    }

    //Fetching Messages From Database
    private void FetchMessages(User user){

        options = new FirebaseRecyclerOptions.Builder<Message>().setQuery(ref,Message.class).build();
        adapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {
            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item,parent,false);
                return new MessageViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i, @NonNull Message message) {

                messageViewHolder.tvMessageSender.setText(user.getDisplayName()); // Setting Sender Name

                // Switch Case on Message Type
                switch (message.getmType()){
                    case "Chat":
                        messageViewHolder.tvMessageBody.setText(message.getMessageBody());
                        messageViewHolder.ivMessageImage.setVisibility(View.GONE);
                        messageViewHolder.tvMessageBody.setVisibility(View.VISIBLE);
                        break;
                    case "Image":
                        messageViewHolder.tvMessageBody.setVisibility(View.GONE);
                        messageViewHolder.ivMessageImage.setVisibility(View.VISIBLE);
                        Glide.with(getApplicationContext()).load(message.getMessageBody()).fitCenter().centerCrop().into(messageViewHolder.ivMessageImage);
                        messageViewHolder.ivMessageImage.setOnClickListener((v)->{
                            DialogFragment dialogFragment = new ImagePreviewDialog();
                            Bundle b = new Bundle();
                            b.putString("model",message.getMessageBody());
                            dialogFragment.setArguments(b);
                            dialogFragment.show(getSupportFragmentManager(),"image");
                        });
                        break;
                }

                setChatAlignment(messageViewHolder,message);
            }
        };
        // Set Recycler View
        rvChat.setHasFixedSize(true);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);
        adapter.startListening();
    }

    // ************************************************
    // --------------- SENDING METHODS ----------------
    // ************************************************

    private void setChatAlignment(MessageViewHolder mvh,Message message){

        // Aliignment and Coloring for Chat
        if(message.getSender().matches(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
            constraintSet = new ConstraintSet();
            constraintSet.clone(mvh.clMessage);
            mvh.tvMessageSender.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            constraintSet.clear(mvh.mContainer.getId(),ConstraintSet.START);
            constraintSet.connect(mvh.mContainer.getId(), ConstraintSet.START, mvh.clMessage.getId(), ConstraintSet.START, 32);
            constraintSet.clear(mvh.tvMessageSender.getId(),ConstraintSet.START);
            constraintSet.connect(mvh.tvMessageSender.getId(), ConstraintSet.START, mvh.clMessage.getId(), ConstraintSet.START, 32);

            constraintSet.applyTo(mvh.clMessage);

        } else {
            constraintSet = new ConstraintSet();
            constraintSet.clone(mvh.clMessage);

            constraintSet.clear(mvh.tvMessageSender.getId(),ConstraintSet.START);
            constraintSet.clear(mvh.mContainer.getId(),ConstraintSet.START);

            constraintSet.connect(mvh.mContainer.getId(), ConstraintSet.END, mvh.clMessage.getId(), ConstraintSet.END, 32);
            constraintSet.connect(mvh.tvMessageSender.getId(), ConstraintSet.END, mvh.clMessage.getId(), ConstraintSet.END, 32);

            constraintSet.applyTo(mvh.clMessage);

            mvh.mContainer.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            mvh.tvMessageBody.setTextColor(getResources().getColor(android.R.color.white));
        }
    }

    private void sendMessage(String message){
        String key = ref.push().getKey();
        ref.child(key).setValue(new Message(message,FirebaseAuth.getInstance().getCurrentUser().getEmail(),key,"Chat")).addOnSuccessListener((s)->{
            adapter.notifyDataSetChanged();
        });
    }

    private void sendImage(String imageUri){
        String key = ref.push().getKey();

        if(contact.getId().compareTo(FirebaseAuth.getInstance().getCurrentUser().getUid()) < 0){
            storageReference = FirebaseStorage.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(contact.getId()+FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key);
        } else {
            storageReference = FirebaseStorage.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()+ contact.getId()).child(key);
        }
                storageReference.putFile(Uri.parse(imageUri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                ref.child(key).setValue(new Message(imageUri,FirebaseAuth.getInstance().getCurrentUser().getEmail(),key,"Image")).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        });
                    }
                });
            }

    private void sendCameraImage(String ImageUri){
        String key = ref.push().getKey();

        if(contact.getId().compareTo(FirebaseAuth.getInstance().getCurrentUser().getUid()) < 0){
            storageReference = FirebaseStorage.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(contact.getId()+FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key);
        } else {
            storageReference = FirebaseStorage.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()+ contact.getId()).child(key);
        }
        storageReference.putFile(Uri.parse(ImageUri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ref.child(key).setValue(new Message(uri.toString(),FirebaseAuth.getInstance().getCurrentUser().getEmail(),key,"Image")).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        });
    }

    // *******************************************
    // ----------- Attach Methods  ---------------
    // *******************************************

    public void openCamera() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                mImageUri = FileProvider.getUriForFile(this, "com.example.textme", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        mImageUri);
                startActivityForResult(pictureIntent,
                        TAKE_PHOTO_REQUEST);
            }
        }
    }

    private String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    // *******************************************
    // ------ Result From Images Intent ----------
    // *******************************************

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri uri = data.getData();
            uri.toString();
            sendImage(uri.toString());
        }

        if(requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK){
            sendCameraImage(mImageUri.toString());
        }
    }

    // Listen To Adapter
    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null){
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter != null){
            adapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter != null){
            adapter.startListening();
        }
    }

}
