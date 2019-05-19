package com.example.textme;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.textme.Adapters.StoryAdapter;
import com.example.textme.Dialogs.editStatusDialog;
import com.example.textme.Dialogs.setProfileImageDialog;
import com.example.textme.Models.Story;
import com.example.textme.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    // ------------- REQUESTS CODES ----------------
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TAKE_PHOTO_REQUEST = 2;
    private static final int PICK_STORY_PHOTO_REQUEST = 3;
    private static final int TAKE_STORY_PHOTO_REQUEST = 4;

    // ************* VARIABLES *****************
    //Views
    ImageView profileImage;
    TextView tvProfileName;
    ImageButton editStatus;
    TextView status;
    ImageButton ivLikes;
    TextView tvLikes;
    //Refs
    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("users_profile_images");
    //User
    User user;
    String userId;
    boolean Like;
    //Profile Img Uri
    private Uri mImageUri;
    //Progress Dialog
    ProgressDialog dialog;
    // Story Variables
    RecyclerView rvStory;
    List<Story> stories = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        userId = getIntent().getStringExtra("id"); // Getting User Id
        findViews();
        buildUI();
        setRvStory();

        // Checking If Client Profile or Contact Profile
        if(!(userId.matches(FirebaseAuth.getInstance().getCurrentUser().getUid()))){
            editStatus.setVisibility(View.GONE);
            toggleLike();
        } else {
            editStatus.setVisibility(View.VISIBLE);
            // ON Profile Image Clicked
            profileImage.setOnClickListener((v -> {// Open Edit Profile Image Dialog
                DialogFragment dialogFragment = new setProfileImageDialog();
                dialogFragment.show(getSupportFragmentManager(), "set");
            }));
            ivLikes.setClickable(false);

            // ON Edit Status Clicked
            editStatus.setOnClickListener((v -> {// Open Edit Status Dialog
                DialogFragment dialogFragment = new editStatusDialog();
                Bundle bundle = new Bundle();
                bundle.putParcelable("model", user);
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getSupportFragmentManager(), "edit");
            }));
        }
    }

    // **********************************
    // ------------ LIKES ---------------
    // **********************************

    // Posting Like on DB and Animating Drawables
    private void toggleLike() {
        ivLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Like) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(userId).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true).addOnSuccessListener((s -> {
                        transition(ivLikes);
                        Like = true;
                    }));
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(userId).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            reverseTransition(ivLikes);
                            Like = false;
                        }
                    });
                }
            }
        });
    }

    // Getting Likes Count Method
    private void bringLikes(){
        FirebaseDatabase.getInstance().getReference().child("Likes").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tvLikes.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    // ***************************************
    // -------- BUILDING UI METHOD -----------
    // ***************************************

    private void buildUI() {
        //Bringing Name
        FirebaseDatabase.getInstance().getReference().child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                tvProfileName.setText(user.getDisplayName());
                // Checking for User status
                if (user.getUserStatus() != null) {
                    status.setText(user.getUserStatus());
                }
                storageReference.child(user.getId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        user.setUserImageUri(uri.toString());
                        Glide.with(getApplicationContext()).load(uri).centerCrop().placeholder(R.drawable.user_placeholder).into(profileImage);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        // Setting The uploading Image Progress Bar
        dialog = new ProgressDialog(ProfileActivity.this);
        dialog.setMessage(getString(R.string.upload_img));
        FirebaseDatabase.getInstance().getReference().child("Likes").child(userId).orderByChild(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    HashMap hashMap = (HashMap) dataSnapshot.getValue();
                    Like = (boolean) hashMap.get(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    if(Like) {ivLikes.setImageResource(R.drawable.heart_filled);
                        tvLikes.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    } else {
                        tvLikes.setTextColor(getResources().getColor(android.R.color.black));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        bringLikes();
    }


    // ***************************************
    // ---------- Activitiy Result -----------
    // ***************************************


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            dialog.show();
            mImageUri = data.getData();
            profileImage.setImageURI(mImageUri);
            UploadProfileImage();
        }

        if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK) {
            dialog.show();
            Glide.with(getApplicationContext()).load(mImageUri).placeholder(R.drawable.user_placeholder).centerCrop().into(profileImage);
            UploadProfileImage();
            buildUI();
        }

        if (requestCode == PICK_STORY_PHOTO_REQUEST && resultCode == RESULT_OK){
                mImageUri = data.getData();
                UploadStory();
        }

        if (requestCode == TAKE_STORY_PHOTO_REQUEST && resultCode == RESULT_OK){
                UploadStory();
        }

    }

    private void UploadStory(){
        DatabaseReference collections = FirebaseDatabase.getInstance().getReference().child("Collections").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        String id = collections.push().getKey();
        collections.child(id).setValue(new Story(user.getDisplayName(),user.getUserImageUri(),id)).addOnSuccessListener((s)->{
            FirebaseStorage.getInstance().getReference().child("Collections").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(id).putFile(mImageUri);
        }).addOnFailureListener((f)->{
            Snackbar.make(tvProfileName,f.getLocalizedMessage(),Snackbar.LENGTH_LONG);
        });
    }

    // Uploading Image from setProfileDialog to FirebaseStorage

    private void UploadProfileImage() {
        if (mImageUri != null) {
            storageReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(ProfileActivity.this, getString(R.string.photo_successfully_changed), Toast.LENGTH_SHORT).show();
                    buildUI();
                    dialog.dismiss();
                }
            }).addOnFailureListener((f -> {
                Snackbar.make(tvProfileName, f.getLocalizedMessage(), Snackbar.LENGTH_LONG);
                dialog.dismiss();
            }));
        }
    }

    public void openCameraForProfile() {
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

    public void openCameraForStory() {
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
                        TAKE_STORY_PHOTO_REQUEST);
            }
        }
    }

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
        String mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void findViews() {
        tvProfileName = findViewById(R.id.tvProfileName);
        profileImage = findViewById(R.id.ciContactImage);
        status = findViewById(R.id.tvStatus);
        editStatus = findViewById(R.id.statusEdit);
        ivLikes = findViewById(R.id.ivLikes);
        tvLikes = findViewById(R.id.tvLikes);
        rvStory = findViewById(R.id.rvStorys);
    }

    // ***************************
    // -------- Storys -----------
    // ***************************

    private void setRvStory(){
        bringStories();
        rvStory.setLayoutManager(new GridLayoutManager(this,4));
        rvStory.setHasFixedSize(true);
    }

    private void bringStories() {
        FirebaseDatabase.getInstance().getReference().child("Collections").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                     stories.add(snapshot.getValue(Story.class));
                 }
                rvStory.setAdapter(new StoryAdapter(stories,getApplicationContext(),userId,getSupportFragmentManager()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        buildUI();
    }

    // *******************************
    // -------- Animations -----------
    // *******************************

    private void transition(ImageButton button){
        Drawable[] layers = new Drawable[2];
        layers[0] = getResources().getDrawable(R.drawable.heart);
        layers[1] = getResources().getDrawable(R.drawable.heart_filled);
        TransitionDrawable transition = new TransitionDrawable(layers);
        transition.setCrossFadeEnabled(true);
        button.setImageDrawable(transition);
        transition.startTransition(500 /*animation duration*/);
        tvLikes.setTextColor(getResources().getColor(android.R.color.holo_red_light));
    }

    private void reverseTransition(ImageButton button){
            Drawable[] layers = new Drawable[2];
            layers[1] = getResources().getDrawable(R.drawable.heart);
            layers[0] = getResources().getDrawable(R.drawable.heart_filled);
            TransitionDrawable transition = new TransitionDrawable(layers);
            transition.setCrossFadeEnabled(true);
            button.setImageDrawable(transition);
            transition.startTransition(500 /*animation duration*/);
        tvLikes.setTextColor(getResources().getColor(android.R.color.black));

    }

}
