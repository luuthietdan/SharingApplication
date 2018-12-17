package com.example.talong.sharingapplication;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.talong.sharingapplication.Model.Posts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView rvAllPostList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDBPostRef;
    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);
        Init();
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        mDBPostRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");
        rvAllPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvAllPostList.setLayoutManager(linearLayoutManager);
        DisplayAllMyPost();
    }

    private void DisplayAllMyPost() {
        Query myPostQuery=mDBPostRef.orderByChild("uid")
                .startAt(currentUserId).endAt(currentUserId + "\uf8ff");
        FirebaseRecyclerAdapter<Posts,MyPostViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Posts, MyPostViewHolder>(
                Posts.class,
                R.layout.all_post_layout,
                MyPostViewHolder.class,
                myPostQuery

        ) {
            @Override
            protected void populateViewHolder(MyPostViewHolder viewHolder, Posts model, int position) {
                viewHolder.setFullName(model.getFullname());
                viewHolder.setDate(model.getDate());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setTime(model.getTime());
                viewHolder.setProfileImage(model.getProfileimage());
                viewHolder.setPostImage(model.getPostimage());
            }
        };
        rvAllPostList.setAdapter(firebaseRecyclerAdapter);
    }

    private void Init() {
        mToolbar=findViewById(R.id.my_post_bar);
        rvAllPostList=findViewById(R.id.rvAllPostList);
    }
    public static class MyPostViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public MyPostViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
        }
        public void setFullName(String fullname){
            TextView username=mView.findViewById(R.id.txtPostUserName);
            username.setText(fullname);
        }
        public void setProfileImage(String profileImage){
            CircleImageView circleImageView=mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileImage).into(circleImageView);
        }
        public void setTime(String time){
            TextView txtTime=mView.findViewById(R.id.txtTime);
            txtTime.setText("  " +time);
        }
        public void setDate(String date){
            TextView txtDate=mView.findViewById(R.id.txtdate);
            txtDate.setText("  "+date);
        }
        public void setStatus(String status){
            TextView txtStatus=mView.findViewById(R.id.txtStatus);
            txtStatus.setText(status);
        }
        public void setPostImage(String postImage){
            ImageView imageView=mView.findViewById(R.id.imgPostImage);
            Picasso.get().load(postImage).into(imageView);
        }
    }
}
