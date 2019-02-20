package com.example.myapplication;

import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

// 分析key事件
public class ScanKeyActivity {

    //延迟500ms，判断扫码是否完成。
    private final static long MESSAGE_DELAY = 500;

    //扫码内容
    private static StringBuffer mStringBufferResult = new StringBuffer();

    //大小写区分
    private static boolean mCaps;
    private static OnScanSuccessListener mOnScanSuccessListener;
    private static Handler mHandler = new Handler();

    private static String TestTag = "TestLog";

    private final static Runnable mScanningFishedRunnable = new Runnable() {
        @Override
        public void run() {
            performScanSuccess();
        }
    };

    //返回扫描结果
    private static void performScanSuccess() {
        String barcode = mStringBufferResult.toString();
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onScanSuccess(barcode);
        mStringBufferResult.setLength(0);
    }

    //key事件处理
    public static void analysisKeyEvent(KeyEvent event) {
        Log.d(TestTag, "Get a KeyEvent!");
        int keyCode = event.getKeyCode();

        Log.d(TestTag, "keyCode:" + keyCode);
        Log.d(TestTag, "flags:" + event.getFlags());
        Log.d(TestTag, "describeC:" + event.describeContents());
        Log.d(TestTag, "Action:" + event.getAction());
        Log.d(TestTag, "Characters:" + event.getCharacters());
        Log.d(TestTag, "DeviceId:" + event.getDeviceId());
        Log.d(TestTag, "DiaplayLabel:" + event.getDisplayLabel());
        Log.d(TestTag, "Number:" + event.getNumber());
        Log.d(TestTag, "ScanCode:" + event.getScanCode());
        Log.d(TestTag, "Unicode:" + event.getUnicodeChar());
        Log.d(TestTag, "toString:" + event.toString());

        //字母大小写判断
        checkLetterStatus(event);

        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            char aChar = getInputCode(event);;

            if (aChar != 0) {
                mStringBufferResult.append(aChar);
            }

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //若为回车键，直接返回
                mHandler.removeCallbacks(mScanningFishedRunnable);
                mHandler.post(mScanningFishedRunnable);
            } else {
                //延迟post，若500ms内，有其他事件
                mHandler.removeCallbacks(mScanningFishedRunnable);
                mHandler.postDelayed(mScanningFishedRunnable, MESSAGE_DELAY);
            }

        }
    }

    //检查shift键
    private static void checkLetterStatus(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                //按着shift键，表示大写
                mCaps = true;
            } else {
                //松开shift键，表示小写
                mCaps = false;
            }
        }
    }


    //获取扫描内容
    private static char getInputCode(KeyEvent event) {

        int keyCode = event.getKeyCode();

        char aChar;

        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            //字母
            aChar = (char) ((mCaps ? 'A' : 'a') + keyCode - KeyEvent.KEYCODE_A);
        } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            //数字
            aChar = (char) ('0' + keyCode - KeyEvent.KEYCODE_0);
        } else {
            //其他符号
            switch (keyCode) {
                case KeyEvent.KEYCODE_PERIOD:
                    aChar = '.';
                    break;
                case KeyEvent.KEYCODE_MINUS:
                    aChar = mCaps ? '_' : '-';
                    break;
                case KeyEvent.KEYCODE_SLASH:
                    aChar = '/';
                    break;
                case KeyEvent.KEYCODE_BACKSLASH:
                    aChar = mCaps ? '|' : '\\';
                    break;
                default:
                    aChar = 0;
                    break;
            }
        }

        return aChar;

    }




    public interface OnScanSuccessListener {
        public void onScanSuccess(String barcode);
    }

    public void setOnBarCodeCatchListener(OnScanSuccessListener onScanSuccessListener) {
        mOnScanSuccessListener = onScanSuccessListener;
    }

    public void onDestroy() {
        mHandler.removeCallbacks(mScanningFishedRunnable);
        mOnScanSuccessListener = null;
    }
}