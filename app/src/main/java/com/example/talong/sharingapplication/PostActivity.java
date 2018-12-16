package com.example.talong.sharingapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar mToolbar;
    private ImageView imgChoicePicture;
    private EditText edtStatus;
    private Button btnUpdatePost;
    private static final int Gallery_Pick=1;
    private Uri imgUri;
    private String Status;
    private StorageReference mStorageRef;
    private DatabaseReference mDBRef,mPostRef;
    private FirebaseAuth mAuth;
    private String saveCurrentData,saveCurrentTime,postRandomName,currentUserId,downloadUri;
    private Task<Uri> result;
    private ProgressDialog progressDialog;
    private long counPosts=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        mStorageRef=FirebaseStorage.getInstance().getReference();
        mDBRef=FirebaseDatabase.getInstance().getReference().child("Users");
        mPostRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        Init();
        imgChoicePicture.setOnClickListener(this);
        btnUpdatePost.setOnClickListener(this);
        progressDialog=new ProgressDialog(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update post");
    }

    private void Init() {
        mToolbar=findViewById(R.id.update_post);
        imgChoicePicture=findViewById(R.id.imgChoicePicture);
        edtStatus=findViewById(R.id.edtStatus);
        btnUpdatePost=findViewById(R.id.btnSubmit);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();

        switch (id){
            case android.R.id.home:
                SendUserToMainActivity();
                break;
                default: break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent=new Intent(PostActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.imgChoicePicture:
                OpenGallery();
                break;
            case R.id.btnSubmit:
                ValidatePostInfo();
                default: break;
        }
    }

    private void ValidatePostInfo() {
        Status=edtStatus.getText().toString();
        if(imgUri==null){
            Toast.makeText(PostActivity.this, "Please select image...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Status)){
            Toast.makeText(PostActivity.this, "Please say something about your image...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressDialog.setTitle("Add new post");
            progressDialog.setMessage("Please wait, while we are updating your new post...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);
            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage() {
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentData=currentDate.format(calendar.getTime());

        Calendar calTime=Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(calTime.getTime());
        postRandomName=saveCurrentData+saveCurrentTime;
        final StorageReference filePath=mStorageRef.child("Post Images").child(imgUri.getLastPathSegment()+postRandomName + ".jpg");
        filePath.putFile(imgUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                             downloadUri=uri.toString();
                            Toast.makeText(PostActivity.this, "Image uploaded successfully to storage...", Toast.LENGTH_SHORT).show();

                            SavePostInfoToDatabase();
                        }
                    });
                }
                else {
                    String message=task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SavePostInfoToDatabase() {
        mPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    counPosts=dataSnapshot.getChildrenCount();
                }
                else {
                    counPosts=0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDBRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String fullname=dataSnapshot.child("fullname").getValue().toString();
                    String profileImage=dataSnapshot.child("profileimage").getValue().toString();
                    HashMap postMap=new HashMap();
                    postMap.put("uid",currentUserId);
                    postMap.put("date",saveCurrentData);
                    postMap.put("time",saveCurrentTime);
                    postMap.put("status",Status);
                    postMap.put("postimage",downloadUri);
                    postMap.put("profileimage",profileImage);
                    postMap.put("fullname",fullname);
                    postMap.put("counter",counPosts);
                    mPostRef.child(currentUserId + postRandomName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        SendUserToMainActivity();
                                        Toast.makeText(PostActivity.this, "New post is updated successfully.", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                    else {
                                        Toast.makeText(PostActivity.this, "Error while updating", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallery() {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null){
            imgUri=data.getData();
            imgChoicePicture.setImageURI(imgUri);
        }
    }
}
