package com.example.lkduy.novice;

import android.graphics.Path;

/**
 * Created by lkduy on 4/7/2017.
 */
public class PathWithTime {
    private Path _path;
    private long _timeStamp;
    private int _id;
    public void setID(int id){_id = id;}
    public int getID(){return _id;}
    public PathWithTime(){
        _path = new Path();
    }
    public Path getPath(){
        return _path;
    }
    public void setPath(Path p){
        _path = new Path();
        _path.set(p);
    }
    public void set_timeStamp(long t){
        _timeStamp = t;
    }
    public long get_timeStamp(){
        return _timeStamp;
    }
    public PathWithTime clone(){
        PathWithTime clone = new PathWithTime();
        clone.setID(_id);
        clone.set_timeStamp(_timeStamp);
        clone.setPath(_path);
        return clone;
    }
}
