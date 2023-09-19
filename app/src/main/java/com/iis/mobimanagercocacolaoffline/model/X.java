package com.iis.mobimanagercocacolaoffline.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class X {
    @SerializedName("data")
    @Expose
    ArrayList<AppList> appLists ;

    public ArrayList<AppList> getAppLists() {
        return appLists;
    }

    public void setAppLists(ArrayList<AppList> appLists) {
        this.appLists = appLists;
    }
}
