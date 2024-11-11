package com.example.mypet;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Dashboard extends AppCompatActivity implements SensorEventListener {
    private ImageView home;
    SensorManager sensorManager;
    Sensor sensor;
    Context context;
    boolean success;
    private RecyclerView recyclerView;
    private PetAdapter petAdapter;
    private ArrayList<Pet> petList;
    private DatabaseReference petsRef;
    private ImageButton addPetButton;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        Button btnGroom = findViewById(R.id.btnGroom);
        Button btnVeterinarian = findViewById(R.id.btnVeterinarian);
        Button btnNutrition = findViewById(R.id.btnNutrition);
        home = findViewById(R.id.imageView4);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();

        if (currentUser == null) {
            Intent intent = new Intent(Dashboard.this, login.class);
            startActivity(intent);
            finish();
            return;
        }

        sensorManager=(SensorManager)getSystemService(Service.SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        recyclerView = findViewById(R.id.pet_recycler_view);
        addPetButton = findViewById(R.id.add_pet);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true); // Optimizing RecyclerView performance

        petList = new ArrayList<>();
        petAdapter = new PetAdapter(petList, this);
        recyclerView.setAdapter(petAdapter);

        petsRef = FirebaseDatabase.getInstance().getReference("pets").child(currentUser.getUid());


        btnGroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, GroomingAppointment.class);
                startActivity(intent);
            }
        });

        btnVeterinarian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, VeterinarianAppointment.class);
                startActivity(intent);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, Dashboard.class);
                startActivity(intent);
            }
        });

        petsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                petList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Pet pet = postSnapshot.getValue(Pet.class);
                    if (pet != null) {
                        petList.add(pet);
                    } else {
                        Log.w("Home", "Invalid Pet data");
                    }
                }
                petAdapter.notifyDataSetChanged();

                if (petList.isEmpty()) {
                    addPetButton.setVisibility(View.VISIBLE);
                } else {
                    addPetButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Dashboard", "Database error: " + databaseError.getMessage());
                Toast.makeText(Dashboard.this, "Failed to load pets", Toast.LENGTH_SHORT).show();
            }
        });

        addPetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, DialogPet.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(Dashboard.this, login.class);
            startActivity(intent);
            finish();
        }
    }

    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        context=getApplicationContext();
        if(event.sensor.getType()==Sensor.TYPE_LIGHT){
            if(event.values[0]<15){
                permission();
                setBrightness(240);
            }
            else if(event.values[0]>80){
                permission();
                setBrightness(50);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void permission(){
        boolean value;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            value= Settings.System.canWrite(getApplicationContext());
            if(value){
                success=true;
            }
            else{
                Intent intent= new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:"+getApplicationContext().getPackageName()));
                startActivityForResult(intent,100);
            }
        }
    }

    protected void onActivityResults(int requestCode, int resultCode, Intent intent){
        if(requestCode==100){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                boolean value=Settings.System.canWrite(getApplicationContext());
                if(value){
                    success=true;
                }
                else{
                    Toast.makeText(this, "Permission is not granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setBrightness(int brightness){
        if(brightness<0){
            brightness=0;
        }
        else if(brightness>255){
            brightness=255;
        }
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    public void openProfile(View view) {
        startActivity(new Intent(this, userProfile.class));
    }

    public void openEmergency(View view) {
        startActivity(new Intent(this, emergency.class));
    }

    public void openFeedback(View view) { startActivity(new Intent(this, Feedback.class)); }

    public void openNutrition(View view) { startActivity(new Intent(this, Nutrition.class)); }
}