package com.example.mypet;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

public class login extends AppCompatActivity {

    private static final int REQUEST_CODE = 101010;
    private EditText loginEmail, loginPassword;
    private ProgressBar loginProgressBar;
    private FirebaseAuth authProfile;
    private ImageView fingerprintImageView;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private static final String TAG = "login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.email);
        loginPassword = findViewById(R.id.password);
        loginProgressBar = findViewById(R.id.progressBar);
        fingerprintImageView = findViewById(R.id.fingerprintImg);

        authProfile = FirebaseAuth.getInstance();

        //Biometric Authentication
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(login.this, "Fingerprint sensor does not exist", Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(login.this, "Sensor not available", Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                startActivityForResult(enrollIntent, REQUEST_CODE);
                break;
        }

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(login.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),"Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                startActivity(new Intent(login.this, userProfile.class));
                Toast.makeText(getApplicationContext(),"Authentication succeeded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();


        fingerprintImageView.setOnClickListener(view -> {
            biometricPrompt.authenticate(promptInfo);
        });


        //Login User
        Button buttonLogin = findViewById(R.id.loginBtn1);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(login.this, "Please enter your email", Toast.LENGTH_LONG).show();
                    loginEmail.setError("Email is required");
                    loginEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(login.this, "Please re-enter your email", Toast.LENGTH_LONG).show();
                    loginEmail.setError("Valid email is required");
                    loginEmail.requestFocus();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(login.this, "Please enter your password", Toast.LENGTH_LONG).show();
                    loginPassword.setError("Password is required");
                    loginPassword.requestFocus();
                } else {
                    loginProgressBar.setVisibility(View.VISIBLE);
                    loginUser(email, password);
                }
            }
        });
    }

    private void loginUser(String email, String password) {
        authProfile.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    //Get instance of the current User
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    Toast.makeText(login.this, "You are logged in now", Toast.LENGTH_LONG).show();
                    //Open User Profile
                    Intent intent = new Intent(login.this, userProfile.class);
                    //To Prevent User from returning back to register activity on pressing back button after registration
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); //to close Register Activity
                } else {
                    try{
                        throw task.getException();
                    }catch(FirebaseAuthInvalidUserException e){
                        loginEmail.setError("User does not exists. Please register again.");
                        loginEmail.requestFocus();
                    }catch(FirebaseAuthInvalidCredentialsException e){
                        loginPassword.setError("Invalid credentials. Kindly check and re-enter.");
                        loginPassword.requestFocus();
                    }catch(Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(login.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                loginProgressBar.setVisibility(View.GONE);
            }
        });
    }


    public void OpenRegister(View view) {
        startActivity(new Intent(this, register.class));
    }
}