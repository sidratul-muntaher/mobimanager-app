package com.iis.mobimanager2.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iis.mobimanager2.R;
import com.iis.mobimanager2.adapter.MessageAdapter;
import com.iis.mobimanager2.data.AppDatabaseHelper;
import com.iis.mobimanager2.model.Message;

import java.util.ArrayList;

public class MessageFragment extends Fragment {
    RecyclerView rvMessage;
    MessageAdapter messageAdapter;
    ArrayList<Message> messageArrayList;
    AppDatabaseHelper appDatabaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        rvMessage = view.findViewById(R.id.rvMessage);
        rvMessage.setLayoutManager(new LinearLayoutManager(getContext()));
        appDatabaseHelper = new AppDatabaseHelper(getContext());
        messageArrayList = appDatabaseHelper.getAllMessageData();
        messageAdapter = new MessageAdapter(messageArrayList,getContext());
        rvMessage.setAdapter(messageAdapter);

        return view;
    }
}