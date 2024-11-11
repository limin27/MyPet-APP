package com.example.mypet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class userProfile extends AppCompatActivity {

    private TextView profileUsername, profileEmail, profileContact;
    private String username, email, contact;
    private ImageView imageView;
    private FirebaseAuth authProfile;
    private DatabaseReference referenceProfile;

    private Button deleteBtn;
    private String imageUrl; // Add this variable to store the image URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        profileUsername = findViewById(R.id.username);
        profileEmail = findViewById(R.id.email);
        profileContact = findViewById(R.id.phoneNo);
        imageView = findViewById(R.id.userImage);
        deleteBtn = findViewById(R.id.delete_btn);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser == null){
            Toast.makeText(userProfile.this, "Something went wrong! User's details are not available.", Toast.LENGTH_LONG).show();
        }else{
            showUserProfile(firebaseUser);
        }

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeletePicture();
            }
        });

    }

    private void confirmDeletePicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(userProfile.this);
        builder.setTitle("Delete Picture");
        builder.setMessage("Are you sure you want to delete your picture? This action is irreversible.");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePicture();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePicture() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            imageRef.delete().addOnSuccessListener(aVoid -> {
                // Image deleted successfully
                Toast.makeText(userProfile.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                imageView.setImageResource(R.drawable.image_round); // Optional: Set a default image or remove image
            }).addOnFailureListener(exception -> {
                // Handle any errors
                Toast.makeText(userProfile.this, "Failed to delete image", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(userProfile.this, "No image to delete", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUserProfile (FirebaseUser firebaseUser){
        String userID = firebaseUser.getUid();

        //Extracting User Reference from Database for "Registered User"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    username = readUserDetails.userName;
                    email = firebaseUser.getEmail();
                    contact = readUserDetails.contactNo;

                    profileEmail.setText(email);
                    profileUsername.setText(username);
                    profileContact.setText(contact);

                    //Set User DP (After user has uploaded)
                    Uri uri = firebaseUser.getPhotoUrl();
                    if (uri != null) {
                        imageUrl = uri.toString(); // Save the image URL for later use
                        Picasso.with(userProfile.this).load(uri).into(imageView);
                    }
                }else{
                    Toast.makeText(userProfile.this, "Something went wrong! User's details are not available.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(userProfile.this, "Something went wrong! User's details are not available.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void openUploadPic(View view) {
        startActivity(new Intent(this, UploadProfilePic.class));
    }

    public void openUpdate(View view) {
        startActivity(new Intent(this, UpdateProfile.class));
    }

    public void openEmergency(View view) {
        startActivity(new Intent(this, emergency.class));
    }

    public void logout(View view) {
        logoutMenu(userProfile.this);
    }

    private void logoutMenu(userProfile userProfile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(userProfile);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(userProfile.this, login.class);
                startActivity(intent);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void openDashboard(View view) {
        startActivity(new Intent(this, Dashboard.class));
    }

    public void openFeedback(View view) {
        startActivity(new Intent(this, Feedback.class));
    }
}
