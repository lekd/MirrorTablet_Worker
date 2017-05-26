package com.example.lkduy.novice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

/**
 * Created by lkduy on 4/7/2017.
 */
public class AnotationView extends View {
    private Paint paint = new Paint();
    List<PathWithTime> existingPaths = new ArrayList<PathWithTime>();
    PathWithTime onGoingPath = new PathWithTime();

    public AnotationView(Context ctx){
        super(ctx);
        this.setBackgroundColor(Color.TRANSPARENT);
        initializePaint();
        schedulingToRefresh();
    }
    public AnotationView(Context context, AttributeSet attrs) {
        super(context);
        // TODO Auto-generated constructor stub
        this.setBackgroundColor(Color.TRANSPARENT);
        initializePaint();
        schedulingToRefresh();
    }

    void schedulingToRefresh(){
        final android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
        Runnable runnableRefresh = new Runnable() {
            @Override
            public void run() {
                invalidate();
                handler.postDelayed(this,100);
            }
        };
        handler.postDelayed(runnableRefresh,100);

    }
    void initializePaint(){
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10f);
        paint.setColor(Color.CYAN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }
    @Override
    protected void onDraw(Canvas canvas){
        long curTimeStamp = System.currentTimeMillis();
        List<PathWithTime> unvalidPaths = new ArrayList<PathWithTime>();
        for(int i=0; i< existingPaths.size(); i++){
            PathWithTime curPath = existingPaths.get(i);
            long elapsedTime = curTimeStamp - curPath.get_timeStamp();
            if(elapsedTime > 2000){
                unvalidPaths.add(curPath);
                continue;
            }
            int opacity = (int)((1.0 - (elapsedTime - 1000)*1.0/1000)*255);
            opacity = opacity>255 ? 255:opacity;
            paint.setColor(Color.argb(opacity,0,255,255));
            canvas.drawPath(curPath.getPath(),paint);
        }
        paint.setColor(Color.CYAN);
        if(onGoingPath != null) {
            canvas.drawPath(onGoingPath.getPath(), paint);

            for (int i = 0; i < unvalidPaths.size(); i++) {
                existingPaths.remove(unvalidPaths.get(i));
            }
            if(System.currentTimeMillis() - lastUpdate > 2000){
                finalizeOnGoingPath();
                onGoingPath = null;
            }
        }
    }
    long lastUpdate = System.currentTimeMillis();
    public void addPointingEvent(int pathID,float x, float y,int eventType){
        if(onGoingPath == null){
            onGoingPath = new PathWithTime();
            onGoingPath.setID(pathID);
            onGoingPath.getPath().moveTo(x,y);
        }
        else {
            if( onGoingPath.getID() != pathID){
                finalizeOnGoingPath();
                onGoingPath = new PathWithTime();
                onGoingPath.setID(pathID);
                onGoingPath.getPath().moveTo(x,y);
            }
            if(eventType == MotionEvent.ACTION_MOVE) {
                onGoingPath.getPath().lineTo(x, y);
            }
            if(eventType == MotionEvent.ACTION_UP){
                finalizeOnGoingPath();
                onGoingPath = null;
            }
        }
        lastUpdate = System.currentTimeMillis();
    }

    private void finalizeOnGoingPath() {
        onGoingPath.set_timeStamp(System.currentTimeMillis());
        existingPaths.add(onGoingPath);
    }
}
