package com.example.anshpuri.chattime;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.anshpuri.chattime.R.id.user_single_img;

public class UsersActivity extends AppCompatActivity {

    Toolbar users_appBar;
    RecyclerView users_list;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        users_appBar = (Toolbar) findViewById(R.id.users_appBar);
        setSupportActionBar(users_appBar);

        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        users_list = (RecyclerView) findViewById(R.id.users_list);

        users_list.setHasFixedSize(true);
        users_list.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users , UsersViewHolder> firebaseRecyclerAdapter  = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class ,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                databaseReference
                ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setDetails(model.getName(),  model.getStatus() , model.getThumb_image() , getApplicationContext());

                final String user_id = getRef(position).getKey();
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileintent = new Intent(UsersActivity.this , ProfileActivity.class);
                        profileintent.putExtra("user_id" , user_id);
                        startActivity(profileintent);
                    }
                });
            }
        };
        users_list.setAdapter(firebaseRecyclerAdapter);

    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder {
       View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView= itemView;
        }

        public void setDetails(String name , String status , String img , Context ctx)
        {
            TextView Username  = (TextView) mView.findViewById(R.id.user_single_name);
            Username.setText(name);
            TextView Userstatus =  (TextView) mView.findViewById(R.id.user_single_status);
            Userstatus.setText(status);

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_img);
            Picasso.with(ctx).load(img).placeholder(R.drawable.avatar).into(userImageView);
        }
    }



}
