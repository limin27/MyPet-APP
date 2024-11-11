package com.example.mypet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class emergency extends AppCompatActivity {

    private static final int REQUEST_CALL_PERMISSION = 1;
    private Button ViewMoreButton, ContactButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        ViewMoreButton = findViewById(R.id.view_btn);
        ViewMoreButton.setOnClickListener((v -> {
            String url = "https://headsupfortails.com/blogs/all/6-first-aid-tips-that-can-save-your-pet-s-life";

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }));

        ContactButton = findViewById(R.id.contactBtn);
        ContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePhoneCall();
            }
        });
    }

    private void makePhoneCall() {
        String number = "011-12724482";
        if (ContextCompat.checkSelfPermission(emergency.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(emergency.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void openProfile(View view) {
        startActivity(new Intent(this, userProfile.class));
    }

    public void openDashboard(View view) {
        startActivity(new Intent(this, Dashboard.class));
    }

    public void openFeedback(View view) { startActivity(new Intent(this, Feedback.class)); }
}
