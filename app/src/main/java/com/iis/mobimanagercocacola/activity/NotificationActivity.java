package com.iis.mobimanagercocacola.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.iis.mobimanagercocacola.R;
import com.iis.mobimanagercocacola.model.Message;
import com.iis.mobimanagercocacola.utils.ImageReaderAsyncTask;

public class NotificationActivity extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce = false;
    Toolbar mToolbar;
    TextView toolbarText;


    TextView tv_title;
    TextView tv_body;
    TextView tv_date;
    ImageView iv_image;

    String imageUrl = null;


    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);


        mToolbar = (Toolbar) findViewById(R.id.includeToolbar_NotificationWindrow);
        toolbarText = (TextView) mToolbar.findViewById(R.id.tv_activityTitle);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");
        toolbarText.setText("Notification");




        init();

        String title = null;
        String body = null;
        String date = null;
        String imageLink = null;
        Message message = null;

        if(getIntent().getSerializableExtra("notification") != null){
            message =(Message) getIntent().getSerializableExtra("notification");
            title = message.getMessageTitle();
            body = message.getMessageDetails();
            date = message.getDateTime();
            imageLink = message.getImageUrl();
            imageUrl = imageLink;
        }else {
            return;
        }



        if (title.matches("") || title.equals(null) || body.matches("") || body.equals(null) ||
                date.matches("") || date.equals(null)){
            Log.d("_mdm_", "couldn't get data!!");
        }else {
            tv_title.setText(title);
            tv_body.setText(body);
            tv_date.setText(date);
        }
        if (imageLink.equals(null) || imageLink.matches("")){
            Log.d("_mdm_", "no image reference");
        }else {
            new ImageReaderAsyncTask(getApplicationContext(), iv_image).execute(imageLink);
//            final RequestCreator creator = Picasso.get().load(message.getImageUrl());
//            creator.into(iv_image);
            iv_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Dialog settingsDialog = new Dialog(NotificationActivity.this);
                    settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.message_image_dialog, null));
                    ImageView imageView = settingsDialog.findViewById(R.id.ivMessage);
                    new ImageReaderAsyncTask(getApplicationContext(), imageView).execute(imageUrl);
//                    creator.into(imageView);
                    settingsDialog.show();
                }
            });
        }
    }
    private void init(){
        tv_title = findViewById(R.id.tv_notification_win_title);
        tv_date = findViewById(R.id.tv_notification_win_date);
        tv_body = findViewById(R.id.tv_notification_win_body);
        iv_image = findViewById(R.id.iv_notification_win_image);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
//                intent.putExtra("From", "notification");
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
//        intent.putExtra("From", "notification");
        startActivity(intent);
        finish();


//        if (doubleBackToExitPressedOnce) {
//            super.onBackPressed();
//            System.exit(0);
//            return;
//        }
//
//        this.doubleBackToExitPressedOnce = true;
//        Toast.makeText(this, "Click twice to exit", Toast.LENGTH_SHORT).show();
//
//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                doubleBackToExitPressedOnce = false;
//            }
//        }, 2000);
    }
}