package com.example.passwordmanager;

import android.content.Context;
import android.widget.Toast;

public class Helper {
    public static String pref_site = "http://192.168.100.50";

    public static void MakeText(Context context, String str){
        Toast.makeText(context,str, Toast.LENGTH_SHORT).show();
    }
}
