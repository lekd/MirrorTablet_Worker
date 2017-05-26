package com.example.lkduy.novice;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, ConnectionEstablisedNotifier, IDataReceivedEvent{
    static {
        System.loadLibrary("opencv_java3");
    }
    private final String _TAG = "ProcessedCameraActivity:";
    private CameraBridgeViewBase mOpenCvCameraView;
    private ImageView imvFrameShower;
    private ImageView imvHelperHand;
    private Button btnSwitchCam;
    private AnotationView annoView;
    NetworkCommunicator networkCommunicator;
    AsyncDataReceiver networkDataReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.openCVCamView);
        mOpenCvCameraView.setCvCameraViewListener(this);
        imvFrameShower = (ImageView)findViewById(R.id.imgVideoFrame);
        imvHelperHand = (ImageView)findViewById(R.id.imvHelperHand);
        addControls();

        networkCommunicator = new NetworkCommunicator(this);
        networkCommunicator.setConnectionEstablisedNotifier(this);
        networkCommunicator.Connect();
    }
    void addControls(){
        RelativeLayout mainContainer = (RelativeLayout)findViewById(R.id.mainContainer);
        annoView = new AnotationView(this);
        RelativeLayout.LayoutParams annoViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        annoView.setLayoutParams(annoViewParams);
        mainContainer.addView(annoView);

        btnSwitchCam = new Button(this);
        btnSwitchCam.setText("SwitchCam");
        btnSwitchCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCam();
            }
        });
        btnSwitchCam.setTextSize(14);
        RelativeLayout.LayoutParams btnSwitchCamParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnSwitchCamParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        btnSwitchCamParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        btnSwitchCam.setLayoutParams(btnSwitchCamParams);
        mainContainer.addView(btnSwitchCam);

    }
    @Override
    public void connectionEstablished(boolean result) {
        networkDataReceiver = new AsyncDataReceiver(networkCommunicator.getComSocket());
        networkDataReceiver.setDataReceivedEventHandler(this);
        new Thread(networkDataReceiver).start();
    }
    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        String TAG = new StringBuilder(_TAG).append("onResume").toString();
        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initiation");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, loaderCallback);
        } else {
            Log.i(TAG, "OpenCV library found inside package. Using it");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    @Override
    protected void onPause() {
        String TAG = new StringBuilder(_TAG).append("onPause").toString();
        Log.i(TAG, "Disabling a camera view");

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }

        super.onPause();
    }
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            String TAG = new StringBuilder(_TAG).append("onManagerConnected").toString();

            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
            }
        }
    };
    @Override
    public void onCameraViewStopped() {

    }
    Mat origin;
    org.opencv.core.Size resultSize = null;
    Mat rotatedOrigin = null;
    Mat bbox = null;
    Mat originFrontRotMatrix = null;
    Mat originBackRotMatrix = null;
    Bitmap videoFrame;
    Size smallSize = null;
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        origin = inputFrame.rgba();
        if(smallSize == null){
            smallSize = new Size(origin.width()/2, origin.height()/2);
        }
        Imgproc.resize(origin,origin, smallSize, 0, 0, Imgproc.INTER_CUBIC);
        int maxDimension = origin.width()>origin.height()?origin.width():origin.height();
        //int maxDimension = smallSizeFrame.width()>smallSizeFrame.height()?smallSizeFrame.width():smallSizeFrame.height();
        if(bbox == null){
            bbox = new Mat(maxDimension,maxDimension,origin.type());
        }
        if (rotatedOrigin == null) {
            //rotatedOrigin = new Mat(origin.cols(),origin.rows(),origin.type());
            resultSize = new Size(origin.height(),origin.width());
            //resultSize = new Size(smallSizeFrame.height(),smallSizeFrame.width());
        }
        if(originFrontRotMatrix == null){
            originFrontRotMatrix = Imgproc.getRotationMatrix2D(new Point(bbox.width()/2,bbox.height()/2),90,1.0);
            originBackRotMatrix = Imgproc.getRotationMatrix2D(new Point(bbox.width()/2,bbox.height()/2),90,1.0);
        }
        if(mCameraId == 0) {
            Imgproc.warpAffine(origin, bbox, originBackRotMatrix, resultSize, Imgproc.INTER_LINEAR);
            //Imgproc.warpAffine(smallSizeFrame, bbox, originBackRotMatrix, resultSize, Imgproc.INTER_LINEAR);
            Core.flip(bbox,bbox,-1);
        }
        else{
            Imgproc.warpAffine(origin, bbox, originFrontRotMatrix, resultSize, Imgproc.INTER_LINEAR);
            //Imgproc.warpAffine(smallSizeFrame, bbox, originFrontRotMatrix, resultSize, Imgproc.INTER_LINEAR);
            Core.flip(bbox,bbox,1);
        }


        rotatedOrigin = bbox.clone();
        videoFrame = Utilities.getBitmapOfMat(rotatedOrigin,false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imvFrameShower.setImageBitmap(videoFrame);
            }
        });
        networkCommunicator.sendVideoFrame(videoFrame);
        videoFrame = null;
        return null;
    }

    int mCameraId = 0;
    void switchCam(){
        mCameraId = mCameraId^1;
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(mCameraId);
        mOpenCvCameraView.enableView();
        smallSize = null;
    }


    @Override
    public void dataReceived(int messageCode, Object[] data) {
        switch(messageCode){
            case 9002:
                int pathID = (int)data[0];
                float relativeX = (float)data[1];
                float relativeY = (float)data[2];
                int eventType = (int)data[3];
                if(annoView.getWidth()==0 || annoView.getHeight() == 0){
                    annoView.addPointingEvent(pathID,relativeX,relativeY,eventType);
                }
                else
                {
                    float x = relativeX * annoView.getWidth();
                    float y = relativeY * annoView.getHeight();
                    annoView.addPointingEvent(pathID,x,y,eventType);
                }
                break;
            case 9003:
                byte[] handBmpData_64Base = (byte[])data[0];
                showRemoteHelperHand(handBmpData_64Base);
                break;
        }
    }
    Bitmap helperHandBmp;
    BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
    HandBitmapOrientationAdjuster handOrientationAdjuster = new HandBitmapOrientationAdjuster();
    void showRemoteHelperHand(byte[] handBmpData_64Base){
        byte[] rawBmpData = Base64.decode(handBmpData_64Base,Base64.DEFAULT);
        helperHandBmp = BitmapFactory.decodeByteArray(rawBmpData,0, rawBmpData.length, bitmap_options);
        helperHandBmp = handOrientationAdjuster.RotateAndFlip(helperHandBmp);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    imvHelperHand.setImageBitmap(helperHandBmp);

                }catch (Exception ex){
                    Log.i("ParsingFrame", "Error parsing");
                }
            }
        });
    }
}
