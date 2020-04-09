package com.example.deliverfood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_orders);
        ls = findViewById(R.id.current_order_listView);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        FirebaseFirestore.getInstance().collection("Current_Orders").whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid()).whereEqualTo("order_delivered", false)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots)
                {

                    adapter.add("\nOrderID : "+ documentSnapshot.getId() + "\n" + "Total : "+ documentSnapshot.get("total").toString() + "\nEatery : "+ documentSnapshot.getString("eatery_name") + "\n");
                    order_ids.add(documentSnapshot.getId());
                }
                ls.setAdapter(adapter);
            }
        });

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
                            startActivity(intent);
                            finish();

                        }
                        else
                        {
                            Intent intent = new Intent(CurrentOrders.this, Deliver_Ordered.class);
                            intent.putExtra("user",  FirebaseAuth.getInstance().getCurrentUser());
                            intent.putExtra("order_id", documentSnapshot.getId());
                            intent.putExtra("uid",  FirebaseAuth.getInstance().getCurrentUser().getUid());
                            startActivity(intent);
                            finish();

                        }
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


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.logout:
            {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
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
