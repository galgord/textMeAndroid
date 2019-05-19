package com.example.textme.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.load.engine.Resource;
import com.example.textme.Models.User;
import com.example.textme.R;
import com.google.firebase.database.FirebaseDatabase;

public class editStatusDialog extends DialogFragment {

    User user;
    EditText etStatus;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
// Now use view.findViewById() to do what you want
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.edit_status,null);
        etStatus = v.findViewById(R.id.etStatus);

        getModel();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setView(v).setPositiveButton(getString(R.string.apply), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user.setUserStatus(etStatus.getText().toString());
                FirebaseDatabase.getInstance().getReference().child("Users").child(user.getId()).setValue(user);
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();
    }

    private void getModel() {
        if(getArguments().getParcelable("model") != null){
            user = getArguments().getParcelable("model");
        }
    }

}