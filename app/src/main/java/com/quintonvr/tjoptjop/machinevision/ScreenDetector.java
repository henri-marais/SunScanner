package com.quintonvr.tjoptjop.machinevision;

import android.app.Application;
import android.content.Context;
import android.content.pm.ComponentInfo;
import android.content.res.AssetFileDescriptor;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RegionIterator;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.service.autofill.SaveCallback;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AlertDialogLayout;

import com.quintonvr.tjoptjop.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.lang.Integer;
import java.util.Random;
import java.util.concurrent.Semaphore;

import org.opencv.android.Utils;
import org.opencv.core.Algorithm;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.Ml;
import org.opencv.ml.SVM;
import org.opencv.ml.SVMSGD;
import org.opencv.objdetect.Objdetect;

import static java.lang.Math.random;
import static java.util.Collections.addAll;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_32S;
import static org.opencv.core.CvType.CV_8S;
import static org.opencv.core.CvType.CV_8SC1;
import static org.opencv.core.CvType.CV_8U;

public class ScreenDetector {
    private static final String TAG = "ScreenDetector";
    private static final int CONFIDENCE_THRESHOLD = 1;        //Need to get at least this many correct readings in a row to declare success.
    private static int RecBaseDim   = 33; //Base dimension of capturing rectange in px
    private static int CapRecHf     = 5;  //Capture rectange height expressed as multiple of base dim
    private static int CapRecWf     = 8;  //Capture rectangle width expressed as multiple of base dim

    private int confidenceCounter = 0;
    private double detectedTemp = 0.0;
    private Context mContext;
    private boolean isLoaded = false;
    private List<Digit> digitsID = new ArrayList<>();

    private Scalar mBlobColorHsv = new Scalar(255);
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);
    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.1;
    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(10,60,40,0);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
    String outputText;
    String processedOutputText;

    // Cache
    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();
    SVM mSVM;

    public interface ScreenReaderListener{
        public void onTemperatureCaptured(String Temperature);
    }

    private ScreenReaderListener listener;

    public void ScreenReaderListener(ScreenReaderListener listener) {
        this.listener = listener;
    }

    public ScreenDetector(Context context){
        this.listener = null;
        mContext = context;
    }

    private boolean svmLoadModel(){
        try {
            File svmDatafile = new File(mContext.getFilesDir() + "/SVMdata.xml");

//            File svmDatafile = new File(String.valueOf(mContext.getExternalCacheDir()) + "/SVMdata.xml");
//            File svmDatafile = new File(Environment.getExternalStorageDirectory().getPath()+"/svm_data.xml");
            mSVM = SVM.load(svmDatafile.getPath());
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public void setCaptureRectHeight(int height){
        CapRecHf = height;
    }

    public void setCaptureRectWidth(int width){
        CapRecWf = width;
    }

    public void setCaptureRectSize(int base_size){
        RecBaseDim = base_size;
    }


    public void setColorRadius(int saturation, int intensity) {
        mColorRadius = new Scalar(mColorRadius.val[0],saturation*10,intensity*10);
    }

    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

        mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;

        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public Mat getSpectrum() {
        return mSpectrum;
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }

    public void process(Mat rgbaImage) {
        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                Core.multiply(contour, new Scalar(4,4), contour);
                mContours.add(contour);
            }
        }
        Log.d(TAG, "Found " + Integer.toString(mContours.size()) + "contours");
    }

    public void configureTarget(Mat rgbImage, int targetH, int targetW){
        int h = rgbImage.width();
        int w = rgbImage.height();
        int fch = h/2;
        int fcw = w/2;
        int recH = targetH;
        int recW = targetW;
        Rect captureRect = new Rect(new Point(fch-recH/2,fcw-recW/2),new Point(fch+recH/2,fcw+recW/2));
        //Extract specific region and do color space conversion
        Mat extractRegionRGB = rgbImage.submat(captureRect);
        Mat extractRegionHSV = new Mat();
        Imgproc.cvtColor(extractRegionRGB,extractRegionHSV,Imgproc.COLOR_RGB2HSV_FULL);
        //Calculate the average colour of the target area
        mBlobColorHsv = Core.sumElems(extractRegionHSV);
        int PixelCount = captureRect.width*captureRect.height;
        for (int i=0;i<mBlobColorHsv.val.length;i++){
            mBlobColorHsv.val[i] /= PixelCount;
        }
        setHsvColor(mBlobColorHsv);
        Log.d(TAG, "Current color target " + mBlobColorHsv.toString() + "contours");
        //Release memory
        extractRegionHSV.release();
        extractRegionRGB.release();
        System.gc();
    }

    public Mat configureMultiMeterTarget(Mat rgbImage){
        int h = rgbImage.width();
        int w = rgbImage.height();
        int Fch = h/2;
        int Fcw = w/2;
        int fch = 0;
        int fcw = 0;
        int rad = 5;
        int recH = RecBaseDim*CapRecHf/2;
        int recW = RecBaseDim*CapRecWf/2;
        Scalar purple = new Scalar(188,64,206);
        Scalar teal = new Scalar(69,181,201);

        //Use the crosshair locations to extract the background metering data
        fch = Fch+recH;
        fcw = Fcw;
        Rect point1 = new Rect(new Point(fch-rad/2,fcw-rad/2),new Point(fch+rad/2,fcw+rad/2));
        fch = Fch+recH;
        fcw = Fcw+(int)(recW*0.8);
        Rect point2 = new Rect(new Point(fch-rad/2,fcw-rad/2),new Point(fch+rad/2,fcw+rad/2));
        fch = Fch-recH;
        fcw = Fcw;
        Rect point3 = new Rect(new Point(fch-rad/2,fcw-rad/2),new Point(fch+rad/2,fcw+rad/2));
        fch = Fch-recH;
        fcw = Fcw+(int)(recW*0.8);
        Rect point4 = new Rect(new Point(fch-rad/2,fcw-rad/2),new Point(fch+rad/2,fcw+rad/2));
        List<Rect> Points = new ArrayList<>();
        Points.add(point1);
        Points.add(point2);
        Points.add(point3);
        Points.add(point4);
        Scalar meteredAvg = new Scalar(255);
        for (int i=0; i<Points.size();i++){
            Mat extractRegionRGB = rgbImage.submat(Points.get(i));

            Mat extractRegionHSV = new Mat();
            Imgproc.cvtColor(extractRegionRGB,extractRegionHSV,Imgproc.COLOR_RGB2HSV_FULL);
            Scalar pointColorAvg = new Scalar(255);
            pointColorAvg = Core.sumElems(extractRegionHSV);
            int PixelCount = Points.get(i).width*Points.get(i).height;
            for (int j=0; j<pointColorAvg.val.length;j++){
                pointColorAvg.val[j] /= PixelCount;
            }
            if (i==0){
                meteredAvg = pointColorAvg;
            }else{
                for (int j=0;j<pointColorAvg.val.length;j++){
                    meteredAvg.val[j] = (meteredAvg.val[j]+pointColorAvg.val[j])/2;
                }
            }
            //Draw the metering point on the canvas
            Rect bbox = Points.get(i);
            Point corner1 = new Point(bbox.x,bbox.y);
            Point corner2 = new Point(bbox.x+bbox.width,bbox.y+bbox.height);
            Imgproc.rectangle(rgbImage,corner1,corner2,new Scalar(255,0,0),1);
        }
//        BMPdumper(rgbImage,"METER");
        setHsvColor(meteredAvg);
        return rgbImage;
    }

    public Mat subFrameMultiMeter(Mat rgbImage){
        int h = rgbImage.width();
        int w = rgbImage.height();
        int Fch = h/2;
        int Fcw = w/2;
        int fch = 0;
        int fcw = 0;
        int rad = 5;
        int recH = RecBaseDim*CapRecHf/2;
        int recW = RecBaseDim*CapRecWf/2;
        Scalar purple = new Scalar(188,64,206);
        Scalar teal = new Scalar(69,181,201);
        Rect point1 = new Rect(new Point(0,0),new Point(10,10));
        Rect point2 = new Rect(new Point(0,0),new Point(h,w));
        Rect point3 = new Rect(new Point(h-5,0),new Point(h,5));
        Rect point4 = new Rect(new Point(h-5,w/2-5),new Point(h,w/2));
        List<Rect> Points = new ArrayList<>();
        Points.add(point1);
//        Points.add(point3);
//        Points.add(point4);
        Scalar meteredAvg = new Scalar(255);
        for (int i=0; i<Points.size();i++){
            Mat extractRegionRGB = rgbImage.submat(Points.get(i));

            Mat extractRegionHSV = new Mat();
            Imgproc.cvtColor(extractRegionRGB,extractRegionHSV,Imgproc.COLOR_RGB2HSV_FULL);
            Scalar pointColorAvg = new Scalar(255);
            pointColorAvg = Core.sumElems(extractRegionHSV);
            int PixelCount = Points.get(i).width*Points.get(i).height;
            for (int j=0; j<pointColorAvg.val.length;j++){
                pointColorAvg.val[j] /= PixelCount;
            }
            if (i==0){
                meteredAvg = pointColorAvg;
            }else{
                for (int j=0;j<pointColorAvg.val.length;j++){
                    meteredAvg.val[j] = (meteredAvg.val[j]+pointColorAvg.val[j])/2;
                }
            }
        }
        setHsvColor(meteredAvg);
        return rgbImage;
    }

    public List<MatOfPoint> getContours() {
        return mContours;
    }

    /**
     * Function draws a rectangle on a reduced resolution frame to guide the use in capturing frame.
     * @param rgbImage
     * @return
     */
    public Mat draw_Crosshairs(Mat rgbImage){
        int h = rgbImage.width();
        int w = rgbImage.height();
        int Fch = h/2;
        int Fcw = w/2;
        int fch = 0;
        int fcw = 0;
        int hairL = 10;
        int recH = RecBaseDim*CapRecHf/3;
        int recW = RecBaseDim*CapRecWf/3;
        Scalar purple = new Scalar(188,64,206);
        Scalar teal = new Scalar(69,181,201);
        //Draw a group of crosshairs (mainly only to aid the developer :)
        fch = Fch+recH;
        fcw = Fcw-recW;
        Imgproc.line(rgbImage,new Point(fch-hairL,fcw), new Point(fch+hairL,fcw),teal,1);
        Imgproc.line(rgbImage,new Point(fch,fcw-hairL), new Point(fch,fcw+hairL),teal,1);
        fch = Fch+recH;
        fcw = Fcw+recW;
        Imgproc.line(rgbImage,new Point(fch-hairL,fcw), new Point(fch+hairL,fcw),teal,1);
        Imgproc.line(rgbImage,new Point(fch,fcw-hairL), new Point(fch,fcw+hairL),teal,1);
        fch = Fch-recH;
        fcw = Fcw-recW;
        Imgproc.line(rgbImage,new Point(fch-hairL,fcw), new Point(fch+hairL,fcw),teal,1);
        Imgproc.line(rgbImage,new Point(fch,fcw-hairL), new Point(fch,fcw+hairL),teal,1);
        fch = Fch-recH;
        fcw = Fcw+recW;
        Imgproc.line(rgbImage,new Point(fch-hairL,fcw), new Point(fch+hairL,fcw),teal,1);
        Imgproc.line(rgbImage,new Point(fch,fcw-hairL), new Point(fch,fcw+hairL),teal,1);
        return rgbImage;
    }

    public Mat drawDigitIDs(Mat rgbImage){
        int fch = rgbImage.height()/2;
        int fcw = rgbImage.width()/2;

        for (int i=0; i<digitsID.size(); i++){
            Rect box = digitsID.get(i).getBoundingBox();
            int tlw = 99/4;
            int tlh = 39/4;
            int brh = (339)/4;
            int brw = (145)/4;
            int recH = RecBaseDim*CapRecHf;
            int recW = RecBaseDim*CapRecWf;
            //Since extraction is done on a 2x frame reduce all box coords

            Point corner1 = new Point(fch-recH/2+tlh,fcw-recW/2+tlw);
            Point corner2 = new Point(fch-recH/2+tlh+brh,fcw-recW/2+tlw);
            //Box is referenced to the extraction frame so should be shifter to the center of the frame
//            Point corner1 = new Point(fch-(box.x)/2,fcw-(box.y)/2);
//            Point corner1 = new Point(fch-tlh,fcw-tlw);
//            Point corner2 = new Point(fch-(box.x+box.width)/2,fcw-(box.y+box.height)/2);
//            Point corner2 = new Point(fch+brh,fcw+brw);
            Imgproc.rectangle(rgbImage,corner1,corner2,new Scalar(255,0,0),1);
            Imgproc.circle(rgbImage,corner1,4,new Scalar(255,255,0),3);
        }
        Point circle = new Point(rgbImage.height()/2*1.2,rgbImage.width()/2*1.2);
        Imgproc.circle(rgbImage,circle,4,new Scalar(255,0,255),3);
        return rgbImage;
    }

    public Mat draw_CaptureBox(Mat rgbImage){
        int h = rgbImage.width();
        int w = rgbImage.height();
        int fch = h/2;
        int fcw = w/2;
        int recH = RecBaseDim*CapRecHf;
        int recW = RecBaseDim*CapRecWf;
        Scalar purple = new Scalar(188,64,206);
        Imgproc.rectangle(rgbImage,new Point(fch-recH/2,fcw-recW/2),new Point(fch+recH/2,fcw+recW/2),purple,2);
        return rgbImage;
    }

    private Mat RotatedSubframe(Mat rgbImage){
        int h = rgbImage.width();
        int w = rgbImage.height();
        int fch = h/2;
        int fcw = w/2;
        int recH = RecBaseDim*CapRecHf;
        int recW = RecBaseDim*CapRecWf;
        Rect captureRect = new Rect(new Point(fch-recH/2,fcw-recW/2),new Point(fch+recH/2,fcw+recW/2));
        Mat subFrame = rgbImage.submat(captureRect);
        //Correct for the rotation of the image in the subframe
        Mat tempImg = subFrame.t();
        Core.flip(tempImg,tempImg,1);
        Mat doubleSize = new Mat();
        Imgproc.pyrUp(tempImg,doubleSize);
//        Mat quadsize = new Mat();
//        Imgproc.pyrUp(doubleSize,quadsize);
        return doubleSize;
    }

    public Bitmap getTargetedImage(Mat rgbImage){
        Mat tempImage = RotatedSubframe(rgbImage);
        Bitmap bitmap = Bitmap.createBitmap(tempImage.width(), tempImage.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tempImage,bitmap);
        //Clean up and temp frames
        tempImage.release();
        return bitmap;
    }

    /**
     * Function extracts temperature values from an image (if possible) if no text could be found "None" is returned, otherwise the found text
     * @param inputImage
     */
    public Mat extractText(Mat inputImage){
        inputImage = configureMultiMeterTarget(inputImage);
        //Get the subframe in the correct orientation
        Mat tempImg = RotatedSubframe(inputImage);
//        BMPdumper(tempImg,"INP");
        //First subtract the background colour
        Mat HSVimg = new Mat();
        Imgproc.cvtColor(tempImg, HSVimg, Imgproc.COLOR_RGB2HSV_FULL);
        Mat mask = HSVimg.clone();
        Core.inRange(HSVimg, mLowerBound, mUpperBound, mask); //Uses default 3x3 kernel
        Mat dilated = new Mat();
        Mat invImg = new Mat();
        Core.bitwise_not(mask,invImg);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE,new Size(3,5));
        Imgproc.dilate(invImg, dilated, kernel,new Point(-1,-1),1);
//        BMPdumper(dilated,"DIL");

        //At this point the digits should be prominent so extract the bounding boxes
        double segPercentage = 0.05;
        double minROI = (dilated.width()*dilated.height()*segPercentage);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat cHierarchy = new Mat();
        Imgproc.findContours(dilated, contours, cHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        List<Rect> digits = new ArrayList<Rect>();
        for (int i=0; i < contours.size();i++){
            Rect bbox = Imgproc.boundingRect(contours.get(i));
            RotatedRect angle_box = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            if ((bbox.height*bbox.width) > minROI){
                double angle = angle_box.angle;
                digits.add(bbox);
            }
        }
        Comparator<Rect> bboxCenterPoint = new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                if (o1.x > o2.x){
                    return 1;
                }else if (o1.x == o2.x){
                    return 0;
                }else{
                    return -1;
                }
            }
        };
        Collections.sort(digits,bboxCenterPoint); //Sort the bounding boxes from left to right

        Imgproc.drawContours(tempImg, contours, -1, new Scalar(255,255,0),1);
        for (int i=0;i < digits.size();i++) {
            Rect bbox = Imgproc.boundingRect(contours.get(i));
            Point corner1 = new Point(bbox.x,bbox.y);
            Point corner2 = new Point(bbox.x+bbox.width,bbox.y+bbox.height);
            Imgproc.rectangle(tempImg,corner1,corner2,new Scalar(200,0,125),2);
        }
//        BMPdumper(tempImg,"CONT");

//        outputText = new String();
//        for (Rect digit:digits) {
//            outputText = outputText + SegHitter(dilated.submat(digit),dilated.height(),dilated.width(),true);
//            if (outputText.length() == 2){
//                outputText = outputText + ".";
//            }
//        }
        //Clean up temp frames
        tempImg.release();
        HSVimg.release();
        mask.release();
        kernel.release();
        dilated.release();
        invImg.release();
        for (MatOfPoint contour:contours) {contour.release();}
        AutoExtractionChecker("saap");
        return tempImg;
    }

    public boolean BMPdumper(Mat image,String nametag){
        boolean debug_mode = true;
        if (debug_mode) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SS");
            String Timestamp = sdf.format(new Date());
//            File MediaDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "VSegDump");
            File MediaDir = new File(mContext.getFilesDir() + File.separator + "VSegDump");
            if (!MediaDir.exists()) {
                if (!MediaDir.mkdirs()) {
                    Log.e(TAG, "failed to create image storage directory");
                }
            }
            String Filename = MediaDir.toString() + "/" + nametag + Timestamp + ".jpg";
            FileOutputStream Dfos = null;
            Mat scaled = new Mat();
            try {
                Imgproc.pyrUp(image, scaled);
                Bitmap bitmap = Bitmap.createBitmap(scaled.width(), scaled.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(scaled, bitmap);
                Dfos = new FileOutputStream(Filename);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, Dfos);
                Dfos.close();
                scaled.release();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (scaled != null) {
                scaled.release();
            }
            return false;
        }else{
            return true;
        }
    }

    public String getOutputText(){
        //since we know that the string must be 3 characters long xx.x degC some processing is done
        //her to format the string correctly. Regex would be overkill so simply hand manipulation is done.
        Log.i("ScreenDetector","Found number in image :"+outputText);
        return outputText;
    };

    private void AutoExtractionChecker(String classificationResult){
        if (classificationResult.length() >=3){ //Correct temperatures cannot be shorter
            try{    //Possibly have a number here
                if (detectedTemp == 0){ //Nothing stored yet
                    detectedTemp = Double.parseDouble(getOutputText());   //Get the number
                    confidenceCounter++;
                }else { //Previous number is stored, check if new number matches
                    if (detectedTemp == Double.parseDouble(getOutputText())){
                        confidenceCounter++;
                    }else{ //If not lose confidence, restart detection process/
                        confidenceCounter=0;
                        detectedTemp = 0.0;
                    }
                }
                confidenceCounter++;
            }catch (NumberFormatException e){
                //String is not a number so keep going
                confidenceCounter = 0;
            }
        }
        if (confidenceCounter >= CONFIDENCE_THRESHOLD){
            Log.i("Decimator","Found temperature of "+String.valueOf(detectedTemp));
            //Fire the intent here
                if (listener != null)
                    listener.onTemperatureCaptured(String.valueOf(detectedTemp));
                    detectedTemp = 0.0;
                    confidenceCounter = 0;
        }
    }

    private void drawSegCheck(Mat ROI, Rect bbox){
        Bitmap bitmap = Bitmap.createBitmap(ROI.width(), ROI.height(), Bitmap.Config.RGB_565);
        Mat ColorROI = new Mat();
        Imgproc.cvtColor(ROI,ColorROI,Imgproc.COLOR_GRAY2RGBA);
        Point corner1 = new Point(bbox.x,bbox.y);
        Point corner2 = new Point(bbox.x+bbox.width,bbox.y+bbox.height);
        Imgproc.rectangle(ColorROI,corner1,corner2,new Scalar(255,0,0));
        Utils.matToBitmap(ColorROI,bitmap);
        ColorROI.release();
    }

    private Bitmap drawSegCheck(Mat ROI, List<Rect> segs){
        Bitmap bitmap = Bitmap.createBitmap(ROI.width(), ROI.height(), Bitmap.Config.RGB_565);
        Mat ColorROI = new Mat();
        Imgproc.cvtColor(ROI,ColorROI,Imgproc.COLOR_GRAY2RGBA);
        for (Rect bbox:segs
             ) {
            Point corner1 = new Point(bbox.x,bbox.y);
            Point corner2 = new Point(bbox.x+bbox.width,bbox.y+bbox.height);
            Imgproc.rectangle(ColorROI,corner1,corner2,new Scalar(255,125,0));
        }
        Utils.matToBitmap(ColorROI,bitmap);
        return bitmap;
    }

    public Mat identifyDigits(Mat inputImage, boolean humanView){
        Mat tempImg = RotatedSubframe(inputImage);
        Mat eroded = new Mat();
        Mat dilated1 = new Mat();
        Mat invImg = new Mat();
        Mat grey = new Mat();
        Mat blurred = new Mat();
        Mat edged = new Mat();
        Mat dilated = new Mat();

        boolean daylightMode = false;
        if (true){
            //Figure out that constitutes the background and remove it
            subFrameMultiMeter(tempImg);
            Mat HSVimg = new Mat();
            Imgproc.cvtColor(tempImg, HSVimg, Imgproc.COLOR_RGB2HSV_FULL);
            Mat background = HSVimg.clone();
            Core.inRange(HSVimg, mLowerBound, mUpperBound, background); //Uses default 3x3 kernel
            Core.bitwise_not(background,invImg);
            //Get rif of any remaining artefacts (decimal points etc.)
            int noiseReductionAggression = 2;
            Mat erosionKernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_CROSS,new Size(5,5));
            Imgproc.erode(invImg, eroded, erosionKernel,new Point(-1,-1),noiseReductionAggression);
            int segmentSpacing = 5;
            //Blur the segments into each other to increase identification region.
            Mat kernel2 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE,new Size(3,5));
            Imgproc.dilate(eroded, dilated, kernel2,new Point(-1,-1),segmentSpacing);
//            return dilated;
//            Imgproc.cvtColor(tempImg,grey,Imgproc.COLOR_RGB2GRAY);
//            Imgproc.equalizeHist(grey, grey);
//            Imgproc.bilateralFilter(grey,blurred,7,10,3);
//            Scalar avg = Core.mean(blurred);
//            Mat mask = blurred.clone();
//            Imgproc.threshold(blurred,invImg,245,255,Imgproc.THRESH_OTSU);
//            return invImg;
//            Imgproc.adaptiveThreshold(blurred,invImg,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,21,0);
//            double sigma = 0.7;
//            Imgproc.Canny(blurred,edged,(1-sigma)*avg.val[0],(1+sigma)*avg.val[0]);
//
//            int noiseReductionAggression = 3;
//            Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_CROSS,new Size(5,5));
//            Imgproc.erode(invImg, dilated1, kernel,new Point(-1,-1),noiseReductionAggression);
//
//            int smoothingAggression = 2;
//            //Blur the segments into each other to increase identification region.
//            Mat kernel2 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE,new Size(5,5));
//            Imgproc.dilate(dilated1, dilated, kernel2,new Point(-1,-1),smoothingAggression);
//
//            Imgproc.erode(dilated, dilated1, kernel,new Point(-1,-1),noiseReductionAggression*2);
//            Imgproc.dilate(dilated1, dilated, kernel2,new Point(-1,-1),smoothingAggression*3);
//            BMPdumper(dilated,"DIL");
        }else{
//            subFrameMultiMeter(tempImg);
//        //First subtract the background colour
//            Mat HSVimg = new Mat();
//            Imgproc.cvtColor(tempImg, HSVimg, Imgproc.COLOR_RGB2HSV_FULL);
//            Mat mask = HSVimg.clone();
//            Core.inRange(HSVimg, mLowerBound, mUpperBound, mask); //Uses default 3x3 kernel
//           Core.bitwise_not(mask,invImg);
////            Get rid of small features such as the decimal point
//            Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_CROSS,new Size(5,5));
//            Imgproc.erode(invImg, dilated1, kernel,new Point(-1,-1),noise_removal_factor);
//            kernel.release();
//            //Blur the segments into each other to increase identification region.
//            Mat kernel2 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_CROSS,new Size(5,5));
//            Imgproc.dilate(dilated1, dilated, kernel,new Point(-1,-1),5);
//            BMPdumper(dilated,"DIL");
        }

//        Mat result = new Mat();
//        Imgproc.cvtColor(grey,result,Imgproc.COLOR_GRAY2RGB);
//        BMPdumper(result,"EQUAL");

//        Imgproc.cvtColor(thresh,result,Imgproc.COLOR_GRAY2RGB);
//        inputImage = configureMultiMeterTarget(inputImage);
//        Get the subframe in the correct orientation
//
//        BMPdumper(tempImg,"INP");
//        BMPdumper(result,"THRESH");
////        <------------------ Handy function to handle saved images -------------------->
//        File importFile = new File(Environment.getExternalStorageDirectory(), "Input10.jpg");
//        Bitmap bMap= BitmapFactory.decodeFile(importFile.getAbsolutePath());
//        Mat tempImg = new Mat();
//        Utils.bitmapToMat(bMap,tempImg);
////        <------------------------------------------------------------------------------>


        //At this point the digits should be prominent so extract the bounding boxes
        double segPercentage = 0.03;
        double minROI = (dilated.width()*dilated.height()*segPercentage);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat cHierarchy = new Mat();
        Imgproc.findContours(dilated, contours, cHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int i=0; i < contours.size();i++){
            Rect bbox = Imgproc.boundingRect(contours.get(i));
            if ((bbox.height*bbox.width) > minROI){
                digitsID.add(new Digit(contours.get(i),bbox));
            }
        }

        Collections.sort(digitsID); //The identified digits' bounding boxes should now be sorted.
        boolean resort = false;
        double segPrcX = 0.05;
        double segPrcY = 0.05;
        for (int i=1; i<digitsID.size();i++){
            if (Math.abs(digitsID.get(i).getBoundingBox().x-digitsID.get(i-1).getBoundingBox().x)<tempImg.width()*segPrcX) {
                //In this case the bounding boxes' starting x coordinates is within segPercentage of the frame width from each other
                //Check if the vertical space between the edges of the bounding boxes is within segPercentage of the Frame height from each other
                if (((digitsID.get(i).getBoundingBox().y - (digitsID.get(i - 1).getBoundingBox().y + digitsID.get(i - 1).getBoundingBox().height)) <= (tempImg.height() * segPrcY)) ||
                        (((digitsID.get(i).getBoundingBox().y + digitsID.get(i).getBoundingBox().height) - digitsID.get(i - 1).getBoundingBox().y) <= (tempImg.height() * segPrcY))) {
                    //Boxes can now be combined
                    MatOfPoint contourA = digitsID.get(i - 1).getContour();
                    MatOfPoint contourB = digitsID.get(i).getContour();
                    List<Point> x = new ArrayList<>(contourA.toList().size() + contourB.toList().size());
                    x.addAll(contourA.toList());
                    x.addAll(contourB.toList());
                    MatOfPoint contourComb = new MatOfPoint();
                    contourComb.fromList(x);
                    Rect box = Imgproc.boundingRect(contourComb);
                    Digit combined = new Digit(contourComb, box);
                    digitsID.remove(i);
                    digitsID.remove(i - 1);
                    digitsID.add(combined);
                    resort = true;
                }
            }
        }
        resort = true;
        if (resort) {Collections.sort(digitsID);} //If digits were merged the sort order might be wrong.
////        <------------ Code to draw identification boxes and save to bitmap------------>
        for (int i=0; i < digitsID.size(); i++) {
            Rect bbox = digitsID.get(i).getBoundingBox();
            Point corner1 = new Point(bbox.x,bbox.y);
            Point corner2 = new Point(bbox.x+bbox.width,bbox.y+bbox.height);
            Imgproc.rectangle(tempImg,corner1,corner2,new Scalar(200,0,25*i),4);
            Imgproc.putText(tempImg,String.valueOf(i+1),new Point(bbox.x+bbox.width/3,bbox.y+bbox.height/3),1,5,new Scalar(255,255,255));
        }


//        BMPdumper(tempImg,"CONT_all");
////        <---------------------------------------------------------------------------->

        //Clean up temp frames
//        tempImg.release();
//        HSVimg.release();
//        mask.release();
//        kernel.release();
//        dilated.release();
//        invImg.release();
//        for (MatOfPoint contour:contours) {contour.release();}
        if (humanView){
            return dilated;
        }else {
            return dilated;
        }
    }

    public void clearIdentifiedRegions(){
        digitsID.clear();
    }

    private void AutomaticMode_StabilityChecker(double temp){
        if (detectedTemp == 0.0){   //Initialise
            detectedTemp = temp;
            confidenceCounter++;
        }else{                      //Already have temp
            if ((temp == detectedTemp) && (confidenceCounter<= CONFIDENCE_THRESHOLD) && (temp != -1.0)){
                confidenceCounter++;
                if (confidenceCounter > CONFIDENCE_THRESHOLD){
                    //Got stable temperature so fire callback
                    if ((detectedTemp > 10.0) && (detectedTemp < 45.00)) {
                        listener.onTemperatureCaptured(String.valueOf(detectedTemp));
                        detectedTemp = 0.0;
                        confidenceCounter = 0;
                    }else{
                        //Bad temperature was captured.
                        detectedTemp = 0.00;
                        confidenceCounter = 0;
                    }
                }
            }else{//Temperature is jumping around
                detectedTemp = temp;
                confidenceCounter = 0;
            }
        }
    }

    private int SegHitter(Mat ROI){
        //Scale the input image to known sizes that is typical of 7 segment display proportions
        int Target_height = 150;
        int Target_width = 75;
        float factor = Target_height/(float)ROI.height();
        int dh = (int)Math.round(ROI.height()*factor);
        int dw = (int)Math.round(ROI.width()*factor);
        if (dw >= Target_width){
            dw = Target_width;
        }
        Mat rescaled = new Mat();
        if (factor >= 1){
            Imgproc.resize(ROI,rescaled,new Size(dw,dh),Imgproc.INTER_CUBIC);
        }else {
            Imgproc.resize(ROI, rescaled, new Size(dw, dh),Imgproc.INTER_AREA);
        }
        //At this point the image should be scaled to a known good vertical size
        //If the segment data is not wide enough add pixels to the right.
        Mat finalScale;
        if (dw < Target_width/3*2) {
            finalScale = Mat.zeros(Target_height, Target_width, CvType.CV_8UC1);
            rescaled.copyTo(finalScale.submat(new Rect(new Point(finalScale.width()-dw,0),new Point(finalScale.width(),finalScale.height()))));
            dw = Target_width;
        }else{
            finalScale = rescaled;
        }
        BMPdumper(finalScale,"ROI");
        //TODO:DEV:TEST: Check if the contour is rotated. If so make adjustement as required.
        int sh = dh/5;          //Height of an "on" segment
        int sw = dw/3;          //Width of an "on" segment
        int sA = sh*sw*255;
        double[] segThresh = new double[7]; //This allows for fine tuning of segment trigger points
        //Value is the % filled pixels that should be contained in the segment to trigger.
        //Rectangular segment checkers still use the square value since the slant only accounts for italic fonts.
        segThresh[0] = 0.1;
        segThresh[1] = 0.1;
        segThresh[2] = 0.1;
        segThresh[3] = 0.1;
        segThresh[4] = 0.1;
        segThresh[5] = 0.1;
        segThresh[6] = 0.1;

        Rect Seg0 = new Rect(new Point(dw/2-sw/2, 0), new Point(dw/2+sw/2, sh));
        drawSegCheck(finalScale,Seg0);
        Rect Seg1 = new Rect(new Point(0, dh/4 - sh / 2), new Point(sw*1.5,dh/4 + sh / 2));
        drawSegCheck(finalScale,Seg1);
        Rect Seg2 = new Rect(new Point(dw-sw,dh/4 - sh / 2),new Point(dw,dh/4 + sh / 2));
        drawSegCheck(finalScale,Seg2);
        Rect Seg3 = new Rect(new Point(dw/2-sw/2, dh/2 - sh / 2), new Point(dw/2+sw/2, dh/2 + sh / 2));
        drawSegCheck(finalScale,Seg3);
        Rect Seg4 = new Rect(new Point(0, 3*dh/4- sh / 2), new Point(sw,3*dh/4+ sh / 2));
        drawSegCheck(finalScale,Seg4);
        Rect Seg5 = new Rect(new Point(dw-2*sw, 3*dh/4 - sh / 2), new Point(dw, 3*dh/4 + sh / 2));
        drawSegCheck(finalScale,Seg5);
        Rect Seg6 = new Rect(new Point(dw/2-sw/2, dh-sh), new Point(dw/2+sw/2, dh));
        drawSegCheck(finalScale,Seg6);
        List<Rect> segboxes = new ArrayList<>();
        segboxes.add(Seg0);
        segboxes.add(Seg1);
        segboxes.add(Seg2);
        segboxes.add(Seg3);
        segboxes.add(Seg4);
        segboxes.add(Seg5);
        segboxes.add(Seg6);
        drawSegCheck(finalScale,segboxes);

        //Make provisions to extract . and , segments
        if (((dh/dw)<=2) && (dh<=0.1*ROI.height())){
            return -1;
        }else {
            List<Mat> Segments = new ArrayList<Mat>();
            Segments.add(finalScale.submat(Seg0));
            Segments.add(finalScale.submat(Seg1));
            Segments.add(finalScale.submat(Seg2));
            Segments.add(finalScale.submat(Seg3));
            Segments.add(finalScale.submat(Seg4));
            Segments.add(finalScale.submat(Seg5));
            Segments.add(finalScale.submat(Seg6));
            double segClass = 0;
            int[] seghits = new int[7];
            for (int i = 0; i < Segments.size(); i++) {
                Scalar sumM = Core.sumElems(Segments.get(i));
                if (sumM.val[0] > segThresh[i] * sA) {
                    segClass = segClass + Math.pow(2, i);
                    seghits[i] = 1;
                }else{
                    seghits[i] = 0;
                }
            }
            //Cleanup just before termination
            rescaled.release();
            finalScale.release();
            for (Mat segment:Segments) {segment.release(); }
            //Below implements a basic lookup compressed truth table.
            String seghitS = new String();
            seghitS += "[";
            for (int i=0; i<seghits.length;i++){
                seghitS += String.valueOf(seghits[i]);
                seghitS += ",";
            }
            seghitS += "]";
            Log.i("SegHitter","Segment hits for digit is "+ seghitS);

            switch ((int) (segClass)) {
                case 119:
                    return 0;
                case 18:
                    return 1;
                case 36:
                    return 1; //Accounts for the multiple locations of a 1
                case 93:
                    return 2;
                case 109:
                    return 3;
                case 46:
                    return 4;
                case 107:
                    return 5;
                case 123:
                    return 6;
                case 122:
                    return 6; //A 6 without the tail
                case 37:
                    return 7;
                case 127:
                    return 8;
                case 111:
                    return 9;
                case 47:
                    return 9; //A 9 without the tail
                default:
                    return -1;
            }
        }
    }

    public String classify_SegCount(Mat rgbImage){
        Mat ROI = identifyDigits(rgbImage,false);
        String full_classification = new String();
        for (int i=0; i<digitsID.size();i++){
            Rect box = digitsID.get(i).getBoundingBox();
            Mat window = ROI.submat(box);
            int digit_classification = SegHitter(window);
            Log.i("SegHit","Classifying digit at loc " + String.valueOf(i+1) + " as " + String.valueOf(digit_classification));
            if (digit_classification > -1){
                full_classification += String.valueOf(digit_classification);
            }
        }
        if (full_classification.length() >= 3) {
            Log.i("SegHit", "Full classification is " + full_classification);
        }
        //Release the working items
        outputText = "";
        outputText = full_classification;
        ROI.release();
        try{
            AutomaticMode_StabilityChecker(Double.parseDouble(full_classification)/10);
        }catch (Exception e){
            AutomaticMode_StabilityChecker(-1);
        }
        clearIdentifiedRegions();
        return full_classification;
    }

    public String classify_SVM(Mat rgbImage){
        if (!isLoaded){
            isLoaded = svmLoadModel();
        }
        Mat ROI = identifyDigits(rgbImage,false);
        String full_classification = new String();
        for (int i=0; i<digitsID.size();i++){
            Rect box = digitsID.get(i).getBoundingBox();
            Mat window = ROI.submat(box);
            int digit_classification = runSVM(window);
//            Log.i("SVM","Classifying digit at loc " + String.valueOf(i+1) + " as " + String.valueOf(digit_classification));
            if (digit_classification > -1){
                full_classification += String.valueOf(digit_classification);
            }
        }
        if (full_classification.length() >= 3) {
            Log.i("SVM", "Full classification is " + full_classification);
        }
        outputText = "";
        outputText = full_classification;
        //Release the working items
        ROI.release();
        try{
            AutomaticMode_StabilityChecker(Double.parseDouble(full_classification)/10);
        }catch (Exception e){
            AutomaticMode_StabilityChecker(-1);
        }
        clearIdentifiedRegions();
        return full_classification;
    }

    private int runSVM(Mat ROI){
        //Scale the input image to known sizes that is typical of 7 segment display proportions
        int Target_height = 150;
        int Target_width = 75;
        float factor = Target_height/(float)ROI.height();
        int dh = (int)Math.round(ROI.height()*factor);
        int dw = (int)Math.round(ROI.width()*factor);
        if (dw >= Target_width){
            dw = Target_width;
        }
        Mat rescaled = new Mat();
        if (factor >= 1){
            Imgproc.resize(ROI,rescaled,new Size(dw,dh),Imgproc.INTER_CUBIC);
        }else {
            Imgproc.resize(ROI, rescaled, new Size(dw, dh),Imgproc.INTER_AREA);
        }
        //At this point the image should be scaled to a known good vertical size
        //If the segment data is not wide enough add pixels to the right.
        Mat finalScale;
        if (dw < Target_width/2) {
            finalScale = Mat.zeros(Target_height, Target_width, CvType.CV_8UC1);
            rescaled.copyTo(finalScale.submat(new Rect(new Point(finalScale.width()-dw,0),new Point(finalScale.width(),finalScale.height()))));
            dw = Target_width;
        }else{
            finalScale = rescaled;
        }
        //Since SVW has been trained on small images resize final images to fit
        Mat smallROI = new Mat();
        Imgproc.resize(finalScale,smallROI,new Size(25,50),Imgproc.INTER_AREA);
        finalScale.release();

        Mat flat = smallROI.reshape(0,1);
        flat.convertTo(flat,CV_32F);
        Mat results = new Mat();
        try {
            int predictedClass = (int) mSVM.predict(flat, results, 0);
//            BMPdumper(smallROI, "ROI_SVM_Class" + String.valueOf(results.get(0, 0)[0]));
            if (results.get(0,0)[0] > -1) {
                return (int) (results.get(0, 0)[0]);
            }
        }catch(Exception e) {
            Log.i("SVM", "Could not process frame");
        }
        return -1;
    };
}


