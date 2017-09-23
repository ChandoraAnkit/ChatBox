package com.example.chandoras.chatbox;

import android.icu.text.DateFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileTotalFriends;
    private Button mProfileSendReqstBtn, mProfileDeclineRqst;
    private ProgressBar mProfileProgress;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;
    private DatabaseReference mUserRef;

    private FirebaseUser currentUser;

    private String currentState;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        final String userId = getIntent().getStringExtra("userId");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friends_request");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        mProfileImage = (ImageView) findViewById(R.id.profile_userImage);
        mProfileName = (TextView) findViewById(R.id.profile_userName);
        mProfileStatus = (TextView) findViewById(R.id.profile_userStatus);
        mProfileTotalFriends = (TextView) findViewById(R.id.profile_totalFriends);
        mProfileSendReqstBtn = (Button) findViewById(R.id.profile_btn_send_rqst);
        mProfileProgress = (ProgressBar) findViewById(R.id.profile_progress);
        mProfileDeclineRqst = (Button) findViewById(R.id.profile_btn_decline_rqst);

        currentState = "not_friends";
        mProfileDeclineRqst.setVisibility(View.INVISIBLE);
        mProfileDeclineRqst.setEnabled(false);

      //  mProfileProgress.setVisibility(View.VISIBLE);


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.child("name").getValue().toString();
                String userImage = dataSnapshot.child("image").getValue().toString();
                String userStatus = dataSnapshot.child("status").getValue().toString();

                mProfileName.setText(userName);
                mProfileStatus.setText(userStatus);

                Picasso.with(ProfileActivity.this).load(userImage).placeholder(R.drawable.default_image).into(mProfileImage);

                //--------FRIENDS LIST FEATURE-------
                mFriendRequestDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(userId)) {

                            String req_type = dataSnapshot.child(userId).child("request_type").getValue().toString();
                            Log.i("DATA",req_type);

                            if (req_type.equals("received")) {
                                currentState = "req_received";
                                mProfileSendReqstBtn.setText("Accept friend request");
                                mProfileDeclineRqst.setVisibility(View.VISIBLE);
                                mProfileDeclineRqst.setEnabled(true);

                            } else if (req_type.equals("sent")) {
                                currentState = "req_sent";
                                mProfileDeclineRqst.setVisibility(View.INVISIBLE);
                                mProfileDeclineRqst.setEnabled(false);
                                mProfileSendReqstBtn.setText("Cancel friend request");
                                mProfileProgress.setVisibility(View.GONE);

                            }
                        } else {
                            mFriendsDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userId)) {
                                        currentState = "friends";
                                        mProfileSendReqstBtn.setText("Unfriend");
                                        mProfileDeclineRqst.setVisibility(View.INVISIBLE);
                                        mProfileDeclineRqst.setEnabled(false);
                                    }
                                    mProfileProgress.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        mProfileProgress.setVisibility(View.GONE);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mProfileSendReqstBtn.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                mProfileSendReqstBtn.setEnabled(false);
                //----------------NOT FRIENDS STATE__________________________//
                if (currentState.equals("not_friends")) {

                    DatabaseReference newNotificationsRef = mRootRef.child("notifications").child(userId).push();
                    String newNotificationId = newNotificationsRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", currentUser.getUid());
                    notificationData.put("type", "request");


                    Map requestMap = new HashMap();
                    requestMap.put("Friends_request/" + currentUser.getUid() + "/" + userId + "/request_type", "sent");
                    requestMap.put("Friends_request/" + userId + "/" + currentUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + userId + "/" + newNotificationId, notificationData);
                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if (databaseError != null) {
                                Toast.makeText(ProfileActivity.this, "There was  some error in sending request", Toast.LENGTH_SHORT).show();
                            }

                            mProfileSendReqstBtn.setEnabled(true);
                            currentState = "req_sent";
                            mProfileSendReqstBtn.setText("CANCEL FRIEND REQUEST");

                        }
                    });


                }

                //---------------CANCEL FRIEND REQUEST--------------------//

                if (currentState.equals("req_sent")) {
                    mFriendRequestDatabase.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    currentState = "not_friends";
                                    mProfileSendReqstBtn.setEnabled(true);
                                    mProfileSendReqstBtn.setText("SEND FRIEND REQUEST");
                                    mProfileDeclineRqst.setVisibility(View.INVISIBLE);
                                    mProfileDeclineRqst.setEnabled(false);

                                }
                            });
                        }
                    });
                }
                //-----------REQUEST_RECEIVED STATE----------//
                if (currentState.equals("req_received")) {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendMap = new HashMap();
                    friendMap.put("Friends/" + currentUser.getUid() + "/" + userId + "/date",currentDate);
                    friendMap.put("Friends/" + userId + "/" + currentUser.getUid() + "/date",currentDate);

                    friendMap.put("Friends_request/" + currentUser.getUid() + "/" + userId,null);
                    friendMap.put("Friends_request/" + userId + "/" + currentUser.getUid(),null);

                    mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){

                                currentState = "friends";
                                mProfileSendReqstBtn.setText("Unfriend");

                                mProfileDeclineRqst.setVisibility(View.INVISIBLE);
                                mProfileDeclineRqst.setEnabled(false);

                            }else {
                                String error  = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }
                            mProfileSendReqstBtn.setEnabled(true);

                        }
                    });


                }

                //-----------UNFRIEND STATE----------//
                if (currentState.equals("friends")) {
                    Map unfriendMap = new HashMap();

                    unfriendMap.put("Friends/" + currentUser.getUid() + "/" + userId,null);
                    unfriendMap.put("Friends/" + userId + "/" + currentUser.getUid(),null);


                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){

                                currentState = "not_friends";
                                mProfileSendReqstBtn.setText("SENT FRIEND REQUEST");

                                mProfileDeclineRqst.setVisibility(View.INVISIBLE);
                                mProfileDeclineRqst.setEnabled(false);

                            }else {
                                String error  = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }
                            mProfileSendReqstBtn.setEnabled(true);

                        }
                    });

                }


            }
        });

        mProfileDeclineRqst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map declineMap = new HashMap();

                declineMap.put("Friends_request/" + currentUser.getUid() + "/" + userId,null);
                declineMap.put("Friends_request/" + userId + "/" + currentUser.getUid(),null);


                mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null){

                            currentState = "not_friends";
                            mProfileSendReqstBtn.setText("SENT FRIEND REQUEST");

                            mProfileDeclineRqst.setVisibility(View.INVISIBLE);
                            mProfileDeclineRqst.setEnabled(false);

                        }else {
                            String error  = databaseError.getMessage();
                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                        }
                        mProfileSendReqstBtn.setEnabled(true);

                    }
                });

            }
        });
    }

    @Override
    protected void onStart() {

        super.onStart();
        mUserRef.child("online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
