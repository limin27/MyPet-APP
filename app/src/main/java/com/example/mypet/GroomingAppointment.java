package com.example.mypet;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GroomingAppointment extends AppCompatActivity {
    private Button showDateBtn;
    private Button showTimeBtn;
    private Spinner serviceSpinner;
    private Button btnConfirm;
    private Button btnHistory;
    private ImageView home;

    private String selectedDate;
    private String selectedTime;
    private String selectedService;

    private DatabaseReference databaseRef;

    private RecyclerView recyclerView;
    private PetAdapter petAdapter;
    private ArrayList<Pet> petList;
    private DatabaseReference petsRef;
    private ImageButton addPetButton;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grooming_appointment);

        // Initialize Firebase Database reference
        databaseRef = FirebaseDatabase.getInstance().getReference("appointments");

        showDateBtn = findViewById(R.id.showDateBtn);
        showTimeBtn = findViewById(R.id.showTimeBtn);
        serviceSpinner = findViewById(R.id.serviceSpinner);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnHistory = findViewById(R.id.btnHistory);
        home = findViewById(R.id.imageView4);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = findViewById(R.id.pet_recycler_view);
        addPetButton = findViewById(R.id.add_pet);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true); // Optimizing RecyclerView performance

        petList = new ArrayList<>();
        petAdapter = new PetAdapter(petList, this);
        recyclerView.setAdapter(petAdapter);

        petsRef = FirebaseDatabase.getInstance().getReference("pets").child(currentUser.getUid());

        showDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDateDialog();
            }
        });

        showTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTimeDialog();
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroomingAppointment.this, Dashboard.class);
                startActivity(intent);
            }
        });

        // Populate the spinner with items and apply the custom layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinnerlayout, getResources().getStringArray(R.array.services_array));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceSpinner.setAdapter(adapter);

        serviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedService = parentView.getItemAtPosition(position).toString();
                Toast.makeText(GroomingAppointment.this, "Selected: " + selectedService, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAppointment();
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroomingAppointment.this, AppointmentHistory.class);
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
                        Log.w("GroomingAppointment", "Invalid Pet data");
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
                Log.e("GroomingAppointment", "Database error: " + databaseError.getMessage());
                Toast.makeText(GroomingAppointment.this, "Failed to load pets", Toast.LENGTH_SHORT).show();
            }
        });

        addPetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroomingAppointment.this, DialogPet.class);
                startActivity(intent);
            }
        });
    }

    private void openDateDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                selectedDate = String.format("%d.%02d.%02d", year, month + 1, day);
                showDateBtn.setText(selectedDate);
            }
        }, year, month, day);

        dialog.show();
    }

    private void openTimeDialog() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                showTimeBtn.setText(selectedTime);
            }
        }, hour, minute, true);

        dialog.show();
    }

    private void saveAppointment() {
        if (selectedDate != null && selectedTime != null && selectedService != null) {
            String userId = currentUser.getUid();
            String appointmentId = databaseRef.child(userId).push().getKey();
            Appointment appointment = new Appointment(selectedDate, selectedTime, selectedService);

            if (appointmentId != null) {
                appointment.setId(appointmentId); // Set the ID of the appointment
                databaseRef.child(userId).child(appointmentId).setValue(appointment);
                Toast.makeText(this, "Appointment saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create appointment ID", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please select date, time, and service", Toast.LENGTH_SHORT).show();
        }
    }

    public void openProfile(View view) {
        startActivity(new Intent(this, userProfile.class));
    }

    public void openEmergency(View view) {
        startActivity(new Intent(this, emergency.class));
    }

    public void openFeedback(View view) {
        startActivity(new Intent(this, Feedback.class));
    }

    // Appointment class to represent an appointment
    public static class Appointment {
        public String date;
        public String time;
        public String service;
        private String id; // Added the id field

        public Appointment() {
            // Default constructor required for calls to DataSnapshot.getValue(Appointment.class)
        }

        public Appointment(String date, String time, String service) {
            this.date = date;
            this.time = time;
            this.service = service;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
