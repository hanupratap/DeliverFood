package com.example.deliverfood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.TimedMetaData;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class Deliver_Ordered extends AppCompatActivity{

    private static final String USER_LIST = "Users";
    private static final float DEFAULT_ZOOM = 15;
    private String order_id;
    private FusedLocationProviderClient fusedLocationProviderClient;


    List<Order_item_template> item_listt = new ArrayList<>();
    OrderAdapter adapter;

    private TextView tv,tv1,tv2, tv3, tv4, tv5, tv6, tv7;
    Map<String, Double> order = new HashMap<>();
    ListView ls;
    String uid;
    private String COLLECTION = "Current_Orders";
    private String DELIVERY_PERSON_NAME = "delivery_person_name";
    private String DELIVERY_PERSON_EMAIL = "delivery_person_email";
    GeoPoint mypos, gp;

    private Location currentLocation;


    boolean temp = false;
    Button btn;



    boolean discount = false;



    double total;
    String message1;

    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver__ordered);
        uid = getIntent().getStringExtra("uid");
        order_id = getIntent().getStringExtra("order_id");

        user = getIntent().getParcelableExtra("user");



        tv = findViewById(R.id.textView22);
        tv1 = findViewById(R.id.textView25);
        tv2 = findViewById(R.id.textView33);
        ls = findViewById(R.id.listView5);
        tv3 = findViewById(R.id.textView27);
        tv4 = findViewById(R.id.textView35);
        btn = findViewById(R.id.button11);
        tv5 = findViewById(R.id.textView36);
        tv6 = findViewById(R.id.textView40);
        tv7 = findViewById(R.id.textView47);






        FirebaseFirestore.getInstance().collection(COLLECTION).document(order_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if( documentSnapshot.get("delivery_person_name")!=null)
                {
                    tv.setText("Name :" + documentSnapshot.get("delivery_person_name").toString());
                }
                if(documentSnapshot.get("delivery_person_phone")!=null)
                {

                    tv1.setText("Phone :" + documentSnapshot.get("delivery_person_phone").toString());
                }
                if(documentSnapshot.get("eatery_name")!=null)
                {

                    tv2.setText("Email: "+documentSnapshot.get("eatery_name").toString());
                }
                if(documentSnapshot.get("delivery_person_email")!=null)
                {

                    tv3.setText("Eatery :" + documentSnapshot.get("delivery_person_email").toString());
                }
                if(documentSnapshot.getString("order_code")!=null)
                {
                    tv6.append(documentSnapshot.getString("order_code"));
                }




                uid = documentSnapshot.get("user_id").toString();

                order = (Map<String, Double>) documentSnapshot.get("order");

               total = Float.parseFloat(String.format("%.3f",documentSnapshot.getDouble("total")));


                FirebaseFirestore.getInstance().collection("Users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Geocoder geocoder;
                        List<Address> addresses;
                        geocoder = new Geocoder(Deliver_Ordered.this, Locale.getDefault());

                        try {
                            if(currentLocation!=null)
                            {
                                addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                                if(addresses.size()!=0)
                                {
                                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                                    tv4.setText("Address :" + address);
                                }
                                     else
                                    {
                                        tv4.setText("Address not found!" );

                                    }

                                    Map a = new HashMap();
                                    gp = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                                    a.put("location", gp);
                                    FirebaseFirestore.getInstance().collection("Users").
                                            document(user.getUid()).set(a,SetOptions.merge());

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            tv4.setText("Address Not Found :");
                        }
                        List<String> list = new ArrayList<>();
                        int iend;
                        String name;
                        int count;
                        Double price;
                        int total_count = 0;
                        String fin;

                        tv5.setText("Order ID - " + order_id);

                        if(documentSnapshot.getBoolean("discount")!=null)
                            discount = documentSnapshot.getBoolean("discount");

                        float total1= 0;
                        for(String s:order.keySet())
                        {
                            iend = s.indexOf("$");
                            name = s.substring(0,iend);
                            price = Double.parseDouble(s.substring(iend+1));
                            count = (int)Double.parseDouble(order.get(s).toString());
                            fin =  name+"\n" +  getString(R.string.tab) +"Price : "+price+"\n"+  getString(R.string.tab) +"Count : " + count;



                            total1 = (float)(price*count);

                            message1 = message1 + "\n " + fin;
                            total_count=total_count+count;

                            item_listt.add(new Order_item_template(name, price, count, price*count));
                        }

                        int percentage_surge = (int)(((total-total1)*100)/(total1));

                        tv7.setText("NOTE: \n"+ percentage_surge +"% was added due to large distance between user and the eatery!\n");

                        if(discount)
                        {
                            total = total*0.8;
                            tv7.append("\nDiscount Applied = 20% " + "\nFINAL TOTAL = " + total);
                        }
                        else
                        {
                            tv7.append("\nNo discount available!");
                        }



                        item_listt.add(new Order_item_template("Final Total",total, total_count, total ));


                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Deliver_Ordered.this, android.R.layout.simple_list_item_1, list );
                        adapter = new OrderAdapter(Deliver_Ordered.this, R.layout.list_order_items, item_listt);
                        ls.setAdapter(adapter);

                    }



                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Deliver_Ordered.this, "Failed To Fetch Details", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


            FirebaseFirestore.getInstance().collection("Current_Orders").document(order_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(e!=null)
                    {
                        Toast.makeText(Deliver_Ordered.this, "Error" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        if(documentSnapshot!=null)
                        {

                            if(documentSnapshot.getBoolean("order_delivered")==true)
                            {
                                String message;
                                String delivery_name = documentSnapshot.getString("delivery_person_name");
                                String delivery_email = documentSnapshot.getString("delivery_person_email");
                                message = "Dear "+user.getDisplayName() + ","+"\nYour order has been delivered by " + delivery_name
                                        + " ( " + delivery_email + " )"+ "\nOrderID: " +   order_id + "\n Order Detail-";

                                message = message + message1;
                                message = message + "\n Final Total(After adjustments) :" + String.valueOf(total);

                                message = message + "\nRegards, \nFoodDeliver";
                                Toast.makeText(Deliver_Ordered.this, String.valueOf(temp), Toast.LENGTH_SHORT).show();



                                Toast.makeText(Deliver_Ordered.this, "Order Delivered", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Deliver_Ordered.this, OrderDeliverFinished.class);
                                intent.putExtra("order_id", order_id);
                                intent.putExtra("message", message);
                                intent.putExtra("send_email", !temp);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }

                        }

                    }
                }
            });




        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Deliver_Ordered.this, UserOderConfirmMap.class);
                intent.putExtra("order_id", order_id);
                intent.putExtra("user", user);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        temp = true;
        this.finish();
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

    void getLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        currentLocation = (Location) task.getResult();

                        if(currentLocation!=null)
                        {
                            mypos = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                            final Map user_instance = new HashMap<>();
                            user_instance.put("location",mypos);

                            if(uid!=null)
                            {
                                FirebaseFirestore.getInstance().collection(USER_LIST).document(uid).set(user_instance, SetOptions.merge());

                            }
                        }
                    }
                    else {
                        Toast.makeText(Deliver_Ordered.this, "Can't locate You!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        catch(Exception e) {
            e.printStackTrace();

        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        getLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocation();
    }
}
