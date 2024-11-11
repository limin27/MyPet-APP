package com.example.mypet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.ArrayList;


public class HealthActivity extends AppCompatActivity {

    private ArrayList<Medication> medicationList = new ArrayList<>();
    private ArrayList<Vaccination> vaccinationList = new ArrayList<>();
    private ArrayList<Reminder> reminderList = new ArrayList<>();

    private RecyclerView medicationRecyclerView, vaccinationRecyclerView, reminderRecyclerView;
    private TextView noMedicationText, noVaccinationText, noReminderText;
    private MedicationAdapter medicationAdapter;
    private VaccinationAdapter vaccinationAdapter;
    private ReminderAdapter reminderAdapter;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private static final int MAX_ITEM = 8, REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder mediaRecorder;
    private String fileName;
    private boolean isRecording = false;
    private String petId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health);

        // Get petId from intent
        Intent intent = getIntent();
        petId = intent.getStringExtra("petId");

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference().child("pets").child(petId); // Changed reference to include petId
        storageReference = FirebaseStorage.getInstance().getReference();


        medicationRecyclerView = findViewById(R.id.medication_recycler_view);
        noMedicationText = findViewById(R.id.no_medication_text);
        Button addMedicationButton = findViewById(R.id.btnAddMedication);

        medicationRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        medicationAdapter = new MedicationAdapter(medicationList);
        medicationRecyclerView.setAdapter(medicationAdapter);

        vaccinationRecyclerView = findViewById(R.id.vaccination_recycler_view);
        noVaccinationText = findViewById(R.id.no_vaccination_text);
        Button addVaccinationButton = findViewById(R.id.btnAddVaccination);

        vaccinationRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        vaccinationAdapter = new VaccinationAdapter(vaccinationList);
        vaccinationRecyclerView.setAdapter(vaccinationAdapter);

        reminderRecyclerView = findViewById(R.id.reminder_recycler_view);
        noReminderText = findViewById(R.id.no_reminder_text);
        Button addReminderButton = findViewById(R.id.btnAddReminder);

        reminderRecyclerView.setLayoutManager(new GridLayoutManager(this, 1)); // 2 columns
        reminderAdapter = new ReminderAdapter(reminderList);
        reminderRecyclerView.setAdapter(reminderAdapter);

        addMedicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (medicationList.size() < MAX_ITEM) {
                    showMedicationDialog(null, -1);
                } else {
                    Toast.makeText(HealthActivity.this, "You can only create up to 8 medications", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addVaccinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vaccinationList.size() < MAX_ITEM) {
                    showVaccinationDialog(null, -1);
                } else {
                    Toast.makeText(HealthActivity.this, "You can only create up to 8 vaccinations", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reminderList.size() < REQUEST_RECORD_AUDIO_PERMISSION) {
                    showReminderDialog(null, -1);
                } else {
                    Toast.makeText(HealthActivity.this, "You can only create up to 200 records", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Load data from Firebase
        loadMedicationData();
        loadVaccinationData();
        loadReminderData();
    }

    private void loadMedicationData() {
        databaseReference.child("medications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicationList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Medication medication = dataSnapshot.getValue(Medication.class);
                    medicationList.add(medication);
                }
                updateMedicationGrid();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HealthActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVaccinationData() {
        databaseReference.child("vaccinations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vaccinationList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Vaccination vaccination = dataSnapshot.getValue(Vaccination.class);
                    vaccinationList.add(vaccination);
                }
                updateVaccinationGrid();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HealthActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadReminderData() {
        databaseReference.child("recordings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reminderList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Reminder recording = dataSnapshot.getValue(Reminder.class);
                    reminderList.add(recording);
                }
                updateReminderGrid();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HealthActivity.this, "Failed to load recordings.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMedicationGrid() {
        if (medicationList.isEmpty()) {
            noMedicationText.setVisibility(View.VISIBLE);
        } else {
            noMedicationText.setVisibility(View.GONE);
        }
        medicationAdapter.notifyDataSetChanged();
    }

    private void updateVaccinationGrid() {
        if (vaccinationList.isEmpty()) {
            noVaccinationText.setVisibility(View.VISIBLE);
        } else {
            noVaccinationText.setVisibility(View.GONE);
        }
        vaccinationAdapter.notifyDataSetChanged();
    }

    private void updateReminderGrid() {
        if (reminderList.isEmpty()) {
            noReminderText.setVisibility(View.VISIBLE);
        } else {
            noReminderText.setVisibility(View.GONE);
        }
        reminderAdapter.notifyDataSetChanged();
    }

    private void showMedicationDialog(final Medication medication, final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dialog_medication, null);
        builder.setView(dialogView);

        final EditText itemEditText = dialogView.findViewById(R.id.edit_item);
        final EditText detailEditText = dialogView.findViewById(R.id.edit_detail);
        Button saveButton = dialogView.findViewById(R.id.save_medication_button);

        if (medication != null) {
            itemEditText.setText(medication.getItem());
            detailEditText.setText(medication.getDetail());
        }

        final AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item = itemEditText.getText().toString();
                String detail = detailEditText.getText().toString();

                if (item.isEmpty() || detail.isEmpty()) {
                    Toast.makeText(HealthActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    if (medication == null) {
                        String id = databaseReference.child("medications").push().getKey();
                        Medication newMedication = new Medication(id, item, detail);
                        databaseReference.child("medications").child(id).setValue(newMedication);
                    } else {
                        Medication updatedMedication = new Medication(medication.getId(), item, detail);
                        databaseReference.child("medications").child(medication.getId()).setValue(updatedMedication);
                    }
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void showVaccinationDialog(final Vaccination vaccination, final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dialog_vaccination, null);
        builder.setView(dialogView);

        final EditText vaccineEditText = dialogView.findViewById(R.id.edit_vaccine_type);
        final EditText dateEditText = dialogView.findViewById(R.id.edit_date);
        Button saveButton = dialogView.findViewById(R.id.save_vaccine_type_button);

        if (vaccination != null) {
            vaccineEditText.setText(vaccination.getVaccine());
            dateEditText.setText(vaccination.getDate());
        }

        final AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vaccine = vaccineEditText.getText().toString();
                String date = dateEditText.getText().toString();

                if (vaccine.isEmpty() || date.isEmpty()) {
                    Toast.makeText(HealthActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    if (vaccination == null) {
                        String id = databaseReference.child("vaccinations").push().getKey();
                        Vaccination newVaccination = new Vaccination(id, vaccine, date);
                        databaseReference.child("vaccinations").child(id).setValue(newVaccination);
                    } else {
                        Vaccination updatedVaccination = new Vaccination(vaccination.getId(), vaccine, date);
                        databaseReference.child("vaccinations").child(vaccination.getId()).setValue(updatedVaccination);
                    }
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void showReminderDialog(final Reminder existingRecording, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_dialog_reminder, null);
        builder.setView(dialogView);

        final EditText editRecordingName = dialogView.findViewById(R.id.edit_recording_name);
        final ImageButton btnStartRecording = dialogView.findViewById(R.id.btnStartRecording);
        final ImageButton btnStopRecording = dialogView.findViewById(R.id.btnStopRecording);
        Button saveButton = dialogView.findViewById(R.id.save_reminder_button);

        if (existingRecording != null) {
            editRecordingName.setText(existingRecording.getName());
        }

        final AlertDialog dialog = builder.create();

        btnStartRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        btnStopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reminderName = editRecordingName.getText().toString();

                if (reminderName.isEmpty() || fileName == null) {
                    Toast.makeText(HealthActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    uploadRecording(reminderName, existingRecording, position);
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    public static class Medication {
        private String id;
        private String item;
        private String detail;

        public Medication() {
            // Default constructor required for calls to DataSnapshot.getValue(Vaccination.class)
        }

        public Medication(String id, String item, String detail) {
            this.id = id;
            this.item = item;
            this.detail = detail;
        }

        public String getId() {
            return id;
        }

        public String getItem() {
            return item;
        }

        public String getDetail() {
            return detail;
        }
    }

    public static class Vaccination {
        private String id;
        private String vaccine;
        private String date;

        public Vaccination() {
            // Default constructor required for calls to DataSnapshot.getValue(Vaccination.class)
        }

        public Vaccination(String id, String vaccine, String date) {
            this.id = id;
            this.vaccine = vaccine;
            this.date = date;
        }

        public String getId() {
            return id;
        }

        public String getVaccine() {
            return vaccine;
        }

        public String getDate() {
            return date;
        }
    }

    public static class Reminder {
        private String id;
        private String name;
        private String url;

        public Reminder() {
            // Default constructor required for Firebase deserialization
        }

        public Reminder(String id, String name, String url) {
            this.id = id;
            this.name = name;
            this.url = url;
        }


        public String getId() {
            return id;
        }


        public String getName() {
            return name;
        }


        public String getUrl() {
            return url;
        }

    }


    private class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {
        private ArrayList<Medication> medications;

        public MedicationAdapter(ArrayList<Medication> medications) {
            this.medications = medications;
        }

        @Override
        public MedicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_medication_card, parent, false);
            return new MedicationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MedicationViewHolder holder, int position) {
            Medication medication = medications.get(position);
            holder.itemTextView.setText(medication.getItem());
            holder.detailTextView.setText(medication.getDetail());

            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMedicationDialog(medication, holder.getAdapterPosition());
                }
            });

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    databaseReference.child("medications").child(medication.getId()).removeValue();
                    medications.remove(holder.getAdapterPosition());
                    updateMedicationGrid();
                }
            });
        }

        @Override
        public int getItemCount() {
            return medications.size();
        }

        public class MedicationViewHolder extends RecyclerView.ViewHolder {
            TextView itemTextView;
            TextView detailTextView;
            ImageButton editButton;
            ImageButton deleteButton;

            public MedicationViewHolder(View itemView) {
                super(itemView);
                itemTextView = itemView.findViewById(R.id.item);
                detailTextView = itemView.findViewById(R.id.detail);
                editButton = itemView.findViewById(R.id.edit_medication_button);
                deleteButton = itemView.findViewById(R.id.delete_medication_button);
            }
        }
    }

    private class VaccinationAdapter extends RecyclerView.Adapter<VaccinationAdapter.VaccinationViewHolder> {
        private ArrayList<Vaccination> vaccinations;

        public VaccinationAdapter(ArrayList<Vaccination> vaccinations) {
            this.vaccinations = vaccinations;
        }

        @Override
        public VaccinationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_vaccination_card, parent, false);
            return new VaccinationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(VaccinationViewHolder holder, int position) {
            Vaccination vaccination = vaccinations.get(position);
            holder.vaccineTypeTextView.setText(vaccination.getVaccine());
            holder.dateTextView.setText(vaccination.getDate());

            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showVaccinationDialog(vaccination, holder.getAdapterPosition());
                }
            });

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    databaseReference.child("vaccinations").child(vaccination.getId()).removeValue();
                    vaccinations.remove(holder.getAdapterPosition());
                    updateVaccinationGrid();
                }
            });
        }

        @Override
        public int getItemCount() {
            return vaccinations.size();
        }

        public class VaccinationViewHolder extends RecyclerView.ViewHolder {
            TextView vaccineTypeTextView;
            TextView dateTextView;
            ImageButton editButton;
            ImageButton deleteButton;

            public VaccinationViewHolder(View itemView) {
                super(itemView);
                vaccineTypeTextView = itemView.findViewById(R.id.vaccine_type);
                dateTextView = itemView.findViewById(R.id.vaccine_date);
                editButton = itemView.findViewById(R.id.edit_vaccination_button);
                deleteButton = itemView.findViewById(R.id.delete_vaccination_button);
            }
        }
    }

    private class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
        private ArrayList<Reminder> reminders;
        private MediaPlayer mediaPlayer;

        ReminderAdapter(ArrayList<Reminder> reminders) {
            this.reminders = reminders;
        }

        @Override
        public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_reminder_card, parent, false);
            return new ReminderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ReminderViewHolder holder, int position) {
            Reminder reminder = reminders.get(position);
            holder.recordingName.setText(reminder.getName());

            holder.playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    } else {
                        mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(reminder.getUrl());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        } catch (IOException e) {
                            Log.e("ReminderAdapter", "onClick: ", e);
                        }
                    }
                }
            });

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeRecording(reminder.getId(), holder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            return reminders.size();
        }

        class ReminderViewHolder extends RecyclerView.ViewHolder {
            TextView recordingName;
            ImageButton playButton, deleteButton;

            ReminderViewHolder(View itemView) {
                super(itemView);
                recordingName = itemView.findViewById(R.id.recording_name);
                playButton = itemView.findViewById(R.id.play_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
        }

        private void removeRecording(String recordingId, final int position) {
            // Get the reference to the recording in the database
            DatabaseReference recordingRef = databaseReference.child("recordings").child(recordingId);

            // Retrieve the URL of the recording file from the database
            recordingRef.child("url").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Delete the recording data from the database
                        databaseReference.child("recordings").child(recordingId).removeValue();

                        // Delete the recording file from Firebase Storage
                        String recordingUrl = dataSnapshot.getValue(String.class);
                        deleteRecordingFromStorage(recordingUrl);

                        // Remove the item from the list and notify the adapter
                        reminders.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, reminders.size());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }

        private void deleteRecordingFromStorage(String recordingUrl) {
            // Get the reference to the recording file in Firebase Storage
            StorageReference recordingRef = FirebaseStorage.getInstance().getReferenceFromUrl(recordingUrl);

            // Delete the recording file
            recordingRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Recording file deleted successfully
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }

        private int getRecordingPosition(String recordingId) {
            for (int i = 0; i < reminders.size(); i++) {
                if (reminders.get(i).getId().equals(recordingId)) {
                    return i;
                }
            }
            return RecyclerView.NO_POSITION;
        }
    }



    private void startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        setupAndStartRecording();
    }

    private void setupAndStartRecording() {
        fileName = getExternalCacheDir().getAbsolutePath() + "/" + UUID.randomUUID().toString() + "_recording.3gp";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(fileName);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("HealthActivity", "startRecording: ", e);
            Toast.makeText(this, "Recording failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException stopException) {
                Log.e("HealthActivity", "stopRecording: ", stopException);
                Toast.makeText(this, "Failed to stop recording: " + stopException.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupAndStartRecording();
            } else {
                Toast.makeText(this, "Permission to record audio denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadRecording(final String recordingName, final Reminder existingRecording, final int position) {
        Uri fileUri = Uri.fromFile(new File(fileName));
        StorageReference recordingRef = storageReference.child("recordings/" + fileUri.getLastPathSegment());
        recordingRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        recordingRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String recordingId = existingRecording != null ? existingRecording.getId() : databaseReference.child("recordings").push().getKey();
                                Reminder recording = new Reminder(recordingId, recordingName, uri.toString());
                                databaseReference.child("recordings").child(recordingId).setValue(recording);
                                if (existingRecording != null) {
                                    reminderList.set(position, recording);
                                } else {
                                    reminderList.add(recording);
                                }
                                reminderAdapter.notifyDataSetChanged();
                                Toast.makeText(HealthActivity.this, "Recording uploaded successfully.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HealthActivity.this, "Failed to upload recording.", Toast.LENGTH_SHORT).show();
                        Log.e("HealthActivity", "uploadRecording: ", e);
                    }
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

