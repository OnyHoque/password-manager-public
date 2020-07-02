package com.example.passwordmanager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;


public class Offline extends AppCompatActivity {



    EditText input_backupPass;
    Button btn_backup, btn_filePicker;
    TextView view_outputTXT;
    String Main_secret_key;
    Vibrator vibe;
    DBhandler db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
        setContentView(R.layout.activity_offline);
        Intent intent = getIntent();
        Main_secret_key = intent.getStringExtra("Main_secret_key");
        btn_backup = findViewById(R.id.btn_offline_backup);
        input_backupPass = findViewById(R.id.input_offline_backupPass);
        view_outputTXT = findViewById(R.id.view_offline_output_view);
        btn_filePicker = findViewById(R.id.btn_offline_selectFile);
        db = new DBhandler(this);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;


        btn_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(5);
                writeLogFile("Password_Manager_backup.pmb");
            }
        });


        btn_filePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(5);
                readLogFile();
            }
        });
    }



    void requestPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("The app needs Storage access permission to read and write the backup file. On the next dialog please press allow.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ActivityCompat.requestPermissions(Offline.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    })
                    .create().show();
        }
    }



    void readLogFile(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Helper.MakeText(getApplicationContext(),"Look for the .pmb file extension");
            new MaterialFilePicker()
                    .withActivity(this)
                    .withRequestCode(1)
                    .withFilter(Pattern.compile(".*\\.pmb$"))
                    .withFilterDirectories(false)
                    .withHiddenFiles(true)
                    .start();
        } else {
            Helper.MakeText(getApplicationContext(),"Permission not granted.");
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            try{
                BufferedReader br = new BufferedReader(new FileReader(filePath));
                String full_data = br.readLine();
                String backup_password = input_backupPass.getText().toString();
                if(backup_password.length() == 0){
                    view_outputTXT.setText(">>Please enter a password to decrypt data.");
                }else{
                    String secret_key = AESUtils.secret_key_maker(backup_password);
                    full_data = AESUtils.decrypt(full_data, secret_key);

                    String lines[] = full_data.split(";;;");
                    if(full_data.length() >0) {
                        for (int i = 0; i < lines.length; ++i) {
                            String values[] = lines[i].split(",");
                            String account_name = values[1];
                            String username = values[2];
                            String password = values[3];
                            db.insertData(account_name, username, password);
                        }
                    }
                    view_outputTXT.setText(">>Backup restored to app database.");
                }

            }catch (BadPaddingException e){
                view_outputTXT.setText(">>Wrong Password!");
            }
            catch (Exception e){
                view_outputTXT.setText(">>ERROR 0020: "+e);
            }
        }
    }



    void writeLogFile(String filename) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File file_path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
            file_path.getParentFile().mkdirs();
            try {
                String data = db.getData();
                String password = input_backupPass.getText().toString();
                if(password.length() ==0){
                    view_outputTXT.setText(">>Please enter a password to backup database.");
                }else {
                    String secret_key = AESUtils.secret_key_maker(password);
                    String encrypted_data = AESUtils.encrypt(data, secret_key);
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file_path, false));
                    bufferedWriter.write(encrypted_data);
                    bufferedWriter.close();
                    view_outputTXT.setText(">>backup file created in: " + file_path);
                }
            } catch (Exception e) {
                Helper.MakeText(getApplicationContext(),"ERROR 0021: Writing to device storage failed.");
            }
        } else {
            Helper.MakeText(getApplicationContext(),"File writing permission not granted.");
        }
    }
}