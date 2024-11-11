package com.example.mypet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateProfile extends AppCompatActivity {

    private EditText editUsername, editContact, editEmail;
    private Button buttonUpdate;
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private DatabaseReference referenceProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        editUsername = findViewById(R.id.username);
        editContact = findViewById(R.id.contact);
        editEmail = findViewById(R.id.email);
        buttonUpdate = findViewById(R.id.update_btn);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(UpdateProfile.this, "Something went wrong! User's details are not available.", Toast.LENGTH_LONG).show();
        } else {
            showUserProfile(firebaseUser);
        }

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users").child(userID);
        referenceProfile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    String username = readUserDetails.userName;
                    String contact = readUserDetails.contactNo;
                    String email = firebaseUser.getEmail();

                    editUsername.setText(username);
                    editContact.setText(contact);
                    editEmail.setText(email);
                } else {
                    Toast.makeText(UpdateProfile.this, "Something went wrong! User's details are not available.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfile.this, "Something went wrong! User's details are not available.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateProfile() {
        String newUsername = editUsername.getText().toString().trim();
        String newContact = editContact.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();

        if (newUsername.isEmpty()) {
            editUsername.setError("Username is required");
            editUsername.requestFocus();
            return;
        }

        if (newContact.isEmpty()) {
            editContact.setError("Contact number is required");
            editContact.requestFocus();
            return;
        }

        if (newEmail.isEmpty()) {
            editEmail.setError("Email is required");
            editEmail.requestFocus();
            return;
        }

        ReadWriteUserDetails updatedUserDetails = new ReadWriteUserDetails(newUsername, newContact);

            referenceProfile.setValue(updatedUserDetails).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(UpdateProfile.this, "Profile updated successfully", Toast.LENGTH_LONG).show();
                    // Navigate back to ProfileActivity
                    Intent intent = new Intent(UpdateProfile.this, userProfile.class);
                    startActivity(intent);
                    finish(); // Finish the UpdateProfile activity
                } else {
                    Toast.makeText(UpdateProfile.this, "Profile update failed", Toast.LENGTH_LONG).show();
                }
            });
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
