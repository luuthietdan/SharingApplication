package com.example.talong.sharingapplication;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.talong.sharingapplication.Model.Posts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView rvPostList;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference,mDBPostRef,mDBLikesRef,mDBLoveRef,mDBSmileRef;
    private CircleImageView circleImageView;
    private TextView txtUsername;
    private String currentId;
    private ImageButton imgAddPost;
    private Boolean likeChecker=false;
    private Boolean loveChecker=false;
    private Boolean smileChecker=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            currentId="";
        }
        else {
            currentId=firebaseAuth.getCurrentUser().getUid();
        }

        databaseReference=FirebaseDatabase.getInstance().getReference().child("Users");
        mDBPostRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        mDBLikesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        mDBLoveRef=FirebaseDatabase.getInstance().getReference().child("Loves");
        mDBSmileRef=FirebaseDatabase.getInstance().getReference().child("Smile");
        Init();
        rvPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvPostList.setLayoutManager(linearLayoutManager);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");
        navigationView = findViewById(R.id.nav_post);
        View view = navigationView.inflateHeaderView(R.layout.navigation_header);
        txtUsername=view.findViewById(R.id.txtUsername);
        circleImageView=view.findViewById(R.id.imgprofile);
        imgAddPost.setOnClickListener(this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        databaseReference.child(currentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("fullname"))
                    {
                        String fullname=dataSnapshot.child("fullname").getValue().toString();
                        txtUsername.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image=dataSnapshot.child("profileimage").getValue().toString();

                        Picasso.get().load(image).into(circleImageView);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });
        DisplayAllUserPosts();


    }

    private void DisplayAllUserPosts() {
        Query sortPost=mDBPostRef.orderByChild("counter");
            FirebaseRecyclerAdapter<Posts,PostViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Posts, PostViewHolder>
                    (
                        Posts.class,
                            R.layout.all_post_layout,
                            PostViewHolder.class,
                            sortPost
                    ) {
                @Override
                protected void populateViewHolder(PostViewHolder viewHolder, Posts model, int position) {
                    final String PostKey=getRef(position).getKey();
                    viewHolder.setFullName(model.getFullname());
                    viewHolder.setDate(model.getDate());
                    viewHolder.setStatus(model.getStatus());
                    viewHolder.setTime(model.getTime());
                    viewHolder.setProfileImage(model.getProfileimage());
                    viewHolder.setPostImage(model.getPostimage());
                    viewHolder.setLikeButtonStatus(PostKey);
                    viewHolder.setLoveButtonStatus(PostKey);
                    viewHolder.setSmileButtonStatus(PostKey);
                    viewHolder.imgCommentPost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent commentIntent=new Intent(MainActivity.this,CommentsActivity.class);
                            commentIntent.putExtra("Postkey",PostKey);
                            startActivity(commentIntent);
                        }
                    });
                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent clickPostIntent=new Intent(MainActivity.this,ClickPostActivity.class);
                            clickPostIntent.putExtra("Postkey",PostKey);
                            startActivity(clickPostIntent);
                        }
                    });
                    viewHolder.imgLikePost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            likeChecker=true;
                            mDBLikesRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                   if (likeChecker.equals(true))
                                   {
                                       if (dataSnapshot.child(PostKey).hasChild(currentId)){
                                           mDBLikesRef.child(PostKey).child(currentId).removeValue();
                                           likeChecker=false;
                                       }
                                       else {
                                           mDBLikesRef.child(PostKey).child(currentId).setValue(true);
                                           likeChecker=false;
                                       }
                                   }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                    viewHolder.imgLovePost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loveChecker=true;
                            mDBLoveRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (loveChecker.equals(true))
                                    {
                                        if (dataSnapshot.child(PostKey).hasChild(currentId)){
                                            mDBLoveRef.child(PostKey).child(currentId).removeValue();
                                            loveChecker=false;
                                        }
                                        else {
                                            mDBLoveRef.child(PostKey).child(currentId).setValue(true);
                                            loveChecker=false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                    viewHolder.imgSmilePost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            smileChecker=true;
                            mDBSmileRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (smileChecker.equals(true))
                                    {
                                        if (dataSnapshot.child(PostKey).hasChild(currentId)){
                                            mDBSmileRef.child(PostKey).child(currentId).removeValue();
                                            smileChecker=false;
                                        }
                                        else {
                                            mDBSmileRef.child(PostKey).child(currentId).setValue(true);
                                            smileChecker=false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });

                }
            };
            rvPostList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class PostViewHolder  extends RecyclerView.ViewHolder{
        ImageButton imgCommentPost,imgLikePost,imgLovePost,imgSmilePost;
        TextView txtLikePost,txtLovePost,txtSmilePost;
        View mView;
        int countLikes,countLoves,countSmile;


        String currentUserId;
        DatabaseReference mDBLikeRef,mDBLoveRef,mDBSmileRef;
        public PostViewHolder(View itemView){
            super(itemView);
            mView=itemView;
            imgLikePost=mView.findViewById(R.id.imgLikePost);
            imgCommentPost=mView.findViewById(R.id.imgComment);
            txtLikePost=mView.findViewById(R.id.txtScoreLikes);
            imgLovePost=mView.findViewById(R.id.imgLovePost);
            imgSmilePost=mView.findViewById(R.id.imgsmile);
            txtLovePost=mView.findViewById(R.id.txtScorelove);
            txtSmilePost=mView.findViewById(R.id.txtScoreSmile);
            mDBLikeRef=FirebaseDatabase.getInstance().getReference().child("Likes");
            mDBLoveRef=FirebaseDatabase.getInstance().getReference().child("Loves");
            mDBSmileRef=FirebaseDatabase.getInstance().getReference().child("Smile");
            currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();

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

        public void setLikeButtonStatus(final String postKey) {
            mDBLikeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserId)){
                        countLikes=(int) dataSnapshot.child(postKey).getChildrenCount();
                        imgLikePost.setImageResource(R.drawable.like);
                        txtLikePost.setText(Integer.toString(countLikes));


                    }
                    else
                    {
                        countLikes=(int) dataSnapshot.child(postKey).getChildrenCount();
                        imgLikePost.setImageResource(R.drawable.dislike);
                        txtLikePost.setText(Integer.toString(countLikes));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setLoveButtonStatus(final String postKey) {
            mDBLoveRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserId)) {
                        countLoves=(int) dataSnapshot.child(postKey).getChildrenCount();
                        imgLovePost.setImageResource(R.drawable.love);
                        txtLovePost.setText(Integer.toString(countLoves));
                    }
                    else {
                        countLoves=(int) dataSnapshot.child(postKey).getChildrenCount();
                        imgLovePost.setImageResource(R.drawable.dislove);
                        txtLovePost.setText(Integer.toString(countLoves));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        public void setSmileButtonStatus(final String postKey){
                mDBSmileRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(postKey).hasChild(currentUserId)) {
                            countSmile=(int) dataSnapshot.child(postKey).getChildrenCount();
                            imgSmilePost.setImageResource(R.drawable.smile);
                            txtSmilePost.setText(Integer.toString(countSmile));
                        }
                        else {
                            countSmile=(int) dataSnapshot.child(postKey).getChildrenCount();
                            imgSmilePost.setImageResource(R.drawable.dissmile);
                            txtSmilePost.setText(Integer.toString(countSmile));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        }
    }
    private void Init() {
        toolbar = findViewById(R.id.main_page_toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        rvPostList = findViewById(R.id.all_users_post_list);

        imgAddPost=findViewById(R.id.imgButtonAddPost);

    }

    @Override
    protected void onStart() {

        super.onStart();
        FirebaseUser currentUser=firebaseAuth.getCurrentUser();
        if (currentUser==null){
            SendUserToLoginActivity();
        }
        else{
            checkUserExistence();
        }
    }

    private void checkUserExistence() {
        final String currentUserId=firebaseAuth.getCurrentUser().getUid();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(currentUserId)){
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent=new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_newpost:
                SendToPostActivity();
                break;
            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;
            case R.id.nav_home:
                Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                SendUserToFriendActivity();
                break;
            case R.id.nav_findfriends:
                SendUserToFindFriendActivity();
                break;
            case R.id.nav_message:
                SendToMessageActivity();
                break;
            case R.id.nav_setting:
                SendUserToSettingActivity();
                break;
            case R.id.nav_logout:
                firebaseAuth.signOut();
                SendUserToLoginActivity();
                break;
            default:
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgButtonAddPost:
                SendToPostActivity();

                break;
                default: break;
        }
    }

    private void SendToPostActivity() {
        Intent postIntent=new Intent(MainActivity.this,PostActivity.class);
        startActivity(postIntent);
    }
    private void SendToMessageActivity() {
        Intent postIntent=new Intent(MainActivity.this,FriendsActivity.class);
        startActivity(postIntent);
    }
    private void SendUserToSettingActivity() {
        Intent loginIntent=new Intent(MainActivity.this,SettingActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

    }
    private void SendUserToProfileActivity() {
        Intent loginIntent=new Intent(MainActivity.this,ProfileActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

    }
    private void SendUserToFriendActivity() {
        Intent loginIntent=new Intent(MainActivity.this,FriendsActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

    }
    private void SendUserToFindFriendActivity() {
        Intent loginIntent=new Intent(MainActivity.this,FindFriendActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

    }
}
