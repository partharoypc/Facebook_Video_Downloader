package com.sikderithub.facebookvideodownloader.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public class FVideo {
    public static final int DOWNLOADING = 1;
    public static final int PROCESSING = 2;
    public static final int COMPLETE = 3;

    //Initial download path
    private String outputPath;
    private String fileName;
    private long downloadId;
    //Where file actually saved in memory after processing
    private String fileUri;
    private int State;
    private final boolean isWatermarked;
    private byte[] thumbnail;

    public Bitmap getThumbnail() {
        return BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
    }

    public void setThumbnail(Bitmap thumbnail) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.PNG, 0, stream);

        this.thumbnail = stream.toByteArray();
    }

    public FVideo(String outputPath, String fileName, long downloadId, boolean isWatermarked) {
        this.outputPath = outputPath;
        this.fileName = fileName;
        this.downloadId = downloadId;
        this.isWatermarked = isWatermarked;
    }

    public boolean isWatermarked() {
        return isWatermarked;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public int getState() {
        return State;
    }

    public void setState(int state) {
        State = state;
    }

}
