package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


@SuppressLint("NewApi")
public class MainActivity extends Activity{
    public static String LocalHost = "192.168.1.106";
    private static String TestLog = "TestLog";
    private static String YAYA_PATH = "yaya/DCIM/SOAY";
    private static String BACK_PATH = "yaya/DCIM/BACK";

    public static int port = 5000;

    private boolean hasLogin = false;

    private static File photo;

    private static int TAKECAMERA = 100;
    private static int LOGININTENT = 200;
    private static int REGISINTENT = 300;
    private static int WATCHINTENT = 400;
    private static int SENDPICINTENT = 500;

    private static int NOTLOGIN = 401;

    private ImageView ivImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_main);

        hasLogin = false;
        ivImage = (ImageView) findViewById(R.id.ivImage);
        ivImage.setVisibility(View.INVISIBLE);

        // 主界面登录按钮
        Button loginButton = (Button) findViewById(R.id.tabbutton_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TestLog, "Sign in");

                // 登录弹窗
                AlertDialog.Builder login_builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);

                final View loginView = factory.inflate(R.layout.activity_login, null);
                final EditText inputName = (EditText) loginView.findViewById(R.id.editText_login_username);
                final EditText inputPsw = (EditText) loginView.findViewById(R.id.editText_login_password);
                final Button sureButton = (Button) loginView.findViewById(R.id.btn_login);

                Log.d(TestLog, "init login var");

                inputName.setHint("请输入用户名");
                inputPsw.setHint("请输入密码");

                Log.d(TestLog, "init hint over");

                login_builder.setTitle("登录");
                login_builder.setIcon(android.R.drawable.ic_dialog_info);
                login_builder.setView(loginView);

                final AlertDialog login_dialog = login_builder.show();

                // 登录弹窗 - 确定键绑定
                sureButton.setOnClickListener( new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        try{
                            String iName = inputName.getText().toString();
                            String iPsw = inputPsw.getText().toString();

                            Log.d(TestLog, "Login  name:" + iName + " psw:" + iPsw);

                            // 合法性审查 通过socket与服务器沟通
                            new Thread(new LoginThread(iName, iPsw)).start();

                            Log.d(TestLog, "dialog dismiss");
                            login_dialog.dismiss();
                        }catch(Exception e){
                            Log.d(TestLog, "dismiss error:" + e.getMessage());
                        }
                    }
                });
            }
        });


        // 主界面注册按钮
        Button registerButton = (Button) findViewById(R.id.tabbutton_regis);
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TestLog, "Sign up");

                // 注册弹窗
                AlertDialog.Builder register_builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                final View registerView = factory.inflate(R.layout.activity_register, null);

                final EditText inputName = (EditText) registerView.findViewById(R.id.register_username);
                final EditText inputPsw1 = (EditText) registerView.findViewById(R.id.register_password);
                final EditText inputPsw2 = (EditText) registerView.findViewById(R.id.register_password_repeat);

                final Button sureButton = (Button) registerView.findViewById(R.id.register_button);

                Log.d(TestLog, "init login var");

                inputName.setHint("请输入用户名");
                inputPsw1.setHint("请输入密码");
                inputPsw2.setHint("请确认密码");

                Log.d(TestLog, "init hint over");

                register_builder.setTitle("注册");
                register_builder.setIcon(android.R.drawable.ic_dialog_info);
                register_builder.setView(registerView);

                // 注册弹窗 - 确定事件绑定
                final AlertDialog register_dialog = register_builder.show();
                sureButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        // 注册信息确定
                        String iName = inputName.getText().toString();
                        String iPsw1 = inputPsw1.getText().toString();
                        String iPsw2 = inputPsw2.getText().toString();
                        hasLogin = true;

                        // 通过socket与服务器查询/实现注册
                        // 查询是否存在/合法性 + 实现及返回结果
                        Log.d(TestLog, "Register  name:" + iName + " psw:" + iPsw1 + " psw confirm:" + iPsw2);
                        new Thread(new RegisterThread(iName, iPsw1, iPsw2)).start();

                        register_dialog.dismiss();
                    }
                });
            }
        });

        // 查看相册功能
        Button watchButton = (Button) findViewById(R.id.tabbutton_see);
        watchButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Log.d(TestLog, "View the Album");

                // 先刷新 后浏览
                File file = new File(Environment.getExternalStorageDirectory(), YAYA_PATH + java.io.File.separator);
                if(file.exists() && file.isFile()) {
                    file.delete();
                }
                if(!file.exists()) {
                    file.mkdir();
                }
<<<<<<< HEAD

=======
>>>>>>> d286848b88cd92cf6e47664a22a325df8b18278d
                scanDir(MainActivity.this, file.getAbsolutePath());

                Log.d(TestLog, "Send Broadcast");

                String intentact = "";
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {//4.4版本前
                    intentact = Intent.ACTION_PICK;
                } else {//4.4版本后
                    intentact = Intent.ACTION_GET_CONTENT;
                }
                Intent intent = new Intent(intentact);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                Log.d(TestLog, "Look the Album");
                startActivityForResult(intent, WATCHINTENT);
            }

        });

        // 主界面拍照按钮
        Button cameraButton = (Button) findViewById(R.id.tabbutton_take);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TestLog, "Took photo");

                Log.d(TestLog, "Check If App is installed");
                if(!CallYaYa.checkYaYaExist(MainActivity.this)){
                    // 未安装YaYa APP - 提示及跳转下载页面

                    Log.d(TestLog, "YaYa App is not installed");
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("出错误啦");
                    builder.setMessage("未下载辅助APP！");
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setNegativeButton("点击下载", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            // 跳转到下载页面
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse("https://android.myapp.com/myapp/detail.htm?apkName=com.wifidevice.coantec.activity#");
                            intent.setData(content_url);
                            startActivity(intent);
                        }
                    });
                    builder.setPositiveButton("稍后再说", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    return;
                }

                Intent intent = new Intent();
                //包名 包名+类名（全路径）
                ComponentName comp = new ComponentName("com.wifidevice.coantec.activity","com.methnm.coantec.activity.MainActivity");
                intent.setComponent(comp);
                intent.setAction("android.intent.action.MAIN");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("data", "123");
                startActivity(intent);
            }
        });

        // 主界面上传图像功能
        ImageView sendPic = (ImageView) findViewById(R.id.img_tab_zhenduan);
        sendPic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //切换至查看已有相册事件
                File dir = new File(Environment.getExternalStorageDirectory(), YAYA_PATH);
                if(dir.exists() && dir.isFile()) {
                    dir.delete();
                }
                if(!dir.exists()) {
                    dir.mkdir();
                }
                // 先刷新后选择
                scanDir(MainActivity.this, dir.getAbsolutePath());

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Uri uri = Uri.fromFile(dir);
                intent.setData(uri);
                intent.setType("image/*");
                startActivityForResult(intent, SENDPICINTENT);
            }
        });

        // 查看图片完毕
        ImageView imageRecover = (ImageView)findViewById(R.id.ivImage);
        imageRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivImage.setVisibility(View.INVISIBLE);
            }
        });


    }


    protected void onActivityResult(int requestCode, int result, Intent data) {
        Log.d(TestLog, "requeseCode = " + requestCode);
        if(requestCode == TAKECAMERA){
            // 拍照功能已改为调用已有YaYa APP对应功能
        }
        else if(requestCode == WATCHINTENT) {
            Log.d(TestLog, "show select pic:");
            // 显示图片
            if(data == null){
                Log.d(TestLog, "Select Pic Cancel");
                return;
            }
            Uri uri = data.getData();
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/*");
            startActivity(intent);
        }
        else if(requestCode == SENDPICINTENT){ // 向服务器上传图片并返回结果
            // 查看已有相册图片 -> 建立连接发送图片 -> 接收图片
            final Uri uri = data.getData();

            String sendPath = UriDeal.Uri2Path(MainActivity.this, uri);
            Log.d(TestLog, "img path " + sendPath);
            File uploadFile = new File(sendPath);
            new Thread(new SocketSendGetThread(uploadFile)).start();
        }
        File mPhotoFile = new File(getAppFile(this, "images/user_take.jpg"));
        switch (requestCode) {
            case CROP_PHOTO: //裁剪照片后
                if (data != null) {
                    Bitmap bitmap = data.getExtras().getParcelable("data");
                    ivImage.setImageBitmap(bitmap);
                }
                //裁剪后删除拍照的照片
                if (mPhotoFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    mPhotoFile.delete();
                }
                break;
            case ACTION_TAKE_PHOTO:
                if (mPhotoFile.exists()) {
                    cropPic(getAppFile(this, "images/user_take.jpg"));
                }
                break;
        }
    }

    public static void setPhoto(File file){
        photo = file;
    }


    public final int CROP_PHOTO = 10;
    public final int ACTION_TAKE_PHOTO = 20;


    /**
     * 获取本应用在系统的存储目录
     */
    public static String getAppFile(Context context, String uniqueName) {
        String cachePath;
        if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable())
                && context.getExternalCacheDir() != null) {
            cachePath = context.getExternalCacheDir().getParent();
        } else {
            cachePath = context.getCacheDir().getParent();
        }
        return cachePath + File.separator + uniqueName;
    }

    /**
     * 跳转到系统裁剪图片页面
     * @param imagePath 需要裁剪的图片路径
     */
    private void cropPic(String imagePath) {
        File file = new File(imagePath);
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, "com.leon.crop.fileprovider", file);
            intent.setDataAndType(contentUri, "image/*");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "image/*");
        }
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0.1);
        intent.putExtra("aspectY", 0.1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        intent.putExtra("scale", true);
        startActivityForResult(intent, CROP_PHOTO);
    }

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if(msg.what == 0) {
                Log.d(TestLog, "in handle Message!");
                Bitmap bitmap = (Bitmap) msg.obj;
                ivImage.setImageBitmap(bitmap);
                ivImage.setVisibility(View.VISIBLE);
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

    // 通过Socket进行图片收发
    public class SocketSendGetThread implements Runnable{
        private File file;
        public SocketSendGetThread(File file) {
            this.file = file;
        }
        @Override
        public void run() {
            Log.d(TestLog, "SocketSendImg");
            Socket socket;
            try {
                // 创建Socket 指定服务器IP和端口号
                socket = new Socket(LocalHost, port);

                // 创建InputStream用于读取文件
                InputStream inputFile = new FileInputStream(file);

                // 创建Socket的InputStream用来接收数据
                InputStream inputConnect = socket.getInputStream();

                // 创建Socket的OutputStream用于发送数据
                OutputStream outputConnect = socket.getOutputStream();

                // 发送识别码
                outputConnect.write("Picture".getBytes());
                outputConnect.flush();

                // send分隔 div 1
                inputConnect.read(new byte[10]);

                // 发送文件大小
                long fileSize = inputFile.available();
                String fileSizeStr = fileSize + "";
                outputConnect.write(fileSizeStr.getBytes());
                outputConnect.flush();

                // send分隔 div 2
                inputConnect.read(new byte[10]);

                //将本地文件转为byte数组
                byte buffer[] = new byte[4 * 1024];
                int tmp = 0;
                // 循环读取文件
                while((tmp = inputFile.read(buffer)) != -1) {
                    outputConnect.write(buffer, 0, tmp);
                }

                // 发送读取数据到服务端
                outputConnect.flush();

                // 关闭输入流
                inputFile.close();

                // 通过socket与RequestURL建立连接，并接受一张图片存到本地
                Log.d(TestLog, "SocketGetImg");

                // 接收返回码
                byte symCodeBuff[] = new byte[200];
                int symCode = inputConnect.read(symCodeBuff);
                String symCodeStr = new String(symCodeBuff, 0, symCode);
                symCode = Integer.valueOf(symCodeStr);
                Log.d(TestLog, "Sym Code is " + symCode);

                // 返回码表明有误
                if(symCode != 0) {
                    // 设置返回信息
                    android.os.Message message = Message.obtain();
                    message.obj = null;
                    message.what = symCode;
                    Log.d(TestLog, "message is ok");
                    handler.sendMessage(message);
                    Log.d(TestLog, "handler is ok");

                    inputConnect.close();
                    outputConnect.close();
                    socket.close();
                    return;
                }

                // 发送分隔符
                outputConnect.write("BreakTime".getBytes());
                outputConnect.flush();

                // 定位输出路径
                File dir = new File(Environment.getExternalStorageDirectory(), BACK_PATH);
                if(dir.exists() && dir.isFile()) {
                    dir.delete();
                }
                if(!dir.exists()) {
                    dir.mkdir();
                }

                // 使用时间作为输出
                Date date = new Date(System.currentTimeMillis());
                Log.d(TestLog, "break in date");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                Log.d(TestLog, "break in sdf");
                String filePath = dir.getAbsolutePath() + "/Receive_" + dateFormat.format(date) + ".jpg";
                Log.d(TestLog, "break2");
                FileOutputStream outputStream = new FileOutputStream(filePath);
                Log.d(TestLog, "break3");

                // 读取接收文件大小
                byte piclenBuff[] = new byte[200];
                int picLen = inputConnect.read(piclenBuff);
                String picLenStr = new String(piclenBuff, 0, picLen);
                picLen = Integer.valueOf(picLenStr);
                Log.d(TestLog, "fileSize is:" + picLen);

                // 发送确认信息
                outputConnect.write("receive".getBytes());
                outputConnect.flush();

                // 读取接收文件
                byte buffer2[] = new byte[picLen];
                int offset = 0;
                while(offset < picLen) {
                    int len = inputConnect.read(buffer2, offset, picLen - offset);
                    Log.d(TestLog, "" + len);
                    outputStream.write(buffer2, offset, len);
                    offset += len;
                }
                Log.d(TestLog, "yeah");
                inputConnect.close();
                outputStream.close();

                // 关闭连接
                socket.close();
                Log.d(TestLog, "Get Img success.The result is " + filePath);
                if(filePath.equals("")) return;
                Bitmap bitmap = BitmapFactory.decodeByteArray(buffer2, 0, offset);
                Log.d(TestLog, "bitmap is ok");
                android.os.Message message = Message.obtain();
                message.obj = bitmap;
                message.what = 0;
                Log.d(TestLog, "message is ok");
                handler.sendMessage(message);
                Log.d(TestLog, "handler is ok");

            }catch(Exception e) {
                Log.d(TestLog, "catch error:" + e.getMessage());
            }

        }

    }

    // 文件刷新
    public static void scanFile(Context context, String filePath) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(scanIntent);
    }

    // 文件夹刷新
    public static void scanDir(Context context, String dir) {
        File[] files = new File(dir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        if(files == null)   return;
        String[] paths = new String[files.length];
        for (int co = 0; co < files.length; co++) {
            paths[co] = files[co].getAbsolutePath();
            Log.d(TestLog, "Scan File :" + files[co].getAbsolutePath());
            scanFile(context, paths[co]);
        }
    }

}
