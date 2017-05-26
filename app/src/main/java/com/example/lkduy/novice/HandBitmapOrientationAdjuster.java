package com.example.lkduy.novice;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * Created by lkduy on 5/18/2017.
 */

public class HandBitmapOrientationAdjuster {
    Matrix matrix = null;
    public Bitmap RotateAndFlip(Bitmap handBmp){
        if(matrix == null){
            matrix = new Matrix();
            matrix.postRotate(-90);
            matrix.postScale(-1,1);
        }
        return Bitmap.createBitmap(handBmp,0,0,handBmp.getWidth(),handBmp.getHeight(),matrix, true);
    }
}
