package com.example.deliverfood;

public class Order_item_template {
    String order_name;
    double order_price;
    int count;
    double sub_total;
    Order_item_template(String order_name, double order_price ,int count, double sub_total )
    {
        this.order_name = order_name;
        this.count = count;
        this.order_price = order_price;
        this.sub_total = sub_total;
    }
}
