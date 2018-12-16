package com.example.talong.sharingapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private TextView txtProfileFullName,txtProfileUserName,txtProfileStatus,txtProfileCountry,txtProfileDate,txtProfileGender,txtProfileRelationship;
    private CircleImageView circleImageView;
    private DatabaseReference mDBRef, mDBFriendRef,mDBPostRef;
    private FirebaseAuth mAuth;
    private String currendUserId;
    private Button btnMyPost,btnMyFriend;
    private int countFriends=0,countPosts=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Init();
        mAuth=FirebaseAuth.getInstance();
        currendUserId=mAuth.getCurrentUser().getUid();
        mDBRef=FirebaseDatabase.getInstance().getReference().child("Users").child(currendUserId);
        mDBFriendRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        mDBPostRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        mDBRef.addValueEventListener(new ValueEventListener() {
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
                    txtProfileUserName.setText(userName);
                    txtProfileFullName.setText(profileName);
                    txtProfileCountry.setText("Country: "+Country);
                    txtProfileDate.setText("Birthday: "+Dob);
                    txtProfileGender.setText("Gender: "+Gender);
                    txtProfileStatus.setText(profileStatus);
                    txtProfileRelationship.setText("Relationship: "+relationshipStatus);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnMyFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToFriendActivity();
            }
        });
        btnMyPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });
        mDBPostRef.orderByChild("uid")
                .startAt(currendUserId).endAt(currendUserId + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            countPosts=(int) dataSnapshot.getChildrenCount();
                            btnMyPost.setText(Integer.toString(countPosts) + " Posts");
                        }
                        else
                        {
                            btnMyPost.setText("0 Post");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        mDBFriendRef.child(currendUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    countFriends=(int) dataSnapshot.getChildrenCount();
                    btnMyFriend.setText(Integer.toString(countFriends)+ " Friends");
                }
                else {
                    btnMyFriend.setText("0 Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void Init() {
        txtProfileFullName=findViewById(R.id.txtProfileFullName);
        txtProfileUserName=findViewById(R.id.txtProfileUserName);
        txtProfileStatus=findViewById(R.id.txtProfileStatus);
        txtProfileCountry=findViewById(R.id.txtProfileCountry);
        txtProfileDate=findViewById(R.id.txtDateOfBirth);
        txtProfileGender=findViewById(R.id.txtGender);
        txtProfileRelationship=findViewById(R.id.txtRelationShip);
        circleImageView=findViewById(R.id.my_profile_pic);
        btnMyPost=findViewById(R.id.btnMyPost);
        btnMyFriend=findViewById(R.id.btnMyFriend);
    }
    private void SendUserToFriendActivity() {
        Intent loginIntent=new Intent(ProfileActivity.this,FriendsActivity.class);

        startActivity(loginIntent);

    }
    private void SendUserToPostActivity() {
        Intent loginIntent=new Intent(ProfileActivity.this,MyPostActivity.class);

        startActivity(loginIntent);

    }
}
