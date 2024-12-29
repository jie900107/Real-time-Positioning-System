package com.example.real_timepositioningrescuesystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.real_timepositioningrescuesystem.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;

public class Maps extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore db;
    private UserLocation userLocation,currentuser;
    private ArrayList<UserLocation> userLocationlist;
    private LatLngBounds latLngBounds;
    private ArrayList<CustomMarker> custommarkers = new ArrayList<>();
    private ClusterManager clusterManager;
    private CustomClusterRenderer customClusterRenderer;
    private Button signal;
    boolean haveseenmsg,oneofsignaler;
    AlertDialog alertDialog;

    Handler handler = new Handler();
    Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        signal = findViewById(R.id.signal);
        signal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Help").document(FirebaseAuth.getInstance().getUid()).set(currentuser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("SERVICE", "Succesfully send help.");
                        } else
                            Log.d("SERVICE", "Failed to send help.");
                    }
                });
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        //setusertouserlocation();
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        else{
            mMap.setMyLocationEnabled(true);
            if(!foregroundservicerunning()){
                Log.d("DEBUG","LocationService is not running yet.");
                Intent intent = new Intent(Maps.this,LocationService.class);
                startForegroundService(intent);
            }
            else
                Log.d("DEBUG","LocationService is already running.");
            //getalluserlocation();
            //setusertouserlocation();
        }
        alertDialog = new AlertDialog.Builder(Maps.this).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                haveseenmsg = true;
                dialog.cancel();
            }
        }).create();
        initMarkers();
    }
    private void checkifsomeoneneedhelp() {
        db.collection("Help").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(!task.getResult().isEmpty()){
                    String helpmessage="";
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        UserLocation temp = queryDocumentSnapshot.toObject(UserLocation.class);
                        if(queryDocumentSnapshot.getId().equals(FirebaseAuth.getInstance().getUid()))
                            oneofsignaler = true;
                        helpmessage += temp.getUser().getUsername() + "\n   Location : " + temp.getLatitude() + " , " + temp.getLongitude() + "\n";
                    }
                    Log.d("DEBUG",helpmessage);
                    if(!oneofsignaler && !haveseenmsg) {
                        alertDialog.setTitle("THESE PEOPLE NEEDS HELP!");
                        alertDialog.setMessage(helpmessage);
                        alertDialog.show();
                    }
                }
            }
        });

    }
    private boolean foregroundservicerunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)){
            if(LocationService.class.getName().equals(serviceInfo.service.getClassName()))
                return true;
        }
        return false;
    }

    private void startrunnable(){
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                retriveAllLocation();
                handler.postDelayed(runnable,3000);
            }
        },3000);
    }
    private void retriveAllLocation(){
        db.collection("UserLocation").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(QueryDocumentSnapshot queryDocumentSnapshots : task.getResult()){
                    UserLocation temp = queryDocumentSnapshots.toObject(UserLocation.class);
                    for(int i=0;i<custommarkers.size();i++) {
                        if (custommarkers.get(i).getUser().getUsername().equals(temp.getUser().getUsername())) {
                            LatLng UpdatedLatLng = new LatLng(temp.getLatitude(), temp.getLongitude());
                            custommarkers.get(i).setPosition(UpdatedLatLng);
                            customClusterRenderer.UpdateMarker(custommarkers.get(i));
                        }
                    }
                }
                checkifsomeoneneedhelp();
            }
        });
    }
    private void initMarkers(){
        userLocationlist = new ArrayList<>();
        db.collection("UserLocation").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot queryDocumentSnapshots : task.getResult()){
                        userLocationlist.add(queryDocumentSnapshots.toObject(UserLocation.class));
                        if(queryDocumentSnapshots.getId().equals(FirebaseAuth.getInstance().getUid())){
                            UserLocation temp = queryDocumentSnapshots.toObject(UserLocation.class);
                            latLngBounds = new LatLngBounds(new LatLng(temp.getLatitude() - .01,temp.getLongitude() - .01),new LatLng(temp.getLatitude() + .01,temp.getLongitude() + .01));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,0));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(temp.getLatitude(),temp.getLongitude())));
                            currentuser =queryDocumentSnapshots.toObject(UserLocation.class);
                        }
                    }
                    if(clusterManager == null)
                        clusterManager = new ClusterManager<CustomMarker>(getApplicationContext(),mMap);
                    if(customClusterRenderer == null)
                        customClusterRenderer = new CustomClusterRenderer(getApplicationContext(),mMap,clusterManager);
                    clusterManager.setRenderer(customClusterRenderer);
                    for(UserLocation userLocation : userLocationlist){
                        int temp = Integer.parseInt(userLocation.getUser().getAvatar());
                        //Log.d("DEBUG",String.valueOf(temp));
                        CustomMarker customMarker = new CustomMarker(new LatLng(userLocation.getLatitude(),userLocation.getLongitude()),userLocation.getUser().getUsername(),"HI",Integer.parseInt(userLocation.getUser().getAvatar()),userLocation.getUser());
                        clusterManager.addItem(customMarker);
                        custommarkers.add(customMarker);
                    }
                    clusterManager.cluster();
                    startrunnable();
                }
            }
        });
    }
}
/*private void getalluserlocation(){
        userLocationlist = new ArrayList<>();
        db.collection("UserLocation").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot queryDocumentSnapshots : task.getResult()){
                        if(queryDocumentSnapshots.getId().equals(FirebaseAuth.getInstance().getUid())){
                            UserLocation temp = queryDocumentSnapshots.toObject(UserLocation.class);
                            latLngBounds = new LatLngBounds(new LatLng(temp.getLatitude() - .01,temp.getLongitude() - .01),new LatLng(temp.getLatitude() + .01,temp.getLongitude() + .01));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,0));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(temp.getLatitude(),temp.getLongitude())));
                        }
                        userLocationlist.add(queryDocumentSnapshots.toObject(UserLocation.class));
                    }
                    for(UserLocation userLocation1 : userLocationlist){
                        Log.d("DEBUG",userLocation1.getUser().getUsername() + "    " + userLocation1.getLatitude().toString() + "    " + userLocation1.getLongitude().toString());
                    }
                    addMarkers();
                }
            }
        });
    }*/
/*private void setusertouserlocation(){
        if(userLocation== null) {
            userLocation = new UserLocation();
            db.collection("User").document(FirebaseAuth.getInstance().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        User user = new User();
                        user = task.getResult().toObject(User.class);
                        userLocation.setUser(user);
                        Log.d("DEBUG", "Succesfully get user object from firebase.");
                        getlastLocation();
                    } else
                        Log.d("DEBUG", "Failed to get user object from firebase.");
                }
            });

        }
        else{
            getlastLocation();
        }

    }*/
    /*private void getlastLocation(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.d("DEBUG","Latitude:" +location.getLatitude() +"    Altitude:" + location.getLongitude());
                userLocation.setLatitude(location.getLatitude());
                userLocation.setLongitude(location.getLongitude());
                latLngBounds = new LatLngBounds(new LatLng(location.getLatitude() - .01,location.getLongitude() - .01),new LatLng(location.getLatitude() + .01,location.getLongitude() + .01));
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,0));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));
                saveuserlocation();
            }
        });
    }*/

    /*private void saveuserlocation(){
        if(userLocation != null) {
            Log.d("DEBUG", userLocation.toString());
            db.collection("UserLocation").document(FirebaseAuth.getInstance().getUid()).set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        getalluserlocation();
                        Log.d("DEBUG", "Succesfully update userlocation.");
                    } else
                        Log.d("DEBUG", "Failed to update userlocation.");
                }
            });
        }
    }*/

