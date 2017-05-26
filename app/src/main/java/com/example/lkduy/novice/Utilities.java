package com.example.lkduy.novice;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;

/**
 * Created by lkduy on 4/7/2017.
 */
public class Utilities {
    public  static Bitmap getBitmapOfMat(Mat img, boolean isTransparent){
        Bitmap bmp = null;
        try {
            if(isTransparent) {
                bmp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
            }
            else{
                bmp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.RGB_565);
            }
            Utils.matToBitmap(img, bmp);
        } catch (CvException e) {
            Log.d("SAVING IMAGE", e.getMessage());
        }
        return bmp;
    }
    public  static byte[] getBytesFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50,stream);
        return stream.toByteArray();
    }

}
