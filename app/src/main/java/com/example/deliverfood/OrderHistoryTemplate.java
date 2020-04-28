package com.example.deliverfood;

public class OrderHistoryTemplate {

    String user_id;
    double total;
    String eatery;
    boolean confirm;
    OrderHistoryTemplate(String user_id ,String eatery, double total, boolean confirm)
    {

        this.confirm = confirm;
        this.user_id = user_id;
        this.total = total ;
        this.eatery = eatery;
    }
}
