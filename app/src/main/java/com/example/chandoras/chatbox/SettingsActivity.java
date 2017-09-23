package com.example.chandoras.chatbox;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;


import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;



public class SettingsActivity extends AppCompatActivity {

    private static final int RC_PHOTOS = 100;

    CircleImageView mUserImage;

    TextView mUserName;
    TextView mUserStatus;

    Button mChangeImageBtn;
    Button mChangeStatusBtn;

    ProgressBar mImageProgress;

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentuser;
    private StorageReference mProfilePhotosStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mUserImage = (CircleImageView) findViewById(R.id.settings_image);
        mUserName = (TextView) findViewById(R.id.settings_name);
        mUserStatus = (TextView) findViewById(R.id.settings_status);
        mChangeImageBtn = (Button) findViewById(R.id.settings_change_imageBtn);
        mChangeStatusBtn = (Button) findViewById(R.id.settings_change_statusBtn);


        mProfilePhotosStorage = FirebaseStorage.getInstance().getReference();
        mCurrentuser = FirebaseAuth.getInstance().getCurrentUser();

        String current_uid = mCurrentuser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                loadDetails(name,status,image);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChangeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = mUserStatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("STATUS", status);
                startActivity(statusIntent);
            }
        });

        mChangeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select photo"), RC_PHOTOS);


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == RC_PHOTOS && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setAspectRatio(1, 1).setMinCropWindowSize(500,500)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageProgress = (ProgressBar) findViewById(R.id.image_progress);
                mImageProgress.setVisibility(View.VISIBLE);

                Uri resultUri = result.getUri();

                File thumb_pathFile= new File(resultUri.getPath());

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                try {
                    Bitmap thumb_bitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200)
                            .setQuality(70).compressToBitmap(thumb_pathFile);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[]  thumb_byte = baos.toByteArray();

                    StorageReference filepath = mProfilePhotosStorage.child("profile-photos").child(userId + ".jpg");
                    final StorageReference thumb_filepath = mProfilePhotosStorage.child("profile-photos").child("thumbs").child(userId + ".jpg");


                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                final String download_url = task.getResult().getDownloadUrl().toString();
                                UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                        if (thumb_task.isSuccessful()){
                                            String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();


                                            Map updateHashmap = new HashMap<>();
                                            updateHashmap.put("image",download_url);
                                            updateHashmap.put("thumb_image",thumb_downloadUrl);

                                            mUserDatabase.updateChildren(updateHashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        mImageProgress.setVisibility(View.GONE);

                                                    } else {
                                                        mImageProgress.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            });

                                        }else {
                                            mImageProgress.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });



                            } else {
                                mImageProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    private void loadDetails(String name, String status, final String image){
        mUserName.setText(name);
        mUserStatus.setText(status);
        if (!image.equals("default")) {


            Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image).into(mUserImage, new com.squareup.picasso.Callback(){

                @Override
                public void onSuccess() {
                    Log.i("COMPUTER","success");

                }

                @Override
                public void onError() {
                    Log.i("COMPUTER","error");
                    Picasso.with(SettingsActivity.this).load(image).into(mUserImage);
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

            mUserDatabase.child("online").setValue("true");

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCurrentuser !=null) {
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
