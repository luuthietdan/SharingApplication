package com.example.talong.sharingapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.talong.sharingapplication.Adapter.MessagesAdapter;
import com.example.talong.sharingapplication.Model.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton imgAddPhoto,imgSendMessage;
    private EditText edtInputMessage;
    private RecyclerView rvMessage;
    private String messageReceiverId,messageReceiverName,messageSenderId,saveCurrentData,saveCurrentTime;
    private TextView txtUserName;
    private CircleImageView imgChatProfile;
    private DatabaseReference mDBRootRef;
    private FirebaseAuth mAuth;
    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Init();
        mAuth=FirebaseAuth.getInstance();
        messageSenderId=mAuth.getCurrentUser().getUid();
        mDBRootRef=FirebaseDatabase.getInstance().getReference();
        setSupportActionBar(mToolbar);
       messageReceiverId=getIntent().getExtras().get("UserId").toString();
        messageReceiverName=getIntent().getExtras().get("UserName").toString();
//        messageImage=getIntent().getExtras().get("ProfileImage").toString();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(action_bar_view);
      // DisplayInfoFriend();


        imgSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
        FetchMessages();
    }

    private void FetchMessages() {
        mDBRootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()){
                            Messages messages=dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messagesAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage() {
        String messageText=edtInputMessage.getText().toString();
        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(ChatActivity.this, "Please write a message...", Toast.LENGTH_SHORT).show();
        }
        else {
            String message_sender_ref="Messages/"+messageSenderId + "/" + messageReceiverId;
            String message_receiver_ref="Messages/"+messageReceiverId + "/" +messageSenderId;
            DatabaseReference user_message_key=mDBRootRef.child("Messages").child(messageSenderId)
                    .child(messageReceiverId).push();
            String message_push_id=user_message_key.getKey();
            Calendar calendar=Calendar.getInstance();
            SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentData=currentDate.format(calendar.getTime());

            Calendar calTime=Calendar.getInstance();
            SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm aa");
            saveCurrentTime=currentTime.format(calTime.getTime());

            Map messageTextBody=new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentData);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);

            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(message_sender_ref +"/"+ message_push_id , messageTextBody);
            messageBodyDetails.put(message_receiver_ref +"/"+ message_push_id , messageTextBody);

            mDBRootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                        edtInputMessage.setText("");
                    }
                    else {
                        String message=task.getException().getMessage();
                        Toast.makeText(ChatActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        edtInputMessage.setText("");
                    }

                }
            });
        }
    }

//    private void DisplayInfoFriend() {
//        txtUserName.setText("Name: " +messageReceiverName);
//        mDBRootRef.child("Users").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//
//                    final String image=dataSnapshot.child("profileimage").getValue().toString();
//                    Picasso.get().load(image).placeholder(R.drawable.circleimage).into(imgChatProfile);
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }


    private void Init() {
        mToolbar=findViewById(R.id.chat_app_bar);
        imgAddPhoto=findViewById(R.id.imgAddPhoto);
        imgSendMessage=findViewById(R.id.imgSendMessage);
        edtInputMessage=findViewById(R.id.edtInputMessage);
        rvMessage=findViewById(R.id.rvMessage);
        txtUserName=findViewById(R.id.txtChatUserName);
        imgChatProfile=findViewById(R.id.imgChatUserProfile);
        messagesAdapter=new MessagesAdapter(messagesList);
        linearLayoutManager=new LinearLayoutManager(this);
        rvMessage.setHasFixedSize(true);
        rvMessage.setLayoutManager(linearLayoutManager);
        rvMessage.setAdapter(messagesAdapter);

    }
}
