package com.iis.mobimanagercocacola.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iis.mobimanagercocacola.R;
import com.iis.mobimanagercocacola.model.ChatMessage;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context mContext;
    private List<ChatMessage> chatMessageList;

    public ChatListAdapter(Context context, List<ChatMessage> chatMessages) {
        mContext = context;
        chatMessageList = chatMessages;
    }

    public ChatListAdapter(Context context) {
        mContext = context;
    }

    public void addChatMessage(ChatMessage chatMessage) {
        chatMessageList.add(chatMessage);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_send, viewGroup, false);
            return new SentMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_received, viewGroup, false);
            return new ReceiveMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        ChatMessage chatMessage = chatMessageList.get(position);
        switch (viewHolder.getItemViewType()){
            case VIEW_TYPE_RECEIVED:
                ReceiveMessageViewHolder receiveMessageViewHolder = (ReceiveMessageViewHolder) viewHolder;
                receiveMessageViewHolder.receivedMessage.setText(chatMessage.getMessage());
                receiveMessageViewHolder.receivedDateTime.setText(chatMessage.getDateTime());
                break;
            case VIEW_TYPE_SENT:
                SentMessageViewHolder sentMessageViewHolder = (SentMessageViewHolder) viewHolder;
                sentMessageViewHolder.sentMessage.setText(chatMessage.getMessage());
                sentMessageViewHolder.sentDateTime.setText(chatMessage.getDateTime());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }
    @Override
    public int getItemViewType(int position) {
        int itemViewType;
        if (chatMessageList.get(position).isAdmin()) {
            itemViewType = VIEW_TYPE_RECEIVED;
        } else {
            itemViewType = VIEW_TYPE_SENT;
        }
        return itemViewType;
    }

    public class ReceiveMessageViewHolder extends RecyclerView.ViewHolder {

        TextView receivedMessage;
        TextView receivedDateTime;

        public ReceiveMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            receivedMessage = itemView.findViewById(R.id.tvReceivedMessage);
            receivedDateTime = itemView.findViewById(R.id.tvReceivedDateTime);


        }
    }

    public class SentMessageViewHolder extends RecyclerView.ViewHolder {

        TextView sentMessage;
        TextView sentDateTime;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sentMessage = itemView.findViewById(R.id.tvSentMessage);
            sentDateTime = itemView.findViewById(R.id.tvSentMessageDateTime);
        }
    }
}
