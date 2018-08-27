package com.example.ashis.firebaseauthdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button buttonSignUp;
    private EditText editTextPassword;
    private TextView textViewSignIn;
    private EditText editTextEmailMain;
    ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    public FirebaseUser user;
    private String email;
    private String password;

    @Override
    protected void onStart() {
        super.onStart();
        if (user != null) {
            //Already logged in
            finish();
            startActivity(new Intent(this, ProfileActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);
        buttonSignUp = findViewById(R.id.button_signUp);
        editTextPassword = findViewById(R.id.editText_password);
        textViewSignIn = findViewById(R.id.textView_SignIn);
        editTextEmailMain = findViewById(R.id.editText_email_id_main);

        email = getIntent().getStringExtra("email");

        if (email != null)
            editTextEmailMain.setText(email);
        email = editTextEmailMain.getText().toString().trim();


        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Register user here
                registerUser();
            }
        });
        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will Open Login Activity
                finish();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class).putExtra("email", email);
                startActivity(intent);
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("SignUp");
        setSupportActionBar(toolbar);
        ImageView imageView = findViewById(R.id.clear_icon);
        imageView.setPadding(5, 5, 5, 5);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextEmailMain.setText("");
            }
        });

    }

    private void registerUser() {
        email = editTextEmailMain.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            //email is empty
            editTextEmailMain.setError("email required");
            editTextEmailMain.requestFocus();
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
            editTextEmailMain.setError("enter a valid email");
            editTextEmailMain.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Minimum length required is 6");
            editTextPassword.requestFocus();
            return;
        }

        //if all validations are Ok
        //first show progress bar

        progressDialog.setMessage("Registering User...");
        progressDialog.show();

        //create user with email and password
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //user is registered successfully
                            //will start profile activity here
                            finish();
                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);


                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                finish();
                                Toast.makeText(MainActivity.this, "Already a user.. Please Login",
                                        Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class).putExtra("email", email));

                            } else {
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                });


    }


}
