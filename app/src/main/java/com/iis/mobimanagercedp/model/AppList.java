package com.iis.mobimanagercedp.model;

public class AppList {
    public String appName;
    public String packageName;
    public String appIcon;
    public String minVersion;
    public String appDownloadUrl;

    String status;

    public AppList(String appDownloadUrl) {
        this.appDownloadUrl = appDownloadUrl;
    }

    public AppList() {

    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(String appIcon) {
        this.appIcon = appIcon;
    }

    public String getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }

    public String getAppDownloadUrl() {
        return appDownloadUrl;
    }

    public void setAppDownloadUrl(String appDownloadUrl) {
        this.appDownloadUrl = appDownloadUrl;
    }
}
