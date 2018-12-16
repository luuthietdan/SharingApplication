package com.example.talong.sharingapplication;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.talong.sharingapplication.Model.Comments;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {
    private ImageButton imgSendComment;
    private EditText edtComment;
    private RecyclerView rvComment;
    private String Post_Key;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference mDBCommentRef, mPostRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key = getIntent().getExtras().get("Postkey").toString();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        mDBCommentRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");
        Init();
        rvComment.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvComment.setLayoutManager(linearLayoutManager);
        imgSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDBCommentRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String userName = dataSnapshot.child("username").getValue().toString();
                            ValidateComment(userName);
                            edtComment.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


    }

    private void Init() {
        imgSendComment = findViewById(R.id.imgSendComment);
        edtComment = findViewById(R.id.edtComment);
        rvComment = findViewById(R.id.rvComments);
    }


    private void ValidateComment(String userName) {

        String comment = edtComment.getText().toString();
        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(CommentsActivity.this, "Please write a comment...", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentData = currentDate.format(calendar.getTime());

            Calendar calTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calTime.getTime());

            final String ramdomkey = currentUserId + saveCurrentData + saveCurrentTime;

            HashMap commentMap = new HashMap();
            commentMap.put("uid", currentUserId);
            commentMap.put("comment", comment);
            commentMap.put("date", saveCurrentData);
            commentMap.put("time", saveCurrentTime);
            commentMap.put("username", userName);
            mPostRef.child(ramdomkey).updateChildren(commentMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CommentsActivity.this, "You comment successfully...", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(CommentsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Comments, CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentViewHolder>(
                Comments.class,
                R.layout.all_comments_layout,
                CommentViewHolder.class,
                mPostRef
        ) {
            @Override
            protected void populateViewHolder(CommentViewHolder viewHolder, Comments model, int position) {
                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
            }
        };
        rvComment.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setUsername(String username) {
            TextView txtCommentUsername = mView.findViewById(R.id.txtCommentUsername);
            txtCommentUsername.setText("@" + username + " ");
        }

        public void setDate(String date) {
            TextView txtDate = mView.findViewById(R.id.txtCommentDate);
            txtDate.setText("Date: " + date);
        }

        public void setTime(String time) {
            TextView txtTime = mView.findViewById(R.id.txtCommentTime);
            txtTime.setText("Time: " + time);
        }

        public void setComment(String comments) {
            TextView txtComment = mView.findViewById(R.id.txtCommentTextHere);
            txtComment.setText(comments);
        }
    }
}
