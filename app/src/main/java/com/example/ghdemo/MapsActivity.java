package com.example.ghdemo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    private static final int GOOGLE_API_AVAILABILITY = 100;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 101;
    private static final int DEFAULT_ZOOM = 15;

    private boolean permissionGranted;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private Location lastLocation;

    private LatLng default_location = new LatLng(35.680833,139.766944);

    GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkGoogleApiAvailability();

        setContentView(R.layout.maps);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLocationPermission();

        moveToCurrentLocation();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getLocationPermission();
    }

    private void getLocationPermission(){
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            permissionGranted = true;
        }else{
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionGranted = false;
        switch (requestCode){
            case PERMISSION_ACCESS_FINE_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    permissionGranted = true;
                }
                break;
        }
       moveToCurrentLocation();
    }
    //Move the camera to current location if permission granted, else move to default location
    private void moveToCurrentLocation(){
        if(mMap == null){
            return;
        }
        try{
            if(permissionGranted){
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful()){
                            lastLocation = task.getResult();
                            if(lastLocation != null){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng((lastLocation.getLatitude()),lastLocation.getLongitude()),DEFAULT_ZOOM));
                            }
                        }else{
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(default_location,DEFAULT_ZOOM));
                            Log.d(TAG,"Fail to get Location");
                        }
                    }
                });
            }else{
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(default_location,DEFAULT_ZOOM));
                lastLocation = null;
            }
        }catch(SecurityException e){
            Log.e("Exception: %s",e.getMessage());
        }
    }


    private void checkGoogleApiAvailability(){
        int googleResult = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (googleResult != ConnectionResult.SUCCESS) {
            googleApiAvailability.showErrorDialogFragment(this, googleResult, GOOGLE_API_AVAILABILITY);
        }
    }
}