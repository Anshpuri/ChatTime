package com.example.anshpuri.chattime;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

   TextView profile_display_name, profile_status  , profile_totalfriends;
    ImageView profile_img;
    Button send_request , decline_btn;
    String current_state;

    DatabaseReference friendrequestdatabase;

    DatabaseReference friendDatabase;

    DatabaseReference notificationdatabase;
    
    FirebaseUser currentuser;

    DatabaseReference UsersDatabase;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        UsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        profile_img = (ImageView) findViewById(R.id.profile_img);
        profile_display_name = (TextView) findViewById(R.id.profile_display_name);
        profile_status = (TextView) findViewById(R.id.profile_status);
        send_request = (Button) findViewById(R.id.send_request);
        decline_btn = (Button) findViewById(R.id.decline_btn);
        profile_totalfriends = (TextView) findViewById(R.id.profile_totalfriends);

        current_state = "not_friends";
        decline_btn.setVisibility(View.INVISIBLE);
        decline_btn.setEnabled(false);


        friendrequestdatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        currentuser = FirebaseAuth.getInstance().getCurrentUser();

        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationdatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("Please wait while we load the user data");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();





        UsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String img = dataSnapshot.child("image").getValue().toString();

                profile_display_name.setText(display_name);
                profile_status.setText(status);
                Picasso.with(getApplicationContext()).load(img).placeholder(R.drawable.avatar).into(profile_img);


                friendrequestdatabase.child(currentuser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id))
                        {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received"))
                            {
                                current_state= "req_received";
                            send_request.setText("Accept Friend Request");

                                decline_btn.setVisibility(View.VISIBLE);
                                decline_btn.setEnabled(true);


                            }

                            else if(req_type.equals(("sent")))
                            {
                                current_state = "req_sent";
                                send_request.setText("Cancel Friend Request");

                                decline_btn.setVisibility(View.INVISIBLE);
                                decline_btn.setEnabled(false);

                            }


                            progressDialog.dismiss();
                        }
                        else
                        {
                            friendDatabase.child(currentuser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id))
                                    {
                                        current_state = "friends";
                                        send_request.setText("Unfriend this person");

                                        decline_btn.setVisibility(View.INVISIBLE);
                                        decline_btn.setEnabled(false);


                                    }
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    progressDialog.dismiss();
                                }
                            });
                        }

                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        send_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                send_request.setEnabled(false);


                if(current_state.equals("not_friends"))
                {

                    friendrequestdatabase.child(currentuser.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        friendrequestdatabase.child(user_id).child(currentuser.getUid()).child("request_type")
                                                 .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {



                                                HashMap<String , String> notificationdata = new HashMap<>();
                                                notificationdata.put("from" , currentuser.getUid());
                                                notificationdata.put("type" , "request");

                                                notificationdatabase.child(user_id).push().setValue(notificationdata).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        current_state = "req_sent";
                                                        send_request.setText("Cancel Friend Request");

                                                        decline_btn.setVisibility(View.INVISIBLE);
                                                        decline_btn.setEnabled(false);
                                                    }
                                                });




                                                Toast.makeText(ProfileActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            });
                }
                else
                {
                    Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                }



            if(current_state.equals("req_sent"))
              {
                friendrequestdatabase.child(currentuser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendrequestdatabase.child(user_id).child(currentuser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                send_request.setEnabled(true);
                                current_state = "Not Friends";
                                send_request.setText("Send Friend Request");
                                decline_btn.setVisibility(View.INVISIBLE);
                                decline_btn.setEnabled(false);
                            }
                        });
                    }
                });
              }

              if(current_state.equals("req_received"))
              {
                  final String currentDate  = DateFormat.getDateTimeInstance().format(new Date());
                  friendDatabase.child(currentuser.getUid()).child(user_id).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void aVoid) {

                          friendDatabase.child(user_id).child(currentuser.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                              @Override
                              public void onSuccess(Void aVoid) {

                                  friendrequestdatabase.child(currentuser.getUid()).child(user_id).child("request_type")
                                          .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task) {
                                          if(task.isSuccessful())
                                          {
                                              friendrequestdatabase.child(user_id).child(currentuser.getUid()).child("request_type")
                                                      .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                  @Override
                                                  public void onSuccess(Void aVoid) {

                                                      send_request.setEnabled(true);
                                                      current_state = "friends";
                                                      send_request.setText("Unfriend");

                                                      decline_btn.setVisibility(View.INVISIBLE);
                                                      decline_btn.setEnabled(false);

                                                      Toast.makeText(ProfileActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                                                  }
                                              });
                                          }
                                      }
                                  });

                              }
                          });
                      }
                  });
              }



            }


        });



    }


}
