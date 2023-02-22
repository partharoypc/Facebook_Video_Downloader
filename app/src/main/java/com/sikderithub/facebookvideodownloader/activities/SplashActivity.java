package com.sikderithub.facebookvideodownloader.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.sikderithub.facebookvideodownloader.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Intent intent = new Intent(this,MainActivity.class);

        new Handler().postDelayed(() -> {
            startActivity(intent);
            finish();
        },3000);


    }
}