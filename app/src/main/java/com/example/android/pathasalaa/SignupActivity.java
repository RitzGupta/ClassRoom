package com.example.android.pathasalaa;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    ProgressBar progressBar;
    EditText editTextName, editTextEmail, editTextPassword, editTextMobile, editTextRePass;


    private  FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();


        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        editTextName = (EditText) findViewById(R.id.input_name);
        editTextEmail = (EditText) findViewById(R.id.input_email);
        editTextMobile = (EditText) findViewById(R.id.input_mobile);
        editTextPassword = (EditText) findViewById(R.id.input_password);
        editTextRePass = (EditText) findViewById(R.id.reEnterPassword);


        findViewById(R.id.btn_signup).setOnClickListener(this);
        findViewById(R.id.link_login).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_signup:
                registerUser();
                break;
            case R.id.link_login:
                finish();
                startActivity(new Intent(SignupActivity.this, LogninActivity.class));
                break;
        }
    }

    private void registerUser() {

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String rePassword = editTextRePass.getText().toString().trim();
        String mobile = editTextMobile.getText().toString().trim();
        String name = editTextName.getText().toString().trim();


        //Personal Regular Expression
        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Minimum lenght of password should be 6");
            editTextPassword.requestFocus();
            return;
        }

        if (rePassword.isEmpty()) {

            editTextRePass.setError("Password is required");
            editTextRePass.requestFocus();
            return;
        }

        if (rePassword.length() < 6) {
            editTextRePass.setError("Minimum lenght of password should be 6");
            editTextRePass.requestFocus();
            return;
        }

        if (!password.equals(rePassword) ) {
            editTextRePass.setError("Password not matched!");
            editTextRePass.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            editTextPassword.setError("Name is required");
            editTextPassword.requestFocus();
            return;
        }

        if (mobile.isEmpty()) {
            editTextPassword.setError("Mobile No. is required");
            editTextPassword.requestFocus();
            return;
        }

        if (mobile.length() != 10) {
            editTextMobile.setError("Mobile no. must be of 10 digit");
            editTextMobile.requestFocus();
            return;
        }


        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    finish();
                    startActivity(new Intent(SignupActivity.this, ProfileActivity.class));

                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "you are allready registered", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }


}
