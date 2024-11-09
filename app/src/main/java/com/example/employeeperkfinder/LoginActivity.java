package com.example.employeeperkfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button loginButton;
    TextView registerRedirectText;
    //private EditText loginEmail, loginPassword;
    //private Button loginButton;
    //private TextView registerRedirectText;
    FirebaseDatabase database;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setTheme(R.style.Theme_AppCompat_Light);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn){
            //Redirect to main page if true
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); //Close LoginActivity
            return;
        }

        //Continue with login process if not logged in
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        registerRedirectText = findViewById(R.id.registerRedirectText);

        ImageView passwordToggle = findViewById(R.id.password_toggle);

        loginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        passwordToggle.setOnClickListener(v -> {
            if (loginPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                // Change to show password
                loginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.baseline_visibility_24); //Show icon
            } else {
                loginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24); //Hide icon
            }
        });

        loginButton.setOnClickListener(view -> {
            String userEmail = loginEmail.getText().toString().trim().toLowerCase();
            String userPassword = loginPassword.getText().toString().trim();

            if (!validateEmail() | !validatePassword()) {
                return;
            } else {
                checkUser(userEmail, userPassword);
            }

        });


        registerRedirectText.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    public Boolean validateEmail() {
        String email = loginEmail.getText().toString();
        if (email.isEmpty()) {
            loginEmail.setError("Email cannot be blank");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.setError("Please enter a valid email");
            return false;
        } else {
            loginEmail.setError(null);
            return true;
        }
    }

    public Boolean validatePassword() {
        String password = loginPassword.getText().toString();
        if (password.isEmpty()) {
            loginEmail.setError("Password cannot be blank");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    public void checkUser(final String userEmail, final String userPassword) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference reference = db.getReference("users");

        Query checkUserQuery = reference.orderByChild("email").equalTo(userEmail);

        checkUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String retrievedEmail = userSnapshot.child("email").getValue(String.class);
                        if (retrievedEmail != null && retrievedEmail.equals(userEmail)) {
                            String storedHashedPassword = userSnapshot.child("hashedPassword").getValue(String.class);
                            String storedSalt = userSnapshot.child("salt").getValue(String.class);

                            if (storedHashedPassword != null && storedSalt != null) {
                                boolean passwordValid = PasswordHasher.verifyPassword(storedSalt, userPassword, storedHashedPassword);
                                if (passwordValid) {
                                    String firstName = userSnapshot.child("first_name").getValue(String.class);
                                    String lastName = userSnapshot.child("last_name").getValue(String.class);

                                    Toast.makeText(LoginActivity.this, "Welcome, " + firstName + " " + lastName, Toast.LENGTH_SHORT).show();

                                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("isLoggedIn", true);
                                    editor.putString("userEmail", userEmail);
                                    editor.apply();


                                    // Start MainActivity after successful login
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish(); // Close LoginActivity
                                    break;
                                } else {
                                    loginPassword.setError("Incorrect Email/Password");
                                    loginPassword.requestFocus();
                                }

                            } else {
                                loginPassword.setError("Missing password hash or salt");
                                loginPassword.requestFocus();
                            }
                        } else {
                            loginEmail.setError("Email does not exist");
                            loginEmail.requestFocus();
                        }
                    }
                } else {
                    loginEmail.setError("Email does not exist");
                    loginEmail.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}