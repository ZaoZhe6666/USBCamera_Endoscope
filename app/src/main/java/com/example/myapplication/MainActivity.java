package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


@SuppressLint("NewApi")
public class MainActivity extends Activity{
    public static String IP = "192.168.1.106";
    private static String TestLog = "TestLog";

    private boolean hasLogin = false;

    private static int TAKECAMERA = 100;
    private static int LOGININTENT = 200;
    private static int REGISINTENT = 300;

    private static int NOTLOGIN = 401;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hasLogin = false;
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TestLog, "Login in");

                hasLogin = true;
            }
        });

        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TestLog, "register in");


            }
        });

        Button cameraButton = (Button) findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TestLog, "Took photo");
                if(hasLogin == false){
                    Log.d(TestLog, "do not Login");
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("尚未登录");
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
                else{
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, CameraActivity.class);
                    startActivityForResult(intent, TAKECAMERA);
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int result, Intent data) {
        Log.d(TestLog, "requeseCode = " + requestCode);

    }

}
