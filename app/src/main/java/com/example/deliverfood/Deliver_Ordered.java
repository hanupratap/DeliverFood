package com.example.deliverfood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.TimedMetaData;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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

    FirebaseUser user;
    private ProgressDialog progressDialog;

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver__ordered);
        order_id = getIntent().getStringExtra("order_id");

        user = FirebaseAuth.getInstance().getCurrentUser();

        SpannableString sp = new SpannableString("Order Confirmed");
        sp.setSpan(new ForegroundColorSpan(Color.rgb(15,157,88)), 0, "Order Confirmed".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(sp);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(0,0,0,0)));

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
        progressDialog = new ProgressDialog(Deliver_Ordered.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );
        progressDialog.setCancelable(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 1000);


        FirebaseFirestore.getInstance().collection(COLLECTION).document(order_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {


                FirebaseFirestore.getInstance().collection(COLLECTION).document(order_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if( documentSnapshot.get("delivery_person_name")!=null)
                        {
                            tv.setText("Name :" + documentSnapshot.get("delivery_person_name").toString());
                        }
                        if(documentSnapshot.get("delivery_person_phone")!=null)
                        {

                            tv1.setText("Phone :" + documentSnapshot.get("delivery_person_phone").toString());
                        }
                        if(documentSnapshot.get("delivery_person_email")!=null)
                        {

                            tv2.setText("Email: "+documentSnapshot.get("delivery_person_email").toString());
                        }
                        if(documentSnapshot.get("eatery_name")!=null)
                        {

                            tv3.setText("Eatery : " + documentSnapshot.get("eatery_name").toString());
                        }
                        if(documentSnapshot.getString("order_code")!=null)
                        {
                            tv6.setText("Code - "+documentSnapshot.getString("order_code"));
                        }
                    }
                });





                order = (Map<String, Double>) documentSnapshot.get("order");

               total = Float.parseFloat(String.format("%.3f",documentSnapshot.getDouble("total")));


                FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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

                        tv5.setText("" + order_id);

                        if(documentSnapshot.getBoolean("discount")!=null)
                            discount = documentSnapshot.getBoolean("discount");

                        float total1= 0;
                        for(String s:order.keySet())
                        {
                            iend = s.indexOf("$");
                            name = s.substring(0,iend);
                            price = Double.parseDouble(s.substring(iend+1));
                            count = (int)Double.parseDouble(order.get(s).toString());
                            total1 = total1 + (float)(price*count);
                            total_count=total_count+count;
                            item_listt.add(new Order_item_template(name, price, count, price*count));
                        }

                        int percentage_surge = (int)(((total-total1)*100)/(total1));

                        tv7.setText("NOTE: \n"+ percentage_surge +"% was added due to large distance between user and the eatery!\n");

                        if(discount)
                        {


                            total = total*0.8;
                            total = round(total,3);
                            String s0 = "\nDiscount Applied = 20% ";
                            String s1 =  "\nFINAL TOTAL = " + total; ;

                            tv7.append(s0);


                            final SpannableStringBuilder sb = new SpannableStringBuilder(s1);

                            final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
                            sb.setSpan(bss, 0, s1.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
                            sb.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE)
                                    ,   0
                                    ,  s1.length()
                                    , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tv7.append(sb);
                        }
                        else
                        {
                            tv7.append("\nNo discount available!");
                        }


                        total = round(total, 3);

                        item_listt.add(new Order_item_template("Total",total, total_count, total ));


                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Deliver_Ordered.this, android.R.layout.simple_list_item_1, list );
                        adapter = new OrderAdapter(Deliver_Ordered.this, R.layout.list_order_items, item_listt);
                        ls.setAdapter(adapter);
                        progressDialog.dismiss();
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
                                if(temp==false)
                                {
                                    Toast.makeText(Deliver_Ordered.this, "Order Delivered", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(Deliver_Ordered.this, SplashComplete.class);
                                    intent.putExtra("order_id", order_id);
                                    intent.putExtra("send_email", temp);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }

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
                                Intent intent = new Intent(Deliver_Ordered.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                startActivity(intent);
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
