package com.nabeeljaved.app.nabeelride;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.SettingsApi;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.Driver;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CutomerMapActivity extends FragmentActivity implements OnMapReadyCallback,

        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener

{


    //INITIALIZE SERVICES
    private static final int REQUEST_CODE_FINE_LOCATION = 7000;
//    private static final String[] INITIAL_PERMS = {
//            android.Manifest.permission.ACCESS_FINE_LOCATION,
//            android.Manifest.permission.ACCESS_COARSE_LOCATION
//    };


    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;


    //DECLARE BUTON/VARIABLES AND FIREBASE
    private Button LogoutCustomerButton, SettingsCustomerButton, CallCab;
    private FirebaseAuth mAuth;
    private FirebaseUser currentuser;
    private Boolean CurrentLogoutCustomerStatus = false;
    private String CustomerID;
    private LatLng customerPickUpLocationLatLng;
    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundId;
    Marker DriverMarker;

    //Databse References
    private DatabaseReference customerAvailableReference;
    private DatabaseReference DriverAvailableReference;
    private DatabaseReference DriverProfile;
    private DatabaseReference DriverLocationRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cutomer_map);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //INITIALIZING FIREBASE METHODS
        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();
        CustomerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        customerAvailableReference = FirebaseDatabase.getInstance().getReference().child("Customers Requests");
        DriverAvailableReference = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");


        //INITIALIZING VARIABLES
        LogoutCustomerButton = (Button) findViewById(R.id.btn_logout);
        SettingsCustomerButton = (Button) findViewById(R.id.btn_setting);
        CallCab = (Button) findViewById(R.id.btn_call_cab);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        LogoutCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.signOut();
                LogoutCustomer();

            }
        });

        CallCab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GeoFire geoFire = new GeoFire(customerAvailableReference);
                geoFire.setLocation(CustomerID, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));
                customerPickUpLocationLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(customerPickUpLocationLatLng).title("Pick up Customer From Here"));
                CallCab.setText("Getting Your Car.....");
                getCloseDriverCab();

            }
        });

    }

    private void getCloseDriverCab() {
        GeoFire geoFire = new GeoFire(DriverAvailableReference);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(customerPickUpLocationLatLng.latitude, customerPickUpLocationLatLng.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound) {
                    driverFound = true;
                    driverFoundId = key;
                    DriverProfile = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerRideID", CustomerID);
                    DriverProfile.updateChildren(driverMap);

                    GettingDriverLocation();
                    CallCab.setText("Looking for Driver Location");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius = radius + 1;
                    getCloseDriverCab();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void GettingDriverLocation() {
        DriverLocationRef.child(driverFoundId).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                    double LocationLat = 0;
                    double LocationLng = 0;
                    CallCab.setText("Driver Found");

                    if (driverLocationMap.get(0) != null) {
                        LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());

                    }
                    if (driverLocationMap.get(1) != null) {
                        LocationLng = Double.parseDouble(driverLocationMap.get(1).toString());
                    }

                    LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);
                    if (DriverMarker != null) {
                        DriverMarker.remove();
                    }

                    Location location1 = new Location("");
                    location1.setLatitude(customerPickUpLocationLatLng.latitude);
                    location1.setLongitude(customerPickUpLocationLatLng.latitude);

                    Location location2 = new Location("");
                    location2.setLatitude(DriverLatLng.latitude);
                    location2.setLongitude(DriverLatLng.longitude);

                    float Distance = location1.distanceTo(location2);
                    CallCab.setText("Driver Found" + String.valueOf(Distance));

                    DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Your Driver is Here"));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {


        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        //*************************************************************please See Code From Here **************************
        requestPermission();

        //*************************************************************please See Code From Here **************************
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

    }

    //*************************************************************this method is being called ******************************
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(CutomerMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(CutomerMapActivity.this)
                        .setMessage("We Need Permission For Location")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(CutomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_FINE_LOCATION);
                            }
                        }).show();
            } else {

                ActivityCompat.requestPermissions(CutomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_FINE_LOCATION);
            }

        } else {
            //permission is granted
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            Toast.makeText(this, "checking else", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission Granted

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    return;
                }

                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                Toast.makeText(this, "Permission is granted", Toast.LENGTH_SHORT).show();

            }else{

                if(!ActivityCompat.shouldShowRequestPermissionRationale(CutomerMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    //this block here means permanently Denied persmission
                    new AlertDialog.Builder(CutomerMapActivity.this)
                            .setMessage("You have Permanently Denied this Permission")
                            .setPositiveButton("Go To Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                         gotToAppSettings();
                                }
                            })
                            .setNegativeButton("cancel", null)
                            .setCancelable(false)
                            .show();
                }else{
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void gotToAppSettings()
    {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);


    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();

    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    private void LogoutCustomer()
    {
        Intent WelcomeActivity = new Intent(CutomerMapActivity.this, WelcomeActivity.class);
        WelcomeActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(this, "Successfully Log Out", Toast.LENGTH_SHORT).show();
        startActivity(WelcomeActivity);
        finish();
    }

}
