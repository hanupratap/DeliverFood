package com.example.deliverfood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class OrderConfirmSplash extends AppCompatActivity {

    @Override
    public void onBackPressed() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm_splash);
        getSupportActionBar().hide();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(OrderConfirmSplash.this, Deliver_Ordered.class);
                intent.putExtra("order_id",getIntent().getStringExtra("order_id"));
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}
