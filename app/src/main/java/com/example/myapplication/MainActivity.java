package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


@SuppressLint("NewApi")
public class MainActivity extends Activity{
    public static String LocalHost = "10.135.20.165";
    private static String TestLog = "TestLog";

    public static int port = 5000;

    private boolean hasLogin = false;

    private static File photo;

    private static int TAKECAMERA = 100;
    private static int LOGININTENT = 200;
    private static int REGISINTENT = 300;
    private static int WATCHINTENT = 400;

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

                // 登录弹窗
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

                // 登录弹窗 - 确定键绑定
                login_builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String iName = inputName.getText().toString();
                        String iPsw = inputPsw.getText().toString();

                        Log.d(TestLog, "Login  name:" + iName + " psw:" + iPsw);

                        // 合法性审查 通过socket与服务器沟通
                        new Thread(new LoginThread(iName, iPsw)).start();
                    }
                });

                // 登录弹窗 - 取消事件绑定
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

                // 注册弹窗
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

                // 注册弹窗 - 取消键绑定
                register_builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Log.d(TestLog, "cancel register");
                    }
                });

                // 注册弹窗 - 确定事件绑定
                register_builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 注册信息确定
                        String iName = inputName.getText().toString();
                        String iPsw1 = inputPsw1.getText().toString();
                        String iPsw2 = inputPsw2.getText().toString();
                        hasLogin = true;

                        // 通过socket与服务器查询/实现注册
                        // 查询是否存在/合法性 + 实现及返回结果
                        Log.d(TestLog, "Register  name:" + iName + " psw:" + iPsw1 + " psw confirm:" + iPsw2);
                        new Thread(new RegisterThread(iName, iPsw1, iPsw2)).start();
                    }
                });

                register_builder.show();
            }
        });

        // 查看相册功能
        Button watchButton = (Button) findViewById(R.id.watch_button);
        watchButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String intentact = "";
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {//4.4版本前
                    intentact = Intent.ACTION_PICK;
                } else {//4.4版本后
                    intentact = Intent.ACTION_GET_CONTENT;
                }
                Intent intent = new Intent(intentact);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, WATCHINTENT);
            }
        });

        // 主界面拍照按钮
        Button cameraButton = (Button) findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TestLog, "Took photo");
                if(hasLogin == false){
                    // 未登录 - 提示及不允许拍摄
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
                    // 已登录 - 进入拍照活动
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, CameraActivity.class);
                    startActivityForResult(intent, TAKECAMERA);
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int result, Intent data) {
        Log.d(TestLog, "requeseCode = " + requestCode);
        if(requestCode == TAKECAMERA){
            if(photo == null) {
                Log.d(TestLog, "Interrupt Cancel in Taking Photo");
                return;
            }

            // 插入系统图库
            Log.d(TestLog, "deal photo:" + photo.getAbsolutePath());
            try {
                MediaStore.Images.Media.insertImage(null, photo.getAbsolutePath(), photo.getName(), null);
            } catch (FileNotFoundException e) {}

            Uri uri = Uri.fromFile(photo);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setData(uri);

            setPhoto(null);
            Log.d(TestLog, "deal ok");
            sendBroadcast(intent);
        }
        else if(requestCode == WATCHINTENT) {
            // 显示图片
            Uri uri = data.getData();
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/*");
            startActivity(intent);
        }
    }

    public static void setPhoto(File file){
        photo = file;
    }

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if(msg.what == 0) {
                Log.d(TestLog, "in handle Message!");
                Bitmap bitmap = (Bitmap) msg.obj;
            }
            else if(msg.what <= 5){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String congraText = "";
                if(msg.what == 1) congraText = "注册成功";
                else if(msg.what == 2) {
                    congraText = "登陆成功";
                    hasLogin = true;
                }
                builder.setTitle("恭喜！") ;
                builder.setMessage(congraText);
                builder.setPositiveButton("确定",null );
                builder.show();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("出错误啦！") ;
                String errorText = "";
                if(msg.what == 1) errorText = "用户名不存在";
                else if(msg.what == 2) errorText = "用户名已被注册";
                else if(msg.what == 3) errorText = "未识别到本机摄像头";
                else if(msg.what == 404) errorText = "与服务器" + LocalHost + ":" + port + "连接失败";
                builder.setMessage(errorText);
                builder.setPositiveButton("确定",null );
                builder.show();
            }
        }
    };

    // 通过Socket实现的 客户端 - 服务器 进行的注册交互
    public class RegisterThread implements Runnable{
        private String name;
        private String psw1;
        private String psw2;

        public RegisterThread(String name, String psw1, String psw2){
            Log.d(TestLog, "Register Thread - init");
            this.name = name;
            this.psw1 = psw1;
            this.psw2 = psw2;
        }

        @Override
        public void run(){
            Log.d(TestLog, "Register Thread - run");
            Socket socket;

            // 创建交互handler，用于保存最终结果
            android.os.Message message = Message.obtain();
            message.obj = null;

            try {
                // 创建Socket 指定服务器IP和端口号
                socket = new Socket(LocalHost, port);

                // 创建Socket的InputStream用来接收数据
                InputStream inputConnect = socket.getInputStream();

                // 创建Socket的OutputStream用于发送数据
                OutputStream outputConnect = socket.getOutputStream();

                Log.d(TestLog, "Register Thread - Connect!");

                // 发送指令号
                outputConnect.write("Register".getBytes());
                outputConnect.flush();

                Log.d(TestLog, "Register Thread - Sym send");

                // send分隔
                inputConnect.read(new byte[10]);
                Log.d(TestLog, "Register Thread - Sym got");

                // 发送用户名
                outputConnect.write(name.getBytes());
                outputConnect.flush();
                Log.d(TestLog, "Register Thread - Name send");

                // 获得用户名查询反馈
                byte name_confirm[] = new byte[10];
                int ncCode = inputConnect.read(name_confirm);
                String ncCodeStr = new String(name_confirm, 0, ncCode);
                ncCode = Integer.valueOf(ncCodeStr);
                Log.d(TestLog, "Register Thread - Name confirm got");

                if(ncCode != 0){ // 用户名已存在
                    Log.d(TestLog, "Register Thread - Same Name!");
                    // 保存信息
                    message.what = 0;
                    handler.sendMessage(message);

                    // 关闭并退出
                    inputConnect.close();
                    outputConnect.close();
                    socket.close();
                    return;
                }

                // 用户不存在
                // 加密用户密码 发送给服务器
                // do something with psw
                outputConnect.write(psw1.getBytes());
                outputConnect.flush();
                Log.d(TestLog, "Register Thread - Psw send");

                // 接收返回码
                byte symCodeBuff[] = new byte[200];
                int symCode = inputConnect.read(symCodeBuff);
                String symCodeStr = new String(symCodeBuff, 0, symCode);
                symCode = Integer.valueOf(symCodeStr);
                Log.d(TestLog, "Register Thread - Psw confirm got");

                // 设置返回信息
                message.what = symCode;
                handler.sendMessage(message);
                Log.d(TestLog, "Register Thread - Return Value is Set");

                inputConnect.close();
                outputConnect.close();
                socket.close();
                Log.d(TestLog, "Register Thread - Finish Success");

            }catch(Exception e) {
                message.what = 404;
                handler.sendMessage(message);
                Log.d(TestLog, "catch error:" + e.getMessage());
            }
        }
    }

    // 通过Socket实现的 客户端 - 服务器 进行的登录交互
    public class LoginThread implements Runnable{
        private String name;
        private String psw;
        public LoginThread(String name, String psw){
            Log.d(TestLog, "Login Thread - run");
            this.name = name;
            this.psw = psw;
        }

        @Override
        public void run(){
            Log.d(TestLog, "Login Thread - run");
            Socket socket;

            // 创建交互handler，用于保存最终结果
            android.os.Message message = Message.obtain();
            message.obj = null;

            try {
                // 创建Socket 指定服务器IP和端口号
                socket = new Socket(LocalHost, port);

                // 创建Socket的InputStream用来接收数据
                InputStream inputConnect = socket.getInputStream();

                // 创建Socket的OutputStream用于发送数据
                OutputStream outputConnect = socket.getOutputStream();

                Log.d(TestLog, "Login Thread - Connect!");

                // 发送指令号
                outputConnect.write("Login".getBytes());
                outputConnect.flush();

                Log.d(TestLog, "Login Thread - Sym send");

                // send分隔
                inputConnect.read(new byte[10]);
                Log.d(TestLog, "Login Thread - Sym got");

                // 发送用户名
                outputConnect.write(name.getBytes());
                outputConnect.flush();
                Log.d(TestLog, "Login Thread - Name send");

                // 获得用户名查询反馈
                byte name_confirm[] = new byte[10];
                int ncCode = inputConnect.read(name_confirm);
                String ncCodeStr = new String(name_confirm, 0, ncCode);
                ncCode = Integer.valueOf(ncCodeStr);
                Log.d(TestLog, "Login Thread - Name confirm got");

                if(ncCode != 0){ // 用户名不存在
                    Log.d(TestLog, "Login Thread - Illegal Name!");
                    // 保存信息
                    message.what = 0;
                    handler.sendMessage(message);

                    // 关闭并退出
                    inputConnect.close();
                    outputConnect.close();
                    socket.close();
                    return;
                }

                // 用户存在
                // 加密用户密码 发送给服务器
                // do something with psw
                outputConnect.write(psw.getBytes());
                outputConnect.flush();
                Log.d(TestLog, "Login Thread - Psw send");

                // 接收返回码
                byte symCodeBuff[] = new byte[200];
                int symCode = inputConnect.read(symCodeBuff);
                String symCodeStr = new String(symCodeBuff, 0, symCode);
                symCode = Integer.valueOf(symCodeStr);
                Log.d(TestLog, "Login Thread - Psw confirm got");

                // 设置返回信息
                message.what = symCode;
                handler.sendMessage(message);
                Log.d(TestLog, "Login Thread - Return Value is Set");

                inputConnect.close();
                outputConnect.close();
                socket.close();
                Log.d(TestLog, "Login Thread - Finish Success");

            }catch(Exception e) {
                message.what = 404;
                handler.sendMessage(message);
                Log.d(TestLog, "catch error:" + e.getMessage());
            }
        }
    }

}
