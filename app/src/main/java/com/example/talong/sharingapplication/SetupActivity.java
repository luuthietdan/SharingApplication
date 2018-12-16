package com.example.talong.sharingapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import org.w3c.dom.Text;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText edtUsername,edtFullname,edtCountry;
    private Button btnSave;
    private CircleImageView circleImageView;
    private FirebaseAuth mAuth;
    private DatabaseReference DBRef;
    private ProgressDialog progressDialog;
    private String CurrentUserId;
    final static int Gallery_Pick=1;
    private StorageReference UserProfileImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Init();
        btnSave.setOnClickListener(this);
        circleImageView.setOnClickListener(this);
        mAuth=FirebaseAuth.getInstance();
        CurrentUserId=mAuth.getCurrentUser().getUid();
        DBRef=FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserId);
        UserProfileImage=FirebaseStorage.getInstance().getReference().child("Profile Images");
        progressDialog=new ProgressDialog(this);
        DBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("profileimage")){
                        String image= dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.circleimage).into(circleImageView);
                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this, "Please select image first...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void Init() {
        edtUsername=findViewById(R.id.edtUserName);
        edtCountry=findViewById(R.id.edtCountry);
        edtFullname=findViewById(R.id.edtFullName);
        btnSave=findViewById(R.id.btnSave);
        circleImageView=findViewById(R.id.circleImage);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==btnSave.getId()){
           SaveAccountInformation();
        }
        if(v.getId()==circleImageView.getId()){
            getImageInProfile();
        }
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

                StorageReference filePath = UserProfileImage.child(CurrentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(SetupActivity.this, "Update Image Successfully.", Toast.LENGTH_SHORT).show();

                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();

                                    DBRef.child("profileimage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                        startActivity(selfIntent);

                                                        Toast.makeText(SetupActivity.this, "Update Image Successfully.", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SetupActivity.this, "Error:", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    }
    private void SaveAccountInformation() {
        String username=edtUsername.getText().toString();
        String fullname=edtFullname.getText().toString();
        String country=edtCountry.getText().toString();
        if(TextUtils.isEmpty(username)){
            Toast.makeText(SetupActivity.this, "Please Enter User Name...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(SetupActivity.this, "Please Enter Full Name...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(country)){
            Toast.makeText(SetupActivity.this, "Please Enter Country...", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.setTitle("Saving Account Information");
            progressDialog.setMessage("Please wait, when we are saving account info...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);
            HashMap userMap=new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("country",country);
            userMap.put("status","Hello everyone, i'm Dan. Nice to meet you.");
            userMap.put("gender","none");
            userMap.put("dob","none");
            userMap.put("relationshipstatus","none");
            DBRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){

                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account created successfully.", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else {
                        String message=task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }
    }


    private void SendUserToMainActivity() {
        Intent loginIntent=new Intent(SetupActivity.this,MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
