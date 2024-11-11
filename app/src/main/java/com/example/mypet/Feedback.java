package com.example.mypet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypet.databinding.ActivityFeedbackBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Feedback extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private RecyclerView recyclerView;
    private ArrayList<FeedbackItem> arrayList;
    private FeedbackAdapter feedbackAdapter;
    private EditText editText;
    private ImageButton uploadPic;
    private ItemTouchHelper itemTouchHelper;
    private static final int REQUEST_CODE_EDIT = 1;

    private static final int CAMERA_PERMISSION_CODE = 1;
    private ActivityFeedbackBinding feedbackBinding;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedbackBinding = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(feedbackBinding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference().child("feedbacks");

        imageUri = createUri();
        registerPictureLauncher();

        feedbackBinding.uploadFeedbackPic.setOnClickListener(v -> {
            checkCameraPermissionAndOpenCamera();
        });

        ImageButton btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Feedback.this, userProfile.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.feedbackList);
        editText = findViewById(R.id.textAddFeedback);
        uploadPic = findViewById(R.id.uploadFeedbackPic);
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        arrayList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        feedbackAdapter = new FeedbackAdapter(arrayList, this);
        recyclerView.setAdapter(feedbackAdapter);

        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("feedback");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String feedbackKey = snapshot.getKey();
                    String feedbackText = snapshot.child("text").getValue(String.class);
                    String imageUriString = snapshot.child("imageUri").getValue(String.class);
                    Uri imageUri = (imageUriString != null) ? Uri.parse(imageUriString) : null;
                    arrayList.add(new FeedbackItem(feedbackText, imageUri, feedbackKey));
                }
                feedbackAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            FeedbackItem removedFeedbackItem = arrayList.get(position);
            String removedFeedback = removedFeedbackItem.getFeedbackText(); // Get the feedback text

            // Temporarily remove the item from the list
            arrayList.remove(position);
            feedbackAdapter.notifyItemRemoved(position);

            // Show a Snackbar with an undo option
            Snackbar snackbar = Snackbar.make(recyclerView, "Feedback removed", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", v -> {
                // Undo the removal by adding the item back to the list and notifying the adapter
                arrayList.add(position, removedFeedbackItem);
                feedbackAdapter.notifyItemInserted(position);
                recyclerView.scrollToPosition(position); // Scroll to the restored item
            });
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    // If the snackbar is dismissed by any other means than the "UNDO" action,
                    // remove the item from the database
                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        removeFeedbackFromDatabase(removedFeedback);
                    }
                }
            });
            snackbar.show();
        }
    };

    private void removeFeedbackFromDatabase(String feedbackToRemove) {
        Query query = mDatabase.orderByChild("text").equalTo(feedbackToRemove);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue()
                            .addOnSuccessListener(aVoid -> {
                                // Feedback removed successfully
                            })
                            .addOnFailureListener(e -> {
                                // Failed to remove feedback
                                Toast.makeText(Feedback.this, "Failed to remove feedback", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
                Toast.makeText(Feedback.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void addFeedback(View view) {
        String feedback = editText.getText().toString();
        if (!feedback.isEmpty()) {
            Toast.makeText(this, "Uploading. Please wait.", Toast.LENGTH_LONG).show();
            // Generate a unique filename based on timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp;

            // Create a reference to the Firebase Storage location
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images").child(imageFileName + ".jpg");

            // Upload the image to Firebase Storage
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, get the download URL
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Save the download URL and feedback text to Firebase Realtime Database
                            String imageUrl = uri.toString();
                            DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference().child("feedback").push();
                            feedbackRef.child("text").setValue(feedback);
                            feedbackRef.child("imageUri").setValue(imageUrl);

                            // Add new item at the top
                            arrayList.add(0, new FeedbackItem(feedback, Uri.parse(imageUrl)));
                            feedbackAdapter.notifyItemInserted(0);
                            recyclerView.scrollToPosition(0); // Scroll to the top to show the new item
                            editText.setText("");
                            feedbackBinding.uploadFeedbackPic.setImageResource(R.drawable.icons8_camera_50); // Reset the image button
                            imageUri = createUri(); // Reset the image URI
                        });
                    })
                    .addOnFailureListener(exception -> {
                        // Handle image upload failure
                        Toast.makeText(this, "Image upload failed. Please capture an image.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public void deleteItem(View view) {
        String text = editText.getText().toString();
        if (!text.isEmpty()) {
            for (int i = 0; i < arrayList.size(); i++) {
                if (arrayList.get(i).getFeedbackText().equals(text)) {
                    arrayList.remove(i);
                    feedbackAdapter.notifyItemRemoved(i);
                    editText.setText("");
                    break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT && resultCode == RESULT_OK && data != null) {
            String updatedText = data.getStringExtra("updated_key");
            // Update the data and UI as needed
            // This example assumes that the updated text replaces the first item
            if (updatedText != null) {
                FeedbackItem updatedItem = arrayList.get(0);
                updatedItem.setFeedbackText(updatedText);
                feedbackAdapter.notifyItemChanged(0);
            }
        }
    }

    public void openDetailsActivity(String text) {
        Intent intent = new Intent(this, FeedbackDetails.class);
        intent.putExtra("key", text);
        startActivity(intent);
    }

    private Uri createUri(){
        // Generate a unique filename based on timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Create a File object with the unique filename
        File imageFile = new File(getApplicationContext().getFilesDir(), imageFileName + ".jpg");

        // Return the URI for the file
        return FileProvider.getUriForFile(
                getApplicationContext(),
                "com.example.mypet.fileProvider",
                imageFile
        );
    }


    public void registerPictureLauncher() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if (result){
                            feedbackBinding.uploadFeedbackPic.setImageURI(null);
                            feedbackBinding.uploadFeedbackPic.setImageURI(imageUri);
                        }
                    }
                }
        );
    }

    private void checkCameraPermissionAndOpenCamera(){
        if (ActivityCompat.checkSelfPermission(Feedback.this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Feedback.this,
                    new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            takePictureLauncher.launch(imageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                takePictureLauncher.launch(imageUri);
            } else {
                Toast.makeText(this, "Camera permission denied, please allow permission to take picture.", Toast.LENGTH_SHORT).show();
            }
        }
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