package com.example.android.pathasalaa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    TextView textView;
    ImageView imageView;
    EditText editText;
    private static final int CHOOSE_IMAGE = 101;
    Uri uriProfileImage;
    ProgressBar progressBar;
    String profileImageUrl;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editText = (EditText) findViewById(R.id.editTextDisplayName);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView)findViewById(R.id.textViewVerified);
        progressBar = findViewById(R.id.progressBar);



        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showImageChooser();
            }
        });


        loadUserInformation();

        findViewById(R.id.button_Save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveUserInformation();

            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LogninActivity.class));
        }
    }



    private void loadUserInformation() {

        final FirebaseUser user = mAuth.getCurrentUser();

        if(user != null) {
            if(user.getPhotoUrl() !=null  ){
                //Glide--An image loading and caching library for Android focused on smooth scrolling

                Glide.with(ProfileActivity.this)
                        .load(user.getPhotoUrl().toString())
                        .into(imageView);

            }
            if(user.getDisplayName()!=null){
                editText.setText(user.getDisplayName());
            }
            if(user.isEmailVerified()){
                textView.setText("Email Verified");
            }else{
                textView.setText("Email not Verified (click to varify)");

                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ProfileActivity.this,"Verification Email Sent ",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }

        }
    }



    private void saveUserInformation() {
        String displayName = editText.getText().toString();

        if (displayName.isEmpty()) {
            editText.setError("Name Required");
            editText.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && profileImageUrl != null) {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(Uri.parse(profileImageUrl))
                    .build();

            user.updateProfile(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();


            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                imageView.setImageBitmap(bitmap);

                uploadImageToFirebasesStorage();


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void uploadImageToFirebasesStorage() {

        final StorageReference profileiImageRef =
                FirebaseStorage.getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg");

        if (uriProfileImage != null) {

            progressBar.setVisibility(View.VISIBLE);
            profileiImageRef.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);

                            profileImageUrl = profileiImageRef.getDownloadUrl().toString();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuLogout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(ProfileActivity.this,LogninActivity.class));
                break;
        }
        return true;
    }
}

