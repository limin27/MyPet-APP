package com.example.mypet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FeedbackDetails extends AppCompatActivity {

    private EditText editNewFeedback;
    private DatabaseReference mDatabase;
    private String feedbackKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_details);

        editNewFeedback = findViewById(R.id.editNewFeedback);

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference().child("feedback");

        // Receive the feedback key
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("key")) {
            feedbackKey = intent.getStringExtra("key");
            loadFeedbackDetails(feedbackKey);
        }

        ImageButton btnUpdate = findViewById(R.id.btnUpdateFeedback);
        btnUpdate.setOnClickListener(v -> updateFeedbackItem());
    }

    private void loadFeedbackDetails(String key) {
        DatabaseReference feedbackRef = mDatabase.child(key);
        feedbackRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String feedbackText = dataSnapshot.child("text").getValue(String.class);
                editNewFeedback.setText(feedbackText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void updateFeedbackItem() {
        String updatedFeedback = editNewFeedback.getText().toString().trim();
        if (!updatedFeedback.isEmpty()) {
            // Update the value of the feedback item in the Firebase database
            DatabaseReference feedbackRef = mDatabase.child(feedbackKey);
            feedbackRef.child("text").setValue(updatedFeedback)
                    .addOnSuccessListener(aVoid -> {
                        // Notify the user that the feedback item has been updated
                        Toast.makeText(FeedbackDetails.this, "Feedback updated successfully", Toast.LENGTH_SHORT).show();
                        // Close the activity
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Notify the user if the update fails
                        Toast.makeText(FeedbackDetails.this, "Failed to update feedback", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Notify the user to enter a valid feedback
            Toast.makeText(this, "Please enter a valid feedback", Toast.LENGTH_SHORT).show();
        }
    }
}