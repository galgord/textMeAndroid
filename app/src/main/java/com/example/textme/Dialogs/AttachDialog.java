package com.example.textme.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.textme.ChatActivity;
import com.example.textme.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AttachDialog extends DialogFragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    public static final int TAKE_PHOTO_REQUEST = 2;

    FloatingActionButton fabCamera,fabGallery;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.attach_dialog,null);
        findViews(v);
        ChatActivity chatActivity = (ChatActivity) getActivity();

        fabGallery.setOnClickListener((view)->{
            openFilePicker();
            dismiss();
        });

        fabCamera.setOnClickListener((view)->{
            chatActivity.openCamera();
            dismiss();
        });


        return new AlertDialog.Builder(getContext()).setView(v).create();
    }

    private void findViews(View v){
        fabCamera = v.findViewById(R.id.fabCamera);
        fabGallery = v.findViewById(R.id.fabGallery);
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
