package com.example.ashis.firebaseauthdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_GET = 1;
    private TextView textViewUser;
    private Button buttonLogout;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    public FirebaseUser user;
    private ImageView imageViewUserImage;
    private Uri userImageUri;
    private ProgressBar progressBar;
    String userImageUrl;
    private Button buttonSaveUser;
    private EditText editTextName;
    private EditText editTextAddress;
    private TextView textViewVerification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        user = firebaseAuth.getCurrentUser();

        textViewUser = findViewById(R.id.textView_user);
        buttonLogout = findViewById(R.id.button_logout);
        imageViewUserImage = findViewById(R.id.imageView_userImage);
        buttonSaveUser = findViewById(R.id.button_saveUser);
        editTextName = findViewById(R.id.editText_name);
        editTextAddress = findViewById(R.id.editText_address);
        textViewVerification = findViewById(R.id.textViewVerification);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        String name = user.getEmail();
        String userName[] = name.split("@");
        textViewUser.setText("Welcome " + userName[0]);


        if (user != null) {
            loadUserInformation();
        }


        imageViewUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        buttonSaveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //signOutAlertDialogBuilder();
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            }
        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("User Profile");
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuLogout: {
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void loadUserInformation() {
        user = firebaseAuth.getCurrentUser();
        user.reload();
        progressBar.setVisibility(View.VISIBLE);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI

                UserInformation userInfo = dataSnapshot.child("users").child(user.getUid()).getValue(UserInformation.class);
                if (userInfo != null) {
                    editTextName.setText(userInfo.userName);
                    editTextAddress.setText(userInfo.address);
                    if (user.isEmailVerified()) {
                        userInfo.isVerified = true;
                        databaseReference.child("users").child(user.getUid()).child("isVerified").setValue(userInfo.isVerified);
                        textViewVerification.setText("Email verified !");
                        textViewVerification.setTextColor(Color.parseColor("#00C853"));
                    } else {
                        textViewVerification.setText("!! Email not verified (click to verify)");
                        textViewVerification.setTextColor(Color.parseColor("#F44336"));
                        textViewVerification.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getApplicationContext(), "verification email sent", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                    if (userInfo.url == null) {
                        Toast.makeText(ProfileActivity.this, "Please add Profile image", Toast.LENGTH_SHORT).show();
                        Picasso.get().load(R.drawable.camera_icon).into(imageViewUserImage);
                        // Glide.with(getApplicationContext()).load(userInfo.url).into(imageViewUserImage);
                    } else {
                        userImageUrl = userInfo.url;
                        Picasso.get().load(userImageUrl).into(imageViewUserImage);
                    }


                } else {
                    textViewVerification.setText("!! Email not verified (click to verify)");
                    textViewVerification.setTextColor(Color.parseColor("#F44336"));
                    textViewVerification.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getApplicationContext(), "verification email sent", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    Toast.makeText(ProfileActivity.this, "Please add your information", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Toast.makeText(ProfileActivity.this, databaseError.toException().getMessage(), Toast.LENGTH_SHORT).show();                // ...
            }
        };

        databaseReference.addValueEventListener(postListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            userImageUri = data.getData();
            if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK && userImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), userImageUri);
                    imageViewUserImage.setImageBitmap(bitmap);
                    uploadImageToFirebaseStorage();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    private void uploadImageToFirebaseStorage() {
        StorageReference userImageRef = FirebaseStorage.getInstance().getReference("profilePics/" + System.currentTimeMillis() + ".jpg");
        if (userImageUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            userImageRef.putFile(userImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    if (taskSnapshot != null) {
                        userImageUrl = taskSnapshot.getDownloadUrl().toString();
                        Toast.makeText(ProfileActivity.this, "Profile Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user == null) {
            //Already logged in
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void saveUserInformation() {
        String userName = editTextName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        boolean isVerified = false;
        if (TextUtils.isEmpty(userName)) {
            editTextName.setError("Name is required");
            editTextName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            editTextAddress.setError("Address is required");
            editTextAddress.requestFocus();
            return;
        }
        user = firebaseAuth.getCurrentUser();
        user.reload();
        if (user != null) {
            if (userImageUri == null && imageViewUserImage == null) {
                Toast.makeText(this, "Profile image can not be empty", Toast.LENGTH_SHORT).show();
                // saveUserInformation();
            } else if (userImageUrl == null && imageViewUserImage == null) {
                Toast.makeText(this, "Wait for image upload .!!", Toast.LENGTH_SHORT).show();
                //saveUserInformation();
            } else {
                if (user.isEmailVerified())
                    isVerified = true;
                UserInformation userInformation = new UserInformation(userName, address, userImageUrl, isVerified);
                databaseReference.child("users").child(user.getUid()).setValue(userInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Information Saved...", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

}