package com.example.mypet;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class EditAppointment extends AppCompatActivity {
    private Button showDateBtn;
    private Button showTimeBtn;
    private Spinner serviceSpinner;
    private Button btnDone;
    private ImageView home;

    private String selectedDate;
    private String selectedTime;
    private String selectedService;

    private DatabaseReference databaseRef;
    private String appointmentId;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_appointment);

        // Retrieve the appointment ID passed from the previous activity
        appointmentId = getIntent().getStringExtra("APPOINTMENT_ID");

        if (appointmentId == null) {
            Toast.makeText(this, "Appointment ID is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            databaseRef = FirebaseDatabase.getInstance().getReference("appointments").child(currentUser.getUid()).child(appointmentId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showDateBtn = findViewById(R.id.showDateBtn);
        showTimeBtn = findViewById(R.id.showTimeBtn);
        serviceSpinner = findViewById(R.id.serviceSpinner);
        btnDone = findViewById(R.id.btnDone);
        home = findViewById(R.id.imageView4);

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
                Intent intent = new Intent(EditAppointment.this, Dashboard.class);
                startActivity(intent);
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinnerlayout, getResources().getStringArray(R.array.services_array));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceSpinner.setAdapter(adapter);

        serviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedService = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAppointment();
            }
        });

        loadAppointmentDetails();
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

    private void loadAppointmentDetails() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GroomingAppointment.Appointment appointment = dataSnapshot.getValue(GroomingAppointment.Appointment.class);
                if (appointment != null) {
                    selectedDate = appointment.date;
                    selectedTime = appointment.time;
                    selectedService = appointment.service;

                    showDateBtn.setText(selectedDate);
                    showTimeBtn.setText(selectedTime);

                    int spinnerPosition = ((ArrayAdapter<String>) serviceSpinner.getAdapter()).getPosition(selectedService);
                    serviceSpinner.setSelection(spinnerPosition);
                } else {
                    Toast.makeText(EditAppointment.this, "Failed to load appointment details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditAppointment.this, "Failed to load appointment details", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveAppointment() {
        if (selectedDate != null && selectedTime != null && selectedService != null) {
            GroomingAppointment.Appointment updatedAppointment = new GroomingAppointment.Appointment(selectedDate, selectedTime, selectedService);
            updatedAppointment.setId(appointmentId); // Ensure the ID is set

            databaseRef.setValue(updatedAppointment);

            Toast.makeText(this, "Appointment updated", Toast.LENGTH_SHORT).show();

            // Navigate back to the GroomingAppointment activity
            Intent intent = new Intent(EditAppointment.this, GroomingAppointment.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Please provide valid information", Toast.LENGTH_SHORT).show();
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
}
