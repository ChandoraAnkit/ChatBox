package com.example.chandoras.chatbox;

import android.content.Intent;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mStatusSavebtn;
    private ProgressBar mStatusProgreesBar;


    private DatabaseReference mStausDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = (Toolbar) findViewById(R.id.status_bar);
        mStatus = (TextInputLayout) findViewById(R.id.status_input);
        mStatusSavebtn = (Button) findViewById(R.id.status_save);
        mStatusProgreesBar = (ProgressBar) findViewById(R.id.staus_progress);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent statusIntent = getIntent();
        String status = statusIntent.getStringExtra("STATUS");
        mStatus.getEditText().setText(status);

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mStausDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);

        mStatusSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStatusProgreesBar.setVisibility(View.VISIBLE);

                String status = mStatus.getEditText().getText().toString();

                mStausDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mStatusProgreesBar.setVisibility(View.GONE);
                        } else {
                            mStatusProgreesBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(StatusActivity.this, "Failed to update status!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }
}
