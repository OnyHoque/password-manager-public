package com.example.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Settings extends AppCompatActivity {

    Button btn1, btn2, btn3, btn4, btn5;

    String Main_secret_key, userID;
    DBhandler db;
    boolean online = false;
    SharedPreferences pref;
    RequestQueue queue;
    Vibrator vibe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        Main_secret_key = intent.getStringExtra("Main_secret_key");
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;
        pref = getApplicationContext().getSharedPreferences("Password_Manager_Info", 0);
        online = pref.getBoolean("online", false);
        userID = pref.getString("username","");
        db = new DBhandler(this);
        btn1 = findViewById(R.id.btn_settings_del_off_bckup);
        btn2 = findViewById(R.id.btn_settings_del_on_bckup);
        btn3 = findViewById(R.id.btn_settings_del_on_acc);
        btn4 = findViewById(R.id.btn_settings_reset_app_data);
        btn5 = findViewById(R.id.btn_settings_en_dark_mode);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(5);
                method_delete_offline_database();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(5);
                if(online)
                    method_delete_online_database();
                else
                    Helper.MakeText(getApplicationContext(),"You can not delete online database in offline mode.");
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(5);
                method_delete_online_account();
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(5);
                Helper.MakeText(getApplicationContext(),"coming soon!");
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(5);
                Helper.MakeText(getApplicationContext(),"coming soon!");
            }
        });

    }

    void method_delete_offline_database(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try{
                            db.clearTable();
                            Helper.MakeText(getApplicationContext(),"Offline database deleted.");
                        }catch (Exception e){
                            Helper.MakeText(getApplicationContext(),"ERROR 0031: Could not delete offline database.");
                        }

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        Helper.MakeText(getApplicationContext(),"Offline database was not deleted.");
                        break;
                } }};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all offline accounts?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    void method_delete_online_database(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try{
                            String hashed_userID = AESUtils.String_to_SHA1(userID);
                            String url = Helper.pref_site+"/psw.php?fn=delete_user_data&data1="+hashed_userID;
                            getDatabaseResponse(url, "success");
                        }catch (Exception e){
                            Helper.MakeText(getApplicationContext(),"ERROR 0022: Online database was not deleted.");
                        }

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        Helper.MakeText(getApplicationContext(),"Online database was not deleted.");
                        break;
                } }};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This will delete all online stored credentials. Offline saved credentials will be still available. Are you sure you want to delete all saved credentials from online?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    void method_delete_online_account(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try{
                            String hashed_userID = AESUtils.String_to_SHA1(userID);
                            String url = Helper.pref_site+"/psw.php?fn=delete_user_account"
                                    +"&username="+userID
                                    +"&data1="+hashed_userID;
                            getDatabaseResponse(url, "success");
                        }catch (Exception e){
                            Helper.MakeText(getApplicationContext(),"Online database was not deleted.");
                        }

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        Helper.MakeText(getApplicationContext(),"Online database was not deleted.");
                        break;
                } }};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This will delete your user account and all stored data from online. You will still be able to use the app offline. Are you sure you want to delete your online account?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    void getDatabaseResponse(String url, final String key) {
        queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String val = response.getString(key);
                            if (val.equals("true")) {
                                Helper.MakeText(getApplicationContext(),"Operation executed.");
                            } else {
                                Helper.MakeText(getApplicationContext(),"ERROR 0023: Operation failed.");
                            }
                        } catch (JSONException e) {
                            Helper.MakeText(getApplicationContext(),"ERROR 0024: Server response error.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Helper.MakeText(getApplicationContext(),"ERROR 0025: A connection to the server could not be established.");
                    }
                });
        queue.add(request);
    }
}