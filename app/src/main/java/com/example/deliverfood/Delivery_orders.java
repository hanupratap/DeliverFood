package com.example.deliverfood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Delivery_orders extends AppCompatActivity {

    Button btn;

    FirebaseUser user;
    Map<String, Integer> dis = new HashMap<>();

    private CollectionReference collectionReference;
    private String COLLECTION_NAME = "Current_Orders";
    private String ITEM_NAME = "eatery_name";
    private String TOTAL = "total";
    private String mPhoneNumber;
    static final int PERMISSION_READ_STATE = 123;
    private RecyclerView recyclerView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    Spinner spinner;

    ProgressDialog progressDialog;
    private SwipeRefreshLayout sp;
    boolean doubleBackToExitPressedOnce = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<String> dishes = new ArrayList<>();
    List<String> prices = new ArrayList<>();
    List<String> order_id = new ArrayList<>();
    List<String> distance = new ArrayList<>();


    LinearLayout linear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_orders);

        linear = findViewById(R.id.linearLayout3);
        spinner = findViewById(R.id.spinner);
        sp = findViewById(R.id.swipeLayout);

        btn = findViewById(R.id.button13);
        getSupportActionBar().setTitle("Deliver");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(0,0,0,0)));

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Delivery_orders.this, DeliveredOrderHistory.class);
                Delivery_orders.this.startActivity(intent);

            }
        });

        dis.put("50m", 50);
        dis.put("200m", 200);
        dis.put("500m", 500);
        dis.put("1Km", 1000);
        dis.put("3Km", 3000);
        dis.put("5Km", 5000);
        dis.put("15Km", 15000);
        dis.put("45Km", 45000);
        dis.put("65Km", 65000);
        dis.put("Infinite", Integer.MAX_VALUE);

        List<String> dis1 = new ArrayList<>();

        dis1.add("50m");
        dis1.add("200m");
        dis1.add("500m");
        dis1.add("1Km");
        dis1.add("3Km");
        dis1.add("5Km");
        dis1.add("15Km");
        dis1.add("45Km");
        dis1.add("65Km");
        dis1.add("Infinite");


        final ArrayAdapter<String> myadap = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dis1);
        spinner.setAdapter(myadap);


        int permissionCheck = ContextCompat.checkSelfPermission(Delivery_orders.this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            mPhoneNumber = tMgr.getLine1Number();
        } else {
            ActivityCompat.requestPermissions(Delivery_orders.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_STATE);
        }

        progressDialog = new ProgressDialog(Delivery_orders.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );
        progressDialog.setCancelable(false);


        recyclerView = findViewById(R.id.recyclerView);

        collectionReference = db.collection(COLLECTION_NAME);

        getLocation();

        func();

        sp.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.setAdapter(null);
                getLocation();
                func();
                sp.setRefreshing(false);

            }
        });
    }


    void getLocation() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            final Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        if(task.getResult()!=null)
                        {
                            currentLocation = (Location) task.getResult();

                        }
                        else
                        {
                            LocationListener locationListener;
                            locationListener = new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    currentLocation = location;

                                }

                                @Override
                                public void onStatusChanged(String s, int i, Bundle bundle) {

                                }

                                @Override
                                public void onProviderEnabled(String s) {

                                }

                                @Override
                                public void onProviderDisabled(String s) {
                                    Snackbar.make(linear, "Please Turn on Location Services", Snackbar.LENGTH_INDEFINITE)
                                            .setAction("Close", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) { }
                                            })
                                            .setActionTextColor(getResources().getColor(R.color.colorAccent)).show();
                                }
                            };

                            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                            locationManager.requestLocationUpdates("gps", 5000,0, locationListener);


                        }
                    }
                    else
                    {

                        Snackbar.make(linear, "Location not found", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Close", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                })
                                .setActionTextColor(getResources().getColor(R.color.colorAccent)).show();

                    }
                }
            });
        }
        catch (Exception e)
        {

        }

    }

@Override
public boolean onCreateOptionsMenu(Menu menu) {
MenuInflater inflater = getMenuInflater();
inflater.inflate(R.menu.auth_menu, menu);
if(FirebaseAuth.getInstance().getCurrentUser()!=null)
{
    menu.findItem(R.id.user_info).setTitle(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
}

else
{
    menu.findItem(R.id.user_info).setTitle("No User logged in");
    menu.findItem(R.id.user_info).setEnabled(false);

}

return super.onPrepareOptionsMenu(menu);
}


    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.logout:
            {
                LoginManager.getInstance().logOut();
                gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
                FirebaseAuth.getInstance().signOut();

                // Google sign out
                mGoogleSignInClient.signOut().addOnCompleteListener(this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(Delivery_orders.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                startActivity(intent);
                                finish();
                            }
                        });

                finish();
            }
            case R.id.Home:
            {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }


        }
        return super.onOptionsItemSelected(item);
    }

void func()
{
    dishes.clear();
    prices.clear();
    order_id.clear();
    distance.clear();
    final float[] dist = new float[10];

 collectionReference.whereEqualTo("confirm",false).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
     @Override
     public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
         for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots)
         {

             if (queryDocumentSnapshots == null) {

                 Toast toast;
                 toast = Toast.makeText(Delivery_orders.this, "No orders to show!", Toast.LENGTH_SHORT);
                 toast.setGravity(Gravity.CENTER, 0,                 0);
                 toast.show();

             } else if (documentSnapshot.get("confirm").toString() != "true") {

                 GeoPoint gp = documentSnapshot.getGeoPoint("location");


                 if(currentLocation!=null)
                 {
                     Location.distanceBetween(gp.getLatitude(), gp.getLongitude(), currentLocation.getLatitude(), currentLocation.getLongitude(), dist);


                     if (dist[0] < (float) dis.get(spinner.getSelectedItem().toString())) {
                         dishes.add(documentSnapshot.get(ITEM_NAME).toString());

                         prices.add(documentSnapshot.get(TOTAL).toString());

                         order_id.add(documentSnapshot.getId());

                         if (dist[0] > 1000) {
                             distance.add(String.valueOf((int) dist[0] / 1000) + "Km");
                         } else {
                             distance.add(String.valueOf((int) dist[0]) + "m");
                         }

                     }
                 }
                 else
                 {
                     getLocation();

                 }



             }
         }
         if (prices.size() == 0)
         {
             Toast toast;
             toast = Toast.makeText(Delivery_orders.this, "No orders to show! (Swipe down to refresh)", Toast.LENGTH_SHORT);
             toast.setGravity(Gravity.CENTER, 0, 0);
             toast.show();
         }


         user = FirebaseAuth.getInstance().getCurrentUser();


         progressDialog.dismiss();

         RecyclerViewAdapter_orders myAdapter = new RecyclerViewAdapter_orders(Delivery_orders.this, dishes, prices, order_id, (FirebaseUser) user, mPhoneNumber, distance);
         recyclerView.setAdapter(myAdapter);
         recyclerView.setLayoutManager(new LinearLayoutManager(Delivery_orders.this));
         }

 });

}



@Override
protected void onResume() {
    super.onResume();
    getLocation();
}

@SuppressLint("MissingPermission")
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
super.onRequestPermissionsResult(requestCode, permissions, grantResults);
switch (requestCode) {
    case PERMISSION_READ_STATE: {
        if (grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            mPhoneNumber = tMgr.getLine1Number();
        } else {
            Toast.makeText(this, "You don't have required permissions to make an action!", Toast.LENGTH_SHORT).show();
        }
    }
}
}


}
