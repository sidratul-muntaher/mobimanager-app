package com.iis.mobimanager2.fragment;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.iis.mobimanager2.R;
import com.iis.mobimanager2.adapter.FilesAdapter;
import com.iis.mobimanager2.data.AppDatabaseHelper;
import com.iis.mobimanager2.model.AsyncTaskParams;
import com.iis.mobimanager2.model.NotificationFile;
import com.iis.mobimanager2.utils.FileDownloaderAsyncTaskForHTTPS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FilesFragment extends Fragment {
    RecyclerView rvFiles;
    FilesAdapter filesAdapter;
    ArrayList<NotificationFile> notificationFiles;
    AppDatabaseHelper appDatabaseHelper;
    Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);
//
//        if(getActivity().getIntent().getExtras() != null){
//            message =(Message) getIntent().getSerializableExtra("notification");
//            title = message.getMessageTitle();
//            body = message.getMessageDetails();
//            date = message.getDateTime();
//            imageLink = message.getImageUrl();
//        }else {
//            return;
//        }
        mContext = getContext();
        rvFiles = view.findViewById(R.id.rvFiles);
        rvFiles.setLayoutManager(new LinearLayoutManager(getContext()));
        appDatabaseHelper = new AppDatabaseHelper(getContext());
        notificationFiles = appDatabaseHelper.getAllFilesData();
        filesAdapter = new FilesAdapter(notificationFiles, getContext());
        rvFiles.setAdapter(filesAdapter);
        createDirectory();
        /*String jsonstr = "{\"data\":{\"date\":\"2022-10-06\",\"file_url\":\"https://43.224.110.67:8443/MobiManager-MDM/api/downloadFile1/ADNT_tech.pptx\",\"size\":\"5mb\",\"file_name\":\"ADNT_tech.pptx\",\"description\":\"delete\",\"title\":\"file\",\"type\":\"pptx\"},\"to\":\"APA91bHhGWNxd_Z3lgZghdnjCoV_PCBq1-blW2_Qve4S9itFizCQT3VTEs-ltrt6S1DfdywE5lCPUIOrmTThs7FOdU8vy5OoMGvr3X_ptSkDH30OgvQDrAk\"}";
        try {
            JSONObject jsonObject = new JSONObject(jsonstr);
            Log.d("_mdm_Files", "onCreate "+ jsonObject);

        }catch (JSONException err){
            Log.d("Error", err.toString());
        }*/
        autoDownloadNotificationsFiles(notificationFiles);

        return view;
    }

    private void createDirectory() {

        File mdm_directory = new File(Environment.getExternalStorageDirectory() + "/Download/Mobimanager");

        if(!mdm_directory.exists()){
            mdm_directory.mkdirs();
            Log.d("_mdm_Files", "createDirectory:  Not exists but done now.");
        }else{
            Log.d("_mdm_Files", "createDirectory: exists");

        }

    }

    private void autoDownloadNotificationsFiles(ArrayList<NotificationFile> notificationFiles) {

        for(NotificationFile notificationFile : notificationFiles){

            downloadFile(notificationFile);

        }

    }

    private void downloadFile(NotificationFile notificationFile) {

        final String fileName = notificationFile.getFileName();
        final String fileUrl = notificationFile.getDownloadUrl();
        final String fileType = notificationFile.getFileType();
        String finalFileName =  fileName + "." + fileType;
        File appSpecificExternalDir = new File(Environment.getExternalStorageDirectory() + "/Download/Mobimanager", finalFileName);
        if (!appSpecificExternalDir.exists()) {

            new FileDownloaderAsyncTaskForHTTPS(mContext).execute(new AsyncTaskParams(fileName, fileType, fileUrl));

        }else{
            if (fileType == "crt"){
                installCertificates(finalFileName);
            }
        }


    }

    private void installCertificates(String fileName){
        File file = new File(Environment.getExternalStorageDirectory() + "/Download/Mobimanager/"+fileName);
        Log.d("httpsInstall", "crt file :"+file);

        try {
            final FileInputStream fileInputStream = new FileInputStream(file);
            final InputStream inputStream = fileInputStream;

            DevicePolicyManager dpm = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);

            if(! installCaCertificateN(inputStream, dpm,  null))
            {
                Toast.makeText(mContext,"installCaCert  Failed",Toast.LENGTH_LONG).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("_mdm_file_", "file : "+file.toString());
    }

    private boolean installCaCertificateN(InputStream certificateInputStream, DevicePolicyManager dpm, ComponentName admin) {
        Log.d("_mdm_file_", "called installCaCertificateN");

        byte[] cert = new byte[0];
        try {
            Log.d("_mdm_file_", "trying to install ca file");
            cert = new byte[certificateInputStream.available()];
            certificateInputStream.read(cert);
            dpm.installCaCert(null, cert);
        } catch (IOException e) {
            Log.d("_mdm_file_", "installCaCertificate error: " + e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
