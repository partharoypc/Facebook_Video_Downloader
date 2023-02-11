package com.sikderithub.facebookvideodownloader.models;

public class DownloadVideo {
    private String outputPath;
    private String fileName;
    private long downloadId;

    public DownloadVideo(String outputPath, String fileName, long downloadId) {
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
}
