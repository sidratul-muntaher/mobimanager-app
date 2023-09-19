package com.iis.mobimanagercocacolaoffline.model;

public class AsyncTaskParams {
   String fileType, fileUrl, fileName;

    public AsyncTaskParams(String fileName, String fileType, String fileUrl) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileUrl = fileUrl;
    }

    public AsyncTaskParams(){}

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
