package com.sikderithub.facebookvideodownloader;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.sikderithub.facebookvideodownloader.Utils.createFileFolder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;

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

        createFileFolder();
        initViews();
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


    private void getFacebookData() {
        try {
            createFileFolder();
            URL url = new URL(txtLink.getText().toString());

            String host = url.getHost();
            if (host.contains(strName) || host.contains(strNameSecond)) {
                Utils.showProgressDialog(activity);
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
                getFacebookUserData();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getFacebookUserData() {

        Toast.makeText(activity, "Hello   Madam", Toast.LENGTH_SHORT).show();

    }


}