package com.example.myapplication;

public class HistoryLog {
    private int pic_num;
    private String username;
    private int date;

    HistoryLog(int pic_num, String username, int date){
        this.pic_num = pic_num;
        this.username = username;
        this.date = date;
    }

    public int getPicNum(){
        return pic_num;
    }
    public String getUsername(){
        return username;
    }
    public int getdate(){
        return date;
    }
}
