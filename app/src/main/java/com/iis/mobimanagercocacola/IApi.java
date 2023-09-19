package com.iis.mobimanagercocacola;

import com.iis.mobimanagercocacola.model.X;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IApi {
    @GET("/MobiManager/api/exertnal-app-list")
    Call<X> getAppData();
}
