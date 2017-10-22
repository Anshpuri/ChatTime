package com.example.anshpuri.chattime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    Toolbar status_app_bar_layout;
    TextInputLayout status_input;
    Button status_save_btn;

   DatabaseReference statusdatabase;

    FirebaseUser currentuser;

    ProgressDialog statusprogress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        status_app_bar_layout = (Toolbar) findViewById(R.id.status_app_bar_layout);
        setSupportActionBar(status_app_bar_layout);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        status_input= (TextInputLayout) findViewById(R.id.status_input);
        status_save_btn = (Button) findViewById(R.id.status_save_btn);

        currentuser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentuser.getUid();

        statusdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        String status = getIntent().getStringExtra("status");
        status_input.getEditText().setText(status);
        status_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                statusprogress = new ProgressDialog(StatusActivity.this);
                 statusprogress.setTitle("Saving Changes");
                statusprogress.setMessage("Please wait");
                statusprogress.setCanceledOnTouchOutside(false);
                statusprogress.show();

                String status = status_input.getEditText().getText().toString();

                statusdatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            statusprogress.dismiss();
                            Toast.makeText(StatusActivity.this, "Status Saved", Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            statusprogress.hide();
                            Toast.makeText(StatusActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
    }
}
