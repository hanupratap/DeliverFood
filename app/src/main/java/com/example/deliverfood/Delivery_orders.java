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
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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


@Override
protected void onStart() {
super.onStart();
getLocation();
}

//@Override
//public void onBackPressed() {
//if (doubleBackToExitPressedOnce) {
//    super.onBackPressed();
//    return;
//}
//
//this.doubleBackToExitPressedOnce = true;
//Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
//
//new Handler().postDelayed(new Runnable() {
//
//    @Override
//    public void run() {
//        doubleBackToExitPressedOnce=false;
//    }
//}, 2000);
//}


@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_delivery_orders);


spinner = findViewById(R.id.spinner);
sp = findViewById(R.id.swipeLayout);



dis.put("50m", 50);
dis.put("200m", 200);
dis.put("500m", 500);
dis.put("1Km", 1000);
dis.put("3Km", 3000);
dis.put("5Km", 5000);
dis.put("15Km", 15000);
dis.put("45Km", 45000);
dis.put("65Km", 65000);

List<String> dis1 = new ArrayList<String>();

dis1.add("50m");
dis1.add("200m");
dis1.add("500m");
dis1.add("1Km");
dis1.add("3Km");
dis1.add("5Km");
dis1.add("15Km");
dis1.add("45Km");
dis1.add("65Km");







ArrayAdapter<String> myadap = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dis1);
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
        @Override
        public void onComplete(@NonNull Task task) {


            if (task.isSuccessful()) {
                currentLocation = (Location) task.getResult();
            }
        }
    });
}
catch(Exception e) {
    e.printStackTrace();

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


@Override
public boolean onOptionsItemSelected(@NonNull MenuItem item) {

switch(item.getItemId())
{
    case R.id.logout:
    {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    case R.id.Home:
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}
return super.onOptionsItemSelected(item);
}

void func()
{
 final List<String> dishes = new ArrayList<>();
 final List<String> prices = new ArrayList<>();
 final List<String> order_id = new ArrayList<>();
 final List<String> distance = new ArrayList<>();
 final float[] dist = new float[10];

 collectionReference.whereEqualTo("confirm",false).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
     @Override
     public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
         for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots)
         {

             if (queryDocumentSnapshots == null) {
                 Toast.makeText(Delivery_orders.this, "No orders to show!", Toast.LENGTH_SHORT).show();
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
                     Toast.makeText(Delivery_orders.this, "Sorry!, could not locate you, try later.", Toast.LENGTH_SHORT).show();
                 }



             }
         }
         if (prices.size() == 0)
             Toast.makeText(Delivery_orders.this, "No orders to show, (Swipe Down to refresh!)", Toast.LENGTH_SHORT).show();

         user = getIntent().getParcelableExtra("user");


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
