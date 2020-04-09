package com.example.deliverfood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Delayed;

public class UserOderConfirmMap extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;


    Marker delivery_marker;
    FirebaseUser user;

    String uid;
    String USER_LIST = "Users";

    Map user_instance;
    GeoPoint mypos;

    private FusedLocationProviderClient fusedLocationProviderClient;

    String order_id ;

    String TAG = "UserOrderConfirmMap -- ";
    private static final float DEFAULT_ZOOM = 10;
    Location currentLocation;

    ProgressDialog progressDialog;

    @Override
    protected void onStart() {
        super.onStart();
        getLocation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_oder_confirm_map);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        uid = getIntent().getStringExtra("uid");

        user = getIntent().getParcelableExtra("user");
        order_id = getIntent().getStringExtra("order_id");





        FirebaseFirestore.getInstance().collection("Users").document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    Toast.makeText(UserOderConfirmMap.this, "Error" + e.toString(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {


                    GeoPoint gp = (GeoPoint) documentSnapshot.get("delivery_person_location");

                    if(gp!=null)
                    {
                        if(delivery_marker!=null)
                            delivery_marker.remove();

                        delivery_marker = mMap.addMarker(new MarkerOptions().position(new LatLng(gp.getLatitude(), gp.getLongitude())).title("Delivery Person's Location") );
                        delivery_marker.showInfoWindow();
                    }
//                                Log.d(TAG, "Current data: " + documentSnapshot.getData());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });


        FirebaseFirestore.getInstance().collection("Current_Orders").
                document(order_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null && documentSnapshot.exists()) {

                    GeoPoint eatery_pos = (GeoPoint) documentSnapshot.get("location");
                    if(eatery_pos!=null)
                    {
                        String s = documentSnapshot.get("eatery_name").toString();
                        Marker eatery = mMap.addMarker(new MarkerOptions().position(new LatLng(eatery_pos.getLatitude(), eatery_pos.getLongitude())).title(s));
                        eatery.showInfoWindow();
                    }
                } else {
                    Log.d("UserOrderConfirm -- ", "Current data: null");
                }
            }
        });


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
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);


    }

    private void moveCamera(LatLng latLng, float zoom)
    {
        Log.d(TAG, "moveCamer: moving camera to lat and long");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }



    void getLocation() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            final Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                         currentLocation = (Location) task.getResult();


                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);



                         mypos = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                         user_instance = new HashMap<>();
                        user_instance.put("location",mypos);


                        FirebaseFirestore.getInstance().collection(USER_LIST).document(uid).set(user_instance, SetOptions.merge());


                    }
                    else
                    {
                        Toast.makeText(UserOderConfirmMap.this, "Found not location", Toast.LENGTH_SHORT).show();

                    }
                }
            });



        }
        catch (Exception e)
        {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocation();
    }
}
