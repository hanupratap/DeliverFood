package com.example.deliverfood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashComplete extends AppCompatActivity {
    private  static int SPLASH_TIME_OUT = 3000;
    @Override
    public void onBackPressed() {}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_complete);
        getSupportActionBar().hide();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashComplete.this, OrderDeliverFinished.class);

                intent.putExtra("order_id",getIntent().getStringExtra("order_id"));
                intent.putExtra("send_email",getIntent().getStringExtra("send_email"));
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
