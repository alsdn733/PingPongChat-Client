package com.example.minwoo.pingpongchat_client;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvPersonName;
        TextView tvPersonEmail;
        ImageView ivPersonPhoto;

        MyViewHolder(View view) {
            super(view);
            tvPersonName = view.findViewById(R.id.tvPersonName);
            tvPersonEmail = view.findViewById(R.id.tvPersonEmail);
            ivPersonPhoto = view.findViewById(R.id.ivPersonPhoto);
        }
    }

    private ArrayList<UserInfo> UserInfoArrayList;
    Context context;

    MyAdapter(Context context, ArrayList<UserInfo> UserInfoArrayList) {
        this.context = context;
        this.UserInfoArrayList = UserInfoArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.tvPersonName.setText(UserInfoArrayList.get(position).personName);
        myViewHolder.tvPersonEmail.setText(UserInfoArrayList.get(position).personEmail);
        Glide.with(context)
             .load(UserInfoArrayList.get(position).personPhotoUrl)
             .into(myViewHolder.ivPersonPhoto);
    }

    @Override
    public int getItemCount() {
        return UserInfoArrayList.size();
    }
}

