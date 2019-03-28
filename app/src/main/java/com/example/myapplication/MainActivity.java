package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentUris;
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
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.Socket;
import java.net.URI;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;


@SuppressLint("NewApi")
public class MainActivity extends Activity{
    public static String LocalHost = "http://192.168.25.220:";
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
    private static int CUTPICINTENT = 600;

    private static int NOTLOGIN = 201;

    private ImageView ivImage;
    private String Username = "";
    public Bitmap afterCutBitmap;//剪切后的图像

    private  void permissionGen(){
        PermissionGen.with(MainActivity.this)
                .addRequestCode(200)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .request();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
    @PermissionSuccess(requestCode = 200)
    public void doSomething(){
        Toast.makeText(this, "成功", Toast.LENGTH_SHORT).show();
    }
    @PermissionFail(requestCode = 200)
    public void doFailSomething(){
        Toast.makeText(this, "失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_main);
        permissionGen();

        hasLogin = false;
        ivImage = (ImageView) findViewById(R.id.ivImage);
        ivImage.setVisibility(View.INVISIBLE);


        // 设置服务器地址及端口号
        Button btn_SetPort = (Button)findViewById(R.id.tabbutton_set);
        btn_SetPort.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TestLog, "dialog button listen");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                final View changeServerView = factory.inflate(R.layout.change_server, null);

                final EditText inputServer = (EditText) changeServerView.findViewById(R.id.text_server);
                final EditText inputPort = (EditText) changeServerView.findViewById(R.id.text_port);

                Log.d(TestLog, "init var");

                inputServer.setHint(LocalHost);
                inputPort.setHint("" + port);

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
                            LocalHost = iServer;
//							outServer.setText("当前服务器：" + iServer);
                        }
                        int inputPort;
                        try {
                            if((inputPort = inputCheckPort(iPort)) != -1) {
                                Log.d(TestLog, "change Port");
                                port = inputPort;
//								outPort.setText(out);
                            }
                        }catch(Exception e) {
                            Log.d(TestLog, e.getMessage());
                        }
                        Log.d(TestLog, "After the change :" + LocalHost + "/" + port);
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

                scanDir(MainActivity.this, file.getAbsolutePath());

                File backDir = new File(Environment.getExternalStorageDirectory(), BACK_PATH);
                if(backDir.exists() && backDir.isFile()) {
                    backDir.delete();
                }
                if(!backDir.exists()) {
                    backDir.mkdir();
                }
                scanDir(MainActivity.this, backDir.getAbsolutePath());

                Log.d(TestLog, "Send Broadcast");

                String intentact = "";
//                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {//4.4版本前
//                    intentact = Intent.ACTION_PICK;
//                } else {//4.4版本后
                intentact = Intent.ACTION_GET_CONTENT;
//                }
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
                    dir.mkdirs();
                }

                // 先刷新后选择
                scanDir(MainActivity.this, dir.getAbsolutePath());

                String intentact = "";
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {//4.4版本前
                    intentact = Intent.ACTION_PICK;
                } else {//4.4版本后
                    intentact = Intent.ACTION_GET_CONTENT;
                }
                Intent intent = new Intent(intentact);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

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
        else if(requestCode == SENDPICINTENT){ // 向服务器上传图片 阶段1：裁剪图片
            Log.d(TestLog, "SEND PIC INTENT 1");
            // 查看已有相册图片 -> 建立连接发送图片 -> 接收图片
            String sendPath = null;
            Uri uri = data.getData();
//            String sendPath = UriDeal.Uri2Path(MainActivity.this, uri);
//            String sendPath = uri.getPath();
//            String sendPath = UriDeal.getFilePathFromContentUri(uri, this.getContentResolver());
            sendPath = UriDeal.Uri2Path(MainActivity.this, uri);
            Log.d(TestLog, "img path " + sendPath);
            if(sendPath == null){
                Log.d(TestLog, "illegal img path : null");
                return;
            }
            // 裁剪图
//            new Thread(new SocketSendGetThread(sendPath)).start();
            cropPic(sendPath);
        }

        else if(requestCode == CUTPICINTENT) { // 向服务器上传图片 阶段2：真正上传图片
            Log.d(TestLog, "SEND PIC INTENT 2");
            Log.d(TestLog, "img path " + photo.getAbsolutePath());
            String filePath = photo.getAbsolutePath();

            // 保存截取图片
            if (data != null) {
                //Bitmap bitmap = data.getExtras().getParcelable("data");
                ivImage.setImageBitmap(afterCutBitmap);
                ivImage.setVisibility(View.VISIBLE);

                try {
                    File fileCutSave = new File(filePath);

                    //裁剪后删除拍照的照片
                    if (fileCutSave.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        fileCutSave.delete();
                    }

                    FileOutputStream out = new FileOutputStream(fileCutSave);
                    afterCutBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    Log.d(TestLog, "ERROR IN SAVE CUT PIC:" + e.getMessage());
                }
            }
            scanFile(MainActivity.this, filePath);
            Log.d(TestLog, "UPDATE CUT PIC");
            Log.d(TestLog, filePath);
            // 发送图片
            new Thread(new SocketSendGetThread(filePath)).start();
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
        Uri contentUri = null;
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".uriprovider", file);
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
        this.photo = file;
        startActivityForResult(intent, CUTPICINTENT);
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

    public class LoggingInterceptor implements Interceptor{
        @Override
        public Response intercept(Chain chain) throws IOException{
            Request request = chain.request();
            long t1 = System.nanoTime();
            Log.d(TestLog, String.format("send %s on %s%n%s", request.url(),
                    chain.connection(), request.headers()));
            Response response = chain.proceed(request);
            long t2 = System.nanoTime();
            ResponseBody responseBody = response.peekBody(1024 * 1024);
            Log.d(TestLog, String.format("rec：[%s] %nreturn json:%s  %.1fms%n%s",
                    response.request().url(),
                    responseBody.string(),
                    (t2-t1) /1e6d,
                    response.headers()
                    ));
            return response;
        }
    }

    public class HttpLogger implements HttpLoggingInterceptor.Logger {
        @Override
        public void log(String message) {
            Log.d("HttpLogInfo", message);//okHttp的详细日志会打印出来
        }
    }

    // 通过Socket进行图片收发
    public class SocketSendGetThread implements Runnable{
        private File file;
        private String filePath;
        public SocketSendGetThread(String filePath) {
            this.filePath = filePath;
        }
        @Override
        public void run() {
            HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLogger());
            logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            file = new File(filePath);
            String fileName = file.getName();
            Log.d(TestLog, "OkhttpSendImg");
            Log.d(TestLog, "filename : " + fileName);
            android.os.Message message = Message.obtain();
            message.obj = null;
            OkHttpClient client = new OkHttpClient.Builder()
                    .addNetworkInterceptor(logInterceptor)
                    .build();
            String fileType = getMimeType(fileName);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
//                    .addFormDataPart("username", Username)
//                    .addFormDataPart("submit", "Upload")
                    .addFormDataPart("file", fileName,
                            RequestBody.create(MediaType.parse(fileType), file))
                    .build();

            Request request = new Request.Builder()
                    .url(LocalHost + port + "/upload")
                    .post(requestBody)
                    .build();
            try{
                Response response = client.newCall(request).execute();
                if(!response.isSuccessful()) throw new IOException("Unexpected code");

//                Log.d(TestLog, "response " + response.body().string());
                if(response.body().string().equals("error file type")){
                    Log.d(TestLog, "Login Thread - Illegal type!");
                    // 保存信息
                    message.what = 1;
                    handler.sendMessage(message);
//                    response.close();
                    return;
                }else{
                    Log.d(TestLog, "save");
                    // 保存信息
                    message.what = 0;
                    handler.sendMessage(message);
                }
                Log.d(TestLog, "Login Thread - Finish Success");
//                System.out.println(requestBody);
            }catch (IOException e){
//                System.out.println(requestBody);
                message.what = 404;
                handler.sendMessage(message);
                Log.d(TestLog, "catch error:" + e.getMessage() + request.url());
            }
            Log.d(TestLog, "catch error:" + requestBody);
        }

        private String getMimeType(String filename) {
            FileNameMap filenameMap = URLConnection.getFileNameMap();
            String contentType = filenameMap.getContentTypeFor(filename);
            if (contentType == null) {
                contentType = "application/octet-stream"; //* exe,所有的可执行程序
            }
            return contentType;
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

        if(files == null) return;

        String[] paths = new String[files.length];
        for (int co = 0; co < files.length; co++) {
            paths[co] = files[co].getAbsolutePath();
            Log.d(TestLog, "Scan File :" + files[co].getAbsolutePath());
            scanFile(context, paths[co]);
        }
    }

}
