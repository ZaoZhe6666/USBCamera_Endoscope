package com.example.myapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends Activity {
    private static String TestLog = "TestLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button getInButton = (Button) findViewById(R.id.yindaoye_button);
        getInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TestLog, "Welcome Activity");
                Intent intent = new Intent();
                intent.setClass(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
