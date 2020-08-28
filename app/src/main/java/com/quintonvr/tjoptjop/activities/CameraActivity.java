package com.quintonvr.tjoptjop.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.quintonvr.tjoptjop.R;
import com.quintonvr.tjoptjop.helpers.CameraPreview;
import com.quintonvr.tjoptjop.machinevision.ScreenDetector;
import com.quintonvr.tjoptjop.machinevision.ScreenReaderCamera;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ID_NUMBER;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_TEMPERATURE;


/**
 *  Created by Quinton van Riet on 2020/05/21.
 */
public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "OCV-CameraAct";

    private static final int TARGET_FRAME_HEIGHT = 600;
    private static final int TARGET_FRAME_WIDTH =  800;
    private static final int FRAME_DECIMATION_FACTOR = 4;   //Only process text once in every X frames (performance setting)


    private ScreenDetector sDetector;
    private Mat mRgbImage;
    private Mat _mRgbImage;
    private boolean camera_Configured = false;
    private ScreenReaderCamera mOpenCvCameraView;
    private Bitmap segment_data;
    private int frameCounter = 0;
    private String detectedTemperature = new String();

    private Camera mCamera;
    private CameraPreview mPreview;
    private TextView mTempInstruction;
    private FloatingActionButton mCaptureButton;
    private EditText mConfirmTempText;
    private LinearLayout mConfirmCancelButtons;
    private ImageView mConfirm, mCancel;
    private ImageView mOutputImage;
    private String mUserID;

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
        setContentView(R.layout.activity_camera);

        mTempInstruction = findViewById(R.id.tInstruction);
        mCaptureButton = findViewById(R.id.button_capture);
        mConfirmTempText = findViewById(R.id.confirm_temp_editText);
        mConfirmCancelButtons = findViewById(R.id.confirm_cancel_buttons_layout);
        mConfirm = findViewById(R.id.imageView_confirm);
        mCancel = findViewById(R.id.imageView_cancel);
        mOutputImage = findViewById(R.id.outputImage);

        mOpenCvCameraView = (ScreenReaderCamera) findViewById(R.id.camera_surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.i(TAG,"Capture Image event");
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//                String Timestamp = sdf.format(new Date());
//                File MediaDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + getResources().getString(R.string.OutputImageDirectory));
//                if (! MediaDir.exists()){
//                    if (! MediaDir.mkdirs()) {
//                        Log.e(TAG, "failed to create image storage directory");
//                    }
//                }else {
//                    String Filename = MediaDir.toString() + "/"+ "IMG" + Timestamp + ".jpg";
//                    mOpenCvCameraView.takePicture(Filename);
//                    segment_data = sDetector.getTargetedImage(_mRgbImage);
//                    //TODO: DEV: Segment data contains the RAW unprocessed image data from the sub-image.
//                    // Only save this file once the user has confirmed the temperature (Sneaky way to obtain labelled data for ML actions).
//                    // Since data is already available in non-file format use that as input to extractor.
//                    String sFilename = MediaDir.toString() + "/"+ "subIMG" + Timestamp + ".jpg";
//                    FileOutputStream fos = null;
//                    try {
//                        fos = new FileOutputStream(sFilename);
//                        segment_data.compress(Bitmap.CompressFormat.JPEG,100,fos);
//                        fos.close();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    //TODO:DEV: Remove in release version only for development
//                    mDebugView.setImageBitmap(sDetector.extractText(_mRgbImage));
//                    sDetector.extractText(_mRgbImage);
//                    mConfirmTempText.setText(sDetector.getOutputText());


//                    String DFilename = MediaDir.toString() + "/"+ "subEdgeIMG" + Timestamp + ".jpg";
//                    FileOutputStream Dfos = null;
//                    try {
//                        Dfos = new FileOutputStream(sFilename);
//                        segment_data.compress(Bitmap.CompressFormat.JPEG,100,Dfos);
//                        Dfos.close();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    //At this point the Mat structures are no longer needed
//                    mRgbImage.release();
//                    _mRgbImage.release();
                    //Continue the processing
                    //mOpenCvCameraView.disableView();
//                    processSegments();
//                }
            }
        });

        mCancel.setOnClickListener(v -> {
            mOpenCvCameraView.enableView();
            mTempInstruction.setText(getString(R.string.position_temperature_value_within_frame));
            mTempInstruction.setVisibility(View.VISIBLE);
            mConfirmTempText.setVisibility(View.GONE);
            mConfirmCancelButtons.setVisibility(View.GONE);
            mCaptureButton.setVisibility(View.VISIBLE);
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
                final MediaPlayer mp = MediaPlayer.create(CameraActivity.this, R.raw.beep1);
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release);

                new AlertDialog.Builder(CameraActivity.this)
                        .setTitle("WARNING!")
                        .setMessage("Temperature NOT within normal range.")
                        .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            Intent cameraIntent = new Intent(CameraActivity.this, MaskActivity.class);
                            cameraIntent.putExtra(INTENT_EXTRA_ID_NUMBER, mUserID);
                            cameraIntent.putExtra(INTENT_EXTRA_TEMPERATURE, mConfirmTempText.getText().toString());
                            startActivity(cameraIntent);
                        }))
                        .setCancelable(false)
                        .show();
            } else {
                Intent cameraIntent = new Intent(CameraActivity.this, MaskActivity.class);
                cameraIntent.putExtra(INTENT_EXTRA_ID_NUMBER, mUserID);
                cameraIntent.putExtra(INTENT_EXTRA_TEMPERATURE, mConfirmTempText.getText().toString());
                startActivity(cameraIntent);
            }
        });

        //TODO:DEV:RELEASE
//        Intent intent = getIntent();
//        if (intent != null) {
//            mUserID = intent.getStringExtra(INTENT_EXTRA_ID_NUMBER);
//
//            if (mUserID != null) {
//                Log.d(TAG, "USER ID: "+ mUserID);
//            } else {
//                Toast.makeText(this, "Select IDENTIFICATION first", Toast.LENGTH_LONG).show();
//                finish();
//            }
//        }

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

    private void setTextAsync(final EditText text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    public void onCameraViewStarted(int width, int height) {
        sDetector = new ScreenDetector(this);
        mRgbImage = new Mat(height, width, CvType.CV_8UC4);
        _mRgbImage = new Mat(height, width, CvType.CV_8UC4);
        mOpenCvCameraView.setDesiredResolution(TARGET_FRAME_WIDTH, TARGET_FRAME_HEIGHT);
        camera_Configured = true;
        mOpenCvCameraView.setOrientation(0);

        sDetector.ScreenReaderListener(new ScreenDetector.ScreenReaderListener() {
            @Override
            public void onTemperatureCaptured(String Temperature) {

                final MediaPlayer mp = MediaPlayer.create(CameraActivity.this, R.raw.scanbeep);
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release);
                setTextAsync(mConfirmTempText,Temperature);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mOpenCvCameraView.disableView();
                        mOutputImage.setVisibility(View.VISIBLE);
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
//        mRgbImage = sDetector.configureMultiMeterTarget(mRgbImage);
        frameCounter++;
        if (frameCounter >= FRAME_DECIMATION_FACTOR){
            frameCounter = 0;
            //sDetector.extractText(_mRgbImage);
//            sDetector.classifySVM(_mRgbImage,mRgbImage.height(),mRgbImage.width());
//            sDetector.classify_SegCount(_mRgbImage);
//            sDetector.BMPdumper(mRgbImage,"LIVE");
//
            Mat Dilated = sDetector.identifyDigits(mRgbImage,false);
            Bitmap bitmap = Bitmap.createBitmap(Dilated.width(), Dilated.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(Dilated, bitmap);
            sDetector.clearIdentifiedRegions();
            sDetector.classify_SVM(mRgbImage);

//            sDetector.classify_SegCount(mRgbImage);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mOutputImage.setImageBitmap(bitmap);
                    mConfirmTempText.setText(sDetector.getOutputText());
                }
            });
            };
//        mRgbImage = sDetector.drawDigitIDs(mRgbImage);
        return sDetector.draw_CaptureBox(_mRgbImage);


    }

    private void processSegments(){
        mTempInstruction.setText(getString(R.string.confirm_temperature));
        mCaptureButton.setVisibility(View.GONE);
        mConfirmTempText.setVisibility(View.VISIBLE);
        mConfirmCancelButtons.setVisibility(View.VISIBLE);
    }
}
