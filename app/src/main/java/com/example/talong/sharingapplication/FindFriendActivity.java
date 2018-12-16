package com.example.talong.sharingapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.talong.sharingapplication.Model.FindFriends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar mToolbar;
    private ImageButton imgSearch;
    private EditText edtInputFriend;
    private RecyclerView rvListFriend;
    private DatabaseReference mDBFriendRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);
        Init();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Search Friends");
        rvListFriend.setHasFixedSize(true);
        rvListFriend.setLayoutManager(new LinearLayoutManager(this));
        imgSearch.setOnClickListener(this);
        mDBFriendRef=FirebaseDatabase.getInstance().getReference().child("Users");


    }

    private void Init() {
        mToolbar=findViewById(R.id.find_friend_toolbar);
        imgSearch=findViewById(R.id.imgSearchFriend);
        edtInputFriend=findViewById(R.id.edtSearchInput);
        rvListFriend=findViewById(R.id.rvListFriend);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgSearchFriend:
                String search=edtInputFriend.getText().toString();
                SearchFriendInfo(search);
                break;
                default: break;
        }
    }

    private void SearchFriendInfo(String search) {
        Toast.makeText(FindFriendActivity.this, "Searching...", Toast.LENGTH_SHORT).show();
        Query searchFriend=mDBFriendRef.orderByChild("fullname").startAt(search).endAt(search + "\uf8ff");
        FirebaseRecyclerAdapter<FindFriends,FindFriendViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<FindFriends, FindFriendViewHolder>(
                FindFriends.class,
                R.layout.all_user_display_layout,
                FindFriendViewHolder.class,
                searchFriend

        ) {
            @Override
            protected void populateViewHolder(FindFriendViewHolder viewHolder, FindFriends model, final int position) {
                viewHolder.setFullname(model.getFullname());
                viewHolder.setProfileImage(model.getProfileimage());
                viewHolder.setStatus(model.getStatus());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String userId=getRef(position).getKey();
                        Intent personIntent=new Intent(FindFriendActivity.this,PersonProfileActivity.class);
                        personIntent.putExtra("UserId",userId);
                        startActivity(personIntent);

                    }
                });
            }
        };
        rvListFriend.setAdapter(firebaseRecyclerAdapter);
    }
    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mView=itemView;
        }
        public void setProfileImage(String profileImage){
            CircleImageView circleImageView=mView.findViewById(R.id.imgAllUser);
            Picasso.get().load(profileImage).placeholder(R.drawable.user).into(circleImageView);
        }
        public void setFullname(String fullname){
            TextView txtFullname=mView.findViewById(R.id.txtAllUserName);
            txtFullname.setText(fullname);
        }

        public void setStatus(String status){
            TextView txtStatus=mView.findViewById(R.id.txtAllUserStatus);
            txtStatus.setText(status);
        }
    }
}
