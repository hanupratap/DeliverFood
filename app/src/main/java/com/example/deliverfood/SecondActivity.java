package com.example.deliverfood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;


public class SecondActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1234;
    private static FirebaseUser user;
    private String TAG = "MyActivity";
    private static final String KEY_TTILE = "Name";
    private static final String KEY_DESCRIPTION = "description";


    private Map map_rel = new HashMap();
    private int total_orders = 0;

    private Button mapp, order_history_btn, current_order_btn;

    private TextView tv;
    private EditText ed1;
    private EditText ed2;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private DocumentReference noteRef = db.collection("Notebook").document("My First Note");


    private TextView tv1;

    private FusedLocationProviderClient client;


    Timestamp tt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        checkPermissions();
        tv = (TextView)findViewById(R.id.textView34);
        user = getIntent().getParcelableExtra("user");
        tv.setText("Welcome,\n"+user.getDisplayName());

        mapp = (Button)findViewById(R.id.button2);
        order_history_btn = findViewById(R.id.button12);
        current_order_btn = findViewById(R.id.button3);
        tv1 = (TextView)findViewById(R.id.textView2);

        FirebaseFirestore.getInstance().collection("site_news").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots)
                {
                    tv1.append(documentSnapshot.getString("text")+"\n");
                }
            }
        });


        FirebaseFirestore.getInstance().collection("Current_Orders").whereEqualTo("user_id",user.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots)
                        {
                            if(tt==null)
                            {
                                tt = documentSnapshot.getTimestamp("order_time");
                            }
                            else if(tt.compareTo(documentSnapshot.getTimestamp("order_time")) < 1)
                            {
                                tt = documentSnapshot.getTimestamp("order_time");
                            }



//                            Toast.makeText(SecondActivity.this, documentSnapshot.getId(), Toast.LENGTH_SHORT).show();
                            total_orders++;
                        }
                        map_rel.put("order_count",total_orders);
                        map_rel.put("last_order_time", tt);
                        FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).set(map_rel, SetOptions.merge());

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SecondActivity.this, "Failed to retrieve data!", Toast.LENGTH_SHORT).show();
            }
        });




        current_order_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(SecondActivity.this, CurrentOrders.class);
                startActivity(intent);

            }
        });


        order_history_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondActivity.this, OrderHistory.class);
                intent.putExtra("user", user);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });


        mapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent a = new Intent(SecondActivity.this, MapsActivity.class);
                a.putExtra("user", user);
                a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                a.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                SecondActivity.this.startActivity(a);

            }
        });
    }







    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
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
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
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
    void checkPermissions(){

        if (ContextCompat.checkSelfPermission(SecondActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(SecondActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(SecondActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);


            }
        } else {
           ;
        }
    }

}
