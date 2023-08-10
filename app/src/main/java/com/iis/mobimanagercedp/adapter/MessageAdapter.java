package com.iis.mobimanagercedp.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.iis.mobimanagercedp.R;
import com.iis.mobimanagercedp.activity.NotificationActivity;
import com.iis.mobimanagercedp.model.Message;
import com.iis.mobimanagercedp.utils.ImageReaderAsyncTask;

import java.util.ArrayList;
import java.util.Random;

import static android.os.Build.*;
import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    ArrayList<Message> messageArrayList;
    Context mContext;

    public MessageAdapter(ArrayList<Message> messageArrayList, Context context) {
        this.messageArrayList = messageArrayList;
        mContext = context;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.message_list_item, parent, false);
        MessageViewHolder viewHolder = new MessageViewHolder(listItem);
        return viewHolder;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        final Message message = messageArrayList.get(position);
        holder.date.setText("" + message.getDateTime());
        holder.title.setText(message.getMessageTitle().trim());
        final String image_url = message.getImageUrl();

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            holder.tvMessageDetails.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }
        holder.tvMessageDetails.setText(message.getMessageDetails());
        if( message.getImageUrl().length()>0){
            holder.imageView.setVisibility(View.VISIBLE);
//            RequestCreator cursor = Picasso.get().load(message.getImageUrl());
//            cursor.into(holder.imageView);
////            .into(holder.imageView);
        } else {
            holder.imageView.setVisibility(View.GONE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,0,30,0);
            holder.date.setLayoutParams(params);
            // holder.imageView.setVisibility(View.INVISIBLE);
        }

        Random random = new Random();
        int max = 10, min = 0;
        int randNumber = random.nextInt(max - min + 1);

        Integer[] colorsArray = {R.color.colorPrimary, R.color.dark_pink, R.color.deep_pink, R.color.blue_violet, R.color.light_coral,
                R.color.violet, R.color.magenta, R.color.light_see_green, R.color.light_blue, R.color.sky_blue, R.color.colorAccent};

        TextDrawable drawable = TextDrawable.builder().beginConfig()
                .width(40)  // width in px
                .height(40) // height in px
                .endConfig()
                .buildRound(message.getMessageTitle().trim().substring(0, 1), mContext.getResources().getColor(colorsArray[randNumber]));

        holder.iv_firstLetter.setImageDrawable(drawable);
        if(message.getImageUrl().length() > 0) {
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

                    int DeviceTotalWidth = metrics.widthPixels;
                    int DeviceTotalHeight = metrics.heightPixels;
                    Dialog dialog = new Dialog(mContext);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setLayout(DeviceTotalWidth, DeviceTotalHeight);
                    dialog.setContentView(R.layout.message_image_dialog);
                    ImageView imageView = dialog.findViewById(R.id.ivMessage);
//                Glide.with(mContext).load(message.getImageUrl())
//                        .into(imageView);
                    Log.v("_mdm_", message.getImageUrl());
                    new ImageReaderAsyncTask(mContext, imageView).execute(message.getImageUrl());
//                    Picasso.get().load(message.getImageUrl()).into(imageView);
                    dialog.setCancelable(true);

                    dialog.show();
                }
            });
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message_serializable  = new Message();
                message_serializable.setMessageTitle(message.getMessageTitle());
                message_serializable.setMessageDetails(message.getMessageDetails());
                message_serializable.setDateTime(message.getDateTime());
                message_serializable.setImageUrl("");

                if(message.getImageUrl().length() > 0){
                    message_serializable.setImageUrl(message.getImageUrl());
                }

                Intent intent = new Intent(mContext, NotificationActivity.class);
                intent.putExtra("notification", message_serializable);
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
       return messageArrayList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView date;
        TextView title;
        TextView priority;
        TextView tvMessageDetails;
        LinearLayout llAttachment;
        ImageView iv_firstLetter;

        MessageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.ivImage);
            date = (TextView) itemView.findViewById(R.id.tvDate);
            title = (TextView) itemView.findViewById(R.id.tvMessageTitle);
//            priority = (TextView) itemView.findViewById(R.id.tvPriority);
            iv_firstLetter = (ImageView) itemView.findViewById(R.id.iv_firstLetterIcon);
            tvMessageDetails = itemView.findViewById(R.id.tvMessageDetails);
            llAttachment = itemView.findViewById(R.id.llAttachment);


        }
    }
}
