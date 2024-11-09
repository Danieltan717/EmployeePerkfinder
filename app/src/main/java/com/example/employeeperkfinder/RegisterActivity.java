package com.example.employeeperkfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.Drawable;
import android.text.method.PasswordTransformationMethod;
import android.text.method.HideReturnsTransformationMethod;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.employeeperkfinder.PasswordHasher;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText signupEmail, signupPassword, signupConfirmPassword, signupEmployeeID;
    Button signupButton;
    TextView loginRedirectText;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupConfirmPassword = findViewById(R.id.signup_confirm_password);
        signupEmployeeID = findViewById(R.id.signup_employee_id);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        ImageView passwordToggle = findViewById(R.id.password_toggle);
        ImageView confirmPasswordToggle = findViewById(R.id.confirm_password_toggle);

        signupPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        signupConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        // Set onClickListener for password toggle
        passwordToggle.setOnClickListener(v -> {
            if (signupPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                // Change to show password
                signupPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.baseline_visibility_24);  // Show icon
            } else {
                // Change to hide password
                signupPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24);  // Hide icon
            }
        });

        // Set onClickListener for confirm password toggle
        confirmPasswordToggle.setOnClickListener(v -> {
            if (signupConfirmPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                // Change to show password
                signupConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                confirmPasswordToggle.setImageResource(R.drawable.baseline_visibility_24);  // Show icon
            } else {
                // Change to hide password
                signupConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                confirmPasswordToggle.setImageResource(R.drawable.baseline_visibility_off_24);  // Hide icon
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String email = signupEmail.getText().toString().trim().toLowerCase();
                String password = signupPassword.getText().toString().trim();
                String confirmPassword = signupConfirmPassword.getText().toString().trim();
                String employeeId = signupEmployeeID.getText().toString().trim().toUpperCase();

                if (!password.equals(confirmPassword)){
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // First, check if Employee ID exists
                reference.child(employeeId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // If Employee ID exists, proceed with registration
                            checkEmail(email, password, employeeId);
                            //registerUser(email, password, employeeId);
                        } else {
                            // Employee ID does not exist, show error
                            Toast.makeText(RegisterActivity.this, "Employee ID does not exist.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RegisterActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }


    private void checkEmail(String email, String password, String employeeId) {
        reference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(RegisterActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(email, password, employeeId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegisterActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(String email, String password, String employeeId) {
        DatabaseReference reference = database.getReference("users");

        // First, fetch the existing data for the employee ID
        reference.child(employeeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve existing data if it exists
                Map<String, Object> existingData = new HashMap<>();
                if (snapshot.exists()) {
                    existingData = (Map<String, Object>) snapshot.getValue();
                }

                // Generate a unique salt and hash the password
                String salt = PasswordHasher.generateSalt();
                String hashedPassword = PasswordHasher.hashPassword(salt, password);

                // Create new registration data
                Map<String, Object> newUserData = new HashMap<>();
                newUserData.put("email", email);
                newUserData.put("hashedPassword", hashedPassword);
                newUserData.put("salt", salt);
                //newUserData.put("employeeId", employeeId);

                // Merge new data with existing data
                existingData.putAll(newUserData);

                // Add user data under the Employee ID node
                reference.child(employeeId).setValue(existingData)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegisterActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
}