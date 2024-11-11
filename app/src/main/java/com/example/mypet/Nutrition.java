package com.example.mypet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Nutrition extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private ArrayList<String> arrayList;
    private NutritionAdapter nutritionAdapter;
    private EditText editText;
    private ItemTouchHelper itemTouchHelper;
    private static final int REQUEST_CODE_EDIT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference().child("foods");
        mAuth = FirebaseAuth.getInstance();

        ImageButton btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Nutrition.this, userProfile.class);
            startActivity(intent);
        });

        ImageButton btnFeedback = findViewById(R.id.btnFeedback);
        btnFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(Nutrition.this, Feedback.class);
            startActivity(intent);
        });

        ImageButton btnInfo = findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(v -> {
            Intent intent = new Intent(Nutrition.this, Feedback.class);
            startActivity(intent);
        });

        setupWebsiteButtons();

        recyclerView = findViewById(R.id.foodList);
        editText = findViewById(R.id.textAddFood);
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        arrayList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        nutritionAdapter = new NutritionAdapter(arrayList, this);
        recyclerView.setAdapter(nutritionAdapter);

        fetchDataFromFirebase();
    }

    private void setupWebsiteButtons() {
        findViewById(R.id.websiteFood1).setOnClickListener(this::openFoodWebsite1);
        findViewById(R.id.websiteFood2).setOnClickListener(this::openFoodWebsite2);
        findViewById(R.id.websiteFood3).setOnClickListener(this::openFoodWebsite3);
        findViewById(R.id.websiteFood4).setOnClickListener(this::openFoodWebsite4);
        findViewById(R.id.websiteFood5).setOnClickListener(this::openFoodWebsite5);
        findViewById(R.id.websiteFood6).setOnClickListener(this::openFoodWebsite6);
        findViewById(R.id.btnInfo).setOnClickListener(this::openInfoWebsite);
    }

    private void fetchDataFromFirebase() {
        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Get the UID of the current user
            String uid = currentUser.getUid();

            // DatabaseReference pointing to the "foods" node under the user's UID
            DatabaseReference userFoodsRef = mDatabase.child(uid).child("foods");

            userFoodsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    arrayList.clear();
                    for (DataSnapshot foodSnapshot : dataSnapshot.getChildren()) {
                        String food = foodSnapshot.getValue(String.class);
                        arrayList.add(food);
                    }
                    nutritionAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle possible errors.
                }
            });
        } else {
            // No user is currently signed in
            Toast.makeText(this, "No user is currently signed in.", Toast.LENGTH_SHORT).show();
        }
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            String removedFood = arrayList.get(position);

            // Temporarily remove the item from the list
            arrayList.remove(position);
            nutritionAdapter.notifyItemRemoved(position);

            // Show a Snackbar with an undo option
            Snackbar snackbar = Snackbar.make(recyclerView, "Food removed", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", v -> {
                // Undo the removal by adding the item back to the list and notifying the adapter
                arrayList.add(position, removedFood);
                nutritionAdapter.notifyItemInserted(position);
                recyclerView.scrollToPosition(position); // Scroll to the restored item
            });
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    // If the snackbar is dismissed by any other means than the "UNDO" action,
                    // remove the item from the database
                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        // Get the current user
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            // Get the UID of the current user
                            String uid = currentUser.getUid();

                            // Find the correct food item to delete based on its value
                            DatabaseReference userFoodsRef = mDatabase.child(uid).child("foods");
                            userFoodsRef.orderByValue().equalTo(removedFood).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        snapshot.getRef().removeValue()
                                                .addOnSuccessListener(aVoid -> {
                                                    // Food removed successfully
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Failed to remove food
                                                    Toast.makeText(Nutrition.this, "Failed to remove food", Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle possible errors.
                                    Toast.makeText(Nutrition.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            });
            snackbar.show();
        }
    };

    private void removeFoodFromDatabase(String foodToRemove) {
        mDatabase.orderByValue().equalTo(foodToRemove).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue()
                            .addOnSuccessListener(aVoid -> {
                                // Food removed successfully
                            })
                            .addOnFailureListener(e -> {
                                // Failed to remove food
                                Toast.makeText(Nutrition.this, "Failed to remove food", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
                Toast.makeText(Nutrition.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void addFood(View view) {
        String food = editText.getText().toString();
        if (!food.isEmpty()) {
            // Get the current user
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // Get the UID of the current user
                String uid = currentUser.getUid();

                // Create a new DatabaseReference pointing to a child node named "foods" under the user's UID
                DatabaseReference userFoodRef = mDatabase.child(uid).child("foods").push();

                // Set the value of the new node with the food name
                userFoodRef.setValue(food);

                editText.setText("");
            } else {
                // No user is currently signed in
                Toast.makeText(this, "No user is currently signed in.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT && resultCode == RESULT_OK && data != null) {
            String updatedText = data.getStringExtra("updated_key");
            // Update the data and UI as needed
            if (updatedText != null) {
                // Get the current user
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // Get the UID of the current user
                    String uid = currentUser.getUid();

                    // DatabaseReference pointing to the "foods" node under the user's UID
                    DatabaseReference userFoodsRef = mDatabase.child(uid).child("foods");

                    userFoodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // Iterate through the list of foods
                            for (DataSnapshot foodSnapshot : dataSnapshot.getChildren()) {
                                String foodKey = foodSnapshot.getKey();
                                String foodValue = foodSnapshot.getValue(String.class);
                                // If the value matches the old text, update it with the new text
                                if (foodValue != null && foodValue.equals(updatedText)) {
                                    userFoodsRef.child(foodKey).setValue(updatedText);
                                    break; // Assuming each food has a unique value, so no need to continue iterating
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle possible errors.
                        }
                    });
                }
            }
        }
    }

    public void openDetailsActivity(String text) {
        Intent intent = new Intent(this, NutritionDetails.class);
        intent.putExtra("key", text);
        startActivity(intent);
    }

    public void openFoodWebsite1(View view) {
        openWebsite("https://itdoesnttastelikechicken.com/easy-homemade-dog-treats/");
    }

    public void openFoodWebsite2(View view) {
        openWebsite("https://wholefully.com/healthy-homemade-dog-treats/");
    }

    public void openFoodWebsite3(View view) {
        openWebsite("https://sustainableslowliving.com/homemade-cat-treats/");
    }

    public void openFoodWebsite4(View view) {
        openWebsite("https://supakit.co/blogs/cat-guides/homemade-tuna-cat-treats-your-cat-will-go-crazy-for");
    }

    public void openFoodWebsite5(View view) {
        openWebsite("https://andy.pet/blogs/all/top-3-rabbit-treats-to-make-at-home");
    }

    public void openFoodWebsite6(View view) {
        openWebsite("https://petfables.com.sg/blogs/pf-pet-wiki/10-best-diy-home-baked-treat-recipes-for-hamsters");
    }

    public void openInfoWebsite(View view) {
        openWebsite("https://www.diamondpet.com/blog/performance/nutrition-performance/mealtime-matters-for-pets/");
    }

    private void openWebsite(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openDashboard(View view) {
        startActivity(new Intent(this, Dashboard.class));
    }

    public void openProfile(View view) {
        startActivity(new Intent(this, userProfile.class));
    }

    public void openEmergency(View view) {
        startActivity(new Intent(this, emergency.class));
    }
}