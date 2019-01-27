package com.example.minwoo.pingpongchat_client;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvPersonName;
        TextView tvPersonEmail;

        MyViewHolder(View view){
            super(view);
            tvPersonName = view.findViewById(R.id.iv_picture);
            tvPersonEmail = view.findViewById(R.id.tv_price);
        }
    }

    private ArrayList<UserInfo> foodInfoArrayList;
    MyAdapter(ArrayList<UserInfo> foodInfoArrayList){
        this.foodInfoArrayList = foodInfoArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        MyViewHolder myViewHolder = (MyViewHolder) holder;

        myViewHolder.ivPicture.setImageResource(foodInfoArrayList.get(position).drawableId);
        myViewHolder.tvPrice.setText(foodInfoArrayList.get(position).price);
    }

    @Override
    public int getItemCount() {
        return foodInfoArrayList.size();
    }
}

