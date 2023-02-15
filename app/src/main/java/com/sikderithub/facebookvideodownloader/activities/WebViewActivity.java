package com.sikderithub.facebookvideodownloader.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.sikderithub.facebookvideodownloader.R;

public class WebViewActivity extends AppCompatActivity {

    public static final String KEY_WEB_ADDRESS = "address";
    public static final String KEY_TITLE  = "title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);


        WebView webView = findViewById(R.id.wv_web_view);
        ProgressBar progressBar = findViewById(R.id.pb_progressbar);

        String address = getIntent().getExtras().getString(KEY_WEB_ADDRESS);
        String title = getIntent().getExtras().getString(KEY_TITLE);

        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });
        webView.loadUrl(address);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}