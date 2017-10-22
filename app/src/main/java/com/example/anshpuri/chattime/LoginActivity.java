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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "TAG" ;
    TextInputLayout login_email , login_password;
    Toolbar login_toolbar;
    Button login_btn;
    ProgressDialog login_progress;
    private FirebaseAuth mAuth;
    private DatabaseReference UserDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login_toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(login_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");


        mAuth = FirebaseAuth.getInstance();

        UserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        login_email = (TextInputLayout) findViewById(R.id.login_email);
        login_password = (TextInputLayout) findViewById(R.id.login_password);
        login_btn = (Button) findViewById(R.id.login_btn);
        login_progress = new ProgressDialog(this);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email =login_email.getEditText().getText().toString();
                String password =login_password.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password))
                {
                    login_progress.setTitle("Logging In");
                    login_progress.setMessage("Please wait while we check your credentials");
                    login_progress.setCanceledOnTouchOutside(false);
                    login_progress.show();
                    loginUser(email , password);
                }
            }
        });

    }
    public void loginUser(String email , String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {

                            login_progress.dismiss();

                            String current_user_id = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                          UserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                              @Override
                              public void onSuccess(Void aVoid) {
                                  Intent mainIntent = new Intent(LoginActivity.this , MainActivity.class);
                                  mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                  startActivity(mainIntent);
                                  finish();
                              }
                          });



                        }
                        else {
                            login_progress.hide();
                            Toast.makeText(LoginActivity.this, "" + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }

                        // ...
                    }
                });
    }
}
