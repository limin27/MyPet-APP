package com.example.mypet;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AppointmentHistoryVe extends AppCompatActivity {

    private LinearLayout historyLayout;
    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;
    private ImageView home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_history_ve);

        historyLayout = findViewById(R.id.historyLayout);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("appointmentsVe").child(currentUser.getUid());
        home = findViewById(R.id.imageView4);

        fetchAppointments();
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppointmentHistoryVe.this, Dashboard.class);
                startActivity(intent);
            }
        });
    }

    private void fetchAppointments() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                historyLayout.removeAllViews();
                List<VeterinarianAppointment.AppointmentVe> appointments = new ArrayList<>();

                // Collect appointments in reverse order
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    VeterinarianAppointment.AppointmentVe appointment = snapshot.getValue(VeterinarianAppointment.AppointmentVe.class);
                    if (appointment != null) {
                        appointment.setId(snapshot.getKey());
                        appointments.add(appointment);
                    }
                }

                // Add appointments to layout in reverse order
                for (int i = appointments.size() - 1; i >= 0; i--) {
                    VeterinarianAppointment.AppointmentVe appointment = appointments.get(i);
                    addAppointmentToLayout(appointment);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
                Toast.makeText(AppointmentHistoryVe.this, "Failed to load appointments: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAppointmentToLayout(VeterinarianAppointment.AppointmentVe appointment) {
        TextView textView = new TextView(AppointmentHistoryVe.this);
        textView.setText("Date\t\t\t\t\t\t\t\t\t\t\t: " + appointment.date + "\nTime\t\t\t\t\t\t\t\t\t\t: " + appointment.time + "\nVeterinarian\t: " + appointment.service);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextSize(20);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textView.setTypeface(getResources().getFont(R.font.georgia));
        }
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the edit activity with the selected appointment details
                Intent intent = new Intent(AppointmentHistoryVe.this, EditAppointmentVe.class);
                intent.putExtra("appointment_id", appointment.getId());
                startActivity(intent);
            }
        });
        historyLayout.addView(textView);

        // Create and set up delete icon
        ImageView deleteIcon = new ImageView(AppointmentHistoryVe.this);
        deleteIcon.setImageResource(R.drawable.delete);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams1.gravity = Gravity.END;
        deleteIcon.setLayoutParams(layoutParams1);
        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmAndDeleteAppointment(appointment.getId());
            }
        });
        historyLayout.addView(deleteIcon);

        // Create and set up a divider line
        View lineView = new View(AppointmentHistoryVe.this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 3);
        layoutParams.setMargins(0, 8, 0, 8); // Adjust margin as needed
        lineView.setLayoutParams(layoutParams);
        lineView.setBackgroundColor(getResources().getColor(R.color.Brown)); // Set line color
        historyLayout.addView(lineView);
    }

    private void confirmAndDeleteAppointment(String appointmentId) {
        // Display a confirmation dialog before deleting
        new AlertDialog.Builder(this)
                .setTitle("Delete Appointment")
                .setMessage("Are you sure you want to delete this appointment?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // User confirmed the deletion
                    deleteAppointment(appointmentId);
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deleteAppointment(String appointmentId) {
        DatabaseReference appointmentRefToDelete = databaseRef.child(appointmentId);
        appointmentRefToDelete.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    // Handle the error if the appointment deletion fails
                    Toast.makeText(AppointmentHistoryVe.this, "Failed to delete appointment: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    // Appointment deletion successful
                    Toast.makeText(AppointmentHistoryVe.this, "Appointment deleted successfully", Toast.LENGTH_SHORT).show();
                    fetchAppointments(); // Refresh the list
                }
            }
        });
    }

    public void openProfile(View view) {
        startActivity(new Intent(this, userProfile.class));
    }

    public void openEmergency(View view) {
        startActivity(new Intent(this, emergency.class));
    }

    public void openFeedback(View view) { startActivity(new Intent(this, Feedback.class)); }
}
