package com.sikderithub.facebookvideodownloader;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.sikderithub.facebookvideodownloader.Utils.RootDirectoryFacebook;
import static com.sikderithub.facebookvideodownloader.Utils.createFileFolder;
import static com.sikderithub.facebookvideodownloader.Utils.startDownload;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;

import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "";
    EditText txtLink;
    TextView btnDownloaded,btnPest;
    private ClipboardManager clipBoard;
    private MainActivity activity;
    private String videoUrl;
    private String strName = "facebook";
    private String strNameSecond = "fb";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLink = findViewById(R.id.link_text);
        btnDownloaded = findViewById(R.id.downloaded_btn);
        btnPest = findViewById(R.id.btn_paste);
        checkPermission();
        initViews();

    }

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

        btnPest.setOnClickListener(view -> {
            pasteText();
        });

        btnDownloaded.setOnClickListener(v -> {
            String ll = txtLink.getText().toString();
            if (ll.equals("")) {
                Utils.setToast(activity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(ll).matches()) {
                Utils.setToast(activity, getResources().getString(R.string.enter_valid_url));
            } else {
                getFacebookData();
            }
        });
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


    class callFacebookData extends AsyncTask<String,Void, Document>{

        Document facebookDocument;

        @Override
        protected Document doInBackground(String... strings) {
            try {
                facebookDocument = Jsoup.connect(strings[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return facebookDocument;
        }

        @Override
        protected void onPostExecute(Document document) {
            Utils.hideProgressDialog(activity);

            try {
                videoUrl = document.select("meta[property=\"og:video\"]").last().attr("content");
                if (!videoUrl.equals("")){
                   Utils.startDownload(videoUrl,RootDirectoryFacebook,activity,"Facebook_"+ System.currentTimeMillis() + ".mp4");
                   videoUrl = "";
                   txtLink.setText("");

                    Toast.makeText(activity, "Fuck  You", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}