package com.example.mypet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UploadProfilePic extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView imageView;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private Button btnUploadPic, btnChoosePic;
    private Uri uriImage;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_pic);

        btnChoosePic = findViewById(R.id.chooseBtn);
        btnUploadPic = findViewById(R.id.uploadBtn);
        imageView = findViewById(R.id.imageViewProfile);
        progressBar = findViewById(R.id.progressBar);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");
        Uri uri = firebaseUser.getPhotoUrl();

        Picasso.with(UploadProfilePic.this).load(uri).into(imageView);

        //Choosing image to upload
        btnChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        //Upload Image
        btnUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                UploadPic();
            }
        });
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData()!=null){
            uriImage = data.getData();
            imageView.setImageURI(uriImage);
        }
    }

    private void UploadPic() {
        if (uriImage != null) {
            //Save the image with uid ot the currently logged user
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid() + "." + getFileExtension(uriImage));

            //Upload the image to Storage
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            firebaseUser = authProfile.getCurrentUser();

                            // Finally set the display image of the user after upload
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(UploadProfilePic.this, "Upload Successful", Toast.LENGTH_LONG).show();

                                        // Redirect to user profile
                                        Intent intent = new Intent(UploadProfilePic.this, userProfile.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(UploadProfilePic.this, "Profile Update Failed!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(UploadProfilePic.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UploadProfilePic.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(UploadProfilePic.this, "No File Selected!", Toast.LENGTH_LONG).show();
        }
    }

    //Obtain the file extension of the image
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public void openEmergency(View view) {
        startActivity(new Intent(this, emergency.class));
    }

    public void openProfile(View view) {
        startActivity(new Intent(this, userProfile.class));
    }

    public void openDashboard(View view) {
        startActivity(new Intent(this, Dashboard.class));
    }

    public void openFeedback(View view) { startActivity(new Intent(this, Feedback.class)); }
}
