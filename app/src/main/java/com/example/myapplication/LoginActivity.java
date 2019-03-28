package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class LoginActivity extends Activity{
    private String TestLog = "TestLog";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TestLog, "Login Activity");


        EditText inputName = (EditText) findViewById(R.id.editText_login_username);
        EditText inputPsw = (EditText) findViewById(R.id.editText_login_password);

        Log.d(TestLog, "EditText Ok");

        // 确认登录 验证填写信息
        Button loginButton = (Button) findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TestLog, "LoginActivity - check Input");
                String iName = inputName.getText().toString();
                String iPsw = inputPsw.getText().toString();

                Log.d(TestLog, "Login  name:" + iName + " psw:" + iPsw);
                new Thread(new LoginThread(iName, iPsw)).start();
            }
        });

        // 切换至注册
        TextView change2regis = (TextView) findViewById(R.id.view_to_register);
        change2regis.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TestLog, "LoginActivity Change to RegisterActivity");
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 设置服务器地址及端口号
        TextView login_set = (TextView) findViewById(R.id.login_to_set);
        login_set.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TestLog, "dialog button listen");
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                LayoutInflater factory = LayoutInflater.from(LoginActivity.this);
                final View changeServerView = factory.inflate(R.layout.change_server, null);

                final EditText inputServer = (EditText) changeServerView.findViewById(R.id.text_server);
                final EditText inputPort = (EditText) changeServerView.findViewById(R.id.text_port);

                Log.d(TestLog, "init var");

                inputServer.setHint(MainActivity.LocalHost);
                inputPort.setHint("" + MainActivity.port);

                Log.d(TestLog, "init hint over");

                builder.setTitle("修改服务器信息");
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setView(changeServerView);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String iServer = inputServer.getText().toString();
                        String iPort = inputPort.getText().toString();

                        Log.d(TestLog, "the change :" + iServer + "/" + iPort);

                        // 合法性审查
                        if(inputCheckServer(iServer)) {
                            Log.d(TestLog, "change Server");
                            MainActivity.LocalHost = iServer;
//							outServer.setText("当前服务器：" + iServer);
                        }
                        int inputPort;
                        try {
                            if((inputPort = inputCheckPort(iPort)) != -1) {
                                Log.d(TestLog, "change Port");
                                MainActivity.port = inputPort;
//								outPort.setText(out);
                            }
                        }catch(Exception e) {
                            Log.d(TestLog, e.getMessage());
                        }
                        Log.d(TestLog, "After the change :" + MainActivity.LocalHost + "/" + MainActivity.port);
                    }
                    private boolean inputCheckServer(String iServer) {
                        // 参考资料https://blog.csdn.net/chaiqunxing51/article/details/50975961/
                        if(iServer == null || iServer.length() == 0) { // 基础检验
                            return false;
                        }
                        String[] parts = iServer.split("\\.");
                        if(parts.length != 4) { // 四段ip设置
                            return false;
                        }
                        for(int i = 0; i < 4; i++) {
                            try {
                                int n = Integer.parseInt(parts[i]);
                                if(n< 0 || n > 255) return false; // ip数检验
                            }catch(NumberFormatException e) {
                                return false; // 非法字符检验
                            }
                        }
                        return true;
                    }
                    private int inputCheckPort(String iPort) {
                        try {
                            int port = Integer.parseInt(iPort);
                            if(1024 < port && port < 65535) {
                                return port;
                            }
                        }catch(NumberFormatException e) {
                        }
                        return -1;
                    }
                    private int inputCheckColor(String iColor) {
                        try {
                            String regex="^#[A-Fa-f0-9]{6}$";
                            if(iColor.matches(regex)) {
                                return 0;
                            }
                        }catch(Exception e) {
                        }
                        return -1;
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

    }

    // 通过Handler实现报错提示
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if(msg.what == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                String congraText = "登陆成功";
                builder.setTitle("恭喜！") ;
                builder.setMessage(congraText);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                builder.show();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("出错误啦！") ;
                String errorText = "";
                if(msg.what == 1) errorText = "用户名不存在";
                else if(msg.what == 2) errorText = "密码有误";
                else if(msg.what == 404) errorText = "与服务器" + MainActivity.LocalHost + ":" + MainActivity.port + "连接失败";
                builder.setMessage(errorText);
                builder.setPositiveButton("确定",null );
                builder.show();
            }
        }
    };

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
                socket = new Socket(MainActivity.LocalHost, MainActivity.port);

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
