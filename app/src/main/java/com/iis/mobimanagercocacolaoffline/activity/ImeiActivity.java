package com.iis.mobimanagercocacolaoffline.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.iis.mobimanagercocacolaoffline.R;
import com.iis.mobimanagercocacolaoffline.ocr.OcrCaptureActivity;
import com.iis.mobimanagercocacolaoffline.utils.AppPreference;
import com.iis.mobimanagercocacolaoffline.utils.SessionManager;

import java.io.IOException;

public class ImeiActivity extends AppCompatActivity {
    private static final int RC_OCR_CAPTURE = 9003;

    private boolean doubleBackToExitPressedOnce = false;
    private boolean isInAutoScanMode = false;
    EditText etImeiOne, etImeitwo;
    private TextView tv_notice;
    String imeiOne, imeiTwo;
    Button submit;
    TelephonyManager tm;
    private Button btn_autoScan;
    private Button btn_ocrScan;
    private ScrollView scroll_view_autoScan;
    private LinearLayout firstScreen;

    //Barcode variables
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private TextView tvSet_imei_scan;
    private Button btn_scan_imei1;
    private Button btn_scan_imei2;
    private Button btnSubmit_scan;
    private CheckBox checkBox;
    private EditText et_ImeiOne_scan;
    private EditText et_ImeiTwo_scan;

    private boolean IsFirstIMEI_Scanning;
    private boolean IsSecondIMEI_Scanning;
    private ProgressDialog progressDialog;

    private SessionManager sessionManager;

    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 101;
    public static Activity imei_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imei);
        Context mContext = this;
        imei_activity = this;

        sessionManager = new SessionManager(getApplicationContext());


        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("please wait .....");
        progressDialog.setCancelable(false);

        final LinearLayout scan_view = findViewById(R.id.scan_view);
        final LinearLayout notice_view = findViewById(R.id.nl_notice);
        btn_scan_imei1 = findViewById(R.id.btn_scan_imei1);
        btn_scan_imei2 = findViewById(R.id.btn_scan_imei2);
        tvSet_imei_scan = findViewById(R.id.tvSet_imei_scan);
        btnSubmit_scan = findViewById(R.id.btnSubmit_scan);
        et_ImeiOne_scan = findViewById(R.id.et_ImeiOne_scan);
        et_ImeiTwo_scan = findViewById(R.id.et_ImeiTwo_scan);
        cameraView = findViewById(R.id.scanSurface);
        tv_notice = findViewById(R.id.tv_notice);
        checkBox = findViewById(R.id.cb_imei_one);

        btn_autoScan = findViewById(R.id.btn_scan);
        btn_ocrScan = findViewById(R.id.btn_ocr);
        scroll_view_autoScan = findViewById(R.id.auto_scan_scroll);
        firstScreen = findViewById(R.id.firstScreenLinearLayout);


        if (AppPreference.getImeiOne(getApplicationContext()).length() > 5) {
            goToMainActivity();
        }

        btn_ocrScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImeiActivity.this, Apps.class);
                startActivity(intent);
            }
        });

        btn_autoScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scroll_view_autoScan.setVisibility(View.VISIBLE);
                firstScreen.setVisibility(View.GONE);
                isInAutoScanMode = true;

                sessionManager.isCalledFromOCR(false);

                btn_scan_imei1.setVisibility(View.VISIBLE);
                //btn_scan_imei2.setVisibility(View.VISIBLE);


                et_ImeiOne_scan.setText("");
                et_ImeiTwo_scan.setText("");
                //et_ImeiTwo_scan.setVisibility(View.GONE);

                // tv_notice.setText("Please Scan your IMEI One and Two");
            }
        });

      /*  et_ImeiOne_scan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });*/
        scan_view.setVisibility(View.GONE);
        notice_view.setVisibility(View.VISIBLE);


        if (sessionManager.isCalledFromOCR() && !isInAutoScanMode) {
            scroll_view_autoScan.setVisibility(View.VISIBLE);
            firstScreen.setVisibility(View.GONE);
            sessionManager.isCalledFromOCR(false);
            isInAutoScanMode = true;

            btn_scan_imei1.setVisibility(View.GONE);
            btn_scan_imei2.setVisibility(View.GONE);

            tv_notice.setText("Please Check Your Inserted IMEI");

            et_ImeiOne_scan.setText(AppPreference.getOcrImeiOne(getApplicationContext()));
            //et_ImeiTwo_scan.setText(AppPreference.getOcrImeiTwo(getApplicationContext()));

        }

        IsFirstIMEI_Scanning = false;
        IsSecondIMEI_Scanning = false;

        tm = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);

        Log.d("allUser", "setIMEIValues: " + android.os.Build.VERSION.SDK_INT + " " + Build.VERSION_CODES.Q);


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    et_ImeiTwo_scan.setVisibility(View.GONE);
                    btn_scan_imei2.setVisibility(View.GONE);
                } else {
                    et_ImeiTwo_scan.setVisibility(View.VISIBLE);
                    btn_scan_imei2.setVisibility(View.VISIBLE);
                }
            }
        });

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

            } else {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                int simCount = telephonyManager.getPhoneCount();

                Log.e("TAG", "onCreate: -------- in " );
                if (simCount == 1) {
                    imeiOne = tm.getDeviceId(0);
                    imeiTwo = imeiOne;
                } else if (simCount == 2) {
                    imeiOne = tm.getDeviceId(0);
                    imeiTwo = tm.getDeviceId(1);
                }


                //imeiTwo = tm.getDeviceId(2);



                Log.e("allUser--", simCount + "   setIMEIValues: " + imeiTwo + " " + imeiOne );


                /*if ((!imeiOne.isEmpty()) && (!imeiTwo.isEmpty())){
                    AppPreference.setImeiOne(getApplicationContext(),imeiOne);
                    AppPreference.setImeiTwo(getApplicationContext(),imeiTwo);

                    goToMainActivity();
                }*/
                if (!imeiOne.isEmpty()) {
                    AppPreference.setImeiOne(getApplicationContext(), imeiOne);
                    AppPreference.setImeiTwo(getApplicationContext(), imeiTwo);

                    goToMainActivity();
                }
            }

        }
        else {
            getPermissions();


            btnSubmit_scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog.show();
                    imeiOne = et_ImeiOne_scan.getText().toString();
                    imeiTwo = et_ImeiTwo_scan.getText().toString();


                    if (checkBox.isChecked())
                        imeiTwo = imeiOne;

                    /*if (imeiOne.isEmpty() || imeiTwo.isEmpty()){
                        progressDialog.dismiss();
                        Toast.makeText(ImeiActivity.this,"IMEI can not be empty",Toast.LENGTH_SHORT).show();
                    }
                    if ((!imeiOne.isEmpty()) || (!imeiTwo.isEmpty())){
                        AppPreference.setImeiOne(getApplicationContext(),imeiOne);
                        AppPreference.setImeiTwo(getApplicationContext(),imeiTwo);

                        sessionManager.isIMEISubmitted(true);
                        goToMainActivity();
                    }*/
                    if (imeiOne.isEmpty()) {
                        Toast.makeText(ImeiActivity.this, "IMEI can not be empty", Toast.LENGTH_SHORT).show();
                    }
                    if (checkBox.isChecked()) {

                        if (et_ImeiOne_scan.getText().toString().length() != 15) {
                            et_ImeiOne_scan.setError("imei must be 15 char long");
                            progressDialog.dismiss();

                        } else {
                            et_ImeiOne_scan.setError(null);

                            AppPreference.setImeiOne(getApplicationContext(), imeiOne);
                            AppPreference.setImeiTwo(getApplicationContext(), imeiTwo);

                            sessionManager.isIMEISubmitted(true);
                            goToMainActivity();
                        }
                    } else {

                        if (et_ImeiOne_scan.getText().toString().length() != 15) {
                            et_ImeiOne_scan.setError("imei must be 15 char long");
                            progressDialog.dismiss();

                        }
                        if (et_ImeiTwo_scan.getText().toString().length() != 15) {
                            et_ImeiTwo_scan.setError("imei must be 15 char long");
                            progressDialog.dismiss();
                        }

                        if (et_ImeiOne_scan.getText().toString().length() != 15 &&
                                et_ImeiTwo_scan.getText().toString().length() != 15) {
                            et_ImeiOne_scan.setError("imei must be 15 char long");
                            et_ImeiTwo_scan.setError("imei must be 15 char long");
                            progressDialog.dismiss();
                        } else if (et_ImeiOne_scan.getText().toString().length() == 15) {
                            et_ImeiOne_scan.setError(null);
                            // progressDialog.dismiss();

                        } else if (et_ImeiTwo_scan.getText().toString().length() == 15) {
                            et_ImeiTwo_scan.setError(null);
                        }
                        if (et_ImeiOne_scan.getText().toString().length() == 15 &&
                                et_ImeiTwo_scan.getText().toString().length() == 15) {
                            if (et_ImeiOne_scan.getText().toString().equals(et_ImeiTwo_scan.getText().toString())) {
                                progressDialog.dismiss();
                                Toast.makeText(mContext, "IMEI 1 and 2 should be different", Toast.LENGTH_SHORT).show();
                            } else {

                                et_ImeiOne_scan.setError(null);

                                AppPreference.setImeiOne(getApplicationContext(), imeiOne);
                                AppPreference.setImeiTwo(getApplicationContext(), imeiTwo);

                                Log.e("TAG", AppPreference.getImeiTwo(mContext) + "  onClick: iii " + AppPreference.getImeiOne(mContext));
                                sessionManager.isIMEISubmitted(true);
                                goToMainActivity();
                            }
                        }
                    }

                    Log.e("o", "onClick: " + et_ImeiOne_scan.getText());
                    Log.e("t", "onClick: " + et_ImeiTwo_scan.getText());

                }
            });

            btn_scan_imei1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    IsFirstIMEI_Scanning = true;
                    IsSecondIMEI_Scanning = false;
                    btn_scan_imei1.setText("Scanning");
                    btn_scan_imei2.setText("Scan");

                    notice_view.setVisibility(View.GONE);
                    scan_view.setVisibility(View.VISIBLE);
//                    if(scan_view.getVisibility() == View.VISIBLE){
//                        scan_view.setVisibility(View.GONE);
//                    }
//                    else{
//                        scan_view.setVisibility(View.VISIBLE);
//                    }
                }
            });
            btn_scan_imei2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IsSecondIMEI_Scanning = true;
                    IsFirstIMEI_Scanning = false;
                    btn_scan_imei2.setText("Scanning");
                    btn_scan_imei1.setText("Scan");


                    notice_view.setVisibility(View.GONE);
                    scan_view.setVisibility(View.VISIBLE);

//                    if(scan_view.getVisibility() == View.VISIBLE){
//                        scan_view.setVisibility(View.GONE);
//                    }
//                    else{
//                        scan_view.setVisibility(View.VISIBLE);
//                    }

                }
            });


            barcodeDetector = new BarcodeDetector.Builder(this)
                    .setBarcodeFormats(Barcode.ALL_FORMATS).build();

            cameraSource = new CameraSource.Builder(this, barcodeDetector)
                    /*.setRequestedPreviewSize(1600, 1024)*/
                    .setAutoFocusEnabled(true) //you should add this feature
                    .build();

            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @SuppressLint("MissingPermission")
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                            Toast.makeText(getApplicationContext(), "barcodes.valueAt(0).displayValue", Toast.LENGTH_SHORT).show();

                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException ie) {
                        Log.e("CAMERA SOURCE", ie.getMessage());
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });

            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections) {

                    final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                    if (barcodes.size() != 0) {
                        Log.d("mdm__", "barcode : " + barcodes.valueAt(0).displayValue);
                        tvSet_imei_scan.post(new Runnable() {
                            public void run() {
//                                Toast.makeText(getApplicationContext(),barcodes.valueAt(0).displayValue,Toast.LENGTH_SHORT).show();
                                if (IsFirstIMEI_Scanning) {
                                    IsSecondIMEI_Scanning = false;
                                    IsFirstIMEI_Scanning = false;
                                    et_ImeiOne_scan.setText(barcodes.valueAt(0).displayValue);

                                    notice_view.setVisibility(View.VISIBLE);
                                    scan_view.setVisibility(View.GONE);
                                    btn_scan_imei1.setText("Scan");
                                } else if (IsSecondIMEI_Scanning) {
                                    IsSecondIMEI_Scanning = false;
                                    IsFirstIMEI_Scanning = false;
                                    et_ImeiTwo_scan.setText(barcodes.valueAt(0).displayValue);


                                    notice_view.setVisibility(View.VISIBLE);
                                    scan_view.setVisibility(View.GONE);
                                    btn_scan_imei2.setText("Scan");
                                } else {
                                    //nothing
                                }
                            }
                        });

                    }
                }
            });
        }


        /*submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imeiOne = etImeiOne.getText().toString();
                imeiTwo = etImeitwo.getText().toString();
                Log.d("allUser", "setIMEIValues: "+imeiTwo+" "+ imeiOne);


                if (imeiOne.isEmpty() || imeiTwo.isEmpty())
                    Toast.makeText(ImeiActivity.this,"IMEI can not be empty",Toast.LENGTH_SHORT).show();

                if ((!imeiOne.isEmpty()) && (!imeiTwo.isEmpty())){
                    Log.d("allUser", "setIMEIValues: "+imeiTwo+" "+ imeiOne);

                    AppPreference.setImeiOne(getApplicationContext(),imeiOne);
                    AppPreference.setImeiTwo(getApplicationContext(),imeiTwo);

                    goToMainActivity();
                }

            }
        });
*/


    }

    private void goToMainActivity() {
        Intent intent = new Intent(ImeiActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    } else {
                        progressDialog.show();



                        int simCount = tm.getPhoneCount();

                        Log.e("TAG", "onCreate: -------- in " );
                        if (simCount == 1) {
                            imeiOne = tm.getDeviceId(0);
                            imeiTwo = imeiOne;
                        } else if (simCount == 2) {
                            imeiOne = tm.getDeviceId(0);
                            imeiTwo = tm.getDeviceId(1);
                        }
                        //imeiTwo = tm.getDeviceId(2);

                        Log.d("allUser", simCount + " -> setIMEIValues: " + imeiTwo + " " + imeiOne);



                        /*if ((!imeiOne.isEmpty()) || (!imeiTwo.isEmpty())){
                            AppPreference.setImeiOne(getApplicationContext(),imeiOne);
                            AppPreference.setImeiTwo(getApplicationContext(),imeiTwo);

                            goToMainActivity();
                        }*/
                        if (!imeiOne.isEmpty()) {
                            AppPreference.setImeiOne(getApplicationContext(), imeiOne);
                            AppPreference.setImeiTwo(getApplicationContext(), imeiTwo);

                            goToMainActivity();
                        }
                    }


                } else {

                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void getPermissions() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        progressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
//        Toast.makeText(this, "Click twice to exit", Toast.LENGTH_SHORT).show();

        if (isInAutoScanMode) {
            scroll_view_autoScan.setVisibility(View.GONE);
            firstScreen.setVisibility(View.VISIBLE);
            isInAutoScanMode = false;
        }

        sessionManager.isCalledFromOCR(false);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
