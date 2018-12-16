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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private CircleImageView circleImageView;
    private EditText edtProfileStatus,edtUsername,edtProfile,edtCountry,edtSettingBirth,edtSettingGender,edtSettingRelation;
    private Button btnSettingAccount;
    private DatabaseReference mDBRef;
    private FirebaseAuth mAuth;
    private StorageReference UserProfileImageRef;
    private String currentUserId;
    private static final int Gallery_Pick=1;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mToolbar=findViewById(R.id.setting_toolbar);
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        mDBRef=FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef=FirebaseStorage.getInstance().getReference().child("Profile Images");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Setting Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Init();
        progressDialog=new ProgressDialog(this);
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
                    edtUsername.setText(userName);
                    edtProfile.setText(profileName);
                    edtCountry.setText(Country);
                    edtSettingBirth.setText(Dob);
                    edtSettingGender.setText(Gender);
                    edtProfileStatus.setText(profileStatus);
                    edtSettingRelation.setText(relationshipStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnSettingAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VailidateAccount();
            }
        });
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageInProfile();
            }
        });
    }
    private void getImageInProfile() {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,Gallery_Pick);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        // Cuando se pulsa en el crop button
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                progressDialog.setTitle("Getting Image");
                progressDialog.setMessage("Please wait, while we are getting image in profile...");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(SettingActivity.this, "Update Image Successfully.", Toast.LENGTH_SHORT).show();

                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();

                                    mDBRef.child("profileimage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent selfIntent = new Intent(SettingActivity.this, SettingActivity.class);
                                                        startActivity(selfIntent);

                                                        Toast.makeText(SettingActivity.this, "Update Image Successfully.", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(SettingActivity.this, "Error:", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    }
    private void VailidateAccount() {
        String username=edtUsername.getText().toString();
        String fullname=edtProfile.getText().toString();
        String country=edtCountry.getText().toString();
        String  dob=edtSettingBirth.getText().toString();
        String status=edtProfileStatus.getText().toString();
        String relation=edtSettingRelation.getText().toString();
        String gender=edtSettingGender.getText().toString();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(SettingActivity.this, "Please enter user name...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(SettingActivity.this, "Please enter full name...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(country)) {
            Toast.makeText(SettingActivity.this, "Please enter country...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(dob)) {
            Toast.makeText(SettingActivity.this, "Please enter date of birth...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(status)) {
            Toast.makeText(SettingActivity.this, "Please enter your status...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(relation)) {
            Toast.makeText(SettingActivity.this, "Please enter your relationship...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(gender)) {
            Toast.makeText(SettingActivity.this, "Please enter your gender...", Toast.LENGTH_SHORT).show();
        }
        else {
            UpdateAccountInfo(username,fullname,country,dob,status,relation,gender);
        }
    }

    private void UpdateAccountInfo(String username, String fullname, String country, String dob, String status, String relation, String gender) {
        HashMap userMap=new HashMap();
        userMap.put("username",username);
        userMap.put("fullname",fullname);
        userMap.put("country",country);
        userMap.put("dob",dob);
        userMap.put("status",status);
        userMap.put("relationshipstatus",relation);
        userMap.put("gender",gender);
        mDBRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    SendUserToMainActivity();
                    Toast.makeText(SettingActivity.this, "You updated your account successfully.", Toast.LENGTH_SHORT).show();
                }
                else {
                    String message=task.getException().getMessage();
                    Toast.makeText(SettingActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void Init() {
        edtProfileStatus=findViewById(R.id.edtSettingStatus);
        edtUsername=findViewById(R.id.edtUsername);
        edtProfile=findViewById(R.id.edtSettingProfileName);
        edtCountry=findViewById(R.id.edtSettingCountry);
        edtSettingBirth=findViewById(R.id.edtSettingBirthday);
        edtSettingGender=findViewById(R.id.edtGender);
        edtSettingRelation=findViewById(R.id.edtRelationship);
        btnSettingAccount=findViewById(R.id.btnSettingUpdateAccount);
        circleImageView=findViewById(R.id.setting_profile_image);
    }

    private void SendUserToMainActivity() {
        Intent loginIntent=new Intent(SettingActivity.this,MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
