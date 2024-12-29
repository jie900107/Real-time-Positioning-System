package com.example.real_timepositioningrescuesystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Register extends AppCompatActivity implements View.OnClickListener {
    public EditText email,pass,confirmpass;
    public FirebaseAuth auth;
    public FirebaseFirestore db;
    public Button register;
    public String avatar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        confirmpass = findViewById(R.id.cofirmpassword);
        register = findViewById(R.id.reg);
        register.setOnClickListener(this);
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        if(intent.getExtras() != null){
            avatar = intent.getStringExtra("Photo");
            Log.d("DEBUG",avatar);
        }
    }

    @Override
    public void onClick(View v) {
        if(!pass.getText().toString().equals(confirmpass.getText().toString())){
            Toast.makeText(this,"Passwords do not match.",Toast.LENGTH_SHORT).show();
        }
        else{
            auth.createUserWithEmailAndPassword(email.getText().toString(),pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(Register.this,"Successfully Registered.",Toast.LENGTH_SHORT).show();
                        User user = new User();
                        user.setAvatar(avatar);
                        user.setEmail(email.getText().toString());
                        user.setUsername(email.getText().toString().substring(0,email.getText().toString().indexOf("@")));
                        db.collection("User").document(FirebaseAuth.getInstance().getUid()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.d("DEBUG","Data have added to firestore.    " + user.toString() + "    " + FirebaseAuth.getInstance().getUid());
                                    Intent intent = new Intent(Register.this,MainActivity.class);
                                    startActivity(intent);
                                }
                                else
                                    Log.d("DEBUG","Update failed.");
                            }
                        });

                    }
                    else
                        Toast.makeText(Register.this,"Register failed.",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}