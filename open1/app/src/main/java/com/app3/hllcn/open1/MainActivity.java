package com.app3.hllcn.open1;

import java.util.Arrays;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;

import static android.R.attr.centerY;

public class ColorBlobDetectionActivity extends Activity implements CvCameraViewListener2 {
    private static final String  TAG = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);


    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
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
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);     //// height ile width işlediği resmin boyutu.
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }
   // public boolean onTouch(View v, MotionEvent event)
 /*   public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        //Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
        //Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2GRAY);
        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        //Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }
*/
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Mat myimage ;
        myimage = mRgba;
        double [] pixel = mRgba.get(0,0);
        //Imgproc.cvtColor(mRgba,mRgba,Imgproc.COLOR_RGB2GRAY);
        //Imgproc.cvtColor(mRgba,	mRgba,Imgproc.COLOR_RGB2HLS_FULL);
        //Imgproc.resize(mRgba,mRgba,new Size(),0.5,0.5,Imgproc.INTER_AREA);
        //ExifInterface exif = ExifInterface(inputFrame);
        //int numberofchannel = myimage.depth();
        //Log.e(TAG, "Channel: " + mRgba.total());
        //Imgproc.cvtColor(mRgba,myimage,Imgproc.COLOR_RGBA2GRAY);


///Detect cirle
       /*
        Mat	grayImage=new Mat();
        Imgproc.cvtColor(mRgba,grayImage,Imgproc.COLOR_RGB2GRAY);
        double	minDist=20;
        int	thickness=5;
        double	cannyHighThreshold=150;
        double	accumlatorThreshold=50;
        Mat	circles	=	new	Mat();
        Imgproc.HoughCircles(grayImage,circles,Imgproc.CV_HOUGH_GRADIENT,1,minDist,cannyHighThreshold,accumlatorThreshold,0,0);
        Imgproc.cvtColor(grayImage,	grayImage,	Imgproc.COLOR_GRAY2RGB);
        for	(int i	=	0;	i	<	circles.cols();	i++){
            double[]circle = circles.get(0,	i);
            double	centerX	=circle[0],
                    radius	=circle[2];
            org.opencv.core.Point	center	=	new	org.opencv.core.Point(centerX,	centerY);
            Imgproc.circle(grayImage,center,(int)radius,new	Scalar(0,0,255),thickness); }
     */
///Detect circle


///Detecting and drawing lines
       /*
        Mat	binaryImage=new	Mat();
        Imgproc.cvtColor(mRgba,binaryImage,Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(binaryImage,binaryImage,80,100);
        Mat	lines=new Mat();
        int	threshold =	50;
        Imgproc.HoughLinesP(binaryImage, lines, 1, Math.PI/180, threshold);
        for (int x = 0; x < lines.rows(); x++)
        {
            double[] vec = lines.get(x, 0);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            double dx = x1 - x2;
            double dy = y1 - y2;
            double dist = Math.sqrt (dx*dx + dy*dy);
            if(dist>300.d)  // show those lines that have length greater than 300
                Imgproc.line(binaryImage, start, end, new Scalar(0,0,255),3);// here initimg is the original image.////
           }
           */


///Detecting and drawing lines

////Edge Detector
        /*
        Mat	blurredImage=new Mat();
        Size size=new Size(7,7);
        Imgproc.GaussianBlur(mRgba,	blurredImage,size,0,0);
        Mat	gray=new Mat();
        Imgproc.cvtColor(blurredImage,gray,Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(gray,mRgba,50,100);*/

       /* Mat	xFirstDervative	=new Mat(),yFirstDervative	=new Mat();
        int	ddepth=CvType.CV_16S;
        Imgproc.Sobel(gray,	xFirstDervative,ddepth,1,0);
        Imgproc.Sobel(gray,	yFirstDervative,ddepth,0,1);
        Mat	absXD=new	Mat(),absYD=new	Mat();
        Core.convertScaleAbs(xFirstDervative,absXD);
        Core.convertScaleAbs(yFirstDervative,absYD);
         Core.addWeighted(absXD,0.5,absYD,0.5,0,mRgba);    */
///Edge detector


 ////Enhancing RGB
        //Mat	redEnhanced=new	Mat();
        ///mRgba.copyTo(redEnhanced);
        //Mat redmask = new Mat(mRgba.rows(),mRgba.cols(),mRgba.type(),new Scalar(1,0,0,0));
        //enhanceChannel(redEnhanced,redmask);
///Enhancing RGb


//////Convert rgb to hSV***************
     /*   Mat	V=new	Mat(mRgba.rows(),mRgba.cols(),CvType.CV_8UC1);
        Mat	S=new	Mat(mRgba.rows(),mRgba.cols(),CvType.CV_8UC1);

        Mat HSV = new Mat();
        Imgproc.cvtColor(mRgba,HSV,Imgproc.COLOR_RGB2HSV);

        byte[]	Vs=new	byte[3];
        byte[]	vsout=new	byte[1];
        byte[]	ssout=new	byte[1];
        for(int	i=0;i<HSV.rows();i++){
            for(int	j=0;j<HSV.cols();j++){
                HSV.get(i,j,Vs);
                V.put(i,j,new byte[]{Vs[2]});
                S.put(i,j,new byte[]{Vs[1]});}}
        Imgproc.equalizeHist(V,	V);
        Imgproc.equalizeHist(S,	S);
        for(int	i=0;i<HSV.rows();i++){
            for(int	j=0;j<HSV.cols();j++){
                V.get(i,j,vsout);
                S.get(i,j,ssout);
                HSV.get(i,j,Vs);
                Vs[2]=vsout[0];
                Vs[1]=ssout[0];
                HSV.put(i,j,Vs);}}
        Imgproc.cvtColor(HSV,mRgba,Imgproc.COLOR_HSV2RGB);   */
//////Convert rgb to hsv***************




//////Histogram kodu*******************************************

        int	mHistSizeNum	=	25;
        Point  mp1;
        Point  mp2;
        MatOfInt mHistSize=new MatOfInt(mHistSizeNum);
        Mat	hist	=	new	Mat();
        float	[]mBuff	=	new	float[mHistSizeNum];
        MatOfFloat	histogramRanges	=	new MatOfFloat(0f,	256f);
        Scalar	mColorsRGB[]=new Scalar[]{
                new	Scalar(200,0,0,255),new	Scalar(0,200,0,255),new	Scalar(0,0,200,255)};
        mp1 = new Point();
        mp2 = new Point();
        int	thikness=(int)(mRgba.width()/(mHistSizeNum+10)/3);
        if(thikness>	3)	thikness	=	3;
        MatOfInt mChannels[]=new MatOfInt[]	{	new	MatOfInt(0),	new	MatOfInt(1),	new	MatOfInt(2)	};
        Size sizeRgba =	mRgba.size();
        int	offset=(int)((sizeRgba.width-(3*mHistSizeNum+30)*thikness));
        for(int	c=0;c<3;c++) {
            Imgproc.calcHist(Arrays.asList(mRgba), mChannels[c],new	Mat(),hist,mHistSize,histogramRanges);
            Core.normalize(hist,hist,sizeRgba.height/2,	0,Core.NORM_INF);
            hist.get(0,0,mBuff);
            for(int	h=0;h<mHistSizeNum;h++){
                mp1.x = mp2.x=offset+(c	*(mHistSizeNum+10)+h)*thikness;
                mp1.y	=	sizeRgba.height-1;
                mp2.y	=	mp1.y-(int)mBuff[h];
                Imgproc.line(mRgba,mp1,mp2,mColorsRGB[c],thikness);}}
//////Histogram kodu*************************************************

/*
         if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
           // Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);
        }
            */
        return mRgba;
    }

    private	void enhanceChannel(Mat	imageToEnhance,Mat	mask) {
        Mat channel = new Mat(mRgba.rows(), mRgba.cols(), CvType.CV_8UC1);
        mRgba.copyTo(channel, mask);
        Imgproc.cvtColor(channel, channel, Imgproc.COLOR_RGB2GRAY, 1);
        Imgproc.equalizeHist(channel,channel);
        Imgproc.cvtColor(channel, channel, Imgproc.COLOR_GRAY2RGB, 3);
        channel.copyTo(imageToEnhance, mask);}

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1,1,CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));}
}
