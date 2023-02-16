package com.sikderithub.facebookvideodownloader.activities;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.sikderithub.facebookvideodownloader.utils.Constants.downloadVideos;
import static com.sikderithub.facebookvideodownloader.utils.Utils.RootDirectoryFacebook;
import static com.sikderithub.facebookvideodownloader.utils.Utils.addWatermark;
import static com.sikderithub.facebookvideodownloader.utils.Utils.createFileFolder;
import static com.sikderithub.facebookvideodownloader.utils.Utils.startDownload;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sikderithub.facebookvideodownloader.Database;
import com.sikderithub.facebookvideodownloader.R;
import com.sikderithub.facebookvideodownloader.adapters.ListAdapter;
import com.sikderithub.facebookvideodownloader.models.FVideo;
import com.sikderithub.facebookvideodownloader.utils.MyApp;
import com.sikderithub.facebookvideodownloader.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends MyApp implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static ListAdapter adapter;
    private static ArrayList<FVideo> videos;
    private final String strName = "facebook";
    private final String strNameSecond = "fb";
    //Broadcast receiver for download complete.
    private final BroadcastReceiver downloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (downloadVideos.containsKey(id)) {
                Log.d("service", "onReceive: download complete");

                FVideo fVideo = downloadVideos.get(id);
                Database.updateState(id, FVideo.PROCESSING);

                assert fVideo != null;
                addWatermark(context, fVideo,
                        fVideo.getOutputPath(),
                        Environment.getExternalStorageDirectory() +
                                "/Download" + RootDirectoryFacebook);


                downloadVideos.remove(id);
            }
        }
    };
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private EditText txtLink;
    private TextView btnDownloaded, btnPest;
    private RecyclerView downloadList;
    private ImageView imageMenu;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ClipboardManager clipBoard;
    private MainActivity activity;

    /**
     * this function update the listAdapter data form the database
     */
    public static void updateListData() {
        videos = Database.getVideos();
        adapter.setVideos(videos);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(downloadComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        txtLink = findViewById(R.id.tv_past_link);
        btnDownloaded = findViewById(R.id.btn_download);
        btnPest = findViewById(R.id.btn_paste);
        downloadList = findViewById(R.id.rv_download_list);

        // Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_View);
        imageMenu = findViewById(R.id.imageMenu);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();


        downloadList.setLayoutManager(new LinearLayoutManager(this));
        //Item click listener for download list
        adapter = new ListAdapter(this, video -> {
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

                    //Downloaded video play into video player
                    File file = new File(location);
                    if (file.exists()) {
                        Uri uri = Uri.parse(location);
                        Intent intent1 = new Intent(Intent.ACTION_VIEW);
                        intent1.setDataAndType(uri, "video/*");
                        startActivity(intent1);
                    } else {

                        //File doesn't exists
                        Toast.makeText(getApplicationContext(), "File doesn't exists", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "onItemClickListener: file " + file.getPath());

                        //Delete the video instance from the list
                        Database.deleteAVideo(video.getDownloadId());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadComplete);

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    /**
     * Initialize views and item click listerner
     */
    @SuppressLint("NonConstantResourceId")
    private void initViews() {
        clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        btnPest.setOnClickListener(this);

        btnDownloaded.setOnClickListener(this);

        //Handel the navigation view click
        navigationView.setNavigationItemSelectedListener(item -> {
            Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
            String address = "";
            String title = "";

            switch (item.getItemId()) {
                case R.id.nav_about_us:
                    address = "www.youtube.com/";
                    title = "About Us";
                    break;
                case R.id.nav_t_c:
                    address = "www.facebook.com";
                    title = "Terms & Condition";
                    break;
                case R.id.nav_contract_us:
                    address = "";
                    title = "Contract Us";
                    break;
                case R.id.nav_privacy_policy:
                    address = "";
                    title = "Privacy Policy";
                    break;
            }

            intent.putExtra(WebViewActivity.KEY_TITLE, title);
            intent.putExtra(WebViewActivity.KEY_WEB_ADDRESS, address);
            startActivity(intent);
            return false;
        });

        //Opening the drawer when image button on action bar is clicked
        imageMenu.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int adapterPosition = viewHolder.getAdapterPosition();
                Database.deleteAVideo(videos.get(adapterPosition).getDownloadId());
            }
        }).attachToRecyclerView(downloadList);
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

    @SuppressLint("NonConstantResourceId")
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
            if (result == null) {
                Log.d(TAG, "onPostExecute: result is null");
                Toast.makeText(getApplicationContext(), "opps! video not found", Toast.LENGTH_LONG).show();
                return;
            }


            try {
                //Extracting video link form the facebook document
                String videoUrl = result.select("meta[property=\"og:video\"]").last().attr("content");
                Log.d(TAG, "onPostExecute: " + videoUrl);
                if (!videoUrl.equals("")) {

                    //downloading the video using download manager
                    FVideo fVideo = startDownload(videoUrl, activity, "facebook_" + System.currentTimeMillis() + ".mp4");
                    downloadVideos.put(fVideo.getDownloadId(), fVideo);
                    txtLink.setText("");
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.d(TAG, "onPostExecute: error!!!" + e);
            }
        }
    }


}