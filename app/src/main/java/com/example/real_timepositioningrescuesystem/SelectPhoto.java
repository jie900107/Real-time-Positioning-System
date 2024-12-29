package com.example.real_timepositioningrescuesystem;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class SelectPhoto extends AppCompatActivity {

    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_photo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        listView = findViewById(R.id.listview);
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(R.drawable.androidstudiologo);
        arrayList.add(R.drawable.firebaselogo);
        arrayList.add(R.drawable.googlelogo);
        arrayList.add(R.drawable.androidlogo);
        for(Integer item : arrayList){
            Log.d("DEBUG",String.valueOf(item));
        }
        CustomAdapter customAdapter = new CustomAdapter(this,R.layout.custom,arrayList);
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SelectPhoto.this,Register.class);
                intent.putExtra("Photo",arrayList.get(position).toString());
                startActivity(intent);
            }
        });
    }
}