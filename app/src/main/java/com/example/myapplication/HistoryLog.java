package com.example.myapplication;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class HistoryLog {
    private String date;
    private String thumb_pic_path;
    private String pic_path;
    private String diagno;

    private String BACK_PATH = MainActivity.BACK_PATH;
    private String DATA_PATH = MainActivity.BACK_DATA_PATH;

    HistoryLog(int pic_num, String username, String date){
        this.date = date;
    }

    HistoryLog(String date){
        this.thumb_pic_path = DATA_PATH + "/thumb/Thumb_" + date + ".jpg";
        this.pic_path = BACK_PATH + "/Receive_" + date + ".jpg";
        this.date = date;
        this.diagno = getDiagnoFromTimeID(date);
    }

    public String getDiagnoFromTimeID(String date){
        File diagnoFile = new File(Environment.getExternalStorageDirectory(),DATA_PATH + "/diagno/Diagno_" + date + ".txt");
        String diagno = "";
        if(diagnoFile.exists()){
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(diagnoFile), "UTF-8"));
                String lineTxt = null;
                while ((lineTxt = br.readLine()) != null) {
                    diagno = diagno + lineTxt + "\n";
                }
                br.close();
            } catch (Exception e) {
                diagno = "获取诊断结果失败 " + e.getMessage();
            }
        }
        else{
            diagno = "诊断结果缺失";
        }
        return diagno;
    }

    public String getDiagno(){
        return diagno;
    }
    public int getInitPic(){
        return R.mipmap.ic_launcher;
    }
    public String getThumbPicPath(){
        if (thumb_pic_path == ""){
            return "";
        }
        File file = new File(Environment.getExternalStorageDirectory(),thumb_pic_path);
        if(!file.exists()){
            return "";
        }
        return file.getAbsolutePath();
    }

    public String getPicPath(){
        if (pic_path == ""){
            return "";
        }
        File file = new File(Environment.getExternalStorageDirectory(),pic_path);
        if(!file.exists()){
            return "";
        }
        return file.getAbsolutePath();
    }

    public String getdate(){
        return date;
    }

    public void setPicPath(String path){
        this.thumb_pic_path = path;
    }
    public void setDate(String date){
        this.date = date;
    }
}