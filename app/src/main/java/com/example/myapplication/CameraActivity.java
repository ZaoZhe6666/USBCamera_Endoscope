package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class CameraActivity extends Activity{
    private Button button;
    private Camera camera;
    private CameraView cameraView;
    private int currentCameraType = 0;//当前打开的摄像头标记
    private String TestLog = "TestLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if(!checkCamera()){
            CameraActivity.this.finish();
        }
        try {
            camera = openCamera(currentCameraType);
            Log.d(TestLog, "init camera :" + camera);
        } catch (Exception e) {
            Log.d(TestLog, "init error:" + e.getMessage());
            e.printStackTrace();
        }
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new Button.OnClickListener() {
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
        cameraView.init(camera);
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

        if(camera != null) {
            camera.stopPreview();
            Log.d(TestLog, "stop success");
            camera.release();
            Log.d(TestLog, "release success");
            camera = null;
        }

        currentCameraType++;
        camera = openCamera(currentCameraType);
        Log.d(TestLog, "open success");

        if(camera != null) {
            camera.setPreviewDisplay(cameraView.getHolder());
            camera.startPreview();
            Log.d(TestLog, "start preview success");
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TestLog, "in diapatch KeyEvent");
        ScanKeyActivity.analysisKeyEvent(event);
        return true;
    }
}
