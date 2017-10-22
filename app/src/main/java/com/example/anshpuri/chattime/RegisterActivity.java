package com.example.anshpuri.chattime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    TextInputLayout reg_display_name;
    TextInputLayout reg_email;
    TextInputLayout reg_password;
    Button reg_create_btn;
    DatabaseReference Database;

    private FirebaseAuth mAuth;
    Toolbar register_toolbar;
    ProgressDialog Reg_Progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        reg_display_name= (TextInputLayout) findViewById(R.id.login_email);
        reg_email = (TextInputLayout) findViewById(R.id.login_password);
        reg_password = (TextInputLayout) findViewById(R.id.reg_password);
        reg_create_btn = (Button) findViewById(R.id.reg_create_btn);
         register_toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(register_toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Reg_Progress  = new ProgressDialog(this);




        mAuth = FirebaseAuth.getInstance();



        reg_create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayname = reg_display_name.getEditText().getText().toString();
                String email = reg_email.getEditText().getText().toString();
                String password = reg_password.getEditText().getText().toString();



                if(!TextUtils.isEmpty(displayname) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password))
                {
                    Reg_Progress.setTitle("Registering User");
                    Reg_Progress.setMessage("Please wait till we create your account !");
                    Reg_Progress.setCanceledOnTouchOutside(false);
                    Reg_Progress.show();
                    register_user(displayname , email , password);
                }

            }
        });
    }

    public void register_user(final String display_name , final String email , final String password) {


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("", "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {
                            FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currentuser.getUid();

                            Database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            HashMap<String , String> users = new HashMap<String, String>();
                            users.put("name" , display_name);
                            users.put("status" , "Hi there !");
                            users.put("image" , "default");
                            users.put("device_token" , FirebaseInstanceId.getInstance().getToken());
                            users.put("thumb_image" , "default");
                            Database.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Reg_Progress.dismiss();
                                        Intent mainintent = new Intent ( RegisterActivity.this , MainActivity.class);
                                        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainintent);
                                        finish();
                                    }
                                }
                            });


                        }
                        else
                        {
                            Reg_Progress.hide();
                            Toast.makeText(RegisterActivity.this, "failed" + " " + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}

