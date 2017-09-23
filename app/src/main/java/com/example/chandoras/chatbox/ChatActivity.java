package com.example.chandoras.chatbox;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String mChatUser;
    private String mChatUserName;

    private Toolbar mChatToolbar;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mUserImageView;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private ImageButton chatAddButton,chatSendButton;
    private EditText chatMessageEditText;

    private RecyclerView mMessagesRecyclerView;
    private List<MessageModel> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;

    private MessageAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        chatAddButton = (ImageButton)findViewById(R.id.chat_add_btn);
        chatSendButton = (ImageButton)findViewById(R.id.chat_send_btn);
        chatMessageEditText = (EditText)findViewById(R.id.chat_msg_et);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();


        mChatUser = getIntent().getStringExtra("userId");
        mChatUserName = getIntent().getStringExtra("userName");

        mMessagesRecyclerView = (RecyclerView)findViewById(R.id.chat_recycler_view) ;
        mLinearLayout = new LinearLayoutManager(this);

        mAdapter = new MessageAdapter(messagesList);

        mMessagesRecyclerView.setHasFixedSize(true);
        mMessagesRecyclerView.setLayoutManager(mLinearLayout);

        mMessagesRecyclerView.setAdapter(mAdapter);
        loadMessages();


        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customActionView = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(customActionView);

        //----------Custom action view-----------------

        mTitleView = (TextView)findViewById(R.id.custom_display_name);
        mLastSeenView = (TextView)findViewById(R.id.custom_last_seen);
        mUserImageView = (CircleImageView) findViewById(R.id.custom_image);

        mTitleView.setText(mChatUserName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String onlineStatus = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                if (onlineStatus.equals("true")){
                    mLastSeenView.setText("Online");

                }else {
                    long lastTime = Long.parseLong(onlineStatus);

                    String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);
                }

                if (!image.equals("default")){
                    Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.default_image).into(mUserImageView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mRootRef.child("Chat").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + currentUserId + "/" + mChatUser,chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + currentUserId,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError !=  null){
                                Log.i("ERROR",databaseError.toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        chatAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        chatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();

            }
        });



    }

    private void loadMessages() {
        mRootRef.child("Messages").child(currentUserId).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MessageModel message  = dataSnapshot.getValue(MessageModel.class);

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String message = chatMessageEditText.getText().toString();

               if( !TextUtils.isEmpty(message)){
                   String current_user_ref = "Messages/" + currentUserId + "/" + mChatUser;
                   String chat_user_ref = "Messages/" + mChatUser + "/" + currentUserId;

                   DatabaseReference user_message_push = mRootRef.child("Messages").child(currentUserId).child(mChatUser).push();

                   String push_key = user_message_push.getKey();
                   Map messageMap = new HashMap();
                   messageMap.put("message",message);
                   messageMap.put("seen",false);
                   messageMap.put("type ","text");
                   messageMap.put("time",ServerValue.TIMESTAMP);
                   messageMap.put("from",currentUserId);


                   Map messageUserMap =  new HashMap();
                   messageUserMap.put(current_user_ref + "/" + push_key,messageMap);
                   messageUserMap.put(chat_user_ref + "/" + push_key,messageMap);

                   chatMessageEditText.setText(" ");

                   mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError !=null){
                                Log.i("CHAT",databaseError.toString());
                            }
                       }
                   });


        }
    }
}
