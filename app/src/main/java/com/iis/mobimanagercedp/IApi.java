package com.iis.mobimanagercedp;

import com.iis.mobimanagercedp.model.AppList;
import com.iis.mobimanagercedp.model.X;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IApi {
    @GET("/api/exertnal-app-list")
    Call<X> getAppData();
}
