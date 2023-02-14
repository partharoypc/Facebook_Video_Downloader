package com.sikderithub.facebookvideodownloader.activities;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.sikderithub.facebookvideodownloader.utils.Constants.downloadVideos;
import static com.sikderithub.facebookvideodownloader.utils.Utils.createFileFolder;

import static com.sikderithub.facebookvideodownloader.utils.Utils.startDownload;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;

import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sikderithub.facebookvideodownloader.Database;
import com.sikderithub.facebookvideodownloader.utils.MyApp;
import com.sikderithub.facebookvideodownloader.R;
import com.sikderithub.facebookvideodownloader.utils.Utils;
import com.sikderithub.facebookvideodownloader.VideoProcessingService;
import com.sikderithub.facebookvideodownloader.adapters.ListAdapter;
import com.sikderithub.facebookvideodownloader.models.FVideo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainActivity extends MyApp implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private EditText txtLink;
    private TextView btnDownloaded, btnPest;
    private RecyclerView downloadList;
    private static ListAdapter adapter;

    private ClipboardManager clipBoard;
    private MainActivity activity;
    private String videoUrl;
    private String strName = "facebook";
    private String strNameSecond = "fb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Start videoProcessing service to process the video and add watermark using FFmpeg
        Intent intent = VideoProcessingService.getIntent(this);
        startService(intent);

        txtLink = findViewById(R.id.tv_past_link);
        btnDownloaded = findViewById(R.id.btn_download);
        btnPest = findViewById(R.id.btn_paste);
        downloadList = findViewById(R.id.rv_download_list);

        downloadList.setLayoutManager(new LinearLayoutManager(this));
        //Item click listener for download list
        adapter = new ListAdapter(this, new ListAdapter.ItemClickListener() {
            @Override
            public void onItemClickListener(FVideo video) {
                assert video != null;
                Log.d(TAG, "onItemClickListener: " + video.getFileUri());

                switch (video.getState()) {
                    case FVideo.DOWNLOADING:
                        //video is in download state
                        Toast.makeText(getApplicationContext(), "Video Downloading", Toast.LENGTH_LONG).show();
                        break;
                    case FVideo.PROCESSING:
                        //Video is processing
                        Toast.makeText(getApplicationContext(), "Video Processing", Toast.LENGTH_LONG).show();
                        break;
                    case FVideo.COMPLETE:
                        //complete download and processing ready to use
                        String location = video.getFileUri();

                        Log.d(TAG, "onItemClickListener: " + location);
                        File file = new File(location);
                        if (file.exists()){
                            Uri uri = Uri.parse(location);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "*/*");
                            startActivity(intent);
                        }else{
                            Toast.makeText(getApplicationContext(), "File doesn't exists", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "onItemClickListener: file " + file.getPath());
                        }


                }

            }
        });

        //This function notify the adapter to dataset change
        updateListData();
        downloadList.setAdapter(adapter);

        //Check the read and write user permission
        checkPermission();

        //Initialize all views and click listener
        initViews();

    }

    /**
     * this function update the listAdapter data form the database
     */
    public static void updateListData() {
        adapter.setVideos(Database.getVideos());
    }

    /**
     * check user permission to read and write in the external strage
     * if permission not granted then it takes user permission
     */
    private void checkPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (!report.areAllPermissionsGranted()) {
                            checkPermission();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                }).check();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activity = this;
        clipBoard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
        pasteText();
    }


    /**
     * Initialize views and item click listerner
     */
    private void initViews() {
        clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        btnPest.setOnClickListener(this);

        btnDownloaded.setOnClickListener(this);
    }

    /**
     * paste clipboard text to edit text
     */
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


    /**
     * this function called when download button is clicked
     * this function call jsoup in a background thread fetch video and download it
     */
    private void getFacebookData() {
        try {
            createFileFolder();
            URL url = new URL(txtLink.getText().toString());
            String host = url.getHost();
            Log.d(TAG, "getFacebookData: url " + url);

            if (host.contains(strName) || host.contains(strNameSecond)) {
                Utils.showProgressDialog(activity);

                //calling jsoup in background thread
                new callFacebookData().execute(txtLink.getText().toString());

            } else {
                Utils.setToast(activity, getResources().getString(R.string.enter_valid_url));
            }
        } catch (Exception e) {
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
        switch (id) {
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


    class callFacebookData extends AsyncTask<String, Void, Document> {

        Document facebookDoc;

        @Override
        protected Document doInBackground(String... urls) {

            try {
                //fetching facebook document form facebook link
                facebookDoc = Jsoup.connect(urls[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: Error");
            }
            //Log.d("videoProcess", "doInBackground: "+ facebookDoc.data());
            return facebookDoc;
        }

        @Override
        protected void onPostExecute(Document result) {
            Utils.hideProgressDialog(activity);
            if (result == null)
                Log.d(TAG, "onPostExecute: result is null");

            try {
                //Extracting video link form the facebook document
                videoUrl = result.select("meta[property=\"og:video\"]").last().attr("content");
                Log.d(TAG, "onPostExecute: " + videoUrl);
                if (!videoUrl.equals("")) {

                    //downloading the video using download manager
                    FVideo fVideo = startDownload(videoUrl, activity, "facebook_" + System.currentTimeMillis() + ".mp4");
                    downloadVideos.put(fVideo.getDownloadId(), fVideo);
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