package com.example.deliverfood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieImageAsset;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OrderConfirm extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Current_Orders");
    private DocumentReference documentReference;


    List<Order_item_template> itemlist = new ArrayList<>();

    private String EATERY_ID = "eatery_id";
    private String EATERY_NAME = "eatery_name";
    private String ORDER_TIME = "order_time";
    private String TOTAL = "total";

    private Location currentLocation;
    String order_id;
    private String TAG = "OrderConfirm";
    TextView tv, tv1, tv2, tv3, tv4;
    ListView ls;

    FirebaseUser user;

    boolean temp = true;


    OrderAdapter orderAdapter;

    private FusedLocationProviderClient fusedLocationProviderClient;




    LottieAnimationView lottieAnimationView;

    CoordinatorLayout coordinatorLayout;



    public void showSnackBar(String s)
    {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, s, Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);
        SpannableString sp = new SpannableString("Waiting for confirmation...");
        sp.setSpan(new ForegroundColorSpan(Color.rgb(0,132,255)), 0, "Waiting for confirmation...".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(sp);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(0,0,0,0)));

        tv = findViewById(R.id.textView5);
        tv1 = findViewById(R.id.textView15);
        tv2 = findViewById(R.id.textView13);
        tv3 = findViewById(R.id.textView14);
        tv4 = findViewById(R.id.textView16);
        ls = findViewById(R.id.listView22);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        lottieAnimationView = findViewById(R.id.lotte);
        getLocation();
        user = FirebaseAuth.getInstance().getCurrentUser();

        order_id = getIntent().getExtras().getString("order_id");
        tv.append(order_id);
        documentReference = collectionReference.document(order_id);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                tv1.setText(documentSnapshot.get(EATERY_NAME).toString());
                tv2.append(String.format("%.3f",documentSnapshot.getDouble(TOTAL)));

                Timestamp timestamp = (Timestamp) documentSnapshot.get(ORDER_TIME);
                tv3.append(timestamp.toDate().toString());
                Map<String, Double> map = (Map<String, Double>) documentSnapshot.get("order");
                String name;
                int count;
                double price;
                double sub_total = 0;
                int sub_count = 0;
                int iend;
                for(String s:map.keySet())
                {
                    iend = s.indexOf("$");
                    name = s.substring(0,iend);
                    price = Double.parseDouble(s.substring(iend+1));
                    count = (int)Double.parseDouble(map.get(s).toString());
                    sub_total = sub_total + price*count;
                    sub_count = sub_count + count;
                    itemlist.add(new Order_item_template(name,price, count, price*count));
                }

                itemlist.add(new Order_item_template("Total", sub_total, sub_count, sub_total));


                orderAdapter = new OrderAdapter(OrderConfirm.this, R.layout.list_order_items, itemlist);
                ls.setAdapter(orderAdapter);



            }
        });






        FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.getTimestamp("first_order_time")==null)
                {
                    HashMap<String, Object> timestampNow = new HashMap<>();
                    timestampNow.put("first_order_time", Timestamp.now());
                    FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).set(timestampNow, SetOptions.merge());
                }
                else
                {
                    FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Timestamp tt = documentSnapshot.getTimestamp("first_order_time");

                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");

                            Date t1 = tt.toDate();

                            Date t2 = Timestamp.now().toDate();

                            long diff = t2.getTime() - t1.getTime();

                            int daysBetween = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

                            int order_count = Integer.parseInt(documentSnapshot.get("order_count").toString());
                            Map a = new HashMap();
                            if(order_count>=3 && daysBetween<7)
                            {
                                a.put("discount", true);
                            }
                            else
                            {
                                a.put("discount", false);

                            }
                            FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).set(a, SetOptions.merge());

                        }
                    });
                }
            }
        });

        FirebaseFirestore.getInstance().collection("Current_Orders").document(order_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e!=null)
                {
                    Log.e(TAG, "OnEvent", e);
                    return;
                }
                if(documentSnapshot!=null)
                {

                    if(documentSnapshot.get("confirm").toString()=="true")
                    {
                        tv4.setText("Order Confirmed!");

                        lottieAnimationView.setAnimationFromUrl("https://assets2.lottiefiles.com/packages/lf20_KznChd.json");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                funcAct();

                            }
                        }, 1500);


                    }


                }
                else {
                    Log.e(TAG, "onEvent:NULL");
                }
            }
        });

    }

    public void funcAct()
    {
        if(temp)
        {
            Intent intent = new Intent(OrderConfirm.this, OrderConfirmSplash.class);
            intent.putExtra("order_id",order_id);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
            temp=false;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        return;
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
                                Intent intent = new Intent(OrderConfirm.this, MainActivity.class);
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
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        currentLocation = (Location) task.getResult();

                        if(currentLocation==null)
                        {
                            showSnackBar("Location Not Found");
                            return;
                        }

                        GeoPoint mypos = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                        final Map user_instance = new HashMap<>();

                        user_instance.put("location",mypos);
                        user_instance.put("initial_user_location", mypos);

                        FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).set(user_instance, SetOptions.merge());

                    }
                }
            });
        }
        catch(Exception e) {
            e.printStackTrace();

        }
    }


}
