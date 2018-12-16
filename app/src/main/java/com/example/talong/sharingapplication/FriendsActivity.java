package com.example.talong.sharingapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.talong.sharingapplication.Model.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {
    private RecyclerView rvFriendList;
    private DatabaseReference mDBFriendRef,mDBUserRef;
    private FirebaseAuth mAuth;
    private String online_user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        mAuth=FirebaseAuth.getInstance();
        online_user_id=mAuth.getCurrentUser().getUid();
        mDBFriendRef=FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        mDBUserRef=FirebaseDatabase.getInstance().getReference().child("Users");
        rvFriendList=findViewById(R.id.rvFriendList);
        rvFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }

    private void DisplayAllFriends() {
        FirebaseRecyclerAdapter<Friends,FriendViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Friends, FriendViewHolder>(
                Friends.class,
                R.layout.all_user_display_layout,
                FriendViewHolder.class,
                mDBFriendRef
        ) {
            @Override
            protected void populateViewHolder(final FriendViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());
                final String userId=getRef(position).getKey();
                mDBUserRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            final String userName=dataSnapshot.child("fullname").getValue().toString();
                            final String profileImage=dataSnapshot.child("profileimage").getValue().toString();

                            viewHolder.setFullname(userName);
                            viewHolder.setProfileImage(profileImage);
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[]=new CharSequence[]{
                                        userName+ "'s Profile",
                                            "Send Message"
                                    };
                                    AlertDialog.Builder builder=new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Option")
                                            .setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which)
                                                    {
                                                        case 0:
                                                            Intent profileIntent=new Intent(FriendsActivity.this,PersonProfileActivity.class);
                                                            profileIntent.putExtra("UserId",userId);
                                                            startActivity(profileIntent);
                                                            break;
                                                        case 1:
                                                            Intent messageIntent=new Intent(FriendsActivity.this,ChatActivity.class);
                                                            messageIntent.putExtra("UserId",userId);
                                                            messageIntent.putExtra("UserName",userName);

                                                            startActivity(messageIntent);
                                                            break;
                                                    }

                                                }

                                            });
                                    builder.show();

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        rvFriendList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder{
        private View mView;
        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
        }
        public void setProfileImage(String profileImage){
            CircleImageView circleImageView=mView.findViewById(R.id.imgAllUser);
            Picasso.get().load(profileImage).placeholder(R.drawable.user).into(circleImageView);
        }
        public void setFullname(String fullname){
            TextView txtFullname=mView.findViewById(R.id.txtAllUserName);
            txtFullname.setText(fullname);
        }
        public void setDate(String date){
            TextView friendDate=mView.findViewById(R.id.txtAllUserStatus);
            friendDate.setText("Friend since: "+date);
        }

    }
}
