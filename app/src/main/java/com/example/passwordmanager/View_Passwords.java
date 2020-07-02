package com.example.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class View_Passwords extends AppCompatActivity implements MyAdapter.OnNoteListener {

    String Main_secret_key, userID;
    DBhandler db;
    private List<user_account> ua_list = new ArrayList<>();
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    boolean online = false;
    SharedPreferences pref;
    LinkedList<String[]> objArr;
    int counter = 0;
    RequestQueue queue;
    Vibrator vibe;
    ImageView btn_search;
    EditText input_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view__passwords);

        Intent intent = getIntent();
        Main_secret_key = intent.getStringExtra("Main_secret_key");
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;
        recyclerView = findViewById(R.id.recycle_view);
        btn_search = findViewById(R.id.img_viewPass_btnSearch);
        input_search = findViewById(R.id.input_viewPass_search);
        myAdapter = new MyAdapter(ua_list, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(myAdapter);
        pref = getApplicationContext().getSharedPreferences("Password_Manager_Info", 0);
        online = pref.getBoolean("online", false);
        userID = pref.getString("username","");
        objArr = new LinkedList();
        db = new DBhandler(getApplicationContext());

        if(online){
            try{
                String hashed_userID = AESUtils.String_to_SHA1(userID);
                String url = Helper.pref_site+"/psw.php?fn=get_data"+
                        "&data1="+hashed_userID;
                getOnlineData(url);
            }catch (Exception e){
                Helper.MakeText(getApplicationContext(),"ERROR 0042: Device hardware limitation issue.");
            }
        }else{
            getOfflineData();
        }

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(5);
                String searchword = input_search.getText().toString();
                updateView(searchword);
            }
        });
    }

    void getOnlineData(String url){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                            user_account ua;
                            for(int i=0;i<response.length();i++){
                                try{
                                    JSONObject user_data = response.getJSONObject(i);
                                    String data1 = user_data.getString("data1");
                                    String data2 = user_data.getString("data2");
                                    String data3 = user_data.getString("data3");
                                    data1 = AESUtils.decrypt(data1, Main_secret_key);
                                    data2 = AESUtils.decrypt(data2, Main_secret_key);
                                    data3 = AESUtils.decrypt(data3, Main_secret_key);
                                    ua = new user_account(data1 , data2, data3);
                                    ua_list.add(ua);
                                    String arr[] = {data1, data2, data3};
                                    objArr.add(arr);
                                    counter++;
                                }catch (JSONException e){
                                    Helper.MakeText(getApplicationContext(),"ERROR 0043: Server response error.");
                                } catch (Exception e) {
                                    Helper.MakeText(getApplicationContext(),"ERROR 0044: The data does not belong to this account.");
                                }
                            }
                            getOfflineData(); }},
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Helper.MakeText(getApplicationContext(),"ERROR 0045: A connection to the server could not be established.");
                    }
                });
        requestQueue.add(jsonArrayRequest);
    }

    void getOfflineData(){
        String full_data = db.getData();
        try{
            user_account ua;
            String lines[] = full_data.split(";;;");
            if(full_data.length() >0) {
                for (int i = 0; i < lines.length; ++i) {
                    String values[] = lines[i].split(",");
                    String account_name = AESUtils.decrypt(values[1], Main_secret_key);
                    String username = AESUtils.decrypt(values[2], Main_secret_key);
                    String password = AESUtils.decrypt(values[3], Main_secret_key);
                    if (check_duplicate(objArr, account_name, username, password) == false) {
                        ua = new user_account(account_name, username, password);
                        ua_list.add(ua);
                        counter++;
                    }
                }
            }
            objArr.clear();
        }catch (Exception e){
            Helper.MakeText(getApplicationContext(),"ERROR 0046: Could not retrieve offline data.");
        }
        myAdapter.notifyDataSetChanged();
        if(counter == 0){
            Helper.MakeText(getApplicationContext(),"No accounts stored.");
        }
    }

    boolean check_duplicate(LinkedList<String[]> obj, String data1, String data2, String data3){
        boolean flag = false;
        for(int i = 0; i < obj.size(); ++i){
            String arr[] = obj.get(i);
            if (arr[0].equals(data1) && arr[1].equals(data2) && arr[2].equals(data3)){
                flag = true;
            }
        }
        return flag;
    }


    public void onDeleteClick(final int position){
        vibe.vibrate(5);
        user_account ua = ua_list.get(position);
        String account_name = ua.getAccount_name(), username = ua.getUsername(), password = ua.getPassword();
        try{
            final String data1 = AESUtils.String_to_SHA1(userID);
            final String data2 = AESUtils.encrypt(account_name, Main_secret_key);
            final String data3 = AESUtils.encrypt(username, Main_secret_key);
            final String data4 = AESUtils.encrypt(password, Main_secret_key);

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            String url = Helper.pref_site+"/psw.php?fn=delete_account&data1="+data1
                                    +"&data2="+data2
                                    +"&data3="+data3
                                    +"&data4="+data4;
                            ua_list.remove(position);
                            if(online)
                                getJSON(url, "success", data2, data3, data4);
                            else
                                offlineDelete(data2, data3, data4);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            Helper.MakeText(getApplicationContext(),"The account was not deleted.");
                            break;
                    } }};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to delete the account?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }catch (Exception e){
            Helper.MakeText(getApplicationContext(),"ERROR 0047: could not delete account.");
        }
    }

    void getJSON(String url, final String key, final String data2, final String data3, final String data4) {
        queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String val = response.getString(key);
                            if (val.equals("true")) {
                                offlineDelete(data2, data3, data4);
                                Helper.MakeText(getApplicationContext(),"Account deleted successfully.");
                            } else {
                                String error = response.getString("error");
                                Helper.MakeText(getApplicationContext(),"ERROR 0048: Could not delete account.");
                            }
                        } catch (JSONException e) {
                            Helper.MakeText(getApplicationContext(),"ERROR 0049: Server response error.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Helper.MakeText(getApplicationContext(),"ERROR : A connection to the server could not be established.");
                    }
                });
        queue.add(request);
    }


    void offlineDelete(String data2, String data3, String data4){
        try{
            db.deleteRow(data2, data3, data4);
        }catch (Exception e){
            Helper.MakeText(getApplicationContext(),""+e);
        }
        myAdapter = new MyAdapter(ua_list, this);
        recyclerView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
    }


    public void onEditClick(int position){
        vibe.vibrate(5);
        user_account ua = ua_list.get(position);
        String account_name = ua.getAccount_name(), username = ua.getUsername(), password = ua.getPassword();
        Intent GoTo = new Intent(getApplicationContext(),Edit_account.class);
        GoTo.putExtra("Main_secret_key", Main_secret_key);
        GoTo.putExtra("account_name", account_name);
        GoTo.putExtra("username", username);
        GoTo.putExtra("password", password);
        startActivity(GoTo);
        finish();
    }


    public void updateView(String search_word){
        search_word = search_word.toLowerCase();
        List<user_account> search_list = new ArrayList<>();
        for(int i = 0; i < ua_list.size(); ++i){
            user_account ua = ua_list.get(i);
            String username = ua.getUsername();
            username = username.toLowerCase();
            String accountName = ua.getAccount_name();
            accountName = accountName.toLowerCase();
            if( username.indexOf(search_word) > -1 || accountName.indexOf(search_word) > -1){
                search_list.add(ua);
            }
        }
        myAdapter = new MyAdapter(search_list, this);
        recyclerView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
    }

}