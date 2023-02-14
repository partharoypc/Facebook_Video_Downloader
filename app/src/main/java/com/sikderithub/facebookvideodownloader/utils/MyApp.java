package com.sikderithub.facebookvideodownloader.utils;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.paperdb.Paper;

public class MyApp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Paper.init(this);
    }
}
