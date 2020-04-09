package com.example.deliverfood;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;


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
    private TextView tv;
    private Button btn;
    private CollectionReference collectionReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    private String SUB_COLLECTION_NAME = "Orders";
    private String ITEM_NAME = "name";
    private String ITEM_PRICE = "price";
    private String MAIN_COLLECTION = "Eateries";




    private List dishes = new ArrayList();
    private List prices = new ArrayList();
    private RecyclerView recyclerView;


    private String EATERY_NAME="";
    private String EATERY_ID="";
    private double EATERY_LOCATION_LATITIUDE=0;
    private double EATERY_LOCATION_LONGITUDE=0;

    private HashMap<String, Data> order = new HashMap<String, Data>();



    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eatery);
        tv = findViewById(R.id.textView3);
        btn = findViewById(R.id.button4);
        recyclerView = findViewById(R.id.recyclerView);



        progressDialog = ProgressDialog.show(Eatery.this, "",
                "Loading. Please wait...", true);

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
                finish();
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
                        EATERY_LOCATION_LATITIUDE = document.getGeoPoint("location").getLatitude();
                        EATERY_LOCATION_LONGITUDE = document.getGeoPoint("location").getLongitude();
                        EATERY_ID = document.getId();
                        tv.setText(document.getString("name"));



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


                 }
                 MyAdapter myAdapter = new MyAdapter(Eatery.this, dishes,  prices);
                 recyclerView.setAdapter(myAdapter);
                 recyclerView.setLayoutManager(new LinearLayoutManager(Eatery.this));
                 progressDialog.dismiss();

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

//                                 data = order.get(dishes.get(position).toString());
//                                 Toast.makeText(Eatery.this, data.name + "- changed to " + data.items + "items", Toast.LENGTH_SHORT).show();
                             }
                             else
                             {
                                 Data data = new Data(dishes.get(position).toString(), Double.parseDouble( prices.get(position).toString()) , item_count);
                                 order.put(dishes.get(position).toString(), data);
                                 if(!btn.isEnabled())
                                 {
                                     btn.setEnabled(true);
                                 }

//                                 Toast.makeText(Eatery.this, order.get(dishes.get(position).toString()).name + "- added " + item_count + " items", Toast.LENGTH_SHORT).show();

                             }


                         }
                     });

                 }
                 catch (Exception e)
                 {
//                     Toast.makeText(Eatery.this, "Set any value!", Toast.LENGTH_SHORT).show();
                 }




             }
         }).addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
//                 Toast.makeText(Eatery.this, "Servers not responding!", Toast.LENGTH_SHORT).show();
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
}