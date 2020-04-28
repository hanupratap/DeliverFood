package com.example.deliverfood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryFinalDispActivity extends AppCompatActivity {

    String order_id;

    List<Order_item_template> item_listt = new ArrayList<>();

    OrderAdapter adapter;

    private TextView tv,tv1,tv2, tv3, tv4, tv5, tv7;
    Map<String, Double> order = new HashMap<>();
    ListView ls;
    boolean discount = false;
    double total;
    double total1 = 0;
    String message1;
    int total_count = 0;

    FirebaseUser user;
    private ProgressDialog progressDialog;


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
                                Intent intent = new Intent(DeliveryFinalDispActivity.this, MainActivity.class);
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_final_disp);
        SpannableString sp = new SpannableString("Order Delivered");
        sp.setSpan(new ForegroundColorSpan(Color.rgb(15,157,88)), 0, "Order Delivered".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(sp);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(0,0,0,0)));

        tv = findViewById(R.id.textView248);
        tv1 = findViewById(R.id.textView250);
        tv2 = findViewById(R.id.textView253);
        ls = findViewById(R.id.listView10);
        tv3 = findViewById(R.id.textView249);
        tv5 = findViewById(R.id.textView252);
        tv7 = findViewById(R.id.textView255);


        progressDialog = new ProgressDialog(DeliveryFinalDispActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );
        progressDialog.setCancelable(false);




        order_id = getIntent().getStringExtra("order_id");
        tv5.setText(""+order_id);

        user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore.getInstance().collection("Current_Orders").document(order_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                if( documentSnapshot.get("delivery_person_name")!=null)
                {
                    tv.setText("Name :" + documentSnapshot.get("user_name").toString());
                }
                if(documentSnapshot.get("delivery_person_phone")!=null)
                {

                    tv1.setText("Phone :" + documentSnapshot.get("user_phone").toString());
                }
                if(documentSnapshot.get("eatery_name")!=null)
                {

                    tv2.setText("Eatery: "+documentSnapshot.get("eatery_name").toString());
                }
                if(documentSnapshot.get("delivery_person_email")!=null)
                {

                    tv3.setText("Email :" + documentSnapshot.get("user_email").toString());
                }

                total = documentSnapshot.getDouble("total");

                order = (Map<String, Double>) documentSnapshot.get("order");
                if(documentSnapshot.getBoolean("discount")!=null)
                    discount = documentSnapshot.getBoolean("discount");
                List<String> list = new ArrayList<>();
                int iend;
                String name;
                int count;
                Double price;
                String fin;
                for(String s:order.keySet())
                {
                    iend = s.indexOf("$");
                    name = s.substring(0,iend);
                    price = Double.parseDouble(s.substring(iend+1));
                    count = (int)Double.parseDouble(order.get(s).toString());

                    fin =  name+"\n" +  getString(R.string.tab) +"Price : "+price+"\n"+  getString(R.string.tab) +"Count : " + count;

                    message1 = message1 + "\n " + fin;
                    item_listt.add(new Order_item_template(name, price, count, price*count));
                    total1 =  total1 + (float)(price*count);

                    total_count = total_count + count;
                }

                int percentage_surge = (int)(((total-total1)*100)/(total1));

                tv7.setText("NOTE: \n"+ percentage_surge +"% was added due to large distance between user and the eatery!\n");

                if(discount)
                {



                    total = total*0.8;

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

                item_listt.add(new Order_item_template("Final Total",total, total_count, total ));

                adapter = new OrderAdapter(DeliveryFinalDispActivity.this, R.layout.list_order_items, item_listt);
                ls.setAdapter(adapter);
                progressDialog.dismiss();

            }
        });

    }
}
