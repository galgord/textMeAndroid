package com.example.textme.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.textme.ProfileActivity;
import com.example.textme.R;

public class addCollectionDialog extends DialogFragment {
    private static final int PICK_IMAGE_REQUEST = 3;

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
            profileActivity.openCameraForStory();
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
