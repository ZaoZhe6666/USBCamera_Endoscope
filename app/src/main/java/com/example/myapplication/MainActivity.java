package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


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

        // 主界面登录按钮
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TestLog, "Sign in");

                AlertDialog.Builder login_builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                final View loginView = factory.inflate(R.layout.login_table, null);

                final EditText inputName = (EditText) loginView.findViewById(R.id.login_username);
                final EditText inputPsw = (EditText) loginView.findViewById(R.id.login_psw);

                Log.d(TestLog, "init login var");

                inputName.setHint("请输入用户名");
                inputPsw.setHint("请输入密码");

                Log.d(TestLog, "init hint over");

                login_builder.setTitle("登录");
                login_builder.setIcon(android.R.drawable.ic_dialog_info);
                login_builder.setView(loginView);

                login_builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String iName = inputName.getText().toString();
                        String iPsw = inputPsw.getText().toString();

                        Log.d(TestLog, "Login  name:" + iName + " psw:" + iPsw);

                        // 合法性审查
                        hasLogin = true;
                        Log.d(TestLog, "Login Success");
                    }
                });

                login_builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Log.d(TestLog, "cancel login");
                    }
                });
                login_builder.show();
            }
        });


        // 主界面注册按钮
        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TestLog, "Sign up");

                AlertDialog.Builder register_builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                final View loginView = factory.inflate(R.layout.register_table, null);

                final EditText inputName = (EditText) loginView.findViewById(R.id.register_username);
                final EditText inputPsw1 = (EditText) loginView.findViewById(R.id.register_psw1);
                final EditText inputPsw2 = (EditText) loginView.findViewById(R.id.register_psw2);

                Log.d(TestLog, "init login var");

                inputName.setHint("请输入用户名");
                inputPsw1.setHint("请输入密码");
                inputPsw2.setHint("请确认密码");

                Log.d(TestLog, "init hint over");

                register_builder.setTitle("注册");
                register_builder.setIcon(android.R.drawable.ic_dialog_info);
                register_builder.setView(loginView);

                register_builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Log.d(TestLog, "cancel register");
                    }
                });
                register_builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String iName = inputName.getText().toString();
                        String iPsw1 = inputPsw1.getText().toString();
                        String iPsw2 = inputPsw2.getText().toString();

                        Log.d(TestLog, "Register  name:" + iName + " psw:" + iPsw1 + " psw confirm:" + iPsw2);

                        // 合法性审查
                        hasLogin = true;
                        Log.d(TestLog, "Register Success");
                    }
                });

                register_builder.show();
            }
        });


        // 主界面拍照按钮
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
