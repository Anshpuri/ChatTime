package com.example.anshpuri.chattime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {



    private DatabaseReference userdatabase;
    private FirebaseUser Currentuser;

    private CircleImageView settings_img;
    private TextView settings_display_name , settings_status;
    private Button settings_status_btn , settings_img_btn;

    private StorageReference mImgStorage;


    private static final int GALLERY_PICK = 1;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings_img = (CircleImageView) findViewById(R.id.settings_img);
        settings_display_name = (TextView) findViewById(R.id.settings_display_name);
        settings_status = (TextView) findViewById(R.id.settings_status);
        settings_status_btn= (Button) findViewById(R.id.settings_status_btn);
        settings_img_btn = (Button) findViewById(R.id.settings_img_btn);

        Currentuser = FirebaseAuth.getInstance().getCurrentUser();

        String current_uid = Currentuser.getUid();

        mImgStorage = FirebaseStorage.getInstance().getReference();

       userdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        userdatabase.keepSynced(true);

        userdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String img = dataSnapshot.child("image").getValue().toString();
                String thumb_img=dataSnapshot.child("thumb_image").getValue().toString();
                settings_display_name.setText(name);
                settings_status.setText(status);


                if(!img.equals("default")) {
//                    Picasso.with(SettingsActivity.this).load(img).placeholder(R.drawable.avatar).into(settings_img);
                    Picasso.with(SettingsActivity.this).load(img).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar).into(settings_img, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {


                            Picasso.with(SettingsActivity.this).load(img).placeholder(R.drawable.avatar).into(settings_img);
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       settings_status_btn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent status = new Intent(SettingsActivity.this , StatusActivity.class);
               status.putExtra("status" , settings_status.getText());
               startActivity(status);
           }
       });

        settings_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent galleryintent = new Intent();
                galleryintent.setType("image/*");
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);


                startActivityForResult(Intent.createChooser(galleryintent , "SELECT IMAGE") , GALLERY_PICK);
//
//                CropImage.activity()                                                                 does sm work as above bt crops as well
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(SettingsActivity.this);
            }


        });




    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode , resultCode , data);

        if (requestCode == GALLERY_PICK && resultCode==RESULT_OK) {
           Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog= new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Uploading Image...");
                progressDialog.setMessage("Please wait while we upload your image.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();


                String current_uid_id = Currentuser.getUid();
                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());


                Bitmap compressedImageBitmap = null;
                try {
                    compressedImageBitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)             //compressing and setting quality and all
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);   // steps for uploading bitmaps to firebase
                    final byte[] thumb_byte = baos.toByteArray();


                StorageReference filepath = mImgStorage.child("profile_images").child(current_uid_id +".jpg");
                final StorageReference thumb_filepath = mImgStorage.child("profile_images").child("thumbs").child(current_uid_id + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful())
                        {

                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_download_url = thumb_task.getResult().getDownloadUrl().toString();
                                    if(thumb_task.isSuccessful())
                                    {


                                        Map update_hashmap = new HashMap();
                                        update_hashmap.put("image" , download_url);
                                        update_hashmap.put("thumb_image" , thumb_download_url);


                                        userdatabase.updateChildren(update_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful())
                                                {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "uplosdes", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }
                                    else
                                    {
                                        Toast.makeText(SettingsActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                        }
                        else
                        {

                            progressDialog.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


//    public static String random() {
//        Random generator = new Random();
//        StringBuilder randomStringBuilder = new StringBuilder();
//        int randomLength = generator.nextInt(10);
//        char tempChar;
//        for (int i = 0; i < randomLength; i++){
//            tempChar = (char) (generator.nextInt(96) + 32);
//            randomStringBuilder.append(tempChar);
//        }
//        return randomStringBuilder.toString();
//    }
}
