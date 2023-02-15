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

    /**
     * Add a video to the paper db
     * @param video downloaded video
     */
    public static void addVideo(FVideo video) {
        ArrayList<FVideo> videos = Paper.book().read(KEY_VIDEOS, new ArrayList<>());
        videos.add(video);
        Paper.book().write(KEY_VIDEOS, videos);
        Log.d(TAG, "addVideo: video added video title " + video.getFileName());
        MainActivity.updateListData();
    }

    /**
     * get all video
     * @return the list of videos
     */
    public static ArrayList<FVideo> getVideos() {
        ArrayList<FVideo> videos = Paper.book().read(KEY_VIDEOS, new ArrayList<>());
        Log.d(TAG, "getVideos: number of video " + videos.size());
        assert videos != null;
        Collections.reverse(videos);
        return videos;
    }

    /**
     *update the state
     * called when a video is in download state or going to processing state
     * or processing to complete state
     * @param downloadId video download id
     * @param state downloading, processing, complete
     */
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

    /**
     * setting the file uri location
     * called when download is complete
     * @param downloadId video download id
     * @param uri file location
     */
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

    /**
     * @param downloadId video download id
     * @return a video according have that download id
     */
    public static FVideo getVideo(long downloadId) {
        ArrayList<FVideo> videos = Paper.book().read(KEY_VIDEOS, new ArrayList<>());

        for (FVideo video : videos) {
            if (video.getDownloadId() == downloadId) {
                return video;
            }
        }
        return null;
    }

    /**
     * Delete a video instance form the paper db also the download list
     * but will not download form the file
     * @param videoId video download id
     */
    public static void deleteAVideo(Long videoId){
        ArrayList<FVideo> videos = Paper.book().read(KEY_VIDEOS, new ArrayList<>());

        for (FVideo video: videos){
            if (video.getDownloadId() == videoId){
                videos.remove(video);
            }
        }
        Paper.book().write(KEY_VIDEOS, videos);
        MainActivity.updateListData();
    }
}
