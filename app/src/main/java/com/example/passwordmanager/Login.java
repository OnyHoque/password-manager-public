package com.example.passwordmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class Login extends AppCompatActivity {

    Button btn_login;
    EditText input_email, input_password;
    RequestQueue queue;
    TextView text_gotoSign_up;
    Switch switch_offlineMode;
    SharedPreferences pref;
//    String pref_site = "http://192.168.68.102";
    boolean online = false;
    SharedPreferences.Editor editor;
    Vibrator vibe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login = findViewById(R.id.btn_login_login);
        input_email = findViewById(R.id.input_login_email);
        input_password = findViewById(R.id.input_login_password);
        text_gotoSign_up = findViewById(R.id.text_login_gotoSignup);
        switch_offlineMode = findViewById(R.id.switch_login_offlineMode);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;

        pref = getApplicationContext().getSharedPreferences("Password_Manager_Info", 0); // 0 - for private mode
        editor = pref.edit();
        online = pref.getBoolean("online", false);

        if(online){
            switch_offlineMode.setChecked(true);
            switch_offlineMode.setText("Online");
        }else{
            switch_offlineMode.setChecked(false);
            switch_offlineMode.setText("Offline");
        }

        String username = pref.getString("username","");
        input_email.setText(username);

        text_gotoSign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GoTo = new Intent(getApplicationContext(), Signup.class);
                startActivity(GoTo);
                finish();
            }
        });

        switch_offlineMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    online = true;
                    editor = pref.edit();
                    editor.putBoolean("online", true);
                    editor.commit();
                    switch_offlineMode.setText("Online");
                } else {
                    online = false;
                    editor = pref.edit();
                    editor.putBoolean("online", false);
                    editor.commit();
                    switch_offlineMode.setText("Offline");
                }
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(5);
                String username = input_email.getText().toString();
                String password = input_password.getText().toString();
                if( username.length() == 0 || password.length() == 0){
                    Helper.MakeText(getApplicationContext(),"Please enter username and password");
                }else {
                    if (online) {
                        online_Y();
                    } else {
                        online_N();
                    }
                }
            }
        });
    }

    void online_N(){
        String pref_username = pref.getString("username", null);
        String pref_password = pref.getString("password", null);

        if(pref_username == null || pref_password == null){
            promptToSignUp();
        }else{
            String username = input_email.getText().toString();
            String password = input_password.getText().toString();
            try {
                String hashed_password = AESUtils.String_to_SHA1(username + password);
                if( pref_username.equals(username) && pref_password.equals(hashed_password) ){
                    goToHome();
                }else{
                    promptWrongCredentials();
                }
            }catch (Exception e){
                Helper.MakeText(getApplicationContext(),"ERROR 0005: Device hardware limitation issue.");
            }
        }
    }


    void online_Y(){
        String pref_username = pref.getString("username", null);
        String pref_password = pref.getString("password", null);

        if(pref_username != null && pref_password != null){
            checkIfOfflineANDInputValid(pref_username, pref_password);
        }else{
            offline_credentials_N();
        }
    }

    void checkIfOfflineANDInputValid(String pref_username,String  pref_password){
        String username = input_email.getText().toString();
        String password = input_password.getText().toString();
        try{
            String hashed_password = AESUtils.String_to_SHA1(username+password);
            if(pref_username.equals(username) && pref_password.equals(hashed_password)){
                offline_credentials_Y(pref_username, pref_password);
            }else{
                promptSignUpAgain();
            }
        }catch (Exception e){
            Helper.MakeText(getApplicationContext(),"ERROR 0006: Device hardware limitation issue.");
        }
    }


    void offline_credentials_N(){
        try{
            String username = input_email.getText().toString();
            String password = input_password.getText().toString();
            String hashed_password = AESUtils.String_to_SHA1(username+password);
            String url = Helper.pref_site+"/psw.php?fn=login&username="+username+"&password="+hashed_password;
            checkIfOnlineAccountExits_N(url);
        }catch (Exception e){
            Helper.MakeText(getApplicationContext(),"ERROR 0007: Device hardware limitation issue.");
        }
    }


    void checkIfOnlineAccountExits_N(String url) {
        queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String val = response.getString("validity");
                            if(val.equals("true")){
                                String username = input_email.getText().toString();
                                String password = input_password.getText().toString();
                                String hashed_password = AESUtils.String_to_SHA1(username+password);
                                editor = pref.edit();
                                editor.putString("username", username);
                                editor.putString("password", hashed_password);
                                editor.commit();
                                goToHome();
                            }else{
                                val = response.getString("output");
                                if(val.equals("no username")){
                                    promptToSignUp();
                                }else if(val.equals("wrong password")){
                                    promptWrongCredentials();
                                }else{
                                    Helper.MakeText(getApplicationContext(),"ERROR 0008: server side error!"+val);
                                }
                            }
                        } catch (JSONException e) {
                            Helper.MakeText(getApplicationContext(),"ERROR 0009: Server response error.");
                        } catch (Exception e) {
                            Helper.MakeText(getApplicationContext(),"ERROR 0010: Device hardware limitation issue.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Helper.MakeText(getApplicationContext(),"ERROR 0011: A connection to the server could not be established.");
                    }
                });
        queue.add(request);
    }


    void offline_credentials_Y(String username, String hashed_password){
        String url = Helper.pref_site+"/psw.php?fn=login&username="+username+"&password="+hashed_password;
        checkIfOnlineAccountExits_Y(url);
    }


    void checkIfOnlineAccountExits_Y(String url) {
        queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String val = response.getString("validity");
                            if(val.equals("true")){
                                goToHome();
                            }else{
                                val = response.getString("output");
                                if(val.equals("no username")){
                                    signUpAccount();
                                }else if(val.equals("wrong password")){
                                    promptSignUpAgain();
                                    promptWrongCredentials();
                                }else{
                                    Helper.MakeText(getApplicationContext(),"ERROR 0012: server side error!"+val);
                                }
                            }
                        } catch (JSONException e) {
                            Helper.MakeText(getApplicationContext(),"ERROR 0013: Server response error.");
                        } catch (Exception e) {
                            Helper.MakeText(getApplicationContext(),"ERROR 0014: Device hardware limitation issue.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Helper.MakeText(getApplicationContext(),"ERROR 0015: A connection to the server could not be established.");
                    }
                });
        queue.add(request);
    }

    void signUpAccount(){
        String pref_username = pref.getString("username", null);
        String pref_password = pref.getString("password", null);
        String url = Helper.pref_site+"/psw.php?fn=signup&username="+pref_username+"&password="+pref_password;
        silentSignUp(url);
    }

    void silentSignUp(String url){
        queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String val = response.getString("validity");
                            if(val.equals("true")){
                                goToHome();
                            }else{
                                Helper.MakeText(getApplicationContext(),"ERROR 0016: Silent sign up could not be completed.");
                            }
                        } catch (JSONException e) {
                            Helper.MakeText(getApplicationContext(),"ERROR 0017: Server response error.");
                        } catch (Exception e) {
                            Helper.MakeText(getApplicationContext(),"ERROR 0018: Device hardware limitation issue.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Helper.MakeText(getApplicationContext(),"ERROR 0019: A connection to the server could not be established.");
                    }
                });
        queue.add(request);
    }


    void promptToSignUp(){
        new AlertDialog.Builder(this)
                .setTitle("Sign up required!")
                .setMessage("You need to sign up before starting to use the app.")
                .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    void promptWrongCredentials(){
        Helper.MakeText(this,"Wrong username of password!");
    }

    void promptSignUpAgain(){
        new AlertDialog.Builder(this)
                .setTitle("Sign up required!")
                .setMessage("Your provided credentials do not match with offline stored credentials. You need to signUp again in offline mode with online credentials.")
                .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    void goToHome(){
        String password = input_password.getText().toString();
        Intent GoTo = new Intent(getApplicationContext(),Home.class);
        GoTo.putExtra("Main_secret_key", AESUtils.secret_key_maker(password));
        startActivity(GoTo);
        finish();
    }



}