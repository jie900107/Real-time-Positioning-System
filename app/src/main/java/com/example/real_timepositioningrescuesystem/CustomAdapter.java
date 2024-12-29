package com.example.real_timepositioningrescuesystem;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<Integer> {
    private Context context;
    private int resource;
    private ArrayList<Integer> list;
    public CustomAdapter(@NonNull Context context, int resource, ArrayList<Integer> list) {
        super(context, resource, list);
        this.context=context;
        this.resource=resource;
        this.list=list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(resource,parent,false);
        ImageView iv = convertView.findViewById(R.id.iv);
        iv.setImageResource(list.get(position));
        return convertView;
    }
}
