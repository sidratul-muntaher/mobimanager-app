package com.iis.mobimanager2.utils;

public class ApiConstants {
    public static final String BASE_URL_PREF_KEY = "server_url";
//    public static final String HOST_NAME = "43.224.110.67";

//    public static final String BASE_URL = "http://43.224.110.67:8080";

    //for walton-mobimanager https
    public static final String BASE_HTTPS_URL = "https://43.224.110.67:8443";
    private static final String GET_EMPLOYEE_INFO = "/MobiManager/api/get-employee/";
    private static final String GET_SAVE_LOCATION_INFO = "/MobiManager/api/save-location";
    private static final String STATUS_INFO_API = "/MobiManager/api/update-device-status/";
    private static final String TOKEN_API = "/MobiManager/api/save-token";
    private static final String DATA_USES_API = "/MobiManager/api/save-data-usage";


    public static final String HTTPS_BASE_URL_SUBMIT_DEVICE_ID = "https://43.224.110.67:8443/MobiManager/api/update-androidId/";

    public static String getEmployeeInfoApi() {
        return BASE_HTTPS_URL + GET_EMPLOYEE_INFO;
    }
    public static String getSaveLocationApi(){
        return BASE_HTTPS_URL+GET_SAVE_LOCATION_INFO;
    }
    public static String getStatusInfoApi(){
        return BASE_HTTPS_URL+STATUS_INFO_API;
    }
    public static String getTokenApi(){
        return BASE_HTTPS_URL+TOKEN_API;
    }
    public static String getDataUsesApi(){
        return BASE_HTTPS_URL+DATA_USES_API;
    }

}
