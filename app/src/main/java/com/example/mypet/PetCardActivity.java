package com.example.mypet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class PetCardActivity extends AppCompatActivity {

    private ImageButton petImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_card);

        petImageButton = findViewById(R.id.petImageButton);

        petImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PetCardActivity.this, EditPetActivity.class);
                // Pass any additional data if necessary
                startActivity(intent);
            }
        });
    }
}