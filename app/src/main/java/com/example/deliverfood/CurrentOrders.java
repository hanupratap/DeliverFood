package com.example.deliverfood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CurrentOrders extends AppCompatActivity {

    ArrayAdapter<String> adapter;
    ListView ls;
    List<String> order_ids = new ArrayList<>();
    boolean temp = false;

    List<OrderHistoryTemplate> list = new ArrayList<>();
    OrderHistoryAdapter adapter1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_orders);
        ls = findViewById(R.id.current_order_listView);

        SpannableString sp = new SpannableString("Current Orders");
        getSupportActionBar().setTitle(sp);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(0,0,0,0)));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        FirebaseFirestore.getInstance().collection("Current_Orders").whereEqualTo("order_delivered", false).whereEqualTo("user_email", FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots)
                {

//                    adapter.add("\nOrderID : "+ documentSnapshot.getId() + "\n" + "Total : "+ documentSnapshot.get("total").toString() + "\nEatery : "+ documentSnapshot.getString("eatery_name") + "\n");
                    order_ids.add(documentSnapshot.getId());
                    list.add(new OrderHistoryTemplate(documentSnapshot.getString("user_id"), documentSnapshot.getString("eatery_name"), documentSnapshot.getDouble("total"), documentSnapshot.getBoolean("confirm")));

                }
                adapter1 = new OrderHistoryAdapter(CurrentOrders.this, R.layout.order_history_list_item, list);

                ls.setAdapter(adapter1);
                ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        FirebaseFirestore.getInstance().collection("Current_Orders").document(order_ids.get(position)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.getBoolean("confirm")==false)
                                {


                                        Intent intent = new Intent(CurrentOrders.this, OrderConfirm.class);
                                        intent.putExtra("user", FirebaseAuth.getInstance().getCurrentUser());
                                        intent.putExtra("order_id", documentSnapshot.getId());
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(intent);


                                }
                                else
                                {


                                        Intent intent = new Intent(CurrentOrders.this, Deliver_Ordered.class);
                                        intent.putExtra("user",  FirebaseAuth.getInstance().getCurrentUser());
                                        intent.putExtra("order_id", documentSnapshot.getId());
                                        intent.putExtra("uid",  FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(intent);

                                }
                            }
                        });

                    }
                });
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
                                Intent intent = new Intent(CurrentOrders.this, MainActivity.class);
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
