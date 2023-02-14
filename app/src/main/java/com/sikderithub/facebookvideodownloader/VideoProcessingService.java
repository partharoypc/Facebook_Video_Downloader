package com.sikderithub.facebookvideodownloader;

import static com.sikderithub.facebookvideodownloader.utils.Constants.downloadVideos;
import static com.sikderithub.facebookvideodownloader.utils.Utils.RootDirectoryFacebook;
import static com.sikderithub.facebookvideodownloader.utils.Utils.addWatermark;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.sikderithub.facebookvideodownloader.activities.MainActivity;
import com.sikderithub.facebookvideodownloader.models.FVideo;

public class VideoProcessingService extends Service {
    private static final String TAG = "VideoProcessingService";

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


    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(downloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(downloadComplete);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service is starting", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, VideoProcessingService.class);
    }
}
