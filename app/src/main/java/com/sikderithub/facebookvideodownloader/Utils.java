package com.sikderithub.facebookvideodownloader;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
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


import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.sikderithub.facebookvideodownloader.models.DownloadVideo;

import java.io.File;
import java.io.FileOutputStream;


public class Utils {
    private static final String TAG = "Utils";
    public static Dialog customDialog;
    private Context context;
    private static final String FFMPEG_BINARY = "ffmpeg";


    public static String RootDirectoryFacebook = "/Facebook Video Downloader/";
    public static File RootDirectoryFacebookShow = new File(Environment.getExternalStorageDirectory() + "/Download/Facebook Video Downloader");

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

    public static DownloadVideo startDownload(String downloadPath, String destinationPath, Context context, String fileName) {
        setToast(context, context.getResources().getString(R.string.download_started));
        Uri uri = Uri.parse(downloadPath); // Path where you want to download file.
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);  // Tell on which network you want to download file.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);  // This will show notification on top when downloading the file.
        request.setTitle(fileName + ""); // Title for notification.
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, destinationPath + fileName);  // Storage directory path
        long downloadId = ((DownloadManager) context.getSystemService(DOWNLOAD_SERVICE)).enqueue(request); // This will start downloading

        Log.d("videoProcess", "startDownload: " + DIRECTORY_DOWNLOADS + destinationPath + fileName);

        try {
            MediaScannerConnection.scanFile(context, new String[]{new File(DIRECTORY_DOWNLOADS + "/" + destinationPath + fileName).getAbsolutePath()},
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.d("videoProcess", "onScanCompleted: " + path);
                        }
                    });


        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DownloadVideo(DIRECTORY_DOWNLOADS + destinationPath + fileName,
                fileName, downloadId);
    }

    private static void saveWatermark(Context context) {
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.watermark_white);
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File file = new File(extStorageDirectory, "watermark.png");
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
        Log.d("videoProcess", "addWatermark: is called");
        Log.d("videoProcess", "addWatermark: inputPath: " + inputPath);
        Log.d("videoProcess", "addWatermark: outputPath" + outputPath);
        saveWatermark(context);

        String watermarkPath = Environment.getExternalStorageDirectory() + "/watermark.png";
        Log.d("videoProcess", "addWatermark: watermark path " + watermarkPath);

        String[] command = new String[]{
                FFMPEG_BINARY,
                "-i", inputPath,
                "-i", watermarkPath,
                "-filter_complex", "overlay=10:10",
                "-c:a", "copy",
                outputPath
        };
        //ffmpeg -i test.mp4 -i watermark.png -filter_complex "overlay=10:10" test1.mp4
        String[] c = new String[]{
                "ffmpeg", "-i",
                inputPath, "-i",
                watermarkPath, "-filter_complex",
                "overlay=10:10",
                outputPath
        };

        String[] array = new String[]{
                "-y", "-i", inputPath,
                "-i", watermarkPath,
                "-filter_complex",
                "[0:v][1:v]overlay=main_w-overlay_w-10:10",
                "-codec:a", "copy", outputPath};

        //uses for bravobit-ffmpeg
        /*
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        fFmpeg.execute(command, new ExecuteBinaryResponseHandler(){
            @Override
            public void onSuccess(String message) {
                super.onSuccess(message);
                Toast.makeText(context, "success", Toast.LENGTH_LONG).show();
                Log.d("videoProcess", "onSuccess: ");
            }

            @Override
            public void onFinish() {
                Toast.makeText(context, "finish", Toast.LENGTH_LONG).show();
                Log.d("videoProcess", "onFinish: ");
            }

            @Override
            public void onFailure(String message) {
                super.onFailure(message);
                Log.d("videoProcess", "onFailure: " + message);
            }
        });*/

        //uses for com.writingminds:FFmpegAndroid:0.3.2

        FFmpeg fFmpeg = FFmpeg.getInstance(context);

        try {
            fFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    super.onFailure();
                    Log.d("videoProcess", "onFailure: loadBinary");
                }

                @Override
                public void onSuccess() {
                    super.onSuccess();
                    Log.d("videoProcess", "onSuccess: loadBinary");
                }

                @Override
                public void onStart() {
                    super.onStart();
                    Log.d("videoProcess", "onStart: loadBinary");

                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    Log.d("videoProcess", "onFinish: loadBinary");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            throw new RuntimeException(e);
        }

        try {
            fFmpeg.execute(c, new ExecuteBinaryResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    super.onSuccess(message);
                    Log.d("videoProcess", "onSuccess: execute");
                }

                @Override
                public void onProgress(String message) {
                    super.onProgress(message);
                }

                @Override
                public void onFailure(String message) {
                    super.onFailure(message);
                    Log.d("videoProcess", "onFailure: execute");
                }

                @Override
                public void onStart() {
                    super.onStart();
                    Log.d("videoProcess", "onStart: execute");
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    Log.d("videoProcess", "onFinish: execute");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            throw new RuntimeException(e);
        }


        String textOverlay = "-y -i" +
                inputPath + "-vf drawtext=”fontsize=30:fontfile=cute.ttf:text=’GFG'”" +
                ":x=w-tw-10:y=h-th-10 -c:v libx264 -preset ultrafast " +
                outputPath;

        /*
        FFmpeg.executeAsync(command, new ExecuteCallback(){

            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("videoProcess", "apply: success");

                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.d("videoProcess", "Async command execution cancelled by user.");
                } else {
                    Log.d("videoPorcess", String.format("Async command execution failed with returnCode=%d.", returnCode));
                }
            }
        });*/


    }

}
