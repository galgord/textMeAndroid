package com.example.textme.Dialogs;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.example.textme.ProfileActivity;
import com.example.textme.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class setProfileImageDialog extends DialogFragment {
    private static final int PICK_IMAGE_REQUEST = 1;






    TextView tvTakePhoto;
    TextView tvGallery;
    ProfileActivity profileActivity;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_set_profile_img,null);
        findViews(v);
        profileActivity = (ProfileActivity) getActivity();

        tvGallery.setOnClickListener((view)->{
            openFilePicker();
            dismiss();
        });

        tvTakePhoto.setOnClickListener((textview)->{
            profileActivity.openCameraForProfile();
            dismiss();
        });

        return new AlertDialog.Builder(getActivity()).setTitle("Set profile image.").setView(v).create();
    }

    private void findViews(View v) {
        tvGallery = v.findViewById(R.id.tvProfileGallery);
        tvTakePhoto = v.findViewById(R.id.tvProfileCamera);
    }

    private void openFilePicker(){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
        getActivity().startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
    }

}


