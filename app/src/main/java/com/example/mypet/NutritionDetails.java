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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NutritionDetails extends AppCompatActivity {

    private EditText editNewFood;
    private DatabaseReference mDatabase;
    private String foodKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_details);

        editNewFood = findViewById(R.id.editNewFood);

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Receive the current food item's value
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("key")) {
            foodKey = intent.getStringExtra("key");
            editNewFood.setText(foodKey);
        }

        ImageButton btnUpdate = findViewById(R.id.btnUpdateFood);
        btnUpdate.setOnClickListener(v -> updateFoodItem());
    }

    private void updateFoodItem() {
        String updatedFood = editNewFood.getText().toString().trim();
        if (!updatedFood.isEmpty()) {
            // Get the current user
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Get the UID of the current user
                String uid = currentUser.getUid();

                // Update the value of the food item in the Firebase database under the user's UID
                DatabaseReference userFoodsRef = mDatabase.child("foods").child(uid).child("foods");

                userFoodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String foodValue = snapshot.getValue(String.class);
                            if (foodValue != null && foodValue.equals(foodKey)) {
                                snapshot.getRef().setValue(updatedFood);
                                break;
                            }
                        }
                        // Notify the user that the food item has been updated
                        Toast.makeText(NutritionDetails.this, "Food schedule updated successfully", Toast.LENGTH_SHORT).show();
                        // Close the activity
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle possible errors.
                        Toast.makeText(NutritionDetails.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            // Notify the user to enter a valid food item
            Toast.makeText(this, "Please enter a valid food schedule", Toast.LENGTH_SHORT).show();
        }
    }
}
