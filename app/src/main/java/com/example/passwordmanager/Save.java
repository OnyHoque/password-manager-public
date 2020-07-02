package com.example.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Save extends AppCompatActivity {

    String Main_secret_key;
    EditText input_username, input_password, input_accountName;
    Button btn_save;
    RequestQueue queue;
    boolean online = false;
    SharedPreferences pref;
    DBhandler db;
    TextView text_view, show_data;
    Vibrator vibe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        Intent intent = getIntent();
        Main_secret_key = intent.getStringExtra("Main_secret_key");
        String generated_password = intent.getStringExtra("Generated_password");
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;
        input_username = findViewById(R.id.input_save_username);
        input_password = findViewById(R.id.input_save_password);
        input_accountName = findViewById(R.id.input_save_accountName);
        btn_save = findViewById(R.id.btn_save_save);
        show_data = findViewById(R.id.text_save_showData);
        text_view = findViewById(R.id.text_save_view);

        if(generated_password != null && generated_password.length() >0){
            input_password.setText(generated_password);
        }
        pref = getApplicationContext().getSharedPreferences("Password_Manager_Info", 0);
        online = pref.getBoolean("online", false);
        db = new DBhandler(this);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(5);
                String accountName = input_accountName.getText().toString();
                String username = input_username.getText().toString();
                String password = input_password.getText().toString();
                if(accountName.length() >0 && username.length() > 0 && password.length() > 0){
                    startDataSaving();
                }else{
                    Helper.MakeText(getApplicationContext(),"Please fill up all the fields.");
                }
            }
        });

        show_data.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ShowData();
                return false;
            }
        });
    }

    void startDataSaving(){
        try{
            String userID = pref.getString("username","");
            String accountName = input_accountName.getText().toString();
            String username = input_username.getText().toString();
            String password = input_password.getText().toString();

            String hashed_userID = AESUtils.String_to_SHA1(userID);
            accountName = AESUtils.encrypt(accountName, Main_secret_key);
            username = AESUtils.encrypt(username, Main_secret_key);
            password = AESUtils.encrypt(password, Main_secret_key);

            if(online){
                onlineSaveData(hashed_userID, accountName, username, password);
            }else{
                offlineSaveData(accountName, username, password);
            }
        }catch (Exception e){
            Helper.MakeText(getApplicationContext(),"ERROR 0026: Could not save account information.");
        }
    }

    void onlineSaveData(String hashed_userID,String accountName, String username, String password){
        String url = Helper.pref_site+"/psw.php?fn=save_account&data1="+hashed_userID+
                "&data2="+accountName+
                "&data3="+username+
                "&data4="+password;
        Online_save(url, "validity", accountName, username, password);
    }

    void Online_save(String url, final String key, final String accountName, final String username, final String password) {
        queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String val = response.getString(key);
                            if (val.equals("true"))
                            {
                                offlineSaveData(accountName, username, password);
                            } else {
                                Helper.MakeText(getApplicationContext(),"ERROR 0027: could not save account online.");
                            }

                        } catch (JSONException e) {
                            Helper.MakeText(getApplicationContext(),"ERROR 0028: Server response error.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Helper.MakeText(getApplicationContext(),"ERROR 0029: A connection to the server could not be established.");
                    }
                });
        queue.add(request);
    }

    void offlineSaveData(String accountName, String username, String password){
        db.insertData(accountName, username, password);
        Helper.MakeText(getApplicationContext(),"account information saved.");
    }

    void ShowData(){
        try{
            String output = ">Encrypted data:\n>>===============\n";
            String full_data = db.getData();
            user_account ua;
            String lines[] = full_data.split(";;;");
            if(full_data.length() >0) {
                for (int i = 0; i < lines.length; ++i) {
                    String values[] = lines[i].split(",");
                    String account_name = AESUtils.decrypt(values[1], Main_secret_key);
                    String username = AESUtils.decrypt(values[2], Main_secret_key);
                    String password = AESUtils.decrypt(values[3], Main_secret_key);
                    output += ">>Account Name: "+account_name+"\n";
                    output += ">>Username: "+username+"\n";
                    output += ">>Password: "+password+"\n";
                    output += ">>===============\n";
                }
            }
            text_view.setText(output);
        }catch (Exception e){
            text_view.setText("ERROR 0030: Output error"+e);
        }
    }

}