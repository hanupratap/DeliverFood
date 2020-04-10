package com.example.deliverfood;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDeliverFinished extends AppCompatActivity {
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



    double total=0;
    double total1 = 0;
    String message1;
    int total_count = 0;

    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_deliver_finished);

        tv = findViewById(R.id.textView48);
        tv1 = findViewById(R.id.textView50);
        tv2 = findViewById(R.id.textView53);
        ls = findViewById(R.id.listView5);
        tv3 = findViewById(R.id.textView49);
        tv4 = findViewById(R.id.textView35);

        tv5 = findViewById(R.id.textView52);
        tv6 = findViewById(R.id.textView40);
        tv7 = findViewById(R.id.textView55);

        String order_id = getIntent().getStringExtra("order_id");
        tv5.append(order_id);


        FirebaseFirestore.getInstance().collection("Current_Orders").document(order_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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

                order = (Map<String, Double>) documentSnapshot.get("order");
                if(documentSnapshot.getBoolean("discount")!=null)
                    discount = documentSnapshot.getBoolean("discount");
                List<String> list = new ArrayList<>();
                int iend;
                String name;
                int count;
                Double price;

                for(String s:order.keySet())
                {
                    iend = s.indexOf("$");
                    name = s.substring(0,iend);
                    price = Double.parseDouble(s.substring(iend+1));
                    count = (int)Double.parseDouble(order.get(s).toString());

                    item_listt.add(new Order_item_template(name, price, count, price*count));
                    total1 =  total1 + (float)(price*count);
                    total_count = total_count + count;
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


                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(OrderDeliverFinished.this, android.R.layout.simple_list_item_1, list );
                adapter = new OrderAdapter(OrderDeliverFinished.this, R.layout.list_order_items, item_listt);
                ls.setAdapter(adapter);


                String message = getIntent().getStringExtra("message");
                boolean send_email = getIntent().getExtras().getBoolean("send_email");

                if(!send_email)
                {
                    SendMail sm = new SendMail(OrderDeliverFinished.this, user.getEmail(), "Order Delivered", message);
                    sm.execute();
                }


            }
        });

    }
}
