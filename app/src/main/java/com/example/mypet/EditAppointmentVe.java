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

public class EditAppointmentVe extends AppCompatActivity {
    private Button showDateBtn;
    private Button showTimeBtn;
    private Spinner veterinarySpinner;
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
        setContentView(R.layout.activity_edit_appointment_ve);

        // Retrieve the appointment ID and user ID passed from the previous activity
        appointmentId = getIntent().getStringExtra("appointment_id");

        if (appointmentId == null) {
            Toast.makeText(this, "Appointment ID is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            databaseRef = FirebaseDatabase.getInstance().getReference("appointmentsVe").child(currentUser.getUid()).child(appointmentId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        showDateBtn = findViewById(R.id.showDateBtn);
        showTimeBtn = findViewById(R.id.showTimeBtn);
        veterinarySpinner = findViewById(R.id.veterinarySpinner);
        btnDone = findViewById(R.id.btnDone);
        home = findViewById(R.id.imageView4);

        // Set up event listeners
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
                Intent intent = new Intent(EditAppointmentVe.this, Dashboard.class);
                startActivity(intent);
            }
        });

        // Set up the veterinary service spinner with a custom layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinnerlayout, getResources().getStringArray(R.array.veterinary_array));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        veterinarySpinner.setAdapter(adapter);

        veterinarySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        // Load the current details of the appointment
        loadAppointmentDetails();
    }

    // Opens a date picker dialog
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

    // Opens a time picker dialog
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

    // Load the details of the current appointment
    private void loadAppointmentDetails() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                VeterinarianAppointment.AppointmentVe appointment = dataSnapshot.getValue(VeterinarianAppointment.AppointmentVe.class);
                if (appointment != null) {
                    selectedDate = appointment.date;
                    selectedTime = appointment.time;
                    selectedService = appointment.service;

                    showDateBtn.setText(selectedDate);
                    showTimeBtn.setText(selectedTime);

                    int spinnerPosition = ((ArrayAdapter<String>) veterinarySpinner.getAdapter()).getPosition(selectedService);
                    veterinarySpinner.setSelection(spinnerPosition);
                } else {
                    Toast.makeText(EditAppointmentVe.this, "Failed to load appointment details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditAppointmentVe.this, "Failed to load appointment details", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // Save the updated appointment details to the database
    private void saveAppointment() {
        if (selectedDate != null && selectedTime != null && selectedService != null) {
            VeterinarianAppointment.AppointmentVe updatedAppointment = new VeterinarianAppointment.AppointmentVe(selectedDate, selectedTime, selectedService);
            updatedAppointment.setId(appointmentId); // Ensure the ID is set

            databaseRef.setValue(updatedAppointment);

            Toast.makeText(this, "Appointment updated", Toast.LENGTH_SHORT).show();

            // Navigate back to the VeterinarianAppointment activity
            Intent intent = new Intent(EditAppointmentVe.this, VeterinarianAppointment.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Please provide valid information", Toast.LENGTH_SHORT).show();
        }
    }

    // Methods to open other activities
    public void openProfile(View view) {
        Intent intent = new Intent(this, userProfile.class);
        startActivity(intent);
    }

    public void openEmergency(View view) {
        Intent intent = new Intent(this, emergency.class);
        startActivity(intent);
    }

    public void openFeedback(View view) {
        Intent intent = new Intent(this, Feedback.class);
        startActivity(intent);
    }
}
