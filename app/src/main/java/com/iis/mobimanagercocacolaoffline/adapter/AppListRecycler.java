package com.iis.mobimanagercocacolaoffline.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.iis.mobimanagercocacolaoffline.BuildConfig;
import com.iis.mobimanagercocacolaoffline.R;
import com.iis.mobimanagercocacolaoffline.model.AppList;
import com.iis.mobimanagercocacolaoffline.utils.FileDownloadTask;

import java.io.File;
import java.util.ArrayList;

public class AppListRecycler extends RecyclerView.Adapter<AppListRecycler.ViewHolder> {
    ArrayList<AppList> appLists;

    public ArrayList<AppList> getAppLists() {
        return appLists;
    }

    public void setAppLists(ArrayList<AppList> appLists) {
        this.appLists = appLists;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    Context context;
    public AppListRecycler(ArrayList<AppList> appLists) {
        this.appLists = appLists;
    }

    @NonNull
    @Override
    public AppListRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_view, parent, false));
    }

    private static final String TAG = "AppListRecycler";
    @Override
    public void onBindViewHolder(@NonNull AppListRecycler.ViewHolder holder, int position) {

        holder.name.setText("Name: "+appLists.get(holder.getAdapterPosition()).getAppName());
        holder.status.setText("Status: "+appLists.get(holder.getAdapterPosition()).getStatus());
        holder.version.setText("Version: "+appLists.get(holder.getAdapterPosition()).getMinVersion());
        Log.e("TAG", "onBindViewHolder: " + appLists.get(holder.getAdapterPosition()).getAppIcon() );
        //Picasso.get().load("https://www.gravatar.com/avatar/c07d8131c0557a12a9121ddda2ef9878?s=64&d=identicon&r=PG&f=y&so-version=2").into(holder.icon);

       /* String packageName = appLists.get(position).getPackageName();

        // Find the installed app
        ApplicationInfo appInfo = getAppInfoByPackageName(context, packageName);

        Drawable icon = null;
        try {
            icon = getContext().getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        holder.icon.setImageDrawable(icon);*/

       // Log.e(TAG, "onBindViewHolder: -------------------------- " + appInfo );
      /*  if (appInfo != null) {
            // Retrieve the app's icon
            Drawable appIcon = appInfo.loadIcon(context.getPackageManager());

            // Display the icon in an ImageView

        }
*/
        holder.icon.setOnClickListener(o->{
            if(appLists.get(position).getStatus().equals("downloaded") || appLists.get(position).getStatus().equals("installed")) {

                String absolutePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + appLists.get(position).getAppName() + ".apk";
                // absolutePath = absolutePath.substring(1);
                Log.e(TAG, "onBindViewHolder: " + absolutePath);
                File apkFile = new File(absolutePath);

                if (apkFile.exists()) {
                    apkFile.setReadable(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // For Android 8.0 and above, you need to request permission to install APKs from unknown sources
                        if (!context.getPackageManager().canRequestPackageInstalls()) {
                            // Request permission to install from unknown sources
                            Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                            permissionIntent.setData(Uri.parse("package:" + context.getPackageName()));
                            Log.e("TAG", "onBindViewHolder: ---- ");
                            //context.startActivityForResult( permissionIntent, 90);
                        } else {
                            // Install the APK
                            installApk(apkFile);
                        }
                    } else {
                        // Install the APK
                        installApk(apkFile);
                    }
                } else {
                    Toast.makeText(context, "APK file not found.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        holder.button.setOnClickListener(i->{
            Log.e(TAG, "onBindViewHolder: 00" );
            deleteFileFromDownloads(appLists.get(position).getAppName() + ".apk");
            appLists.get(position).setStatus("removed");
            Log.e(TAG, "onBindViewHolder: " );
            notifyDataSetChanged();

            FileDownloadTask downloadTask = new FileDownloadTask(new FileDownloadTask.DownloadCallback() {
                @Override
                public void onProgressUpdate(int progress) {
                    appLists.get(position).setStatus(" downloading... " );
                    notifyDataSetChanged();
                }

                @Override
                public void onDownloadComplete() {
                    appLists.get(position).setStatus("downloaded");
                   notifyDataSetChanged();
                   // preferences.edit().putString(a.getPackageName(), value).commit();
                }

                @Override
                public void onDownloadFailed() {
                    appLists.get(position).setStatus("failed");
                    notifyDataSetChanged();
                }
            });

            Log.e("TAG", "startyDownloads: " + getContext().getFilesDir().getAbsolutePath() );
            downloadTask.execute(appLists.get(position).getAppDownloadUrl(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + appLists.get(position).getAppName() + ".apk");

        });
    }

    private ApplicationInfo getAppInfoByPackageName(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean deleteFileFromDownloads(String fileName) {
        try {
            String downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            String filePath = downloadsDirectory + File.separator + fileName;

            File fileToDelete = new File(filePath);

            if (fileToDelete.exists()) {
                if (fileToDelete.delete()) {
                    Log.e(TAG, "deleteFileFromDownloads: " );
                    // File deleted successfully
                    return true;
                } else {
                    // Failed to delete the file
                    return false;
                }
            } else {
                // File does not exist
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle any exceptions that may occur during file deletion
            return false;
        }
    }

    private void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri apkUri = Uri.fromFile(apkFile);
        Log.e(TAG, "installApk: " + apkUri );
        intent.setDataAndType(FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider",
                apkFile), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }
    @Override
    public int getItemCount() {
        return appLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView name, version, status;
        ImageView icon;
        Button button;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.appName);
            version = itemView.findViewById(R.id.appVersion);
            status = itemView.findViewById(R.id.appStatus);
            icon = itemView.findViewById(R.id.icon);
            button = itemView.findViewById(R.id.delete);
        }
    }
}
