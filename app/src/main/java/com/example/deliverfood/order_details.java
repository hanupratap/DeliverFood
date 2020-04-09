package com.example.deliverfood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;


import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;


import java.io.Serializable;


import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import io.paperdb.Paper;

public class order_details extends AppCompatActivity implements Serializable {
 

    private TextView tv, tv9;
    private TextView tv1;
    private ListView ls;

    int i=0;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private Location currentLocation;


    private Button confirm;
    private List<Data> list = new ArrayList<>();

    private ProgressDialog progressDialog;

    private ArrayAdapter<String> ordered_items;

    private String CURRENT_ORDERS = "Current_Orders";
    private String mPhoneNumber;
    static final int PERMISSION_READ_STATE = 123;
    private Double total = 0.0;

    FirebaseUser user;
    float[] dist = new float[10];
    GeoPoint gp;

    OrderAdapter adapter;

    List<Order_item_template> itemlist = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(CURRENT_ORDERS);

    @Override
    protected void onStart() {
        super.onStart();
        getLocation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        tv9 = findViewById(R.id.textView44);

        final Double lat = getIntent().getExtras().getDouble("eatery_location_latitude");
        final Double lon = getIntent().getExtras().getDouble("eatery_location_longitude");
        gp = new GeoPoint(lat, lon);
        user = FirebaseAuth.getInstance().getCurrentUser();

        ordered_items = new ArrayAdapter<String>(order_details.this, android.R.layout.simple_list_item_1);

        int permissionCheck = ContextCompat.checkSelfPermission(order_details.this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            mPhoneNumber = tMgr.getLine1Number();
        } else {
            ActivityCompat.requestPermissions(order_details.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_STATE);
        }


        tv = (TextView)findViewById(R.id.textView6);
        tv1 = (TextView)findViewById(R.id.textView7);
        ls = (ListView)findViewById(R.id.listView22);
        confirm = (Button)findViewById(R.id.button5);

        Paper.init(order_details.this);

        List<String> allKeys = Paper.book().getAllKeys();

        Data d = new Data();
        for(String key: allKeys)
        {
            d = Paper.book().read(key);
            Paper.book().delete("contacts");

            if(d.items!=0)
            {
                list.add(d);
                ordered_items.add(String.valueOf(d.items)+ " " + d.name + " @ Rs." + String.valueOf(d.price) );
                total += d.price*d.items;

                itemlist.add(new Order_item_template(d.name, d.price, d.items, d.price*d.items));

            }

        }



        Intent intent = order_details.this.getIntent();
        final String eatery_id = intent.getStringExtra("eatery_id");
        final String eatery_name = intent.getStringExtra("eatery_name");






        Paper.init(order_details.this);
        Paper.book().destroy();







        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog = new ProgressDialog(order_details.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );





                Map<String,Double> map = new HashMap();

                for(Data dat:list)
                {
                    map.put(dat.name+"$"+dat.price, (double) dat.items);
                }








                String uid =  user.getUid();
                String uname = user.getDisplayName();
                String uemail = user.getEmail();




                CurrentOrder currentOrder = new CurrentOrder( map , uid, total, eatery_id, eatery_name, gp, uname, uemail, mPhoneNumber);

                Map<String, String> user_info = new HashMap<>();
                user_info.put("user_name",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                user_info.put("user_email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(user_info, SetOptions.merge());

                Toast.makeText(order_details.this, String.valueOf(i++), Toast.LENGTH_SHORT).show();
                collectionReference.add(currentOrder).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(order_details.this, "Success", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        Intent intent = new Intent(order_details.this, OrderConfirm.class);
                        intent.putExtra("order_id", documentReference.getId());
                        intent.putExtra("user", user);
                        startActivity(intent);
                        finish();
                    }
                });




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

                        if(currentLocation==null)
                        {
                            Toast.makeText(order_details.this, "Location Not Found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }

                    if(currentLocation!=null)
                    {

                        Location.distanceBetween(gp.getLatitude(), gp.getLongitude(), currentLocation.getLatitude(), currentLocation.getLongitude(), dist);
                        if(dist[0]<20.0*1000 && dist[0]>10.0*1000)
                        {
                            total = total*1.05;

                            tv9.setText("\nNOTE: \n5% added due to large distance between you and the eatery!\n");

                        }
                        else if(dist[0]<20.0*1000)
                        {
                            total = total*1.1;

                            tv9.setText("\nNOTE: \n10% added due to large distance between you and the eatery!\n");
                        }
                        else if(dist[0]<40.0*1000)
                        {
                            total = total*1.15;

                            tv9.setText("\nNOTE: \n15% added due to large distance between you and the eatery!\n");
                        }
                        else
                        {
                            total = total*1.2;

                            tv9.setText("\nNOTE: \n20% added due to large distance between you and the eatery!\n");
                        }


                    }
                    tv1.append(String.valueOf(total));


                    adapter = new OrderAdapter(order_details.this, R.layout.list_order_items, itemlist);
                    ls.setAdapter(adapter);





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
