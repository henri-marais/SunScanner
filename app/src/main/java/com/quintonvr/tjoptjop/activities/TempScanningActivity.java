package com.quintonvr.tjoptjop.activities;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.method.DialerKeyListener;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.quintonvr.tjoptjop.R;
import com.quintonvr.tjoptjop.helpers.CameraPreview;
import com.quintonvr.tjoptjop.machinevision.ScreenDetector;
import com.quintonvr.tjoptjop.machinevision.ScreenReaderCamera;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ADDRESS;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_CITY;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_DIRECTION;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_EMERGENCY_CONTACT;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_IDTYPE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ID_NUMBER;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_NAME;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_TEMPERATURE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_TEMPTYPE;


/**
 *  Created by Quinton van Riet on 2020/05/21.
 */
public class TempScanningActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "OCV-CameraAct";

    private static final int TARGET_FRAME_HEIGHT = 600;
    private static final int TARGET_FRAME_WIDTH =  800;
    private static final int FRAME_DECIMATION_FACTOR = 1;   //Only process text once in every X frames (performance setting)
    SharedPreferences prefs;

    private ScreenDetector sDetector;
    private Mat mRgbImage;
    private Mat _mRgbImage;
    private boolean camera_Configured = false;
    private ScreenReaderCamera mOpenCvCameraView;
    private Bitmap segment_data;
    private int frameCounter = 0;
    private String detectedTemperature = new String();
    private int box_height = 5;
    private int box_width = 7;
    private int box_scale = 30;
    private int sat,val;

    private Camera mCamera;
    private CameraPreview mPreview;
    private TextView mTempInstruction;
//    private FloatingActionButton mCaptureButton;
    private ImageButton mSettingsButton;
    private EditText mConfirmTempText;
    private LinearLayout mConfirmCancelButtons;
    private ImageView mConfirm, mCancel;
    private ImageView mOutputImage;
    private String mUserID;
    private String mName = "";
    private String mPhone;
    private String mAddress;
    private String mCity;
    private String mIDtype;
    private String mDir;

    private SharedPreferences sharedPreferences;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tempscanning);
        mTempInstruction = findViewById(R.id.tInstruction);
        mSettingsButton = findViewById(R.id.settingButton);
        mConfirmTempText = findViewById(R.id.capturedTemp);
        mConfirmCancelButtons = findViewById(R.id.confirm_cancel_buttons_layout);
        mConfirm = findViewById(R.id.imageView_confirm);
        mCancel = findViewById(R.id.imageView_cancel);
        mOutputImage = findViewById(R.id.imageView);

        mOpenCvCameraView = (ScreenReaderCamera) findViewById(R.id.camera_surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        Context context = getApplicationContext();
        prefs = context.getSharedPreferences("tjoptjopprefs",MODE_PRIVATE);

        File Datadir = new File(String.valueOf(getFilesDir()));
        InputStream dataSVM = context.getResources().openRawResource(R.raw.svm_data);
        String outputFilename = Datadir.toString() + "/SVMdata.xml";
        byte[] buffer = new byte[0];
        try {
            buffer = new byte[dataSVM.available()];
            dataSVM.read(buffer);
            FileOutputStream fos = new FileOutputStream(new File(outputFilename));
            fos.write(buffer);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mOpenCvCameraView.setCameraZoom(Integer.parseInt(prefs.getString("box_zoom","10")));

        //Get the information from the current intent
        Intent intent = getIntent();
        if (intent != null) {
            mDir = intent.getStringExtra(INTENT_EXTRA_DIRECTION);
            mUserID = intent.getStringExtra(INTENT_EXTRA_ID_NUMBER);
            mName= intent.getStringExtra(INTENT_EXTRA_NAME);
            mPhone = intent.getStringExtra(INTENT_EXTRA_EMERGENCY_CONTACT);
            mAddress = intent.getStringExtra(INTENT_EXTRA_ADDRESS);
            mCity = intent.getStringExtra(INTENT_EXTRA_CITY);
            mIDtype = intent.getStringExtra(INTENT_EXTRA_IDTYPE);
            if (mUserID != null) {
                Log.d(TAG, "USER ID: "+ mUserID);
            } else {
                Toast.makeText(this, "Select IDENTIFICATION first", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((FloatingActionButton)findViewById(R.id.manualMode)).setVisibility(View.VISIBLE);
            }
        },1*1000);

        ((FloatingActionButton)findViewById(R.id.manualMode)).setOnClickListener(v -> {
            Intent Manualintent = new Intent(this, ManualEntryActivity.class);
            Manualintent.putExtra(INTENT_EXTRA_ID_NUMBER,mUserID);
            Manualintent.putExtra(INTENT_EXTRA_IDTYPE,mIDtype);
            Manualintent.putExtra(INTENT_EXTRA_NAME,mName);
            Manualintent.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT,mPhone);
            Manualintent.putExtra(INTENT_EXTRA_ADDRESS,mAddress);
            Manualintent.putExtra(INTENT_EXTRA_CITY,mCity);
            Manualintent.putExtra(INTENT_EXTRA_DIRECTION,mDir);
            startActivity(Manualintent);
        });

        mSettingsButton.setOnClickListener(v ->{
            Intent settingsIntent = new Intent(this,TempCaptureSettingsActivity.class);
            startActivity(settingsIntent);
        });

        mCancel.setOnClickListener(v -> {
            mOpenCvCameraView.enableView();
            mTempInstruction.setText(getString(R.string.position_temperature_value_within_frame));
            mTempInstruction.setVisibility(View.VISIBLE);
            mConfirmTempText.setVisibility(View.GONE);
            mConfirmCancelButtons.setVisibility(View.GONE);
            mOpenCvCameraView.enableView();
        });

        mConfirm.setOnClickListener(v -> {
            //User has confirmed the temp so kill the camera
            mOpenCvCameraView.disableView();
            if (_mRgbImage != null) {
                _mRgbImage.release();
            }
            if (mRgbImage != null){
                mRgbImage.release();
            }

            double measuredTemperature = Double.parseDouble(mConfirmTempText.getText().toString());

            if (measuredTemperature > 37.4) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    vibrator.vibrate(500);
                }
                //sound the alarm
                final MediaPlayer mp = MediaPlayer.create(TempScanningActivity.this, R.raw.beep1);
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release);

                new AlertDialog.Builder(TempScanningActivity.this)
                        .setTitle("WARNING!")
                        .setMessage("Temperature NOT within normal range.")
                        .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            Intent maskIntent = new Intent(TempScanningActivity.this, MaskActivity.class);
                            maskIntent.putExtra(INTENT_EXTRA_ID_NUMBER, mUserID);

                            maskIntent.putExtra(INTENT_EXTRA_IDTYPE,mIDtype);
                            maskIntent.putExtra(INTENT_EXTRA_TEMPERATURE, mConfirmTempText.getText().toString());
                            maskIntent.putExtra(INTENT_EXTRA_TEMPTYPE,"A");
                            if (mName != null){
                                maskIntent.putExtra(INTENT_EXTRA_NAME,mName);
                                maskIntent.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT,mPhone);
                                maskIntent.putExtra(INTENT_EXTRA_ADDRESS,mAddress);
                                maskIntent.putExtra(INTENT_EXTRA_CITY,mCity);

                            }
                            maskIntent.putExtra(INTENT_EXTRA_DIRECTION,mDir);
                            startActivity(maskIntent);
                        }))
                        .setCancelable(false)
                        .show();
            } else {
                Intent maskIntent = new Intent(TempScanningActivity.this, MaskActivity.class);
                maskIntent.putExtra(INTENT_EXTRA_ID_NUMBER, mUserID);
                maskIntent.putExtra(INTENT_EXTRA_IDTYPE,mIDtype);
                maskIntent.putExtra(INTENT_EXTRA_TEMPERATURE, mConfirmTempText.getText().toString());
                maskIntent.putExtra(INTENT_EXTRA_TEMPTYPE,"A");
                if (mName != null){
                    maskIntent.putExtra(INTENT_EXTRA_NAME,mName);
                    maskIntent.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT,mPhone);
                    maskIntent.putExtra(INTENT_EXTRA_ADDRESS,mAddress);
                    maskIntent.putExtra(INTENT_EXTRA_CITY,mCity);
                }
                maskIntent.putExtra(INTENT_EXTRA_DIRECTION,mDir);
                startActivity(maskIntent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        if (mRgbImage != null && _mRgbImage != null) {
            mRgbImage.release();
            _mRgbImage.release();
        }
    }

    private void cancelCameraActivity() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    public void onCameraViewStarted(int width, int height) {
        sDetector = new ScreenDetector(this);
        sDetector.setCaptureRectHeight(Integer.parseInt(prefs.getString("box_height","5")));
        sDetector.setCaptureRectWidth(Integer.parseInt(prefs.getString("box_width","8")));
        sDetector.setCaptureRectSize(Integer.parseInt(prefs.getString("box_base","30")));
        sat = Integer.parseInt(prefs.getString("backlight_intensity","6"));
        val = Integer.parseInt(prefs.getString("backlight_evenness","4"));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOutputImage.setImageDrawable(getDrawable(R.mipmap.temperature));
                mConfirmTempText.setVisibility(View.GONE);
            }
        });
        sDetector.setColorRadius(sat,val);
        mRgbImage = new Mat(height, width, CvType.CV_8UC4);
        _mRgbImage = new Mat(height, width, CvType.CV_8UC4);
        mOpenCvCameraView.setDesiredResolution(TARGET_FRAME_WIDTH, TARGET_FRAME_HEIGHT);
        camera_Configured = true;
        mOpenCvCameraView.setOrientation(0);
        sDetector.ScreenReaderListener(new ScreenDetector.ScreenReaderListener() {
            @Override
            public void onTemperatureCaptured(String Temperature) {
                final MediaPlayer mp = MediaPlayer.create(TempScanningActivity.this, R.raw.scanbeep);
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mOpenCvCameraView.disableView();
                        mConfirmTempText.setText(Temperature);
                        mConfirmCancelButtons.setVisibility(View.VISIBLE);
                        processSegments();
                    }
                });
            };
        });
    }

    public void onCameraViewStopped() {
        mRgbImage.release();
        _mRgbImage.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgbImage = inputFrame.rgba();
        mRgbImage.copyTo(_mRgbImage);
        frameCounter++;
        if (frameCounter >= FRAME_DECIMATION_FACTOR){
            frameCounter = 0;
            boolean training_mode = true;
            if (prefs.getBoolean("training_mode",false)==true) {
                Mat humanview = sDetector.identifyDigits(mRgbImage, true);
                Bitmap bitmap = Bitmap.createBitmap(humanview.width(), humanview.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(humanview, bitmap);
                if (mOutputImage != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mOutputImage.setImageBitmap(bitmap);
                            mConfirmTempText.setVisibility(View.VISIBLE);
                            mConfirmTempText.setText("Cal.");
                        }
                    });
                }
            }else {
                sDetector.clearIdentifiedRegions();
                sDetector.classify_SVM(mRgbImage);
            }
        };
        return sDetector.draw_CaptureBox(_mRgbImage);
    }

    private void processSegments(){
        mTempInstruction.setText(getString(R.string.confirm_temperature));
        mConfirmTempText.setVisibility(View.VISIBLE);
        mConfirmCancelButtons.setVisibility(View.VISIBLE);
    }
}
