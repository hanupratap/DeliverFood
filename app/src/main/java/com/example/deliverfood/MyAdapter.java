package com.example.deliverfood;

import android.content.Context;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {



    List name, prices, images;
    Context context;

    private OnItemClickListener mListener;


    private int item_count = 0;

    public interface OnItemClickListener{

        void onValueChangeListener(int position, int item_count);
    }



    public void SetonValueChangeListener(OnItemClickListener listener)
    {
        mListener = listener;
    }


    public MyAdapter(Context ct, List name, List prices, List image_url)
    {
        this.name = name;
        this.prices = prices;
        this.context = ct;
        this.images = image_url;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.order_item_list_view,parent,false);
        return new MyViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.mytext1.setText(name.get(position).toString());
        holder.mytext2.setText("Price : ");
        holder.mytext2.append(String.valueOf(prices.get(position)));
        if(images.get(position)!=null)
        {
            Glide.with(context)
                    .load(Uri.parse(images.get(position).toString()))
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.imgView);
        }





    }

    @Override
    public int getItemCount() {
        return name.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView mytext1, mytext2;
        public ImageView imgView;
        public NumberPicker np;





        public MyViewHolder(@NonNull final View itemView, final OnItemClickListener listener) {
            super(itemView);
            mytext1 = itemView.findViewById(R.id.textView5);
            mytext2 = itemView.findViewById(R.id.textView6);
            np = itemView.findViewById(R.id.number_picker);
            imgView = itemView.findViewById(R.id.imageView);
            np.setMaxValue(25);
            np.setMinValue(0);

            np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    item_count = newVal;
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION)
                        listener.onValueChangeListener(position, item_count);

                }


            });


        }
    }

}
