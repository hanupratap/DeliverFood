package com.example.deliverfood;
import com.google.android.gms.auth.api.proxy.ProxyApi;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;


import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;




@IgnoreExtraProperties
public class CurrentOrder {




    public Map<String, Double> order;
    public GeoPoint location;
    public String user_id;
    public Double total;
    public String eatery_id;
    public String eatery_name;
    public Boolean confirm;
    public String user_name;
    public String user_email;
    public String user_phone;
    public Boolean order_delivered;
    public String order_code;
    public boolean email_sent_user;
    public boolean email_sent_delivery;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    public @ServerTimestamp Date order_time = null;

    public CurrentOrder(Map<String,Double> a,String user_id, Double total, String eatery_id, String eatery_name,GeoPoint gp, String user_name, String user_email, String phone)
    {
        this.eatery_name = eatery_name;
        this.location = gp;
        this.order = a;
        this.user_id = user_id;
        this.total = total;
        this.eatery_id = eatery_id;
        this.confirm=false;
        this.user_name = user_name;
        this.user_email = user_email;
        this.user_phone = phone;
        this.order_delivered = false;
        this.email_sent_user = false;
        this.email_sent_delivery = false;
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(100000);
        this.order_code = String.format("%05d", num);

    }






}
