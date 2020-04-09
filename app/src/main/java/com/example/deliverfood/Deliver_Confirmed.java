package com.example.deliverfood;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Bundle;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Deliver_Confirmed extends AppCompatActivity implements ExampleDialog.ExampleDialogListener {

    private String USER_LIST = "Users", id;
    TextView tv,tv1,tv2,tv3,tv4, tv5;
    String order_id;
    FirebaseUser user;
    ListView ls;
    Button btn, confirm_btn;

    Map user_instance = new HashMap<>();
    Location currentLocation;

    FusedLocationProviderClient fusedLocationProviderClient;

    Map<String, Double> order = new HashMap<>();
    String uid="default";

    private String COLLECTION = "Current_Orders";
    GeoPoint mypos, gp, gp1;

    boolean discount = false;

    List<String> item_list = new ArrayList<>();

    String user_name, user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver__confirmed);
        user = getIntent().getParcelableExtra("user");
        order_id = getIntent().getExtras().getString("order_id");
        id = user.getUid();
        tv = findViewById(R.id.textView23);
        tv1 = findViewById(R.id.textView26);
        tv2 = findViewById(R.id.textView31);
        tv3 = findViewById(R.id.textView30);
        tv4 = findViewById(R.id.textView32);
        tv5 = findViewById(R.id.textView38);
        btn = findViewById(R.id.button10);
        ls = findViewById(R.id.listView22);
        confirm_btn = findViewById(R.id.button);



        confirm_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ExampleDialog exampleDialog = new ExampleDialog();
                exampleDialog.show(getSupportFragmentManager(), "Example Dialog");


            }
        });



        tv.append(order_id);

        Map m1 = new HashMap();
        m1.put("delivery_person_id", user.getUid());

        FirebaseFirestore.getInstance().collection(COLLECTION).document(order_id).set(m1, SetOptions.merge());

        FirebaseFirestore.getInstance().collection(COLLECTION).document(order_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                tv1.append(documentSnapshot.get("user_name").toString());
                tv2.append(documentSnapshot.get("user_email").toString());
                tv3.append(documentSnapshot.get("eatery_name").toString());
                tv5.append(documentSnapshot.get("user_phone").toString());
                if(documentSnapshot.get("user_id")!=null)
                {
                    uid = documentSnapshot.get("user_id").toString();
                }




                order = (Map<String, Double>) documentSnapshot.get("order");


                final float total = Float.parseFloat(documentSnapshot.get("total").toString());

                FirebaseFirestore.getInstance().collection("Users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        user_email = documentSnapshot.getString("user_email");
                        user_name = documentSnapshot.getString("user_name");



                        gp1 = (GeoPoint) documentSnapshot.get("location");
                        if(gp1==null)
                        {
                            Toast.makeText(Deliver_Confirmed.this, "User location null!", Toast.LENGTH_SHORT).show();
                        }
                        Geocoder geocoder;
                        List<Address> addresses;
                        geocoder = new Geocoder(Deliver_Confirmed.this, Locale.getDefault());

                        try {
                            addresses = geocoder.getFromLocation(gp1.getLatitude(), gp1.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                            if(addresses.size()!=0)
                            {
                                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                tv4.setText("Address :" + address);

                            }

                            else
                            {
                                tv4.setText("Address not found!" );

                            }



                        } catch (IOException e) {
                            e.printStackTrace();
                            tv4.setText("Address Not Found! ");
                        }




                        if(currentLocation!=null)
                        {

                            user_instance.put("delivery_person_id", id);
                            user_instance.put("delivery_person_location",mypos);
                            user_instance.put("initial_delivery_person_location",mypos);

                            FirebaseFirestore.getInstance().collection(USER_LIST).document(uid).set(user_instance, SetOptions.merge());
                        }


                        if(documentSnapshot.getBoolean("discount")!=null)
                        discount = documentSnapshot.getBoolean("discount");
                        List<String> list = new ArrayList<>();
                        int iend;
                        String name;
                        int count;
                        Double price;
                        String fin;
                        float total1=0;
                        for(String s:order.keySet())
                        {
                            iend = s.indexOf("$");
                            name = s.substring(0,iend);
                            price = Double.parseDouble(s.substring(iend+1));
                            count = (int)Double.parseDouble(order.get(s).toString());
                            fin =  name+"\n" +  getString(R.string.tab) +"Price : "+price+"\n"+  getString(R.string.tab) +"Count : " + count;
                            list.add(fin);
                            total1 =  total1 + (float)(price*count);
                            item_list.add(fin);
                        }
                        list.add("Total = " + total);
                        int percentage_surge = (int)(((total-total1)*100)/(total1));

                        list.add("\n NOTE: \n"+ percentage_surge +"% was added due to large distance between user and the eatery!\n");

                        if(discount)
                        {
                            list.add("-----DISCOUNT APPLIED 20%-----");
                            Toast.makeText(Deliver_Confirmed.this, "Discount Applied, we care for our regular customers!!", Toast.LENGTH_SHORT).show();
                            list.add("Final Total : " + total *0.8 );
                            list.add(0,"Final Total = " + total *0.8);
                            list.add(1, "------------- Details --------------");


                        }
                        else
                        {

                            list.add("NO DISCOUNT AVAILABLE :(");
                            list.add(0,"Final Total : " + total);
                            list.add(1, "------------- Details --------------");


                        }

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Deliver_Confirmed.this, android.R.layout.simple_list_item_1, list );
                        ls.setAdapter(arrayAdapter);
                        tv.append(order_id);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Deliver_Confirmed.this, "ERROR!!!", Toast.LENGTH_SHORT).show();
                    }
                });


             }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Deliver_Confirmed.this, DeliveryOrderConfirmMap.class);
                intent.putExtra("order_id", order_id);
                intent.putExtra("user", user);

                intent.putExtra("uid", uid);
                startActivity(intent);


            }
        });








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
            final Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        currentLocation = (Location) task.getResult();



                        if(currentLocation!=null)
                        {
                            mypos = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());

                            user_instance.put("delivery_person_id", id);
                            user_instance.put("delivery_person_location",mypos);

                            FirebaseFirestore.getInstance().collection(USER_LIST).document(uid).set(user_instance, SetOptions.merge());
                        }


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






    @Override
    public void checkCode(final String order_code) {
         FirebaseFirestore.getInstance().collection("Current_Orders").document(order_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                 if(documentSnapshot.getString("order_code").equals(order_code))
                {
                     Map map = new HashMap();
                    map.put("order_delivered", true);
                    FirebaseFirestore.getInstance().collection("Current_Orders").document(order_id).set(map, SetOptions.merge());
                    Intent intent = new Intent(Deliver_Confirmed.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Toast.makeText(Deliver_Confirmed.this, "Order Delivered!", Toast.LENGTH_SHORT).show();

                    String message;
                    message = "Dear "+user.getDisplayName() + ","+"\nYou have delivered the order of " + user_name
                            + " ( " + user_email + " )"+ "\nOrderID: " +   order_id + "\n Order Detail-";
                    for(String s:item_list)
                    {
                        message = message + "\n" + s;
                    }

                    message = message + "\nRegards, \nFoodDeliver";


                    SendMail sm = new SendMail(Deliver_Confirmed.this, user.getEmail(), "Order Delivered", message);
                    sm.execute();

                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(Deliver_Confirmed.this, "Wrong Code, Try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
