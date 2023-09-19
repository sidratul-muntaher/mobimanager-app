package com.iis.mobimanagercocacolaoffline;

import com.iis.mobimanagercocacolaoffline.model.X;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IApi {
    @GET("/MobiManager/api/exertnal-app-list")
    Call<X> getAppData();
}
