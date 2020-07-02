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
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Edit_account extends AppCompatActivity {

    String Main_secret_key;
    String old_account_name, old_username, old_password;
    EditText input_accountName, input_username, input_password;
    Button btn_Update;
    DBhandler db;
    SharedPreferences pref;
    boolean online;
    String userID;
    Vibrator vibe;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);
        Intent intent = getIntent();
        db = new DBhandler(getApplicationContext());
        Main_secret_key = intent.getStringExtra("Main_secret_key");
        old_account_name = intent.getStringExtra("account_name");
        old_username = intent.getStringExtra("username");
        old_password = intent.getStringExtra("password");
        pref = getApplicationContext().getSharedPreferences("Password_Manager_Info", 0);
        online = pref.getBoolean("online", false);
        userID = pref.getString("username","");
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;
        input_accountName = findViewById(R.id.input_editAccount_accountName);
        input_username = findViewById(R.id.input_editAccount_username);
        input_password = findViewById(R.id.input_editAccount_password);
        btn_Update = findViewById(R.id.btn_editAccount_update);
        input_accountName.setText(old_account_name);
        input_username.setText(old_username);
        input_password.setText(old_password);

        btn_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(5);
                startUpdate();
            }
        });
    }

    void test(){
        try {
            String new_account_name = input_accountName.getText().toString();
            String new_username = input_username.getText().toString();
            String new_password = input_password.getText().toString();
            final String data2 = AESUtils.encrypt(old_account_name, Main_secret_key);
            final String data3 = AESUtils.encrypt(old_username, Main_secret_key);
            final String data4 = AESUtils.encrypt(old_password, Main_secret_key);
            final String data5 = AESUtils.encrypt(new_account_name, Main_secret_key);
            final String data6 = AESUtils.encrypt(new_username, Main_secret_key);
            final String data7 = AESUtils.encrypt(new_password, Main_secret_key);
            db.updateRow(data2, data3, data4, data5, data6, data7);
        }catch (Exception e){
            Helper.MakeText(getApplicationContext(),"ERROR 0001: Could not update data.");
        }
    }

    void startUpdate(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try{
                            String new_account_name = input_accountName.getText().toString();
                            String new_username = input_username.getText().toString();
                            String new_password = input_password.getText().toString();
                            final String data1 = AESUtils.String_to_SHA1(userID);
                            final String data2 = AESUtils.encrypt(old_account_name, Main_secret_key);
                            final String data3 = AESUtils.encrypt(old_username, Main_secret_key);
                            final String data4 = AESUtils.encrypt(old_password, Main_secret_key);
                            final String data5 = AESUtils.encrypt(new_account_name, Main_secret_key);
                            final String data6 = AESUtils.encrypt(new_username, Main_secret_key);
                            final String data7 = AESUtils.encrypt(new_password, Main_secret_key);
                            if(online) {
                                updateOnline(data1, data2, data3, data4, data5, data6, data7);
                            }
                            db.updateRow(data2, data3, data4, data5, data6, data7);
                        }catch (Exception e){
                            Helper.MakeText(getApplicationContext(),"ERROR 0002: Could not update online information.");
                        }

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        Helper.MakeText(getApplicationContext(),"Account was Not updated.");
                        break;
                }
            }};
        AlertDialog.Builder builder = new AlertDialog.Builder(Edit_account.this);
        builder.setMessage("Are you sure you want to update the account?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    void updateOnline(String data1, String data2, String data3, String data4, String data5, String data6, String data7){
        String url = Helper.pref_site+"/psw.php?fn=edit_account&data1="+data1
                +"&data2="+data2
                +"&data3="+data3
                +"&data4="+data4
                +"&data5="+data5
                +"&data6="+data6
                +"&data7="+data7;
        getJSON(url, "success");
    }

    void getJSON(String url, final String key) {
        queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String val = response.getString(key);
                            if (val.equals("true")) {
                                Helper.MakeText(getApplicationContext(),"account information updated.");
                            } else {
                                Helper.MakeText(getApplicationContext(),"Could not update online account.");
                            }
                        } catch (JSONException e) {
                            Helper.MakeText(getApplicationContext(),"ERROR 0003: Server response error.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Helper.MakeText(getApplicationContext(),"ERROR 0004: A connection to the server could not be established.");
                    }
                });
        queue.add(request);
    }

}