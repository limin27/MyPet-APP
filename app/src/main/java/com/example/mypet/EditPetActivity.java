package com.example.mypet;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import com.google.firebase.database.Query;

public class EditPetActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText petName, gender, age, breed;
    private ImageView petImageView;
    private Button updatePetButton, deletePetButton;
    private ImageButton healthButton;
    private Uri imageUri;
    private String petId, imageUrl;
    private DatabaseReference petsRef;
    private StorageReference storageRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pet);

        petName = findViewById(R.id.petName);
        gender = findViewById(R.id.gender);
        age = findViewById(R.id.age);
        breed = findViewById(R.id.breed);
        petImageView = findViewById(R.id.petImageView);
        updatePetButton = findViewById(R.id.add_pet_button);
        updatePetButton.setText("Update Pet");
        deletePetButton = findViewById(R.id.delete_pet_button);
        healthButton = findViewById(R.id.health);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        petsRef = FirebaseDatabase.getInstance().getReference("pets").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("pet_images").child(currentUser.getUid());

        // Get the pet details from the intent
        Intent intent = getIntent();
        petId = intent.getStringExtra("petId");
        String name = intent.getStringExtra("petName");
        String petGender = intent.getStringExtra("petGender");
        String petAge = intent.getStringExtra("petAge");
        String petBreed = intent.getStringExtra("petBreed");
        imageUrl = intent.getStringExtra("petImageUrl");

        petName.setText(name);
        gender.setText(petGender);
        age.setText(petAge);
        breed.setText(petBreed);
        Picasso.with(this).load(imageUrl).into(petImageView);

        petImageView.setOnClickListener(v -> chooseImage());

        updatePetButton.setOnClickListener(v -> updatePetData());

        deletePetButton.setOnClickListener(v -> {
            // Call a method to delete the pet from the database and storage
            deletePet();
            // Switch to Dashboard activity
            Intent intent1 = new Intent(EditPetActivity.this, Dashboard.class);
            startActivity(intent1);
        });

        healthButton.setOnClickListener(v -> {
            // Switch to HealthActivity
            Intent intent12 = new Intent(EditPetActivity.this, HealthActivity.class);
            intent12.putExtra("petId", petId);
            startActivity(intent12);
        });


    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                petImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePetData() {
        if (imageUri != null) {
            // If a new image is selected, delete the old image first
            deleteOldImage();

            // Upload the new image
            final StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Update pet data in the database with the new image URL
                updatePetInDatabase(uri.toString());
                Toast.makeText(EditPetActivity.this, "Pet updated successfully", Toast.LENGTH_SHORT).show();
            })).addOnFailureListener(e -> {
                Toast.makeText(EditPetActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
        } else {
            // If no new image is selected, simply update pet data in the database
            updatePetInDatabase(imageUrl);
            Toast.makeText(EditPetActivity.this, "Pet updated successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePetInDatabase(String imageUrl) {
        Pet updatedPet = new Pet(petId, petName.getText().toString(), gender.getText().toString(), age.getText().toString(), breed.getText().toString(), imageUrl);
        petsRef.child(petId).setValue(updatedPet).addOnSuccessListener(aVoid -> {
            setResult(RESULT_OK);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(EditPetActivity.this, "Failed to update pet data", Toast.LENGTH_SHORT).show();
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void deletePet() {
        petsRef.child(petId).removeValue().addOnSuccessListener(aVoid -> {
            // Pet data deleted successfully
            deleteAssociatedData();
            deleteImageFromStorage();

            Toast.makeText(EditPetActivity.this, "Pet deleted successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(EditPetActivity.this, "Failed to delete pet", Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteAssociatedData() {
        // Delete associated medication data
        DatabaseReference medicationRef = FirebaseDatabase.getInstance().getReference().child("medications");
        Query medicationQuery = medicationRef.orderByChild("petId").equalTo(petId);
        medicationQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot medicationSnapshot : dataSnapshot.getChildren()) {
                    medicationSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditPetActivity.this, "Failed to delete medications", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete associated vaccination data
        DatabaseReference vaccinationRef = FirebaseDatabase.getInstance().getReference().child("vaccinations");
        Query vaccinationQuery = vaccinationRef.orderByChild("petId").equalTo(petId);
        vaccinationQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot vaccinationSnapshot : dataSnapshot.getChildren()) {
                    vaccinationSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditPetActivity.this, "Failed to delete vaccinations", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete associated recording data
        // Inside deleteAssociatedData() method
        DatabaseReference recordingRef = FirebaseDatabase.getInstance().getReference().child("recordings");
        Query recordingQuery = recordingRef.orderByChild("petId").equalTo(petId);
        recordingQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot recordingSnapshot : dataSnapshot.getChildren()) {
                    recordingSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                        // Delete associated record from storage
                        deleteRecordFromStorage(recordingSnapshot.getKey());
                    }).addOnFailureListener(e -> {
                        // Handle any errors
                        Toast.makeText(EditPetActivity.this, "Failed to delete recording data", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditPetActivity.this, "Failed to delete recordings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteImageFromStorage() {
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        imageRef.delete().addOnSuccessListener(aVoid -> {
            // Image deleted successfully
            Toast.makeText(EditPetActivity.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(EditPetActivity.this, "Failed to delete image", Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteRecordFromStorage(String recordId) {
        // Construct the reference to the storage location of the record
        StorageReference recordRef = FirebaseStorage.getInstance().getReference().child("recordings").child(recordId);

        // Delete the record from Firebase Storage
        recordRef.delete().addOnSuccessListener(aVoid -> {
            // Record deleted successfully
            Toast.makeText(EditPetActivity.this, "Record deleted successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(EditPetActivity.this, "Failed to delete record", Toast.LENGTH_SHORT).show();
        });
    }


    private void deleteOldImage() {
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        imageRef.delete().addOnSuccessListener(aVoid -> {
            // Old image deleted successfully
            Toast.makeText(EditPetActivity.this, "Old image deleted successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(EditPetActivity.this, "Failed to delete old image", Toast.LENGTH_SHORT).show();
        });
    }

    public void openDashboard(View view) {
        startActivity(new Intent(this, Dashboard.class));
    }

    public void openProfile(View view) {
        startActivity(new Intent(this, userProfile.class));
    }

    public void openFeedback(View view) { startActivity(new Intent(this, Feedback.class)); }

    public void openEmergency(View view) {
        startActivity(new Intent(this, emergency.class));
    }
}

