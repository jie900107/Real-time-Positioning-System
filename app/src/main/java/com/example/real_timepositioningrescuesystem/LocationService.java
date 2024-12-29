package com.example.real_timepositioningrescuesystem;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocationService extends Service {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private UserLocation userLocation;
    private FirebaseFirestore db;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        String Channel = "LocationChannel";
        NotificationChannel notificationChannel = new NotificationChannel(Channel,"Mychannel", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,Channel)
                .setContentTitle(" ")
                .setContentTitle(" ");
        startForeground(1,builder.build());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setusertouserlocation();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stopForeground(true);
        //stopSelf();
    }
    private void getLocation(){
        LocationRequest.Builder builder = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,4000);
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
        fusedLocationProviderClient.requestLocationUpdates(builder.build(), new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                userLocation.setLatitude(location.getLatitude());
                userLocation.setLongitude(location.getLongitude());
                saveLocation();
            }
        }, Looper.myLooper());
    }
    private void setusertouserlocation() {
        userLocation = new UserLocation();
        db.collection("User").document(FirebaseAuth.getInstance().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    User user = new User();
                    user = task.getResult().toObject(User.class);
                    userLocation.setUser(user);
                    Log.d("DEBUG", "Succesfully get user object from firebase.");
                    getLocation();
                } else
                    Log.d("DEBUG", "Failed to get user object from firebase.");
            }
        });
    }
    private void saveLocation() {
        if (userLocation != null) {
            Log.d("DEBUG", userLocation.toString());
            db.collection("UserLocation").document(FirebaseAuth.getInstance().getUid()).set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("SERVICE", "Succesfully update userlocation.");
                    } else
                        Log.d("SERVICE", "Failed to update userlocation.");
                }
            });
        }
    }
}
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

        }
        private void getlastLocation(){
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
        }
        private void saveuserlocation(){
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