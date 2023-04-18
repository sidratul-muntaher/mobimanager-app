package com.iis.mobimanager2.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreference {

    private static final String PREFS_NAME = "AppPreferences";

    private static final String NETWORK_RESTRICTION_TYPE = "network_restriction";
    private static final String DATA_USAGE_TYPE = "data_usage_type";
    private static final String DATA_USAGE_TOTAL_LIMIT = "data_usage_total_limit";
    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";
    private static final String DAILY_LIMIT = "daily_limit";
    private static final String MONTHLY_LIMIT = "monthly_limit";

    private static final String IMEI_ONE = "IMEI_ONE";
    private static final String IMEI_TWO = "IMEI_TWO";

    private static final boolean SEND_TOKEN = true;
    // MONTHLY_LIMIT

    public static String getMonthlyLimit(final Context context) {
        return context.getSharedPreferences(AppPreference.PREFS_NAME,
                Context.MODE_PRIVATE).getString(AppPreference.MONTHLY_LIMIT, "");
    }

    public static void setMonthlyLimit(final Context context, final String type) {
        final SharedPreferences prefs = context.getSharedPreferences(
                AppPreference.PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AppPreference.MONTHLY_LIMIT, type);
        editor.apply();
    }

    // DAILY_LIMIT

    public static String getDailyLimit(final Context context) {
        return context.getSharedPreferences(AppPreference.PREFS_NAME,
                Context.MODE_PRIVATE).getString(AppPreference.DAILY_LIMIT, "");
    }

    public static void setDailyLimit(final Context context, final String type) {
        final SharedPreferences prefs = context.getSharedPreferences(
                AppPreference.PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AppPreference.DAILY_LIMIT, type);
        editor.apply();
    }

    // END_DATE

    public static String getEndDate(final Context context) {
        return context.getSharedPreferences(AppPreference.PREFS_NAME,
                Context.MODE_PRIVATE).getString(AppPreference.END_DATE, "");
    }

    public static void setEndDate(final Context context, final String type) {
        final SharedPreferences prefs = context.getSharedPreferences(
                AppPreference.PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AppPreference.END_DATE, type);
        editor.apply();
    }

    // START_DATE

    public static String getStartDate(final Context context) {
        return context.getSharedPreferences(AppPreference.PREFS_NAME,
                Context.MODE_PRIVATE).getString(AppPreference.START_DATE, "");
    }

    public static void setStartDate(final Context context, final String type) {
        final SharedPreferences prefs = context.getSharedPreferences(
                AppPreference.PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AppPreference.START_DATE, type);
        editor.apply();
    }


    // DATA_USAGE_TOTAL_LIMIT

    public static String getDataUsageTotalLimit(final Context context) {
        return context.getSharedPreferences(AppPreference.PREFS_NAME,
                Context.MODE_PRIVATE).getString(AppPreference.DATA_USAGE_TOTAL_LIMIT, "");
    }

    public static void setDataUsageTotalLimit(final Context context, final String type) {
        final SharedPreferences prefs = context.getSharedPreferences(
                AppPreference.PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AppPreference.DATA_USAGE_TOTAL_LIMIT, type);
        editor.apply();
    }


    //data_usage_type
    public static String getDataUsageType(final Context context) {
        return context.getSharedPreferences(AppPreference.PREFS_NAME,
                Context.MODE_PRIVATE).getString(AppPreference.DATA_USAGE_TYPE, "");
    }

    public static void setDataUsageType(final Context context, final String type) {
        final SharedPreferences prefs = context.getSharedPreferences(
                AppPreference.PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AppPreference.DATA_USAGE_TYPE, type);
        editor.apply();
    }

    //network_restriction
    public static String getNetworkRestrictionType(final Context context) {
        return context.getSharedPreferences(AppPreference.PREFS_NAME,
                Context.MODE_PRIVATE).getString(AppPreference.NETWORK_RESTRICTION_TYPE, "");
    }

    public static void setNetworkRestrictionType(final Context context, final String type) {
        final SharedPreferences prefs = context.getSharedPreferences(
                AppPreference.PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AppPreference.NETWORK_RESTRICTION_TYPE, type);
        editor.apply();
    }


    public static String getImeiOne(final Context context) {
        return context.getSharedPreferences(AppPreference.PREFS_NAME,
                Context.MODE_PRIVATE).getString(AppPreference.IMEI_ONE, "");
    }

    public static void setImeiOne(final Context context, final String imeiOne) {
        final SharedPreferences prefs = context.getSharedPreferences(
                AppPreference.PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AppPreference.IMEI_ONE, imeiOne);
        editor.apply();
    }

    public static String getImeiTwo(final Context context) {
        return context.getSharedPreferences(AppPreference.PREFS_NAME,
                Context.MODE_PRIVATE).getString(AppPreference.IMEI_TWO, "");
    }

    public static void setImeiTwo(final Context context, final String imeiTwo) {
        final SharedPreferences prefs = context.getSharedPreferences(
                AppPreference.PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AppPreference.IMEI_TWO, imeiTwo);
        editor.apply();
    }


    public static String getOcrImeiOne(final Context context) {
        return context.getSharedPreferences(AppPreference.PREFS_NAME,
                Context.MODE_PRIVATE).getString(AppPreference.IMEI_ONE, "");
    }

    public static void setOcrImeiOne(final Context context, final String imeiOne) {
        final SharedPreferences prefs = context.getSharedPreferences(
                AppPreference.PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AppPreference.IMEI_ONE, imeiOne);
        editor.apply();
    }

    public static String getOcrImeiTwo(final Context context) {
        return context.getSharedPreferences(AppPreference.PREFS_NAME,
                Context.MODE_PRIVATE).getString(AppPreference.IMEI_TWO, "");
    }

    public static void setOcrImeiTwo(final Context context, final String imeiTwo) {
        final SharedPreferences prefs = context.getSharedPreferences(
                AppPreference.PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AppPreference.IMEI_TWO, imeiTwo);
        editor.apply();
    }


}
