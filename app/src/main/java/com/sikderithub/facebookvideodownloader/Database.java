package com.sikderithub.facebookvideodownloader;

import android.net.Uri;
import android.util.Log;

import com.sikderithub.facebookvideodownloader.activities.MainActivity;
import com.sikderithub.facebookvideodownloader.models.FVideo;

import java.util.ArrayList;
import java.util.Collections;

import io.paperdb.Paper;

public class Database {
    private static final String KEY_VIDEOS = "downloaded";
    private static final String TAG = "Database";

    public static void addVideo(FVideo video) {
        ArrayList<FVideo> videos = Paper.book().read(KEY_VIDEOS, new ArrayList<>());
        videos.add(video);
        Paper.book().write(KEY_VIDEOS, videos);
        Log.d(TAG, "addVideo: video added video title " + video.getFileName());
        MainActivity.updateListData();
    }

    public static ArrayList<FVideo> getVideos() {
        ArrayList<FVideo> videos = Paper.book().read(KEY_VIDEOS, new ArrayList<>());
        Log.d(TAG, "getVideos: number of video " + videos.size());
        assert videos != null;
        Collections.reverse(videos);
        return videos;
    }

    public static void updateState(long downloadId, int state) {
        ArrayList<FVideo> videos = Paper.book().read(KEY_VIDEOS, new ArrayList<>());
        assert videos != null;
        for (FVideo video : videos) {
            if (video.getDownloadId() == downloadId) {
                Log.d(TAG, "updateState: video found video title " + video.getFileName());
                Log.d(TAG, "updateState: set state " + state);
                video.setState(state);
            }
        }
        Paper.book().write(KEY_VIDEOS, videos);
        MainActivity.updateListData();
    }

    public static void setUri(long downloadId, String uri) {
        ArrayList<FVideo> videos = Paper.book().read(KEY_VIDEOS, new ArrayList<>());
        assert videos != null;
        for (FVideo video : videos) {
            if (video.getDownloadId() == downloadId) {
                Log.d(TAG, "setUri: video found video title " + video.getFileName());
                Log.d(TAG, "setUri: set uri " + uri);
                video.setFileUri(uri);
            }
        }
        Paper.book().write(KEY_VIDEOS, videos);
        MainActivity.updateListData();
    }

    public static FVideo getVideo(long downloadId) {
        ArrayList<FVideo> videos = Paper.book().read(KEY_VIDEOS, new ArrayList<>());

        for (FVideo video : videos) {
            if (video.getDownloadId() == downloadId) {
                return video;
            }
        }
        return null;
    }

}
