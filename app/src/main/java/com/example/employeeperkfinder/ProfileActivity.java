package com.example.employeeperkfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.BarcodeEncoder;

// Added this myself
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;


public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference userRef;
    private TextView firstNameText, lastNameText, emailText, contactText, addressText;
    private ImageView qrCodeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        // Adjust padding for system bars (status bar, navigation bar, etc.)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        firstNameText = findViewById(R.id.text_first_name);
        lastNameText = findViewById(R.id.text_last_name);
        emailText = findViewById(R.id.text_email);
        contactText = findViewById(R.id.text_contact);
        addressText = findViewById(R.id.text_address);
        qrCodeImageView = findViewById(R.id.qr_code_image_view);

        // Get user email from SharedPreferences (saved during login)
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("userEmail", null);

        if (userEmail != null) {
            // Reference to the user's data in Firebase based on the email
            userRef = FirebaseDatabase.getInstance().getReference("users");

            // Fetch user data from Firebase
            loadUserData(userEmail);
        } else {
            Toast.makeText(ProfileActivity.this, "No user is logged in", Toast.LENGTH_SHORT).show();
        };


        // Disabled category button warning users to return to home before accessing button
        ImageButton btnPlaceCategories = findViewById(R.id.img_category);
        btnPlaceCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Display warning message
                Toast.makeText(ProfileActivity.this, "Return to the home screen to access this feature", Toast.LENGTH_SHORT).show();  // ¯\_(ツ)_/¯ ~Justyn
            }
        });

        // Home Button click listener
        ImageButton btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MainActivity when Home button is clicked
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Optionally finish the current activity so the user can't go back to the profile screen
            }
        });

        // Profile Button click listener
        ImageButton btnProfile = findViewById(R.id.btn_profile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ProfileActivity when the Profile button is clicked
                Toast.makeText(ProfileActivity.this, "You are already at the profile screen!", Toast.LENGTH_SHORT).show(); // Replaced previous logic with this toast. Should prevent constant page switching while in the same page ~Justyn
//                Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
//                startActivity(intent);
            }
        });

        // Logout Button click listener
        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(view -> logout());
    }

    private void loadUserData(String email) {
        userRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Populate the UI with retrieved data
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String employeeId = userSnapshot.child("employee_id").getValue(String.class);
                        firstNameText.setText(userSnapshot.child("first_name").getValue(String.class));
                        lastNameText.setText(userSnapshot.child("last_name").getValue(String.class));
                        emailText.setText(userSnapshot.child("email").getValue(String.class));
                        contactText.setText(userSnapshot.child("contact").getValue(String.class));
                        addressText.setText(userSnapshot.child("address").getValue(String.class));

                        // Generate QR code from employee_id
                        if (employeeId != null) {
                            generateQrCode(employeeId);
                        }
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("ProfileActivity", "Failed to load user data", error.toException());
                Toast.makeText(ProfileActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void generateQrCode(String employeeId) {
        // Changed the QR generation because I have OCD ~Justyn
        try {
            // Generate BitMatrix representing the QR code
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(employeeId, BarcodeFormat.QR_CODE, 350, 350); // Adjust width & height as needed

            // Convert BitMatrix to a transparent Bitmap
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];

            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = matrix.get(x, y) ? 0xFF000000 : 0x00000000; // Black or transparent pixel
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            // Set the transparent QR code to the ImageView
            qrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e("ProfileActivity", "Error generating QR code", e);
            Toast.makeText(ProfileActivity.this, "Failed to generate QR code.", Toast.LENGTH_SHORT).show();
        }
    }


    public void logout() {
        // Set isLoggedIn to false in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        // Start LoginActivity
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);

        // Finish ProfileActivity so the user can't go back to it
        finish();
    }
}


