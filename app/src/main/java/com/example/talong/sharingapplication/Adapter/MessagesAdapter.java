package com.example.talong.sharingapplication.Adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.talong.sharingapplication.Model.Messages;
import com.example.talong.sharingapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDBUserRef;

    public MessagesAdapter (List<Messages> userMessagesList){
        this.userMessagesList=userMessagesList;
    }

    public class MessageViewHolder  extends RecyclerView.ViewHolder{
        public TextView txtSendMessage,txtReceiveMessage;
        public CircleImageView img_Message_Profile;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSendMessage=itemView.findViewById(R.id.txtSendMessage);
            txtReceiveMessage=itemView.findViewById(R.id.txtReceiverMessage);
            img_Message_Profile=itemView.findViewById(R.id.img_message_profile);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v=LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_user,viewGroup,false);
        mAuth=FirebaseAuth.getInstance();
        return  new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        String messageSenderID=mAuth.getCurrentUser().getUid();
        Messages messages=userMessagesList.get(i);
        String fromUserId=messages.getFrom();
        String fromMessageType=messages.getType();
        mDBUserRef=FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        mDBUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String image=dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.get().load(image).into(messageViewHolder.img_Message_Profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (fromMessageType.equals("text")){
            messageViewHolder.txtReceiveMessage.setVisibility(View.INVISIBLE);
            messageViewHolder.img_Message_Profile.setVisibility(View.INVISIBLE);
            if (fromUserId.equals(messageSenderID))
            {
                messageViewHolder.txtSendMessage.setBackgroundResource(R.drawable.send_message_background);
                messageViewHolder.txtSendMessage.setTextColor(Color.WHITE);
                messageViewHolder.txtSendMessage.setGravity(Gravity.LEFT);
                messageViewHolder.txtSendMessage.setText(messages.getMessage());

            }
            else {
                messageViewHolder.txtSendMessage.setVisibility(View.INVISIBLE);
                messageViewHolder.txtReceiveMessage.setVisibility(View.VISIBLE);
                messageViewHolder.img_Message_Profile.setVisibility(View.VISIBLE);
                messageViewHolder.txtReceiveMessage.setBackgroundResource(R.drawable.receive_message_background);
                messageViewHolder.txtReceiveMessage.setTextColor(Color.WHITE);
                messageViewHolder.txtReceiveMessage.setGravity(Gravity.LEFT);
                messageViewHolder.txtReceiveMessage.setText(messages.getMessage());
            }
        }
    }



    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}
