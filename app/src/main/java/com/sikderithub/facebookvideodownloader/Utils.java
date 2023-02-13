package com.sikderithub.facebookvideodownloader;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getDataDirectory;
import static android.os.Environment.getExternalStorageDirectory;

import static com.arthenica.ffmpegkit.Packages.getPackageName;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import android.os.Build;


import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.sikderithub.facebookvideodownloader.models.DownloadVideo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;


public class Utils {
    private static final String TAG = "Utils";
    public static Dialog customDialog;
    private Context context;
    private static final String FFMPEG_BINARY = "ffmpeg";


    public static String RootDirectoryFacebook = "/Facebook_Video_Downloader/";
    public static final String DATA_DIRECTORY= getExternalStorageDirectory() +
            "/Android/data/com.sikderithub.facebookvideodownloader/files/data" +
            RootDirectoryFacebook;
    public static File RootDirectoryFacebookShow = new File(Environment.getExternalStorageDirectory() + "/Download" + RootDirectoryFacebook);

    public Utils(Context mContext) {
        context = mContext;
    }

    public static void setToast(Context mContext, String str) {
        Toast toast = Toast.makeText(mContext, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void createFileFolder() {
        if (!RootDirectoryFacebookShow.exists()) {
            RootDirectoryFacebookShow.mkdirs();
        }
    }

    public static void showProgressDialog(Activity activity) {
        System.out.println("Show");
        if (customDialog != null) {
            customDialog.dismiss();
            customDialog = null;
        }
        customDialog = new Dialog(activity);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View mView = inflater.inflate(R.layout.progress_dialog, null);
        customDialog.setCancelable(false);
        customDialog.setContentView(mView);
        if (!customDialog.isShowing() && !activity.isFinishing()) {
            customDialog.show();
        }
    }

    public static void hideProgressDialog(Activity activity) {
        System.out.println("Hide");
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.dismiss();
        }
    }

    public static DownloadVideo startDownload(String downloadPath, Context context, String fileName) {
        setToast(context, context.getResources().getString(R.string.download_started));
        Uri uri = Uri.parse(downloadPath); // Path where you want to download file.
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);  // Tell on which network you want to download file.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);  // This will show notification on top when downloading the file.
        request.setTitle(fileName + ""); // Title for notification.
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalFilesDir(context,
                Environment.getDataDirectory().getPath() + RootDirectoryFacebook , fileName);// Storage directory path
        long downloadId = ((DownloadManager) context.getSystemService(DOWNLOAD_SERVICE)).enqueue(request); // This will start downloading

        Log.d(TAG, "startDownload: " + Environment.getDataDirectory().getPath() + RootDirectoryFacebook + fileName);

        try {
            MediaScannerConnection.scanFile(context, new String[]{new File(DIRECTORY_DOWNLOADS + "/" + fileName).getAbsolutePath()},
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.d("videoProcess", "onScanCompleted: " + path);
                        }
                    });


        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DownloadVideo(DATA_DIRECTORY + fileName,
                fileName, downloadId);
    }

    private static void saveWatermark(Context context) {
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.watermark_white);
        File file = new File(DATA_DIRECTORY, "watermark.png");
        if (!file.exists()) {
            try {
                FileOutputStream outStream = new FileOutputStream(file);
                bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addWatermark(Context context, String inputPath, String outputPath) {

        saveWatermark(context);
        String watermarkPath = DATA_DIRECTORY + "/watermark.png";
        String outFileName = "watermark_" + System.currentTimeMillis()+".mp4";

        /* this is working command
        String command = "-i "+inputPath+ " -i "+watermarkPath+" -filter_complex overlay=10:10 -c:a copy "+
                outputPath + outFileName;
        */
        String command = "-i "+inputPath+ " -i "+watermarkPath+
                " -filter_complex \"[1]scale=50:25[newoverlay], " +
                "[0][newoverlay]overlay=main_w-overlay_w-5:main_h-overlay_h-10\" " +
                "-preset slow " +
                "-c:a copy " +
                outputPath + outFileName;

        Log.d(TAG, "addWatermark: command " + command);

        FFmpegSession session = FFmpegKit.execute(command);
        if (ReturnCode.isSuccess(session.getReturnCode())) {

            // SUCCESS
            Log.d(TAG, "addWatermark: success");

            File cacheFile = new File(inputPath);
            if (cacheFile.exists())
                cacheFile.delete();

        } else if (ReturnCode.isCancel(session.getReturnCode())) {

            // CANCEL
            Log.d(TAG, "addWatermark: cancel");

        } else {

            // FAILURE
            Log.d(TAG, String.format("Command failed with state %s and rc %s.%s", session.getState(), session.getReturnCode(), session.getFailStackTrace()));

        }
    }

    public static void fileDir(Context context){
        ContextWrapper contextWrapper = new ContextWrapper(context);
        Log.d("aaaaaa", "fileDir: " + DATA_DIRECTORY);
    }





}
