package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class CallYaYa {
    private static String yayaPkgName = "com.wifidevice.coantec.activity";
    private static String TestLog = "TestLog";

    public static boolean checkApkExist(Context context, String packageName){
        if (TextUtils.isEmpty(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(packageName,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            Log.d(TestLog, info.toString());
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TestLog, e.toString());
            return false;
        }
    }

    public static boolean checkYaYaExist(Context context){
        return checkApkExist(context, yayaPkgName);
    }
}
