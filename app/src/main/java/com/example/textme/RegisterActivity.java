package com.example.textme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.textme.Models.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    // Views
    EditText etEmail, etPass, etDisplay;
    CardView registerBtn;
    ProgressBar pb;

    // Auth
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide(); // FullScreen
        findViews(); // Init views

        registerBtn.setOnClickListener((v) -> {
            String pass = etPass.getText().toString();
            String email = etEmail.getText().toString();
            String name = etDisplay.getText().toString();
            if (!validation(email, pass, name)) {
                return;
            }
            pb.setVisibility(View.VISIBLE);

            auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener((r) -> {

                //Changing User DisplayName
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(name).build();
                r.getUser().updateProfile(profileUpdates).addOnSuccessListener(((s) -> {

                    FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

                    //Writing User to Database
                    User user = new User(fUser.getEmail(), fUser.getUid(), fUser.getDisplayName());
                    FirebaseDatabase.getInstance().getReference().child("Users").child(r.getUser().getUid()).setValue(user).addOnSuccessListener((succes) -> {
                        //Toasting Client
                        Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show();
                        pb.setVisibility(View.GONE); // Progress Bar OFF
                        // Delaying Open Main Activity
                        registerBtn.postDelayed(() -> {
                            Intent i = new Intent(this, ContactsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            finish();
                        }, 1000);
                    }).addOnFailureListener((f) -> {
                        pb.setVisibility(View.GONE); // Progress Bar OFF
                        Snackbar.make(registerBtn, f.getLocalizedMessage(), Snackbar.LENGTH_LONG);
                    });
                })).addOnFailureListener((f) -> {
                    pb.setVisibility(View.GONE); // Progress Bar OFF
                    Snackbar.make(registerBtn, f.getLocalizedMessage(), Snackbar.LENGTH_LONG);
                });


            }).addOnFailureListener((r) -> {
                pb.setVisibility(View.GONE);
                Snackbar.make(registerBtn, r.getLocalizedMessage(), Snackbar.LENGTH_LONG);
            });
        });
    }

    // Init Views Method
    private void findViews() {
        etDisplay = findViewById(R.id.etRegDisplay);
        etEmail = findViewById(R.id.etRegEmail);
        etPass = findViewById(R.id.etRegPass);
        registerBtn = findViewById(R.id.registerBtn);
        pb = findViewById(R.id.pbReg);
        auth = FirebaseAuth.getInstance();
    }

    private boolean validation(String email, String pass, String name) {
        if (pass.length() < 8) {
            etPass.setError("Password must contains atleast 8 characters.");
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Invalid email.");
        }

        if (name.length() == 0) {
            etDisplay.setError("Name required.");
        }

        return pass.length() >= 8 && isValidEmail(email) && name.length() != 0;
    }

    // Validate Email method
    public static boolean isValidEmail(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
}
