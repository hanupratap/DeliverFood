package com.example.deliverfood;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryAdapter extends ArrayAdapter {

    Context mcontext;
    List<OrderHistoryTemplate> list;
    int mResouce;


    public OrderHistoryAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        mcontext = context;
        this.list = objects;
        mResouce = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        String user_id = list.get(position).user_id;
        double total = list.get(position).total;
        String eatery = list.get(position).eatery;
        boolean confirm = list.get(position).confirm;

        LayoutInflater inflater = LayoutInflater.from(mcontext);


        convertView = inflater.inflate(mResouce, parent, false);

        final TextView user_name = convertView.findViewById(R.id.textView5111);
        TextView eatery_name = convertView.findViewById(R.id.textView6111);
        TextView total1 = convertView.findViewById(R.id.textView1165);
        final ImageView prof_pic = convertView.findViewById(R.id.imageView111);
        final LottieAnimationView anim = convertView.findViewById(R.id.card_anim);

        if(confirm==true)
        {
            anim.setAnimation("tick.json");
            anim.setRepeatCount(0);


        }
        else
        {
            anim.setAnimation("wait.json");


        }




        eatery_name.setText("Eatery : " + eatery);
        total1.setText("Total : " + total);

        FirebaseFirestore.getInstance().collection("Users").document(user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Glide.with(mcontext)
                        .load(documentSnapshot.getString("profile_img"))
                        .apply(RequestOptions.circleCropTransform())
                        .override(200, 300)
                        .into(prof_pic);


                    user_name.setText(documentSnapshot.getString("user_name"));


            }
        });







        return convertView;

    }
}
