package com.example.csi.mActivityManager;

public class FileItem {
    private String fileName;
    private String fileUrl;
    private String fileDescription;
    private String fileDate;

    public FileItem(String fileName, String fileUrl, String fileDescription, String fileDate) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileDescription = fileDescription;
        this.fileDate = fileDate;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileDescription() {
        return fileDescription;
    }

    public String getFileDate() {
        return fileDate;
    }
}

