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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class Online extends AppCompatActivity {

    String Main_secret_key, userID;
    Button btn1, btn2, btn3;
    RequestQueue queue;
    boolean online = false;
    SharedPreferences pref;
    DBhandler db;
    Vibrator vibe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        Intent intent = getIntent();
        Main_secret_key = intent.getStringExtra("Main_secret_key");
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;
        pref = getApplicationContext().getSharedPreferences("Password_Manager_Info", 0);
        online = pref.getBoolean("online", false);
        userID = pref.getString("username","");
        db = new DBhandler(this);

        btn1 = findViewById(R.id.btn_online_1);
        btn2 = findViewById(R.id.btn_online_2);
        btn3 = findViewById(R.id.btn_online_3);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(5);
                if(online)
                    method_delete_online_account();
                else
                    Helper.MakeText(getApplicationContext(),"You can not delete online database in offline mode.");
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
                try{
                    String hashed_userID = AESUtils.String_to_SHA1(userID);
//                    uploadDataOnline(hashed_userID);
                    Helper.MakeText(getApplicationContext(),"coming Soon!");
                }catch (Exception e){
                    Helper.MakeText(getApplicationContext(),"ERROR 00: JSON exception");
                }
            }
        });
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

//    void uploadDataOnline(String hashed_userID) throws JSONException {
//        LinkedList<user_account> acc_list = db.getData_raw();
//        JSONArray list = new JSONArray();
//        for(int i = 0; i < acc_list.size(); ++i){
//            user_account ua = acc_list.get(i);
//            JSONObject jsonObj = new JSONObject();
//            jsonObj.put("account_name", ua.getAccount_name());
//            jsonObj.put("username", ua.getUsername());
//            jsonObj.put("password", ua.getPassword());
//            list.put(jsonObj);
//        }
//        JSONObject data_payload = new JSONObject();
//        data_payload.put("array",list);
//        data_payload.put("userid",hashed_userID);
//        String url = "";
//        RequestQueue queue = Volley.newRequestQueue(this);
//        JsonObjectRequest jobReq = new JsonObjectRequest(Request.Method.POST, url, data_payload,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject jsonObject) {
//
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError volleyError) {
//
//                    }
//                });
//
//        queue.add(jobReq);
//    }

}