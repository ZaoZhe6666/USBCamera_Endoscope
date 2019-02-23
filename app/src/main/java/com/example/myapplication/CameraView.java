package com.example.myapplication;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraView extends SurfaceView implements Callback {
    private SurfaceHolder surfaceHolder;
    private Camera mCamera;

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Camera camera){
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        mCamera = camera;
    }

    /**
     * 初始化SurfaceView时调用一次，另外更改surface或者onpause->onresume时调用
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if(holder.getSurface() == null || mCamera == null){
            return;
        }
        mCamera.stopPreview();
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(mCamera == null){
            return;
        }
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
    }

}
