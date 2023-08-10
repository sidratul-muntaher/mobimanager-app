package com.iis.mobimanagercedp.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.iis.mobimanagercedp.R;
import com.iis.mobimanagercedp.utils.AppUtil;

public class ChatActivity extends AppCompatActivity {
    ImageView ivClose;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        setContentView(R.layout.activity_chat);
        AppUtil.changeStatusBarColor(R.color.colorPrimaryDark, this, this);
        ivClose = findViewById(R.id.ivClose);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
