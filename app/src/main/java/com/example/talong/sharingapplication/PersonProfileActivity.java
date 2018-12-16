package com.example.talong.sharingapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {
    private TextView txtUserName,txtFullName,txtUserStatus,txtUserGender,txtUserRelation,txtUserCountry,txtDateOfBirth;
    private CircleImageView circleImageView;
    private Button btnSendRequest,btnDeclineRequest;
    private FirebaseAuth mAuth;
    private DatabaseReference mDBUserRef,mDBFriendRequestRef,mDBFriendRef;
    private String sendUserId,receiveUserId, currentState;
    private String saveCurrentData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        Init();

        mAuth=FirebaseAuth.getInstance();
        sendUserId=mAuth.getCurrentUser().getUid();
        receiveUserId=getIntent().getExtras().get("UserId").toString();

        mDBUserRef=FirebaseDatabase.getInstance().getReference().child("Users");
        mDBFriendRequestRef=FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        mDBFriendRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        mDBUserRef.child(receiveUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String profileImage=dataSnapshot.child("profileimage").getValue().toString();
                    String userName=dataSnapshot.child("username").getValue().toString();
                    String profileName=dataSnapshot.child("fullname").getValue().toString();
                    String Gender=dataSnapshot.child("gender").getValue().toString();
                    String relationshipStatus=dataSnapshot.child("relationshipstatus").getValue().toString();
                    String profileStatus=dataSnapshot.child("status").getValue().toString();
                    String Dob=dataSnapshot.child("dob").getValue().toString();
                    String Country=dataSnapshot.child("country").getValue().toString();
                    Picasso.get().load(profileImage).into(circleImageView);
                    txtUserName.setText(userName);
                    txtFullName.setText(profileName);
                    txtUserCountry.setText("Country: "+Country);
                    txtDateOfBirth.setText("Birthday: "+Dob);
                    txtUserGender.setText("Gender: "+Gender);
                    txtUserStatus.setText(profileStatus);
                    txtUserRelation.setText("Relationship: "+relationshipStatus);
                    MaintanceofButton();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnDeclineRequest.setVisibility(View.INVISIBLE);
        btnDeclineRequest.setEnabled(false);
        if (!sendUserId.equals(receiveUserId)){
            btnSendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnSendRequest.setEnabled(false);
                    if (currentState.equals("not_friends")){
                        SendFriendRequestToPerson();
                    }
                    if (currentState.equals("request_sent")){
                        CancelFriendRequest();
                    }
                    if (currentState.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                    if (currentState.equals("friends")){
                        UnfriendAnExistingFriend();
                    }
                }
            });
        }
        else
        {
            btnDeclineRequest.setVisibility(View.INVISIBLE);
            btnSendRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void UnfriendAnExistingFriend() {
        mDBFriendRef.child(sendUserId).child(receiveUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mDBFriendRef.child(receiveUserId).child(sendUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                btnSendRequest.setEnabled(true);
                                                currentState="not_friends";
                                                btnSendRequest.setText("Send Friend Request");
                                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentData=currentDate.format(calendar.getTime());

        mDBFriendRef.child(sendUserId).child(receiveUserId).child("date").setValue(saveCurrentData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mDBFriendRef.child(receiveUserId).child(sendUserId).child("date").setValue(saveCurrentData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                mDBFriendRequestRef.child(sendUserId).child(receiveUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    mDBFriendRequestRef.child(receiveUserId).child(sendUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        btnSendRequest.setEnabled(true);
                                                                                        currentState="friends";
                                                                                        btnSendRequest.setText("Unfriend This Person");
                                                                                        btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                                                        btnDeclineRequest.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest() {
        mDBFriendRequestRef.child(sendUserId).child(receiveUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mDBFriendRequestRef.child(receiveUserId).child(sendUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                btnSendRequest.setEnabled(true);
                                                currentState="not_friends";
                                                btnSendRequest.setText("Send Friend Request");
                                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void MaintanceofButton() {

        mDBFriendRequestRef.child(sendUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiveUserId)){
                            String request_type=dataSnapshot.child(receiveUserId).child("request_type").getValue().toString();
                            if (request_type.equals("sent"))
                            {
                                currentState="request_sent";
                                btnSendRequest.setText("Cancel Friend Request");
                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                                btnDeclineRequest.setEnabled(false);
                            }
                            else if (request_type.equals("received")){
                                currentState="request_received";
                                btnSendRequest.setText("Accept Friend Request");

                                btnDeclineRequest.setVisibility(View.VISIBLE);
                                btnDeclineRequest.setEnabled(true);

                                btnDeclineRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelFriendRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            mDBFriendRef.child(sendUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiveUserId))
                                            {
                                                currentState="friends";
                                                btnSendRequest.setText("Unfriend This Person");

                                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineRequest.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendFriendRequestToPerson() {
        mDBFriendRequestRef.child(sendUserId).child(receiveUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mDBFriendRequestRef.child(receiveUserId).child(sendUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                btnSendRequest.setEnabled(true);
                                                currentState="request_sent";
                                                btnSendRequest.setText("Cancel Friend Request");
                                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void Init() {
        txtFullName=findViewById(R.id.txtPersonFullName);
        txtUserName=findViewById(R.id.txtPersonProfileUserName);
        txtUserCountry=findViewById(R.id.txtPersonCountry);
        txtUserGender=findViewById(R.id.txtPersonGender);
        txtUserStatus=findViewById(R.id.txtPersonStatus);
        txtUserRelation=findViewById(R.id.txtPersonRelationShip);
        txtDateOfBirth=findViewById(R.id.txtPersonDateOfBirth);
        circleImageView=findViewById(R.id.imgPersonProfile);
        btnSendRequest=findViewById(R.id.btnPersonSendRequest);
        btnDeclineRequest=findViewById(R.id.btnDeclineRequest);
        currentState="not_friends";
    }
}
