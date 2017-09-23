package com.example.chandoras.chatbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;

    private Button mRegisterBtn;

    private Toolbar mToolbar;

    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar = (Toolbar) findViewById(R.id.reg_toolbar);


        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();




        mDisplayName = (TextInputLayout) findViewById(R.id.reg_dispname);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mRegisterBtn = (Button) findViewById(R.id.reg_create_btn);
        mProgressBar = (ProgressBar) findViewById(R.id.reg_progress_bar);


        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String displayName = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();


                if (!TextUtils.isEmpty(displayName) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setIndeterminate(false);

                    registerUser(displayName, email, password);

                }


            }
        });
    }

    private void registerUser(final String displayName, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {


                        if (task.isSuccessful()) {

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currentUser.getUid();
                            String tokenId = FirebaseInstanceId.getInstance().getToken();

                            mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);


                            HashMap<String, String> userMap = new HashMap<>();

                            userMap.put("name", displayName);
                            userMap.put("token_id",tokenId);
                            userMap.put("status", "Hy there,I'm using Chat Box");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");

                            mDatabaseRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mProgressBar.setVisibility(View.GONE);
                                        Toast.makeText(RegisterActivity.this, "You've just signed in!",
                                                Toast.LENGTH_SHORT).show();
                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();

                                    }
                                }
                            });


                        } else {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(RegisterActivity.this, "Sign-Up failed!",
                                    Toast.LENGTH_SHORT).show();

                        }


                    }
                });
    }
}
