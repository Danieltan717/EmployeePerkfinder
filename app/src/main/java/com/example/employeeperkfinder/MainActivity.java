package com.example.employeeperkfinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private DatabaseReference mDatabase;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private List<Marker> markers = new ArrayList<>();
    private Map<String, List<Marker>> categoryMarkers = new HashMap<>(); // Map to categorize markers

    private AutoCompleteTextView searchBar;
    private List<String> locationNames = new ArrayList<>();
    private ImageButton btnSearch;
    private LinearLayout categoryButtonsContainer; // Container for buttons

    private LinearLayout categoryPopup;
    private Button btnCategoryToggle, btnHome;
    private ImageButton btnProfileNav;

    private RelativeLayout descriptionLayout;
    private TextView markerNameText, markerAddressText, markerCategoryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Database reference
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mDatabase = FirebaseDatabase.getInstance().getReference("locations");

        // Initialize UI components

        btnSearch = findViewById(R.id.btn_search);
        categoryButtonsContainer = findViewById(R.id.category_buttons_container);
        categoryPopup = findViewById(R.id.category_popup);

        // Initialize UI components
        searchBar = findViewById(R.id.search_bar);

        // Show keyboard when the search bar is focused
        searchBar.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                searchBar.postDelayed(() -> {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT);
                    }
                }, 200); // Delay for UI readiness
            }
        });

        // Toggle Category Popup
        ImageButton btnCategoryToggle = findViewById(R.id.btn_category_toggle);
        btnCategoryToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoryPopup.getVisibility() == View.GONE) {
                    categoryPopup.setVisibility(View.VISIBLE);
                } else {
                    categoryPopup.setVisibility(View.GONE);
                }
            }
        });

        // Home Button - Redirect to MainActivity
        ImageButton btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "You are already at the home screen!", Toast.LENGTH_SHORT).show(); // Replaced previous logic with this toast. Should prevent constant page switching while in the same page ~Justyn
//                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });

        // Add the Profile Button and set its click listener
        ImageButton profileButton = findViewById(R.id.btn_profile);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ProfileActivity when the profile button is clicked
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Initialize the description layout and TextViews
        descriptionLayout = findViewById(R.id.marker_info_layout);
        markerNameText = findViewById(R.id.marker_name);
        markerCategoryText = findViewById(R.id.marker_category);
        markerAddressText = findViewById(R.id.marker_address);

        // Hide the description layout by default
        descriptionLayout.setVisibility(View.GONE);

        // Load locations from Firebase
        loadLocationNamesFromFirebase();
        // Load categories from Firebase
        loadCategoriesFromFirebase();

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationName = searchBar.getText().toString();
                if (!locationName.isEmpty()) {
                    searchLocation(locationName);
                    // Hide keyboard and clear focus after searching
                    searchBar.clearFocus();
                    hideKeyboard();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a location to search", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Check and request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, load the map
            loadMap();
        }

        //Load the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Method to dynamically load categories and create buttons
    private void loadCategoriesFromFirebase() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> uniqueCategories = new HashSet<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String category = snapshot.child("category").getValue(String.class);
                    if (category != null) {
                        uniqueCategories.add(category);
                    }
                }

                // Clear any existing buttons in the container
                categoryButtonsContainer.removeAllViews();

                // Create "All" button to show all markers
                Button allButton = new Button(MainActivity.this);
                allButton.setText("All");
                allButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMarkersByCategory("All");
                    }
                });
                categoryButtonsContainer.addView(allButton);

                // Dynamically create a button for each unique category
                for (String category : uniqueCategories) {
                    Button categoryButton = new Button(MainActivity.this);
                    categoryButton.setText(category);
                    categoryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showMarkersByCategory(category);
                        }
                    });
                    categoryButtonsContainer.addView(categoryButton);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load categories: " + databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showMarkersByCategory(String category) {
        for (Marker marker : markers) {
            String[] tagData = (String[]) marker.getTag(); // Cast to String[]
            if (tagData != null) {
                String markerCategory = tagData[0]; // Get the category from the tag data
                if ("All".equals(category) || category.equals(markerCategory)) {
                    marker.setVisible(true);
                } else {
                    marker.setVisible(false);
                }
            }
        }
    }

    private void loadMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        // Check and request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
            getCurrentLocationAndZoom();
        } else {
            checkLocationPermission();
        }

        // Reposition the My Location button to the bottom-right corner
        moveMyLocationButton();

        // Load and display pins from Firebase
        loadPinsFromFirebase();

        // Set a click listener on the map
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Hide the description layout
                descriptionLayout.setVisibility(View.GONE);
                // Clear focus from the search bar
                searchBar.clearFocus();
                // Optionally hide the keyboard
                hideKeyboard();
            }
        });

        // Set marker click listener
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Retrieve category and address from marker’s tag
                String[] tagData = (String[]) marker.getTag();
                String category = tagData[0];
                String address = tagData[1];

                // Show the description layout and set name and address
                descriptionLayout.setVisibility(View.VISIBLE);
                markerNameText.setText(marker.getTitle());
                markerCategoryText.setText(category);
                markerAddressText.setText(address != null ? address : "Address not available");

                // Optionally move the camera to the marker
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));

                return false;
            }
        });
    }

    // Method to hide the keyboard
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void moveMyLocationButton() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        View mapView = mapFragment.getView();

        if (mapView != null) {
            View locationButton =((View) mapView.findViewById(Integer.parseInt("1")).getParent())
                    .findViewById(Integer.parseInt("2"));

            // Adjust position to bottom-right corner
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            params.setMargins(0, 0, 30, 30); // Adjust margins if needed

            locationButton.setLayoutParams(params);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request the location permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, get current location and zoom
            getCurrentLocationAndZoom();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get the current location
                getCurrentLocationAndZoom();
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Location permission is required to show your current location.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getCurrentLocationAndZoom() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        gMap.setMyLocationEnabled(true);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Get the user's current location
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // Move the camera to the user's location and zoom in
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                }
            }
        });
    }

    private void loadPinsFromFirebase() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Double latitude = snapshot.child("latitude").getValue(Double.class);
                        Double longitude = snapshot.child("longitude").getValue(Double.class);
                        String name = snapshot.child("name").getValue(String.class);
                        String category = snapshot.child("category").getValue(String.class);
                        String address = snapshot.child("address").getValue(String.class); // Retrieve the address

                        if (latitude != null && longitude != null && name != null && category != null && address != null) {
                            LatLng location = new LatLng(latitude, longitude);

                            Marker marker = gMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                            if (marker != null) {
                                marker.setTag(new String[]{category, address}); // Store category and address
                                markers.add(marker);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load pins: " + databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadLocationNamesFromFirebase() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    locationNames.clear(); // Clear the list to avoid duplicates
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null) {
                            locationNames.add(name); // Add names to the list
                        }
                    }

                    // Set up ArrayAdapter for AutoCompleteTextView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            MainActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            locationNames
                    );
                    searchBar.setAdapter(adapter); // Connect the adapter to the search bar

                    // Optional: Start auto-complete after typing 1 character
                    searchBar.setThreshold(1);
                } else {
                    Log.d("FirebaseData", "No locations found in Firebase");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load locations: " + databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void searchLocation(String locationName) {
        // Check if the location exists in the database
        boolean locationFoundInDatabase = false;

        for (Marker marker : markers) {
            if (marker.getTitle().equalsIgnoreCase(locationName)) {
                // Move the camera to the marker's position and zoom in
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
                locationFoundInDatabase = true;
                Toast.makeText(this, "Found in database: " + locationName, Toast.LENGTH_SHORT).show();
                break;
            }
        }

        // If not found in database, use Geocoder to search normally
        if (!locationFoundInDatabase) {
            Geocoder geocoder = new Geocoder(this);
            try {
                List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    // Move the camera to the searched location and zoom in
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    Toast.makeText(this, "Found via Geocoder: " + locationName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error retrieving location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (gMap != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
        }
    }
}