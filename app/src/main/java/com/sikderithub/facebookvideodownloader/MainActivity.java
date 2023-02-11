package com.sikderithub.facebookvideodownloader;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static android.content.ContentValues.TAG;
import static com.sikderithub.facebookvideodownloader.Utils.RootDirectoryFacebook;
import static com.sikderithub.facebookvideodownloader.Utils.addWatermark;
import static com.sikderithub.facebookvideodownloader.Utils.createFileFolder;

import static com.sikderithub.facebookvideodownloader.Utils.startDownload;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;

import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sikderithub.facebookvideodownloader.models.DownloadVideo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private EditText txtLink;
    private TextView btnDownloaded,btnPest;
    private WebView wvPage;
    private ProgressBar pbLoading;
    private ClipboardManager clipBoard;
    private MainActivity activity;
    private String videoUrl;
    private String strName = "facebook";
    private String strNameSecond = "fb";

    private Map<Long, DownloadVideo> downloadVideos = new HashMap<>();

    private final BroadcastReceiver downloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (downloadVideos.containsKey(id)){
                Log.d("videoProcess", "onReceive: download complete");

                DownloadVideo downloadVideo = downloadVideos.get(id);

                assert downloadVideo != null;
                addWatermark(context, downloadVideo.getOutputPath(), RootDirectoryFacebook + "watermark_"+ System.currentTimeMillis()+".mp4");

                downloadVideos.remove(id);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(downloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


        txtLink = findViewById(R.id.tv_past_link);
        btnDownloaded = findViewById(R.id.btn_download);
        btnPest = findViewById(R.id.btn_paste);
        pbLoading = findViewById(R.id.pb_loading_wv);
        wvPage = findViewById(R.id.wv_page);

        //Check the read and write user permission
        checkPermission();
        initViews();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadComplete);
    }

    /**
     * check user permission to read and write in the external strage
     * if permission not granted then it takes user permission
     */
    private void checkPermission(){
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
                    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (!report.areAllPermissionsGranted()){
                            checkPermission();
                        }
                    }
                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                }).check();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activity = this;
        clipBoard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
        pasteText();
    }



    private void initViews() {
        clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        btnPest.setOnClickListener(this);

        btnDownloaded.setOnClickListener(this);
    }

    private void pasteText() {
        try {
            txtLink.setText("");
            String copyIntent = getIntent().getStringExtra("CopyIntent");
            if (copyIntent == null || copyIntent.equals("")) {
                if (!(clipBoard.hasPrimaryClip())) {
                    Log.d(TAG, "pasteText: PasteText");
                } else if (!(clipBoard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {

                    if (clipBoard.getPrimaryClip().getItemAt(0).getText().toString().contains(strName)) {
                        txtLink.setText(clipBoard.getPrimaryClip().getItemAt(0).getText().toString());
                    } else if (clipBoard.getPrimaryClip().getItemAt(0).getText().toString().contains(strNameSecond)) {
                        txtLink.setText(clipBoard.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipBoard.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains(strName)) {
                        txtLink.setText(item.getText().toString());
                    } else if (item.getText().toString().contains(strNameSecond)) {
                        txtLink.setText(item.getText().toString());
                    }

                }
            } else {
                if (copyIntent.contains(strName)) {
                    txtLink.setText(copyIntent);
                } else if (copyIntent.contains(strNameSecond)) {
                    txtLink.setText(copyIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void getFacebookData()  {
        try {
            createFileFolder();
            URL url = new URL(txtLink.getText().toString());
            String host = url.getHost();

            if (host.contains(strName) || host.contains(strNameSecond)) {
                Utils.showProgressDialog(activity);
                new callFacebookData().execute(txtLink.getText().toString());

            } else {
                Utils.setToast(activity, getResources().getString(R.string.enter_valid_url));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 100 && resultCode == RESULT_OK) {
                getFacebookData();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btn_paste:
                pasteText();
                break;
            case R.id.btn_download:
                String ll = txtLink.getText().toString();
                if (ll.equals("")) {
                    Utils.setToast(activity, getResources().getString(R.string.enter_url));
                } else if (!Patterns.WEB_URL.matcher(ll).matches()) {
                    Utils.setToast(activity, getResources().getString(R.string.enter_valid_url));
                } else {
                    getFacebookData();
                }

        }
    }


    class callFacebookData extends AsyncTask<String,Void, Document>{

        Document facebookDoc;

        @Override
        protected Document doInBackground(String... urls) {
            try {
                facebookDoc = Jsoup.connect(urls[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: Error");
            }
            Log.d(TAG, "doInBackground: "+ facebookDoc.data());
            return facebookDoc;
        }

        @Override
        protected void onPostExecute(Document result) {
            Utils.hideProgressDialog(activity);

            try {
                videoUrl = result.select("meta[property=\"og:video\"]").last().attr("content");
                Log.d(TAG, "onPostExecute: " + videoUrl);
                if (!videoUrl.equals("")) {
                    DownloadVideo downloadVideo = startDownload(videoUrl, RootDirectoryFacebook, activity, "facebook_"+ System.currentTimeMillis()+".mp4");
                    downloadVideos.put(downloadVideo.getDownloadId(), downloadVideo);
                    videoUrl = "";
                    txtLink.setText("");
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.d(TAG, "onPostExecute: error!!!" + e.toString());
            }
        }
    }


}