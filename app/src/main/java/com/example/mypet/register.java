package com.example.mypet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class register extends AppCompatActivity {

    private EditText registerUsername, registerEmail, registerPhoneNo, registerPassword;
    private TextView loginRedirectText;
    private Button registerButton;
    private ProgressBar progressBar;
    private static final String TAG = "register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toast.makeText(register.this, "You can register now", Toast.LENGTH_LONG).show();

        progressBar = findViewById(R.id.progressBar);
        registerUsername = findViewById(R.id.username);
        registerEmail = findViewById(R.id.email);
        registerPhoneNo = findViewById(R.id.phoneNo);
        registerPassword = findViewById(R.id.password);
        loginRedirectText = findViewById(R.id.loginBtn);
        registerButton = findViewById(R.id.registerBtn);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obtain the entered data
                String username = registerUsername.getText().toString();
                String email = registerEmail.getText().toString();
                String contact = registerPhoneNo.getText().toString();
                String password = registerPassword.getText().toString();

                if(TextUtils.isEmpty(username)){
                    Toast.makeText(register.this, "Please enter your full name", Toast.LENGTH_LONG).show();
                    registerUsername.setError("Full Name is required");
                    registerUsername.requestFocus();
                }else if(TextUtils.isEmpty(email)){
                    Toast.makeText(register.this, "Please enter your email", Toast.LENGTH_LONG).show();
                    registerEmail.setError("Email is required");
                    registerEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(register.this, "Please re-enter your email", Toast.LENGTH_LONG).show();
                    registerEmail.setError("Valid email is required");
                    registerEmail.requestFocus();
                } else if (TextUtils.isEmpty(contact)) {
                    Toast.makeText(register.this, "Please enter your phone no.", Toast.LENGTH_LONG).show();
                    registerPhoneNo.setError("Phone No. is required");
                    registerPhoneNo.requestFocus();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(register.this, "Please enter your password", Toast.LENGTH_LONG).show();
                    registerPassword.setError("Password is required");
                    registerPassword.requestFocus();
                } else if (password.length() < 6) {
                    Toast.makeText(register.this, "Password should be at least 6 digits", Toast.LENGTH_LONG).show();
                    registerPassword.setError("Password too weak");
                    registerPassword.requestFocus();
                }else {
                    progressBar.setVisibility((View.VISIBLE));
                    registerUser(username, email, contact, password);
                }
            }
        });
    }


    private void registerUser(String username, String email, String contact, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        //Create User Profile
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(register.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    //Enter User Data into firebase Realtime Database;
                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(username, contact);

                    //Extracting User reference from Database for "Registered Users"
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance("https://cd21059-finalassisgnment-default-rtdb.firebaseio.com/").getReference("Registered Users");

                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                //Send Verification Email
                                firebaseUser.sendEmailVerification();

                                Toast.makeText(register.this, "User registered successfully. Please verify your email.", Toast.LENGTH_LONG).show();

                                //Open User Profile after successful registration
                                Intent intent = new Intent(register.this, userProfile.class);
                                //To Prevent User from returning back to register activity on pressing back button after registration
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish(); //to close Register Activity
                            }else{
                                Toast.makeText(register.this, "User registered failed. Please try again.", Toast.LENGTH_LONG).show();
                            }
                            //Hide progress bar whether user is successful or failed
                            progressBar.setVisibility(View.GONE);

                        }
                    });
                }else{
                    try{
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e){
                        registerPassword.setError("Your password is too weak. Kindly use a mix of alphabets, numbers and special characters");
                        registerPassword.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e){
                        registerEmail.setError("Your email is invalid or already in use. Kindly re-enter");
                        registerEmail.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e){
                        registerEmail.setError("User is already registered with this email. Please use another email.");
                        registerEmail.requestFocus();
                    }catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(register.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    //Hide progress bar whether user is successful or failed
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }


    public void openLogin(View view) {
        startActivity(new Intent(this, login.class));
    }
}