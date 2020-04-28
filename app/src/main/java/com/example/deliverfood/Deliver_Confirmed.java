package com.example.deliverfood;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.location.LocationListener;
import android.location.LocationManager;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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

import io.opencensus.trace.Span;

public class Deliver_Confirmed extends AppCompatActivity implements ExampleDialog.ExampleDialogListener {

    private String USER_LIST = "Users", id;
    TextView tv,tv1,tv2,tv3,tv4, tv5, tv6;
    String order_id;
    FirebaseUser user;
    ListView ls;
    Button btn, confirm_btn;

    Map user_instance = new HashMap<>();
    Location currentLocation;

    FusedLocationProviderClient fusedLocationProviderClient;

    List<Order_item_template> item_list = new ArrayList<>();
    OrderAdapter adapter;

    Map<String, Double> order = new HashMap<>();
    String uid="default";

    private String COLLECTION = "Current_Orders";
    GeoPoint mypos, gp, gp1;

    boolean discount = false;

    List<String> item_listt = new ArrayList<>();

    String user_name, user_email;
    double total;
    int total_count = 0;

    CoordinatorLayout linear;
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
        setContentView(R.layout.activity_deliver__confirmed);
        SpannableString sp = new SpannableString("Order Confirmed");
        sp.setSpan(new ForegroundColorSpan(Color.rgb(15,157,88)), 0, "Order Confirmed".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(sp);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(0,0,0,0)));
        linear = findViewById(R.id.linearLayout);
        user = getIntent().getParcelableExtra("user");
        order_id = getIntent().getExtras().getString("order_id");
        id = user.getUid();
        tv = findViewById(R.id.textView23);
        tv1 = findViewById(R.id.textView26);
        tv2 = findViewById(R.id.textView31);
        tv3 = findViewById(R.id.textView30);
        tv4 = findViewById(R.id.textView45);
        tv5 = findViewById(R.id.textView38);
        tv6 = findViewById(R.id.note);
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

        progressDialog = new ProgressDialog(Deliver_Confirmed.this);
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


        tv.setText(order_id);

        Map m1 = new HashMap();
        m1.put("delivery_person_id", user.getUid());

        FirebaseFirestore.getInstance().collection(COLLECTION).document(order_id).set(m1, SetOptions.merge());

        FirebaseFirestore.getInstance().collection(COLLECTION).document(order_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {



                tv1.setText("Ordered by : "+documentSnapshot.get("user_name").toString());
                tv2.setText("User Email : "+documentSnapshot.get("user_email").toString());
                tv3.setText("Eatery : "+documentSnapshot.get("eatery_name").toString());
                tv5.setText("User Phone : "+documentSnapshot.get("user_phone").toString());
                if(documentSnapshot.get("user_id")!=null)
                {
                    uid = documentSnapshot.get("user_id").toString();
                }

                order = (Map<String, Double>) documentSnapshot.get("order");


                total = Float.parseFloat(String.format("%.3f",documentSnapshot.getDouble("total")));

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
                                tv4.setText("Address : " + address);

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

                        float total1=0;
                        for(String s:order.keySet())
                        {
                            iend = s.indexOf("$");
                            name = s.substring(0,iend);
                            price = Double.parseDouble(s.substring(iend+1));
                            count = (int)Double.parseDouble(order.get(s).toString());

                            item_listt.add(name+"\n" +  getString(R.string.tab) +"Price : "+price+"\n"+  getString(R.string.tab) +"Count : " + count);
                            total1 =  total1 + (float)(price*count);
                            item_list.add(new Order_item_template(name, price, count, price*count));
                            total_count = total_count + count;
                        }

                        int percentage_surge = (int)(((total-total1)*100)/(total1));

                        tv6.setText("NOTE: \n"+ percentage_surge +"% was added due to large distance between user and the eatery!");

                        total = round(total,3);

                        if(discount)
                        {
                            total = total*0.8;
                            total = round(total,3);
                            String s0 = "\nDiscount Applied = 20% ";
                            String s1 =  "\nFINAL TOTAL = " + total; ;

                            tv6.append(s0);


                            final SpannableStringBuilder sb = new SpannableStringBuilder(s1);

                            final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
                            sb.setSpan(bss, 0, s1.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
                            sb.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE)
                                    ,   0
                                    ,  s1.length()
                                    , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tv6.append(sb);


                        }
                        else
                        {
                            tv6.append("\nNo discount available!");
                        }

                        item_list.add(new Order_item_template("Final Total",total, total_count, total ));
                         adapter = new OrderAdapter(Deliver_Confirmed.this, R.layout.list_order_items, item_list);
                        ls.setAdapter(adapter);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(progressDialog.isShowing() && progressDialog!=null)
                                    progressDialog.dismiss();
                            }
                        }, 2000);



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
                                Intent intent = new Intent(Deliver_Confirmed.this, MainActivity.class);
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
            final Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @SuppressLint("MissingPermission")
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
                                    Snackbar.make(linear, "Enable Location Services!", Snackbar.LENGTH_INDEFINITE)
                                            .setAction("Close", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                }
                                            })
                                            .setActionTextColor(getResources().getColor(R.color.colorAccent)).show();                                }
                            };

                            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                            locationManager.requestLocationUpdates("gps", 5000,0, locationListener);
                            Snackbar.make(linear, "Enable Location Services!", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Close", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    })
                                    .setActionTextColor(getResources().getColor(R.color.colorAccent)).show();





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
    public void onBackPressed() {
        super.onBackPressed();
        Deliver_Confirmed.this.finish();
        Deliver_Confirmed.this.startActivity(new Intent(Deliver_Confirmed.this, DeliverOrOrder.class));
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

                    Toast.makeText(Deliver_Confirmed.this, "Order delivered successfully!", Toast.LENGTH_SHORT).show();

                    String message, message_user;
                    message_user ="Dear "+user_name + ","+"\nYou order has been delivered by " + user.getDisplayName()
                            + " ( " + user.getEmail() + " )"+ "\nOrderID: " +   order_id + "\n Order Detail-";

                    message = "Dear "+user.getDisplayName() + ","+"\nYou have delivered the order of " + user_name
                            + " ( " + user_email + " )"+ "\nOrderID: " +   order_id + "\n Order Detail-";
                    for(String s1:item_listt)
                    {
                        message = message + "\n" + s1;
                        message_user = message_user + "\n" + s1 ;
                    }
                    message = message + "\n Final Total(After adjustments) :" + String.valueOf(total);
                    message_user = message_user + "\n Final Total(After adjustments) :" + String.valueOf(total);

                    message = message + "\nRegards, \nFoodDeliver";
                    message_user = message_user + "\nRegards, \nFoodDeliver";

                    user_email = documentSnapshot.getString("user_email");

                        SendMail sm = new SendMail(Deliver_Confirmed.this, user.getEmail(), "Order Delivered", message);
                    sm.execute();

                            SendMail sm1= new SendMail(Deliver_Confirmed.this, user_email, "Order Delivered", message_user);
                            sm1.execute();

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                else
                {

                    Snackbar.make(linear, "Wrong Code, Try again!", Snackbar.LENGTH_INDEFINITE)
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
}
