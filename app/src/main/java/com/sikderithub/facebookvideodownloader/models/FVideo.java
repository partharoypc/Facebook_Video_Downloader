package com.sikderithub.facebookvideodownloader.models;

import android.net.Uri;

public class FVideo {
    public static final int DOWNLOADING = 1;
    public static final int PROCESSING = 2;
    public static final int COMPLETE = 3;

    private String outputPath;
    private String fileName;
    private long downloadId;
    private String fileUri;
    private int State;

    public FVideo(String outputPath, String fileName, long downloadId) {
        this.outputPath = outputPath;
        this.fileName = fileName;
        this.downloadId = downloadId;
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
