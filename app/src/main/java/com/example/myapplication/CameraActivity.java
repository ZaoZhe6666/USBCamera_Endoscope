package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.hardware.Camera.PictureCallback;

public class CameraActivity extends Activity{
    private Button switchButton, takeButton;
    private Camera myCamera;
    private CameraView cameraView;
    private int currentCameraType = 0;//当前打开的摄像头标记
    private String TestLog = "TestLog";

    private byte[] tmpPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if(!checkCamera()){
            CameraActivity.this.finish();
        }
        try {
            myCamera = openCamera(currentCameraType);
            Log.d(TestLog, "init camera :" + myCamera);
        } catch (Exception e) {
            Log.d(TestLog, "init error:" + e.getMessage());
            e.printStackTrace();
        }

        // 切换摄像头按钮
        switchButton = (Button)findViewById(R.id.btn_switch);
        switchButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    changeCamera();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        // 拍照按钮
        takeButton = (Button)findViewById(R.id.btn_take);
        takeButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    changeCamera();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        cameraView = (CameraView)findViewById(R.id.cameraview);
        cameraView.init(myCamera);
    }

    /**
     * @return 摄像头是否存在
     */
    private boolean checkCamera(){
        return CameraActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @SuppressLint("NewApi")
    private Camera openCamera(int type){
        Log.d(TestLog, "in open camera");
        int frontIndex =-1;
        int backIndex = -1;
        int cameraCount = Camera.getNumberOfCameras();
        Log.d(TestLog, "camera count : " + cameraCount);
        CameraInfo info = new CameraInfo();

        type %= cameraCount;
        return Camera.open(type);
    }

    private void changeCamera() throws IOException{
        Log.d(TestLog, "in change camera ");

        if(myCamera != null) {
            myCamera.stopPreview();
            Log.d(TestLog, "stop success");
            myCamera.release();
            Log.d(TestLog, "release success");
            myCamera = null;
        }

        currentCameraType++;
        myCamera = openCamera(currentCameraType);
        Log.d(TestLog, "open success");

        if(myCamera != null) {
            myCamera.setPreviewDisplay(cameraView.getHolder());
            myCamera.startPreview();
            Log.d(TestLog, "start preview success");
        }
    }


    // 监控触发按键
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TestLog, "in diapatch KeyEvent");
        ScanKeyActivity.analysisKeyEvent(event);
        return true;
    }

    PictureCallback myJpegCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TestLog, "Temperary Save the Pic byte[]");
            tmpPic = data;
            myCamera.stopPreview();
        }
    };

    // 本地存储拍摄结果并结束当前活动
    public void saveJpeg() {
        try {
            Log.d(TestLog, "Save in ./Endoscope");
            // 定位确定本地文件夹
            File dir = new File(Environment.getExternalStorageDirectory(), "Endoscope");
            if(dir.exists() && dir.isFile()) {
                dir.delete();
            }
            if(!dir.exists()) {
                dir.mkdir();
            }

            // 采用时间戳作为文件名
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String filePath = dir.getAbsolutePath() + "/Take_" + dateFormat.format(date) + ".jpg";
            File file = new File(filePath);
            file.createNewFile();
            Log.d(TestLog, "FilePath:" + filePath);

            // byte数组转bitmap存储
            Bitmap bitmap = BitmapFactory.decodeByteArray(tmpPic, 0, tmpPic.length);
            BufferedOutputStream buffStream = new BufferedOutputStream(new FileOutputStream(file));
            Log.d(TestLog, "First Bitmap is set :" + bitmap.getWidth() + ":" + bitmap.getHeight());

            // bitmap 旋转
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap bitmapRoute = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            Log.d(TestLog, "Route Bitmap is set :" + bitmapRoute.getWidth() + ":" + bitmapRoute.getHeight());

            buffStream.close();

            // 远程刷新
    //        MainActivity.setPhoto(file);
            Log.d(TestLog, "Saved Success");
        } catch (Exception e) {
            Log.d(TestLog, "catch in saveJpeg:" + e.getMessage());
        }
    }








}
