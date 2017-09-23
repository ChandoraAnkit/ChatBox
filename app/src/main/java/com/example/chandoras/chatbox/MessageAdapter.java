package com.example.chandoras.chatbox;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by chandoras on 9/23/17.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<MessageModel>messagesList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<MessageModel>messagesList){
        this.messagesList = messagesList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single,parent,false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
//        String current_user_id = mAuth.getCurrentUser().getUid();

        MessageModel messageModel = messagesList.get(position);

        String from_id  = messageModel.getFrom();

//        if (from_id.equals(current_user_id)){
//
//        }else {
//
//        }

        holder.messageText.setText(messageModel.getMessage());


    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public CircleImageView profileImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView)itemView.findViewById(R.id.message_name_layout);
            profileImage = (CircleImageView) itemView.findViewById(R.id.message_image_layout);

        }


    }
}
