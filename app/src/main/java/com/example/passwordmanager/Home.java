package com.example.passwordmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Home extends AppCompatActivity {

    ImageView img1, img2, img3, img4, img5, img6;
    String Main_secret_key;
    Vibrator vibe;
    FloatingActionButton add_new;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        Main_secret_key = intent.getStringExtra("Main_secret_key");
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;

        img1 = findViewById(R.id.img_dummy1);
        img2 = findViewById(R.id.img_dummy2);
        img3 = findViewById(R.id.img_dummy3);
        img4 = findViewById(R.id.img_dummy4);
        img5 = findViewById(R.id.img_dummy5);
        img6 = findViewById(R.id.img_dummy6);
        add_new = findViewById(R.id.floatingActionButton);

        add_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(5);
                Intent GoTo = new Intent(getApplicationContext(), Save.class);
                GoTo.putExtra("Main_secret_key", Main_secret_key);
                startActivity(GoTo);
            }
        });

        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(5);
                Intent GoTo = new Intent(getApplicationContext(), Save.class);
                GoTo.putExtra("Main_secret_key", Main_secret_key);
                startActivity(GoTo);
            }
        });

        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(5);
                Intent GoTo = new Intent(getApplicationContext(), GeneratePassword.class);
                GoTo.putExtra("Main_secret_key", Main_secret_key);
                startActivity(GoTo);
            }
        });

        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(5);
                Intent GoTo = new Intent(getApplicationContext(), View_Passwords.class);
                GoTo.putExtra("Main_secret_key", Main_secret_key);
                startActivity(GoTo);
            }
        });

        img4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(5);
                Intent GoTo = new Intent(getApplicationContext(), Offline.class);
                GoTo.putExtra("Main_secret_key", Main_secret_key);
                startActivity(GoTo);
            }
        });

        img5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(5);
                Intent GoTo = new Intent(getApplicationContext(), Settings.class);
                GoTo.putExtra("Main_secret_key", Main_secret_key);
                startActivity(GoTo);
            }
        });

        img6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(5);
                Helper.MakeText(getApplicationContext(),"Coming Soon!");
            }
        });


    }
}