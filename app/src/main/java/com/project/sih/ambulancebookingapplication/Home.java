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
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Home extends AppCompatActivity implements OnMapReadyCallback {

    // variable that indicates if user information needed to be stored to the Firebase Realtime Database
    // will be True when the user logs in for the first time (ie creates his account)
    // and false for subsequent entrance to this activity
    public static boolean store_data;

    private boolean regTokenWritten = false;

    // variable that stores your own registration token
    private String myRegToken;

    private String userLocation;

    // extract some data from shared preference
    SharedPreferences sharedPreferences;
    String phoneNumber;
    double user_latitude, user_longitude;
    String id;
    DatabaseReference user_root, users_root;

    // will be used to fetch the current location
    private FusedLocationProviderClient client;

    private GoogleMap gMap = null;

    // A list of all the markers
    List<Marker> markers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MainActivity.category = "Rider";

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // extract some data from shared preference
        sharedPreferences = getSharedPreferences(MainActivity.USER_FILE_KEY, MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("user_number", null);
        id = sharedPreferences.getString("user_id", null);

        SharedPreferences.Editor sharedEditor = sharedPreferences.edit();
        sharedEditor.putString("category", "Rider");
        sharedEditor.apply();

        user_root = MainActivity.databaseReferenceRoot.child(phoneNumber);
        users_root = FirebaseDatabase.getInstance().getReference("Users");
        users_root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(gMap != null)
                    MarkAllDriverLocations(gMap);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(gMap != null)
                    MarkAllDriverLocations(gMap);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if(gMap != null)
                    MarkAllDriverLocations(gMap);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(gMap != null)
                    MarkAllDriverLocations(gMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // perform some Firebase Realtime Database neccessary operations.
        // includes fetching(and then stroing) the location
        // initialising reg token and then finally storing other information(if needed, see the function storeData for a detail on this)
        fetchLocation();
        InitToken();
        storeData();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        markers.clear();

        gMap = googleMap;

        // set the camera zoom factor and mark the location of all the drivers
        MarkAllDriverLocations(googleMap);

        // create an AlertDialog if the user clicks on a marker
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(Home.this, android.R.style.Theme_Material_Dialog_Alert);
                } else
                    builder = new AlertDialog.Builder(Home.this);

                final String ambulanceDetails = marker.getTitle();

                builder.setTitle("Book an Ambulance from the clicked location?").setMessage("Are you sure you want to book this " +
                        "Ambulance?\nDetails:\n"+ambulanceDetails).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(Home.this, "Your request will be forwarded", Toast.LENGTH_LONG).show();

                        String targetRegToken = marker.getTag() != null ? marker.getTag().toString() : myRegToken;
                        double receiver_latitude = marker.getPosition().latitude, receiver_longitude = marker.getPosition().longitude;

                        String driverPhoneNumber = "";
                        String []separatedDetails = ambulanceDetails.split("\n");
                        for(String separatedDetail : separatedDetails) {
                            if(separatedDetail.contains("Number")) {
                                driverPhoneNumber = separatedDetail.split("Number: ")[1];
                                break;
                            }
                        }


                        MessageSender.getInstance().sendMessage(targetRegToken, "Need ambulance at " + userLocation,
                                "Urgently need the Ambulance!,"+"Driver,"+myRegToken+","+user_latitude+","+user_longitude+","+
                        receiver_latitude+","+receiver_longitude+","+phoneNumber+","+driverPhoneNumber);
                    }
                }).show();


                return false;
            }
        });

    }

    // helper function that adds marker to the map and return the instance of added marker.
    private Marker addMarker(GoogleMap googleMap, LatLng latLng, String regToken, String title) {
        Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(title));
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.driver_low));
        if(regToken != null)
            marker.setTag(regToken);
        return marker;
    }

    // This will initialise the myRegToken variable with the Registration token
    private void InitToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(Home.this, new OnSuccessListener<InstanceIdResult>() {
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

        // always store Reg Token
        if (myRegToken != null) {
            user_root.child("Reg Token").setValue(myRegToken);
            regTokenWritten = true;
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

                if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    getLocationPermission();
                }

                client.getLastLocation().addOnSuccessListener(Home.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        user_latitude = location.getLatitude();
                        user_longitude = location.getLongitude();
                        //Toast.makeText(Home.this, user_latitude + "", Toast.LENGTH_LONG).show();

                        Geocoder geocoder = new Geocoder(Home.this, Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(user_latitude, user_longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        if(addresses != null) {
                            user_root.child("Location").setValue(userLocation = addresses.get(0).getAddressLine(0));
                            user_root.child("LatLong").setValue(user_latitude + "," + user_longitude);
                        }
                    }
                }).addOnFailureListener(Home.this, new OnFailureListener() {
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

    // This function puts a marker on the map for every driver location
    // It first fetches the location of all the drivers and then add a marker for each of them
    // It also handles setting the position of camera based on the mean of latitude and longitude.
    void MarkAllDriverLocations(final GoogleMap googleMap) {

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                HashMap<String, DriverInfo> passToUtil = new HashMap<>();

                for(DataSnapshot number : dataSnapshot.getChildren()) {
                    String category = "", latLong = "", regToken = "", status = "";
                    final String num = number.getKey();

                    for(DataSnapshot numberDetail : number.getChildren()) {
                        if(numberDetail.getKey().equals("Category"))
                            category = numberDetail.getValue().toString();
                        else if(numberDetail.getKey().equals("LatLong"))
                            latLong = numberDetail.getValue().toString();
                        else if(numberDetail.getKey().equals("Reg Token"))
                            regToken = numberDetail.getValue().toString();
                        else if(numberDetail.getKey().equals("Status"))
                            status = numberDetail.getValue().toString();
                    }

                    if(category.equals("Driver") && status.equals("Online")) {
                        String []latitudeLongitude = latLong.split(",");
                        final double latitude = Double.parseDouble(latitudeLongitude[0]);
                        final double longitude = Double.parseDouble(latitudeLongitude[1]);

                        DriverInfo driverInfo = new DriverInfo(latitude, longitude, regToken);
                        passToUtil.put(num, driverInfo);
                    }
                }

                addMarkerUtil(passToUtil, googleMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        };

        MainActivity.databaseReferenceRoot.addListenerForSingleValueEvent(postListener);
    }

    private void addMarkerUtil(final HashMap<String, DriverInfo> passToUtil, final GoogleMap googleMap) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // remove the old markers from map and the list
                for(Marker marker : markers)
                    marker.remove(); // remove markers from the map
                markers.clear(); // clear the markers list

                for(DataSnapshot number : dataSnapshot.getChildren()) {
                    if(passToUtil.containsKey(number.getKey())) {
                        HashMap<String, String> ambulanceDetails = new HashMap<>();

                        for(DataSnapshot ambulanceDetail : number.getChildren()) {
                            ambulanceDetails.put(ambulanceDetail.getKey(), ambulanceDetail.getValue().toString());
                        }

                        ambulanceDetails.put("Number", number.getKey());
                        DriverInfo driverInfo = passToUtil.get(number.getKey());

                        LatLng latLng = new LatLng(driverInfo.lati, driverInfo.longi);
                        String markerLabel = getMarkerLabel(ambulanceDetails);
                        String regisToken = driverInfo.regisToken;

                        Marker marker = addMarker(googleMap, latLng, regisToken, markerLabel);
                        markers.add(marker);
                    }
                }

                if(!markers.isEmpty()) {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Marker marker : markers)
                        builder.include(marker.getPosition());
                    LatLngBounds bounds = builder.build();

                    int padding = 50;
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    googleMap.animateCamera(cameraUpdate);
                } else {
                    showSnackBar("No drivers as of now, we will indicate as soon as someone joins");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        MainActivity.databaseReferenceRootDriver.addListenerForSingleValueEvent(valueEventListener);
    }

    // Constructs a label to be put on a marker
    private String getMarkerLabel(HashMap<String, String> ambulanceDetails) {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("Associated with: " + ambulanceDetails.get("Associated With"));
        stringBuffer.append("\n Doctor Availability: " + ambulanceDetails.get("Doctor Availability"));
        stringBuffer.append("\nName: " + ambulanceDetails.get("Name"));
        stringBuffer.append("\nNumber: " + ambulanceDetails.get("Number"));
        if(ambulanceDetails.get("Staff Availability").equals("true"))
            stringBuffer.append("\nStaff Number: " + ambulanceDetails.get("Staff Number"));
        else
            stringBuffer.append("\nNo Staff Available");

        stringBuffer.append("\nFacilities:\n" + (ambulanceDetails.get("Facilities").length() > 0 ? ambulanceDetails.get("Facilities")
        : "Nothing Specific"));

        return stringBuffer.toString();
    }

    // logs the user out and turn his status to Offline
    public void logout() {
        FirebaseAuth firebaseAuth = MainActivity.firebaseAuth;

        if(firebaseAuth != null) {
            firebaseAuth.signOut();
            user_root.child("Status").setValue("Offline");
            startActivity(new Intent(Home.this, MainActivity.class));
        }
        else
            Toast.makeText(Home.this, "An unknown error occurred!", Toast.LENGTH_LONG).show();
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

    // toggle the status from Online to Offline and Vice Versa
    private void toggleStatus() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("STCHG", "called");
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    if(child.getKey().equals("Status")) {
                        String status = child.getValue().toString();
                        if(status.equals("Online")) {
                            MainActivity.databaseReferenceRoot.child(phoneNumber).child("Status").setValue("Offline");
                            showSnackBar("You are now Offline!");
                        }
                        else {
                            MainActivity.databaseReferenceRoot.child(phoneNumber).child("Status").setValue("Online");
                            showSnackBar("You are now Online!");
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

    // shows a Snackbar Message indicating the change in status
    private void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void adjustMarkerBounds() {
        if(!markers.isEmpty() && gMap != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers)
                builder.include(marker.getPosition());
            LatLngBounds bounds = builder.build();

            int padding = 50;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            gMap.animateCamera(cameraUpdate);
        } else {
            showSnackBar("No drivers as of now, we will indicate as soon as someone joins");
        }
    }

    private void addSingleMarkerUtil(DataSnapshot dataSnapshot) {
        // Todo Later
    }
}

class DriverInfo {
    double lati, longi;
    String regisToken;

    DriverInfo(double lati, double longi, String regisToken) {
        this.lati = lati;
        this.longi = longi;
        this.regisToken = regisToken;
    }
}
