package com.example.textme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private  EditText etEmail,etPass;
    private TextView tvRegister;
    private CardView btnLogin;
    private ProgressBar pb;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); // FullScreen
        findView(); // Init Views

        // Check if User Already Logged In
        if(auth.getCurrentUser() != null){ // Clearing Back Stack and Open Contacts Activity
            Intent i = new Intent(this,ContactsActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }

        // on Login Clicked
        btnLogin.setOnClickListener((v)->{ // Check Validation
            if(!Validation(etPass.getText().toString(),etEmail.getText().toString())){
                return;
            }
            pb.setVisibility(View.VISIBLE); // Show ProgressBar
            // Getting Email and Pass
            String email = etEmail.getText().toString();
            String pass = etPass.getText().toString();
            // Sign In
            auth.signInWithEmailAndPassword(email,pass).addOnSuccessListener((r)->{
                pb.setVisibility(View.GONE); // Dissmissing ProgressBar
                // Toasting User
                Toast.makeText(this, getString(R.string.login_succes), Toast.LENGTH_SHORT).show();
                btnLogin.postDelayed(()->{// Clear Back and Open Contacts Activity
                    Intent i = new Intent(this,ContactsActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                },1000);
            }).addOnFailureListener((r)->{ // Failed to Login
                pb.setVisibility(View.GONE); // Dissmissing ProgressBar
                Snackbar.make(btnLogin,r.getLocalizedMessage(),Snackbar.LENGTH_LONG);
            });
        });
        // On Register Clicked
        tvRegister.setOnClickListener((v)->{
            startActivity(new Intent(this,RegisterActivity.class));
        });
    }
    // Init Views Method
    private void findView(){
        etEmail = findViewById(R.id.etLoginEmail);
        etPass = findViewById(R.id.etLoginPass);
        tvRegister = findViewById(R.id.tvRegister);
        btnLogin = findViewById(R.id.btnLogin);
        pb = findViewById(R.id.pbLogin);
        auth = FirebaseAuth.getInstance();
    }

    // Validatio Method
    private boolean Validation(String pass,String email){
        if(pass.length() < 8){ // Password is invalid
            etPass.setError("Password must contain 8 characters");
        }
        if(!isValidEmail(email)){
            etEmail.setError("Invalid Email");
        }
        if(pass.length() < 8 || !isValidEmail(email)){
            return true;
        }
        return true;
    }

    // Validate Email method
    public static boolean isValidEmail(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
}
