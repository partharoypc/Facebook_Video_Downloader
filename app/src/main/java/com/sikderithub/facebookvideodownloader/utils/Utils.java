package com.sikderithub.facebookvideodownloader.utils;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStorageDirectory;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;


import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.sikderithub.facebookvideodownloader.Database;
import com.sikderithub.facebookvideodownloader.R;
import com.sikderithub.facebookvideodownloader.activities.MainActivity;
import com.sikderithub.facebookvideodownloader.models.FVideo;

import java.io.File;
import java.io.FileOutputStream;


public class Utils {
    private static final String TAG = "Utils";
    public static Dialog customDialog;
    private Context context;
    private static final String FFMPEG_BINARY = "ffmpeg";


    public static String RootDirectoryFacebook = "/Facebook_Video_Downloader/";
    public static final String DATA_DIRECTORY = getExternalStorageDirectory() +
            "/Android/data/com.sikderithub.facebookvideodownloader/files/data" +
            RootDirectoryFacebook;
    public static File RootDirectoryFacebookShow = new File(Environment.getExternalStorageDirectory() + "/Download" + RootDirectoryFacebook);

    public Utils(Context mContext) {
        context = mContext;
    }

    public static void setToast(Context mContext, String str) {
        Toast toast = Toast.makeText(mContext, str, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Create directory facebook video downloader in download directory
     */
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

    /**
     * start download using download manager
     * @param downloadPath download location
     * @param context
     * @param fileName filename ex. facebook_16737..
     * @return FVideo object
     */
    public static FVideo startDownload(String downloadPath, Context context, String fileName) {
        setToast(context, context.getResources().getString(R.string.download_started));
        Uri uri = Uri.parse(downloadPath); // Path where you want to download file.
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);  // Tell on which network you want to download file.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);  // This will show notification on top when downloading the file.
        request.setTitle(fileName + ""); // Title for notification.
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalFilesDir(context,
                Environment.getDataDirectory().getPath() + RootDirectoryFacebook, fileName);// Storage directory path
        long downloadId = ((DownloadManager) context.getSystemService(DOWNLOAD_SERVICE)).enqueue(request); // This will start downloading

        //Creating a video object to track donload is completed
        FVideo video = new FVideo(DATA_DIRECTORY, fileName, downloadId);
        video.setState(FVideo.DOWNLOADING);

        Database.addVideo(video);

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

        return new FVideo(DATA_DIRECTORY + fileName,
                fileName, downloadId);
    }

    /**
     * saving watermark image to the app data directory
     *
     * @param context takes application context
     */
    static void saveWatermark(Context context) {
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

    /**
     * Adding watermark on the video using FFmpeg
     * Called by the broadcast receiver when download complete
     *
     * @param context    application context
     * @param inputPath  is the appdata directory
     * @param outputPath is the download directory
     */
    public static void addWatermark(Context context, FVideo video,
                                    String inputPath, String outputPath) {

        saveWatermark(context);
        String watermarkPath = DATA_DIRECTORY + "/watermark.png";
        String outFileName = video.getFileName();

        String command = "-i " + inputPath + " -i " + watermarkPath +
                " -filter_complex \"[1]scale=50:25[newoverlay], " +
                "[0][newoverlay]overlay=main_w-overlay_w-5:main_h-overlay_h-10\" " +
                "-preset ultrafast " +
                "-c:a copy " +
                outputPath + outFileName;

        Log.d(TAG, "addWatermark: command " + command);

        FFmpegKit.executeAsync(command, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {

                // SUCCESS
                Log.d(TAG, "addWatermark: success");

                File cacheFile = new File(inputPath);
                if (cacheFile.exists())
                    cacheFile.delete();


                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.post(() -> {
                    Database.updateState(video.getDownloadId(), FVideo.COMPLETE);

                    String path = outputPath + outFileName;
                    Log.d(TAG, "addWatermark: uri" + path);
                    Database.setUri(video.getDownloadId(), path);
                });

            } else if (ReturnCode.isCancel(session.getReturnCode())) {

                // CANCEL
                Log.d(TAG, "addWatermark: cancel");

            } else {

                // FAILURE
                Log.d(TAG, String.format("Command failed with state %s and rc %s.%s", session.getState(), session.getReturnCode(), session.getFailStackTrace()));

            }


        });
    }

}
