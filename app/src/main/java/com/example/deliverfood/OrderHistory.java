package com.example.deliverfood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OrderHistory extends AppCompatActivity {

    FirebaseUser user;
    ProgressDialog progressDialog;
    private List<String> order_ids = new ArrayList<>();
    private List<String> order_ids1 = new ArrayList<>();

    ListView ls;

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
                                Intent intent = new Intent(OrderHistory.this, MainActivity.class);
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

    List<OrderHistoryTemplate> list = new ArrayList<>();
    OrderHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        SpannableString sp = new SpannableString("Order history");
//        sp.setSpan(new ForegroundColorSpan(Color.rgb(15,157,88)), 0, "Current Orders".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(sp);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(0,0,0,0)));
        progressDialog = new ProgressDialog(OrderHistory.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );
        progressDialog.setCancelable(false);

        ls = findViewById(R.id.listV);
        user = getIntent().getParcelableExtra("user");




        FirebaseFirestore.getInstance().collection("Current_Orders").whereEqualTo("user_email", user.getEmail()).whereEqualTo("order_delivered", true).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots!=null)
                {
                    for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots)
                    {
                            Timestamp tp = (com.google.firebase.Timestamp)documentSnapshot.get("order_time");

                            order_ids1.add(documentSnapshot.getId());
//                            order_ids.add("Total : " + documentSnapshot.get("total") + "\n \n" + "Time : " + tp.toDate().toString() + "\n" + "Order ID : "+documentSnapshot.getId() + "\n");
                            list.add(new OrderHistoryTemplate(documentSnapshot.getString("delivery_person_id"), documentSnapshot.getString("eatery_name"), documentSnapshot.getDouble("total"), documentSnapshot.getBoolean("confirm")));


                    }
                    progressDialog.dismiss();
                    adapter = new OrderHistoryAdapter(OrderHistory.this, R.layout.order_history_list_item, list);

//                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(OrderHistory.this, android.R.layout.simple_list_item_1, order_ids);
                    ls.setAdapter(adapter);

                    ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Intent intent = new Intent(OrderHistory.this, OrderDeliverFinished.class);
                            intent.putExtra("send_email", true);
                            intent.putExtra("order_id", order_ids1.get(i));
                            startActivity(intent);
                        }
                    });
                }

            }
        });


    }
}
