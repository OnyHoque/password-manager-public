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
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class Signup extends AppCompatActivity {

    Button btn_signup;
    EditText input_email, input_password;
    RequestQueue queue;
    boolean online = false;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Vibrator vibe;
    DBhandler db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        btn_signup = findViewById(R.id.btn_signup_signup);
        input_email = findViewById(R.id.input_signup_email);
        input_password = findViewById(R.id.input_signup_password);
        db = new DBhandler(this);
        pref = getApplicationContext().getSharedPreferences("Password_Manager_Info", 0);
        editor = pref.edit();
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(5);
                online = pref.getBoolean("online", false);
                if(online){
                    Start_Online_Signup_Process();
                }else{
                    Start_Offline_Signup_Process();
                }
            }
        });
    }

    private void Start_Offline_Signup_Process() {
        String username = input_email.getText().toString();
        String password = input_password.getText().toString();
        try{
            String hashed_password = AESUtils.String_to_SHA1(username+password);
            editor = pref.edit();
            editor.putString("username", username);
            editor.putString("password", hashed_password);
            editor.commit();
            db.clearTable();
            Helper.MakeText(getApplicationContext(),"Offline sign-up successful");
            Intent GoTo = new Intent(getApplicationContext(),Login.class);
            startActivity(GoTo);
            finish();
        }catch (Exception e){
            Helper.MakeText(getApplicationContext(),"ERROR 0036: Device hardware limitation issue.");
        }

    }

    boolean validity = false;

    private void Start_Online_Signup_Process() {
        String username = input_email.getText().toString();
        String password = input_password.getText().toString();
        try{
            String hashed_password = AESUtils.String_to_SHA1(username+password);
            String url = Helper.pref_site+"/psw.php?fn=signup&username="
                    +username+"&password="+hashed_password;
            Signup(url,"validity");
        }catch (Exception e){
            Helper.MakeText(getApplicationContext(),"ERROR 0037: Device hardware limitation issue.");
        }
    }

    void gotoLogin(){
        String username = input_email.getText().toString();
        String password = input_password.getText().toString();
        try{
            String hashed_password = AESUtils.String_to_SHA1(username+password);
            editor = pref.edit();
            editor.putString("username", username);
            editor.putString("password", hashed_password);
            editor.commit();
            db.clearTable();
            Intent GoTo = new Intent(getApplicationContext(),Login.class);
            startActivity(GoTo);
            finish();
        }catch (Exception e){
            Helper.MakeText(getApplicationContext(),"ERROR 0038: Device hardware limitation issue.");
        }

    }


    void Signup(String url, final String key) {
        queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String val = response.getString(key);
                            String duplicate = response.getString("duplicate");

                            if (val.equals("true")) {
                                validity = true;
                                Helper.MakeText(getApplicationContext(),"Online Sign-up successful");
                                gotoLogin();
                            } else if(duplicate.equals("true")) {
                                Helper.MakeText(getApplicationContext(),"The username already exists!");
                            }else{
                                Helper.MakeText(getApplicationContext(),"ERROR 0039: Sign-up failed! could not create an account.");
                            }
                        } catch (JSONException e) {
                            Helper.MakeText(getApplicationContext(),"ERROR 0040: server response error.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Helper.MakeText(getApplicationContext(),"ERROR 0041: A connection to the server could not be established.");
                    }
                });
        queue.add(request);
    }
}