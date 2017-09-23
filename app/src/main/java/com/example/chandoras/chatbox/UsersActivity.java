package com.example.chandoras.chatbox;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    Toolbar mToolbar;
    RecyclerView  mUsersList;

    FirebaseAuth mAuth;
    DatabaseReference mUserDatabaseRef;
    DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = (Toolbar)findViewById(R.id.users_appbar);
        mUsersList =  (RecyclerView)findViewById(R.id.users_recyclerview);

        mAuth  = FirebaseAuth.getInstance();
        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

            mUserRef.child("online").setValue("true");


        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,R.layout.user_profile,UsersViewHolder.class,mUserDatabaseRef
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users user, final int position) {
                viewHolder.setName(user.getName(),user.getStatus(),user.getThumb_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String userId= getRef(position).getKey();

                        Log.i("ID",userId);

                            if (userId!= null) {
                                Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("userId", userId);
                                startActivity(profileIntent);
                            }
                    }
                });
            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }
        public void setName(String name, String status, final String image){
            TextView mUserName = mView.findViewById(R.id.user_name);
            TextView mUserStatus = mView.findViewById(R.id.user_status);
            final CircleImageView mUserImage = mView.findViewById(R.id.user_image);

            mUserName.setText(name);
            mUserStatus.setText(status);

            if (!image.equals("default")) {


                Picasso.with(mView.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image).into(mUserImage, new com.squareup.picasso.Callback(){

                    @Override
                    public void onSuccess() {
                        Log.i("COMPUTER","success");

                    }

                    @Override
                    public void onError() {
                        Log.i("COMPUTER","error");
                        Picasso.with(mView.getContext()).load(image).into(mUserImage);
                    }
                });
            }
        }
    }
}
