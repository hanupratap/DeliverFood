package com.example.deliverfood;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends ArrayAdapter {

    Context mcontext;
    List<Order_item_template> list;
    int mResouce;

    public OrderAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        mcontext = context;
        this.list = objects;
        mResouce = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String name = list.get(position).order_name;
        double price = list.get(position).order_price;
        int count = list.get(position).count;
        double sub_total = list.get(position).sub_total;

        Order_item_template item = new Order_item_template(name,price, count, sub_total);


        LayoutInflater inflater = LayoutInflater.from(mcontext);


        convertView = inflater.inflate(mResouce, parent, false);

        TextView order_item_namet = convertView.findViewById(R.id.order_item_name);
        TextView order_item_subtotalt = convertView.findViewById(R.id.order_item_subtotal);
        TextView order_item_countt = convertView.findViewById(R.id.order_item_count);
        TextView order_item_pricet = convertView.findViewById(R.id.order_item_price);

        order_item_countt.setText("Count : " + count);
        order_item_namet.setText(name);
        order_item_pricet.setText("Price : " +String.valueOf(price));
        order_item_subtotalt.setText("SubTotal : " +String.valueOf(sub_total));




        return convertView;

    }
}
