package com.example.myapplication;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.core.content.FileProvider;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


@SuppressLint("NewApi")
public class MainActivity extends Activity{
    public static String LocalHost = "http://115.236.52.123";
    private static String TestLog = "TestLog";
    private static String YAYA_PATH = "DCIM/SOAY";
    public static String BACK_PATH = "yaya/DCIM/BACK";

    public static String BACK_DATA_PATH = "yaya/DCIM/BACK/data";
    private static String BACK_TMP_PATH = "yaya/DCIM/BACK/data/thumb";
    private static String BACK_DIAGNO_PATH = "yaya/DCIM/BACK/data/diagno";

    public static int port = 9080;

    private boolean isLogin = false;

    private static File photo;
    private static int TAKECAMERA = 100;
    private static int LOGININTENT = 200;
    private static int REGISINTENT = 300;
    private static int WATCHINTENT = 400;
    private static int SENDPICINTENT = 500;
    private static int CUTPICINTENT = 600;

    private static int NOTLOGIN = 201;

    private ImageView ivImage;
    private Uri teethResultPath;
    //private String IMAGE_FILE_LOCATION = "";
    //private Uri resultUri = Uri.parse(IMAGE_FILE_LOCATION);

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

    @Override
    public void onBackPressed() {
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(launcherIntent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
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

        isLogin = false;
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
/*                File file = new File(Environment.getExternalStorageDirectory(), YAYA_PATH + java.io.File.separator);
                if(file.exists() && file.isFile()) {
                    file.delete();
                }
                if(!file.exists()) {
                    file.mkdir();
                }
*/
                File file = preCreateDir(YAYA_PATH);

                scanDir(MainActivity.this, file.getAbsolutePath());

/*                File backDir = new File(Environment.getExternalStorageDirectory(), BACK_PATH);
                if(backDir.exists() && backDir.isFile()) {
                    backDir.delete();
                }
                if(!backDir.exists()) {
                    backDir.mkdir();
                }
                */
                File backDir = preCreateDir(BACK_PATH);
                scanDir(MainActivity.this, backDir.getAbsolutePath());

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
        Button sendPic = (Button) findViewById(R.id.tabbutton_update);
        sendPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换至查看已有相册事件
/*                File dir = new File(Environment.getExternalStorageDirectory(), YAYA_PATH);
                if(dir.exists() && dir.isFile()) {
                    dir.delete();
                }
                if(!dir.exists()) {
                    dir.mkdirs();
                }
*/
                File dir = preCreateDir(YAYA_PATH);
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
        // 查看历史记录功能
        ImageView checkHistory = (ImageView) findViewById(R.id.img_tab_history);
        checkHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CheckHistoryActivity.class);
                startActivity(intent);
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
            Log.d(TestLog, "img uri " + uri);
//            String sendPath = UriDeal.Uri2Path(MainActivity.this, uri);
//            String sendPath = uri.getPath();
//            String sendPath = UriDeal.getFilePathFromContentUri(uri, this.getContentResolver());
            sendPath = UriDeal.getFilePathByUri(uri, MainActivity.this);
            Log.d(TestLog, "img path " + sendPath);
            if(sendPath == null){
                Log.d(TestLog, "illegal img path : null");
                return;
            }
            //IMAGE_FILE_LOCATION = sendPath + "_result.jpg";
            //resultUri = Uri.parse(IMAGE_FILE_LOCATION);
            // 裁剪图
//            new Thread(new SocketSendGetThread(sendPath)).start();
            cropPic(sendPath);
        }

        else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) { // 向服务器上传图片 阶段2：真正上传图片
            Log.d(TestLog, "SEND PIC INTENT 2");
            CropImage.ActivityResult cropResult = CropImage.getActivityResult(data);
            //if(result == RESULT_OK){
            Uri resultUri = cropResult.getUri();
            //}
            //else if(result == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                //Exception error = cropResult.getError();
            //}
//            Log.d(TestLog, "img path " + photo.getAbsolutePath());
//            String filePath = photo.getAbsolutePath();
//           Uri resultUri = UCrop.getOutput(data);
//            Log.d(TestLog, "uCrop result: " + resultUri.toString());
//            String filePath = resultUri.toString();
            // 保存截取图片
/*
            Log.d(TestLog, "result: " + resultUri);
            if(resultUri != null){
                Bitmap bitmap = null;
                try{
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                }catch(FileNotFoundException e){
                    e.printStackTrace();
                }
                ivImage.setImageBitmap(bitmap);
                ivImage.setVisibility(View.VISIBLE);
            }
*/

            if (data != null) {
                Log.d(TestLog, "data not null");
//                Bitmap bitmap = data.getExtras().getParcelable("data");
                ivImage.setImageURI(resultUri);
                Log.d(TestLog, "resultUri : " + resultUri);
                ivImage.setVisibility(View.VISIBLE);
/*
                try {
                    File fileCutSave = new File(resultUri);

                    //裁剪后删除拍照的照片
                    if (fileCutSave.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        fileCutSave.delete();
                    }

                    FileOutputStream out = new FileOutputStream(fileCutSave);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    Log.d(TestLog, "ERROR IN SAVE CUT PIC:" + e.getMessage());
                }
*/
            }
            String cropResultPath = UriDeal.getFilePathByUri(resultUri, MainActivity.this );
            scanFile(MainActivity.this, cropResultPath);
            Log.d(TestLog, "UPDATE CUT PIC");
            Log.d(TestLog, cropResultPath);
            //LoadingDialog.getInstance(this).show();
            // 发送图片
            Thread sendPhoto = new Thread(new SocketSendGetThread(cropResultPath));
            sendPhoto.start();
            //LoadingDialog.getInstance(this).dismiss();
        }
        else if(requestCode == 123){ //结果显示？
            ivImage.setImageURI(teethResultPath);
            Log.d(TestLog, "resultUri : " + teethResultPath);
            ivImage.setVisibility(View.VISIBLE);
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
        Log.d(TestLog, "imagePath: " + imagePath);
        File file = new File(imagePath);
/*
        File out = new File(imagePath);

        Uri contentUri = null;
        contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".uriprovider", file);
        File outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
        String outFile = contentUri.toString() + "_result.jpg";

        Uri destinationUri = Uri.parse(outFile);
        Log.d(TestLog, "content: " + contentUri);
        Log.d(TestLog, "destination: " + destinationUri);
        UCrop uCrop = UCrop.of(contentUri, destinationUri);

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setAllowedGestures(UCropActivity.NONE, UCropActivity.ROTATE, UCropActivity.ALL);
        options.setHideBottomControls(true);
        options.setToolbarColor(ActivityCompat.getColor(this, R.color.colorPrimary));
        options.setStatusBarColor(ActivityCompat.getColor(this, R.color.colorPrimary));
        options.setFreeStyleCropEnabled(false);
        options.setCompressionQuality(100);
        options.setShowCropFrame(true);


        uCrop.withOptions(options);
        uCrop = uCrop.withAspectRatio(700,700);
        uCrop = uCrop.withMaxResultSize(700, 700);
        uCrop.start(MainActivity.this, CUTPICINTENT);
*/
        Uri contentUri = null;
        contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".uriprovider", file);
        //Intent intent = new Intent("com.android.camera.action.CROP");
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //    contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".uriprovider", file);
        //    intent.setDataAndType(contentUri, "image/*");
        //} else {
        //    intent.setDataAndType(Uri.fromFile(file), "image/*");
        //}
        Log.d(TestLog, "SEND PIC INTENT 1");
        CropImage.activity(contentUri)
                .setActivityTitle("牙齿剪裁")
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setCropMenuCropButtonTitle("Done")
                .setRequestedSize(700,700)
                .setCropMenuCropButtonIcon(R.drawable.ic_launcher_background)
                .start(this);


        //intent.putExtra("crop", "true");
        //intent.putExtra("aspectX", 1);
        //intent.putExtra("aspectY", 1);
        //intent.putExtra("outputX", 700);
        //intent.putExtra("outputY", 700);
        //intent.putExtra("scale", true);
        //intent.putExtra("return-data", true);
        //intent.putExtra("noFaceDetection",true);
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, resultUri);
        //intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //this.photo = file;

        //startActivityForResult(intent, CUTPICINTENT);

    }

    private File preCreateDir(String path){
        File dir = new File(Environment.getExternalStorageDirectory(), path);
        Log.d(TestLog, "dir.exists is:" + dir);
        if(dir.exists() && dir.isFile()) {
            dir.delete();
            Log.d(TestLog, "dir.delete:" + dir);
        }
        if(!dir.exists()) {
            Log.d(TestLog, "dir.exists");
            if(dir.mkdirs()) {
                Log.d(TestLog, "pre Create Dir:" + dir.getAbsolutePath());
            }
        }
        else {
            Log.d(TestLog, "dir.exists");
        }
        return dir;
    }


    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if(msg.what == 0) {
                Log.d(TestLog, "分析结束");
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
                    isLogin = true;
                }
                else if(msg.what == 3) {
                    congraText = "上传成功, 正在分析";
                    isLogin = true;
                }
                else if(msg.what == 5) {
                    congraText = "分析结束，点击查看结果";
                    isLogin = true;
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
        private String resultPath;
        private String Username = LoginActivity.userName;
        public SocketSendGetThread(String filePath) {
            this.filePath = filePath;
        }
        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            String date = String.valueOf(calendar.get(Calendar.YEAR)) + "."
                    + String.valueOf(calendar.get(Calendar.MONTH) + 1) + "."
                    + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLogger());
            logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            file = new File(filePath);
            resultPath = filePath + "_result.jpg";
            String fileName = file.getName();
            Log.d(TestLog, "OkhttpSendImg");
            Log.d(TestLog, "filename : " + fileName);
            android.os.Message message = Message.obtain();
            message.obj = null;
            OkHttpClient client = new OkHttpClient.Builder()
                    .callTimeout(25, TimeUnit.SECONDS)
                    .connectTimeout(25, TimeUnit.SECONDS)
                    .readTimeout(25, TimeUnit.SECONDS)
                    .writeTimeout(25, TimeUnit.SECONDS)
                    .addNetworkInterceptor(logInterceptor)
                    .build();
            String fileType = getMimeType(fileName);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
//                    .addFormDataPart("username", Username)
//                    .addFormDataPart("submit", "Upload")
                    .addFormDataPart("file", fileName,
                            RequestBody.create(MediaType.parse(fileType), file))
                    .addFormDataPart("Username", Username)
                    .addFormDataPart("Date", date)
                    .addFormDataPart("Type", fileType)
                    .addFormDataPart("submit", "Upload")
                    .build();

            Request request = new Request.Builder()
                    .url(LocalHost + ":" + port + "/upload")
                    .post(requestBody)
                    .build();
            try{
                Call call = client.newCall(request);
                message.what = 3;
                handler.sendMessage(message);
                Response response = call.execute();
                if(!response.isSuccessful()) throw new IOException("Unexpected code");

//                Log.d(TestLog, "response " + response.body().string());
                ResponseBody body = response.body();
                long contentLength = body.contentLength();
                Log.d(TestLog, "contentLength" + String.valueOf(contentLength));
                if(contentLength <= 25){
                    Log.d(TestLog, "Login Thread - Illegal type!");
                    // 保存信息
                    android.os.Message messageE = Message.obtain();
                    messageE.obj = null;
                    messageE.what = 1;
                    handler.sendMessage(messageE);
//                    response.close();
                    return;
                }else{
                    Log.d(TestLog, "save");
                    // 保存信息
/*                    BufferedSource source = body.source();
                    File resultFile = new File(resultPath);
                    BufferedSink sink = Okio.buffer(Okio.sink(resultFile));
                    sink.writeAll(source);
                    sink.flush();
                    Log.d(TestLog, "resultpath : " + resultPath);
                    Log.d(TestLog, "Login Thread - Finish Success");
*/
                    Bitmap bitmap = parseJsonWithJsonObject(body);
                    android.os.Message messageRW = Message.obtain();
                    messageRW.obj = null;
                    messageRW.what = 5;
                    handler.sendMessage(messageRW);
                    Log.d(TestLog, "message is ok");
                    android.os.Message messageR = Message.obtain();
                    messageR.obj = bitmap;
                    messageR.what = 0;
                    handler.sendMessage(messageR);
                    Log.d(TestLog, "message is ok");
//                    teethResultPath = Uri.fromFile(resultFile);
                    return;
                }

//                System.out.println(requestBody);
            }catch (IOException e){
//                System.out.println(requestBody);
                android.os.Message messageError = Message.obtain();
                messageError.obj = null;
                messageError.what = 404;
                handler.sendMessage(messageError);
                Log.d(TestLog, "catch error:" + e.getMessage() + request.url());
            }
        }

        private String getMimeType(String filename) {
            FileNameMap filenameMap = URLConnection.getFileNameMap();
            String contentType = filenameMap.getContentTypeFor(filename);
            if (contentType == null) {
                contentType = "application/octet-stream"; //* exe,所有的可执行程序
            }
            return contentType;
        }
        private Bitmap parseJsonWithJsonObject(ResponseBody body) throws IOException{
            String responseData = body.string();
            try{
                // 定位输出路径
                File dir = preCreateDir(BACK_PATH);
                File dir_data = preCreateDir(BACK_DATA_PATH);
                File dir_thumb = preCreateDir(BACK_TMP_PATH);
                File dir_diagno = preCreateDir(BACK_DIAGNO_PATH);
                // 使用时间作为输出
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String timePath =  dateFormat.format(date);
                String filePath = dir.getAbsolutePath() + "/Receive_" + timePath + ".jpg";
                File outputFile = new File(filePath);
                Log.d(TestLog, "filepath is: " + filePath);
                Log.d(TestLog, "iscreate");
                if(outputFile.exists()){
                    outputFile.delete();
                }
                Log.d(TestLog, "iscreate");
                boolean iscreate = outputFile.createNewFile();
                Log.d(TestLog, "iscreate" + iscreate);
                FileOutputStream outputStream = new FileOutputStream(outputFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

                Log.d(TestLog, "Receive is ok");
                //解析json文件
                JSONObject jsonObject = new JSONObject(responseData);
                String photoName = jsonObject.getString("name");
                Log.d(TestLog, "photo name is:" + photoName);
                String imageBase64String = jsonObject.getString("image_base64_string");
                String ratio = jsonObject.getString("ratio");
                //保存图片
                byte[] buffer = Base64.decode(imageBase64String, Base64.DEFAULT);
                bufferedOutputStream.write(buffer);
                bufferedOutputStream.flush();
                outputStream.close();
                //创建显示用bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                Log.d(TestLog, "bitmap is ok");
                // 获取缩略图
                String thumbPath = dir_thumb.getAbsolutePath() + "/Thumb_" + timePath + ".jpg";
                BufferedOutputStream buffStream = new BufferedOutputStream(new FileOutputStream(new File(thumbPath)));
                Bitmap bitmap_thumb = Bitmap.createScaledBitmap(bitmap, 100 , 100, true);
                bitmap_thumb.compress(Bitmap.CompressFormat.JPEG, 100, buffStream);
                buffStream.flush();
                buffStream.close();
                //存储评价信息
                String diagnoPath = dir_diagno.getAbsolutePath() + "/Diagno_" + timePath + ".txt";
                File diagnoFile = new File(diagnoPath);
                if(diagnoFile.exists() && diagnoFile.isDirectory()){
                    diagnoFile.delete();
                    diagnoFile.createNewFile();
                }
                else if(!diagnoFile.exists()){
                    diagnoFile.createNewFile();
                }
                FileWriter fwd = new FileWriter(diagnoFile);
                BufferedWriter bwd = new BufferedWriter(fwd);
                bwd.write( ratio + "%\n");
                bwd.close();
                fwd.close();
                // 存储时间戳信息
                String historyPath = dir_data.getAbsolutePath() + "/History.txt";
                File historyFile = new File(historyPath);
                if(historyFile.exists() && historyFile.isDirectory()){
                    historyFile.delete();
                    historyFile.createNewFile();
                }
                else if(!historyFile.exists()){
                    historyFile.createNewFile();
                }
                //SimpleDateFormat timeFormat = new SimpleDateFormat("yy-MM-dd hh:mm:ss a E", Locale.getDefault());
                //String time =  dateFormat.format(date);

                FileWriter fwt=new FileWriter(historyFile, true);
                BufferedWriter bwt=new BufferedWriter(fwt);
                bwt.write(timePath + "\n");
                bwt.close();
                fwt.close();

                return bitmap;
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = null;
            return bitmap;
        }

    }
/*
    public class OkhttpGetThread implements Runnable{
        private String resultPath;
        public OkhttpGetThread(String resultPath){
            this.resultPath = resultPath;
        }
        @Override
        public void run() {
            HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLogger());
            logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        }
    }
    */
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
    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

}
