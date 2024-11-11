package com.example.mypet;

import android.app.Activity;
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
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.io.IOException;

public class DialogPet extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText petName, gender, age, breed;
    private ImageView petImageView;
    private Button addPetButton;
    private Uri imageUri;
    private DatabaseReference petsRef;
    private StorageReference storageRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_pet);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        petName = findViewById(R.id.petName);
        gender = findViewById(R.id.gender);
        age = findViewById(R.id.age);
        breed = findViewById(R.id.breed);
        petImageView = findViewById(R.id.petImageView);
        addPetButton = findViewById(R.id.add_pet_button);

        petsRef = FirebaseDatabase.getInstance().getReference("pets").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("pet_images").child(currentUser.getUid());

        petImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        addPetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPetData();
                Intent intent = new Intent(DialogPet.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                petImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadPetData() {
        if (imageUri != null) {
            final StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String id = petsRef.push().getKey();
                            Pet pet = new Pet(id, petName.getText().toString(), gender.getText().toString(), age.getText().toString(), breed.getText().toString(), uri.toString());
                            petsRef.child(id).setValue(pet);
                            setResult(Activity.RESULT_OK);
                            finish();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle the error
                }
            });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
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
