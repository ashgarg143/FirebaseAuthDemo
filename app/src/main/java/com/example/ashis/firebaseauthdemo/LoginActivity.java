package com.example.ashis.firebaseauthdemo;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private Button buttonSignIn;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignUp;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    public FirebaseUser user;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editText_email_id);
        editTextPassword = findViewById(R.id.editText_password);
        buttonSignIn = findViewById(R.id.button_signIn);
        textViewSignUp = findViewById(R.id.textView_SignUp);

        email = getIntent().getStringExtra("email");

        if (email != null)
            editTextEmail.setText(email);

        email = editTextEmail.getText().toString().trim();


        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Login user here
                loginUser();
            }
        });
        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will Open SignUp Activity
                finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class).putExtra("email", email);
                startActivity(intent);
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        ImageView imageView = findViewById(R.id.clear_icon);
        imageView.setPadding(5,5,5,5);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextEmail.setText("");
            }
        });

    }

    private void loginUser() {
        email = editTextEmail.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            //email is empty
            editTextEmail.setError("email required");
            editTextEmail.requestFocus();
            //stopping the function and return
            return;
        }
        if (TextUtils.isEmpty(password)) {
            //password is empty
            editTextPassword.setError("Password required");
            editTextPassword.requestFocus();
            //stopping the function and return
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("enter a valid email");
            editTextEmail.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Minimum length required is 6");
            editTextPassword.requestFocus();
            return;
        }
        int colorCode = Color.parseColor("#5C6BC0");
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(colorCode));
        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(LoginActivity.this, "Password not match", Toast.LENGTH_SHORT).show();
                            editTextPassword.requestFocus();
                        } else if (task.isSuccessful()) {
                            finish();
                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                        } else {

                            Toast.makeText(getApplicationContext(), "Not Registered.. Please register",
                                    Toast.LENGTH_LONG).show();
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("email", email));
                        }
                    }

                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user != null) {
            finish();
            startActivity(new Intent(this, ProfileActivity.class));
        }
    }
}
