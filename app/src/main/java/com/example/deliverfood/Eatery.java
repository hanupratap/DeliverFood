package com.example.deliverfood;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;


import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;


public class Eatery extends AppCompatActivity implements Serializable {

    private Button btn;
    private CollectionReference collectionReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    private String SUB_COLLECTION_NAME = "Orders";
    private String ITEM_NAME = "name";
    private String ITEM_PRICE = "price";
    private String MAIN_COLLECTION = "Eateries";




    private List dishes = new ArrayList();
    private List prices = new ArrayList();
    private List image_urls = new ArrayList();

    private RecyclerView recyclerView;


    private String EATERY_NAME="";
    private String EATERY_ID="";
    private double EATERY_LOCATION_LATITIUDE=0;
    private double EATERY_LOCATION_LONGITUDE=0;

    private HashMap<String, Data> order = new HashMap<>();



    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(Eatery.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );
        progressDialog.setCancelable(false);

        setContentView(R.layout.activity_eatery);
        btn = findViewById(R.id.button4);
        recyclerView = findViewById(R.id.recyclerView);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 2000);

        Paper.init(Eatery.this);

        if(order.size()==0)
        btn.setEnabled(false);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(String dat: order.keySet())
                {
                    Paper.book().write(dat, order.get(dat));
                }

                Intent intent = new Intent(Eatery.this, order_details.class);
                intent.putExtra("eatery_location_latitude",  EATERY_LOCATION_LATITIUDE);
                intent.putExtra("eatery_location_longitude",  EATERY_LOCATION_LONGITUDE);
                intent.putExtra("eatery_name", EATERY_NAME);
                intent.putExtra("eatery_id", EATERY_ID);
                intent.putExtra("user",getIntent().getParcelableExtra("user"));

                startActivity(intent) ;
            }
        });



        final String pos = getIntent().getExtras().getString("marker");
        DocumentReference dr = db.collection(MAIN_COLLECTION).document(pos);
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {

                        EATERY_NAME = document.getString("name");
                        SpannableString sp = new SpannableString(EATERY_NAME);
                        sp.setSpan(new ForegroundColorSpan(Color.rgb(0,132,255)), 0, EATERY_NAME.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        getSupportActionBar().setTitle(sp);
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(0,0,0,0)));

                        EATERY_LOCATION_LATITIUDE = document.getGeoPoint("location").getLatitude();
                        EATERY_LOCATION_LONGITUDE = document.getGeoPoint("location").getLongitude();
                        EATERY_ID = document.getId();

                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });

        collectionReference = dr.collection(SUB_COLLECTION_NAME);

         collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
             @Override
             public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                 for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots)
                 {
                     dishes.add(documentSnapshot.get(ITEM_NAME));

                     prices.add(Double.parseDouble(documentSnapshot.get(ITEM_PRICE).toString()));

                     image_urls.add(documentSnapshot.getString("image_url"));
                 }
                 MyAdapter myAdapter = new MyAdapter(Eatery.this, dishes,  prices, image_urls);
                 recyclerView.setAdapter(myAdapter);

                 recyclerView.setLayoutManager(new LinearLayoutManager(Eatery.this));

                 try {
                     myAdapter.SetonValueChangeListener(new MyAdapter.OnItemClickListener() {


                         @RequiresApi(api = Build.VERSION_CODES.N)
                         @Override
                         public void onValueChangeListener(int position, int item_count) {
                             if(item_count==0)
                             {
                                 order.remove(dishes.get(position).toString());
                                 if(btn.isEnabled())
                                 {
                                     btn.setEnabled(false);
                                 }
                             }
                             else if(order.containsKey(dishes.get(position).toString()))
                             {

                                 Data data = new Data(dishes.get(position).toString(), Double.parseDouble( prices.get(position).toString()) , item_count);
                                 order.replace(dishes.get(position).toString(), data);
                                 if(!btn.isEnabled())
                                 {
                                     btn.setEnabled(true);
                                 }
                             }
                             else
                             {
                                 Data data = new Data(dishes.get(position).toString(), Double.parseDouble( prices.get(position).toString()) , item_count);
                                 order.put(dishes.get(position).toString(), data);
                                 if(!btn.isEnabled())
                                 {
                                     btn.setEnabled(true);
                                 }
                             }
                         }
                     });

                 }
                 catch (Exception e)
                 {
                     Log.d("Eatery Activity - ", e.toString());
                 }
             }
         }).addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 Toast.makeText(Eatery.this, "Sorry! We are having some problem!", Toast.LENGTH_SHORT).show();
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
                                Intent intent = new Intent(Eatery.this, MainActivity.class);
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
}
