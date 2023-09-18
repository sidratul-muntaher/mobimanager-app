package com.iis.mobimanagercedp.adapter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iis.mobimanagercedp.R;
import com.iis.mobimanagercedp.model.AppList;

import java.util.ArrayList;

public class AppListRecycler extends RecyclerView.Adapter<AppListRecycler.ViewHolder> {
    ArrayList<AppList> appLists;

    public ArrayList<AppList> getAppLists() {
        return appLists;
    }

    public void setAppLists(ArrayList<AppList> appLists) {
        this.appLists = appLists;
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

    @Override
    public void onBindViewHolder(@NonNull AppListRecycler.ViewHolder holder, int position) {

        holder.name.setText(appLists.get(holder.getAdapterPosition()).getAppName());
        holder.status.setText(appLists.get(holder.getAdapterPosition()).getStatus());
        holder.version.setText(appLists.get(holder.getAdapterPosition()).getMinVersion());

    }

    @Override
    public int getItemCount() {
        return appLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView name, version, status;
        ImageView icon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.appName);
            version = itemView.findViewById(R.id.appVersion);
            status = itemView.findViewById(R.id.appStatus);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}
