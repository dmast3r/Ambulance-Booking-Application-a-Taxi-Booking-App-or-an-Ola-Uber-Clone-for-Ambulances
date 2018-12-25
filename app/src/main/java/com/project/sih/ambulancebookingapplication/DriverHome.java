package com.project.sih.ambulancebookingapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DriverHome extends AppCompatActivity implements OnMapReadyCallback {

    public static boolean shouldShowDialog = false;

    // variable that indicates if user information needed to be stored to the Firebase Realtime Database
    // will be True when the user logs in for the first time (ie creates his account)
    // and false for subsequent entrance to this activity
    public static boolean store_data;

    private boolean regTokenWritten = false;

    // variable that stores your own registration token
    private String myRegToken;

    // extract some data from shared preference
    SharedPreferences sharedPreferences;
    String phoneNumber;
    double user_latitude = 26.918760, user_longitude = 75.789226;
    String id;
    DatabaseReference user_root;
    GoogleMap gMap = null;

    // will be used to fetch the current location
    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        MainActivity.category = "Driver";

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // extract some data from shared preference
        sharedPreferences = getSharedPreferences(MainActivity.USER_FILE_KEY, MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("user_number", null);
        id = sharedPreferences.getString("user_id", null);

        SharedPreferences.Editor sharedEditor = sharedPreferences.edit();
        sharedEditor.putString("category", "Driver");
        sharedEditor.apply();

        user_root = MainActivity.databaseReferenceRoot.child(phoneNumber);

        // perform some Firebase Realtime Database neccessary operations.
        // includes fetching(and then stroing) the location
        // initialising reg token and then finally storing other information(if needed, see the function storeData for a detail on this)
        fetchLocation();
        InitToken();
        storeData();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        if(shouldShowDialog) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(DriverHome.this, android.R.style.Theme_Material_Dialog_Alert);
            } else
                builder = new AlertDialog.Builder(DriverHome.this);

            builder.setTitle("You will get requests");
            builder.setMessage("Hold tight, you may be getting booking requests, if so, you will be notified with request location " +
                    "indicated on the map");
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();

            shouldShowDialog = false;
        }
    }

    // helper function that adds marker to the map and return the instance of added marker.
    private Marker addMarker(GoogleMap googleMap, LatLng latLng) {
        Marker marker = googleMap.addMarker(new MarkerOptions().title("You are here!").position(latLng));
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.user_low));
        marker.showInfoWindow();
        return marker;
    }

    // This will initialise the myRegToken variable with the Registration token
    private void InitToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(DriverHome.this,
                new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                myRegToken = instanceIdResult.getToken();

                if (!regTokenWritten) {
                    user_root.child("Reg Token").setValue(myRegToken);
                    regTokenWritten = true;
                }
            }
        });
    }

    // store user data to the Firebase Realtime database
    private void storeData() {
        if (store_data) {
            Intent intent = getIntent();
            String[] details = intent.getStringArrayExtra("details");

            user_root.child("Id").setValue(id);
            user_root.child("Name").setValue(details[0]);
            user_root.child("Category").setValue(details[1]);
            user_root.child("Status").setValue("Online");

            store_data = false;
        }
    }

        // get location permission using dexter
    void getLocationPermission() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            // do you work now
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    // this a background thread function, which makes continuous callbacks to location fetching APIs unless location is fetched
    // once location is fetched, it also stores Latitude, Longitude and corresponding address.
    void fetchLocation() {
        // initialise the client, check for permission and if not granted already, ask for permission
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (ActivityCompat.checkSelfPermission(DriverHome.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(DriverHome.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    getLocationPermission();
                }

                client.getLastLocation().addOnSuccessListener(DriverHome.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        user_latitude = location.getLatitude();
                        user_longitude = location.getLongitude();
                        //Toast.makeText(Home.this, user_latitude + "", Toast.LENGTH_LONG).show();

                        Geocoder geocoder = new Geocoder(DriverHome.this, Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(user_latitude, user_longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        if(addresses != null) {
                            user_root.child("Location").setValue(addresses.get(0).getAddressLine(0));
                            user_root.child("LatLong").setValue(user_latitude + "," + user_longitude);

                            if(gMap != null) {
                                Marker mark = addMarker(gMap, new LatLng(user_latitude, user_longitude));
                                Marker []markers = {mark};

                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for (Marker marker : markers)
                                    builder.include(marker.getPosition());
                                LatLngBounds bounds = builder.build();

                                int padding = 50;
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                gMap.animateCamera(cameraUpdate);
                            }
                        }
                    }
                }).addOnFailureListener(DriverHome.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        try {
                            Thread.sleep(1000);
                            run();

                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

            }
        });
    }

    public void logout() {
        FirebaseAuth firebaseAuth = MainActivity.firebaseAuth;

        if(firebaseAuth != null) {
            firebaseAuth.signOut();
            user_root.child("Status").setValue("Offline");
            startActivity(new Intent(DriverHome.this, MainActivity.class));
        }
        else
            Toast.makeText(DriverHome.this, "An unknown error occurred!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutMenu:
                logout();
                break;

            case R.id.toogleMenu:
                toggleStatus();
                break;

            case R.id.settingsMenu:
                // todo later
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    private void toggleStatus() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    if(child.getKey().equals("Status")) {
                        String status = child.getValue().toString();
                        if(status.equals("Online")) {
                            MainActivity.databaseReferenceRoot.child(phoneNumber).child("Status").setValue("Offline");
                            showSnackBar("You are now Offline! As a driver, this means your visibility on the map is now gone.");
                        }
                        else {
                            MainActivity.databaseReferenceRoot.child(phoneNumber).child("Status").setValue("Online");
                            showSnackBar("You are now Online! Your visibility on the map is back to live.");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        MainActivity.databaseReferenceRoot.child(phoneNumber).addListenerForSingleValueEvent(valueEventListener);
    }

    private void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}