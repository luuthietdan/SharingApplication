package com.example.talong.sharingapplication;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity implements View.OnClickListener{
    ImageView imgPost;
    Button btnDeletePost, btnEditPost;
    EditText edtPost;
    private String postKey, currentUserId, databaseUserId, Status, Image;
    private DatabaseReference mDBRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);
        Init();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        postKey = getIntent().getExtras().get("Postkey").toString();
        btnDeletePost.setVisibility(View.INVISIBLE);
        btnEditPost.setVisibility(View.INVISIBLE);
        btnDeletePost.setOnClickListener(this);
        btnEditPost.setOnClickListener(this);
        mDBRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        mDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if (dataSnapshot.exists()){
                  Status = dataSnapshot.child("status").getValue().toString();
                  Image = dataSnapshot.child("postimage").getValue().toString();
                  databaseUserId = dataSnapshot.child("uid").getValue().toString();
                  if (Status.isEmpty()){
                      Toast.makeText(ClickPostActivity.this, "Don't have status.", Toast.LENGTH_SHORT).show();
                  }
                  else {
                      edtPost.setText(Status);
                  }

                  Picasso.get().load(Image).into(imgPost);
                  if (currentUserId.equals(databaseUserId)) {
                      btnDeletePost.setVisibility(View.VISIBLE);
                      btnEditPost.setVisibility(View.VISIBLE);

                  }
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void Init() {
        imgPost = findViewById(R.id.post_image);
        btnDeletePost = findViewById(R.id.btnDeletePost);
        btnEditPost = findViewById(R.id.btnEditPost);
        edtPost = findViewById(R.id.edtStatus);
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.btnDeletePost:
                mDBRef.removeValue();
                SendUserToMainActivity();
                Toast.makeText(ClickPostActivity.this, "Post has been deleted.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnEditPost:
                EditStatus(Status);
                break;
                default: break;
        }
    }

    private void EditStatus(String status) {
        AlertDialog.Builder builder=new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post:");
        final EditText inputField=new EditText(ClickPostActivity.this);
        inputField.setText(status);
        builder.setView(inputField);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDBRef.child("status").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post update successfully...", Toast.LENGTH_SHORT).show();
                SendUserToMainActivity();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog=builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    private void SendUserToMainActivity() {
        Intent loginIntent=new Intent(ClickPostActivity.this,MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
