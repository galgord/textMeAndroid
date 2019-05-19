package com.example.textme.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.textme.R;

public class ImagePreviewDialog extends DialogFragment {
    ImageView image;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String uri = (String)getArguments().getString("model");

        View v = getActivity().getLayoutInflater().inflate(R.layout.image_preview,null);
        image = v.findViewById(R.id.ivImagePreview);
        Glide.with(getContext()).load(uri).fitCenter().centerCrop().into(image);
        return new AlertDialog.Builder(getContext()).setView(v).create();
    }
}
