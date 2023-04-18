package com.iis.mobimanager2.adapter;

import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iis.mobimanager2.R;
import com.iis.mobimanager2.model.AsyncTaskParams;
import com.iis.mobimanager2.model.NotificationFile;
import com.iis.mobimanager2.utils.AppUtil;
import com.iis.mobimanager2.utils.FileDownloaderAsyncTaskForHTTPS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<NotificationFile> notificationFileArrayList;
    Context mContext;
    boolean downloaded = false;
    String downloadedFile;

    public FilesAdapter(ArrayList<NotificationFile> fileArrayList, Context context) {
        notificationFileArrayList = fileArrayList;
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
//        switch (viewType) {
//            case NotificationFile.NEW_FILE:
//                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_notification_item, viewGroup, false);
//                return new FilesViewHolder(view);
//            case NotificationFile.DOWNLODED_FILE:
//                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_notification_item, viewGroup, false);
//                return new DownLoadedFilesViewHolder(view);
//            break;
//        }
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_notification_item, viewGroup, false);
        return new FilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        final NotificationFile notificationFile = notificationFileArrayList.get(position);
        String fileName = notificationFile.getFileName();
        String fileDate = notificationFile.getFileDate();
        String fileSize = notificationFile.getFileSize();
        String details = notificationFile.getDescription();
        String fileUrl = notificationFile.getDownloadUrl();
        String fileType = notificationFile.getFileType();

        FilesViewHolder filesViewHolder = (FilesViewHolder) viewHolder;
        if (fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg")) {
            filesViewHolder.ivFileType.setImageResource(R.drawable.jpg_color);

        } else if (fileType.equalsIgnoreCase("pdf")) {
            filesViewHolder.ivFileType.setImageResource(R.drawable.pdf_color);

        } else if (fileType.equalsIgnoreCase("mp3")) {
            filesViewHolder.ivFileType.setImageResource(R.drawable.mp3_color);

        } else if (fileType.equalsIgnoreCase("mp4")) {
            filesViewHolder.ivFileType.setImageResource(R.drawable.mp4_color);

        }else if (fileType.equalsIgnoreCase("apk")) {
            filesViewHolder.ivFileType.setImageResource(R.drawable.mp4_color);

        }

        filesViewHolder.tvFileName.setText(fileName);
        filesViewHolder.tvDate.setText("Date: " + fileDate);
        filesViewHolder.tvFileSize.setText("Size: " + fileSize);
        filesViewHolder.llInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   Toast.makeText(mContext,notificationFile.getFileName(),Toast.LENGTH_SHORT).show();
                // show dialog
                showFileDialog(mContext, notificationFile);


            }
        });
        filesViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileDialog(mContext, notificationFile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationFileArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
//        if (notificationFileArrayList.get(position).isDownloaded()) {
//            return NotificationFile.DOWNLODED_FILE;
//        } else {
//            return NotificationFile.NEW_FILE;
//        }
        return 0;
    }

//
//    public static class DownLoadedFilesViewHolder extends RecyclerView.ViewHolder {
//
//        TextView txtType;
//
//        public DownLoadedFilesViewHolder(View itemView) {
//            super(itemView);
//
//            this.txtType = (TextView) itemView.findViewById(R.id.type);
//        }
//    }

    public static class FilesViewHolder extends RecyclerView.ViewHolder {

        ImageView ivFileType;
        TextView tvFileName;
        TextView tvDate;
        TextView tvFileSize;
        LinearLayout llInfo;

        public FilesViewHolder(View itemView) {
            super(itemView);

            this.ivFileType = itemView.findViewById(R.id.ivFileType);
            this.tvFileName = itemView.findViewById(R.id.tvFileName);
            this.tvDate = itemView.findViewById(R.id.tvFileDate);
            this.llInfo = itemView.findViewById(R.id.llInfo);
            this.tvFileSize = itemView.findViewById(R.id.tvFileSize);
        }
    }

    public void showFileDialog(Context context, NotificationFile notificationFile) {

        downloaded = false;
        final String fileName = notificationFile.getFileName();
        String fileDate = notificationFile.getFileDate();
        String fileSize = notificationFile.getFileSize();
        String details = notificationFile.getDescription();
        final String fileUrl = notificationFile.getDownloadUrl();
        final String fileType = notificationFile.getFileType();

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.file_details_dialog);

        TextView tvDialogFileName = dialog.findViewById(R.id.tvDialogFileName);
        TextView tvDialogFileDate = dialog.findViewById(R.id.tvDialogFileDate);
        TextView tvDialogFileSize = dialog.findViewById(R.id.tvDialogFileSize);
        TextView tvDialogFileDetails = dialog.findViewById(R.id.tvDialogFileDetails);
        TextView tvFileOpenOrDownload = dialog.findViewById(R.id.tvFileOpenOrDownload);
        TextView tvDialogFileCancel = dialog.findViewById(R.id.tvDialogFileCancel);

        tvDialogFileName.setText(fileName);
        tvDialogFileDate.setText("Date: " + fileDate);
        tvDialogFileSize.setText("Size: " + fileSize);
        tvDialogFileDetails.setText(details);
        tvFileOpenOrDownload.setText("DOWNLOAD");
        tvDialogFileCancel.setText("CANCEL");
        downloadedFile = fileName + "." + fileType;


        File appSpecificExternalDir = new File(Environment.getExternalStorageDirectory() + "/Download/Mobimanager", downloadedFile);
        if (appSpecificExternalDir.exists()) {
            Log.v("_mdm_", appSpecificExternalDir.getAbsolutePath());
            downloaded = true;
            tvFileOpenOrDownload.setText("OPEN");
        } else {
            Log.v("_mdm_", "null file");
            downloaded = false;
        }

        tvFileOpenOrDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                File file = AppUtil.getFileStorageDir(mContext, fileType);
                if (!downloaded) {
                    new FileDownloaderAsyncTaskForHTTPS(mContext).execute(new AsyncTaskParams(fileName, fileType, fileUrl));
//                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
//                    request.setDescription(fileName);
//                    request.setTitle("MDM FILE DOWNDLOAD");
//                    request.allowScanningByMediaScanner();
//                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                    request.setDestinationInExternalFilesDir(mContext, AppUtil.getDirName(fileType), downloadedFile);
//                    DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
//                    manager.enqueue(request);
                    dialog.dismiss();
                } else {
                    File appSpecificExternalDir = new File(Environment.getExternalStorageDirectory() + "/Download/Mobimanager", downloadedFile);
                    //File appSpecificExternalDir = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)+
                            //File.separator+downloadedFile);
//                    File appSpecificExternalDir = new File(mContext.getExternalFilesDir(AppUtil.getDirName(fileType)), downloadedFile);
//                    if (appSpecificExternalDir != null) {
//                        Log.v("_tst_", appSpecificExternalDir.getAbsolutePath());
//
//                    } else {
//                        Log.v("_tst_", "null file");
//                    }
                    if (fileType == "crt"){
                        installCertificates(downloadedFile);
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", appSpecificExternalDir), AppUtil.getIntentViewType(fileType));
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        dialog.dismiss();
                        mContext.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(mContext, "No file Viewer Installed", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

        tvDialogFileCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    private void installCertificates(String fileName){
        File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)+
                File.separator+fileName);
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
            Log.d("_mdm_file_", "installCaCertificate error: "+e);
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
