package com.example.chandoras.chatbox;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView mFriendsList;


    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendsDatabase;
    private FirebaseAuth mAuth;

    String mCurrentUserId;
    View mMainView;

    public FriendsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends,container,false);

        mFriendsList =(RecyclerView) mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mFriendsDatabase.keepSynced(true);
        mUserDatabase  = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);


        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,R.layout.user_profile,FriendsViewHolder.class,mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());

                final String currentUserID = getRef(position).getKey();

                mUserDatabase.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String user_name = dataSnapshot.child("name").getValue().toString();
                        String user_status = dataSnapshot.child("status").getValue().toString();
                        String user_image = dataSnapshot.child("thumb_image").getValue().toString();



                        if (dataSnapshot.hasChild("online")) {
                            String user_online = dataSnapshot.child("online").getValue().toString();

                            viewHolder.userOnlineImage(user_online);
                        }
                        viewHolder.setName(user_name);
                        viewHolder.setImage(user_image,getContext());


                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence []options = new CharSequence[]{"Open Profile","Send message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select option");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int position) {
                                        if (position == 0){
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("userId", currentUserID);
                                            startActivity(profileIntent);
                                        }else if(position == 1){
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("userId", currentUserID);
                                            chatIntent.putExtra("userName",user_name);
                                            startActivity(chatIntent);

                                        }
                                    }
                                });
                                 builder.show();

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mFriendsList.setAdapter(friendsAdapter);



        }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }
        public void setDate(String date){
            TextView userStatusView = (TextView)itemView.findViewById(R.id.user_status);
            userStatusView.setText(date);
        }
        public void setName(String name){
            TextView userNameView = (TextView)itemView.findViewById(R.id.user_name);
            userNameView.setText(name);
        }
        public  void setImage(String image, Context context){
            CircleImageView userImageView = (CircleImageView)itemView.findViewById(R.id.user_image);

            Picasso.with(context).load(image).placeholder(R.drawable.default_image).into(userImageView);

        }
        public void userOnlineImage(String online_info){
            ImageView userOnline = (ImageView)mView.findViewById(R.id.user_online);

            if (online_info.equals("true")){
                userOnline.setVisibility(View.VISIBLE);
            }else {
                userOnline.setVisibility(View.INVISIBLE);
            }
        }
    }
}
