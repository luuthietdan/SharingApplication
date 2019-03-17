package com.example.talong.sharingapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
/*
* Ngày chỉnh sửa: 17/03/2019
* Người chỉnh sửa: Lâm Thành Long
* Nội dung chỉnh sửa: Fix bug khi đăng nhập
* */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private Button mLogin;
    private EditText edtEmail,edtPassword;
    private TextView mtxtNewAccount;
    private FirebaseAuth mAuthLamLong;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressDialog=new ProgressDialog(this);
        Init();
        mAuthLamLong=FirebaseAuth.getInstance();
        mtxtNewAccount.setOnClickListener(this);
        mLogin.setOnClickListener(this);
    }

    private void SendUserToRegisterActivity() {
        Intent registerIntent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
        finish();
    }

    private void Init() {
        mLogin=findViewById(R.id.btnLogin);
        edtEmail=findViewById(R.id.edtEmail);
        edtPassword=findViewById(R.id.edtPassword);
        mtxtNewAccount=findViewById(R.id.txtClick);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==mtxtNewAccount.getId()){
            SendUserToRegisterActivity();
        }
        if (v.getId()==mLogin.getId()){
           AllowingUserLogin();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuthLamLong.getCurrentUser();
        if (currentUser!=null){
            SendUserToMainActivity();
        }
    }

    private void AllowingUserLogin() {
        String email=edtEmail.getText().toString();
        String password=edtPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(LoginActivity.this, "Please Enter Email...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(LoginActivity.this, "Please Enter Password...", Toast.LENGTH_SHORT).show();
        }
        if(!password.equals(password)){
            Toast.makeText(LoginActivity.this, "Please Enter Password Again...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressDialog.setTitle("Log In");
            progressDialog.setMessage("Please waite, while we are authenticating account...");
            progressDialog.show();
            mAuthLamLong.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(LoginActivity.this, "You Login Successfully...", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else {
                        String message=task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent loginIntent=new Intent(LoginActivity.this,MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
