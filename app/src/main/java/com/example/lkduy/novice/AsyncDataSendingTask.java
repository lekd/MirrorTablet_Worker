package com.example.lkduy.novice;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by lkduy on 4/7/2017.
 */
public class AsyncDataSendingTask extends AsyncTask<Object, Void, Boolean> {
    static boolean readyToSend = true;
    Socket sendingSocket = null;
    public AsyncDataSendingTask(Socket s){
        sendingSocket = s;
    }
    @Override
    protected Boolean doInBackground(Object[] params) {
        if (readyToSend == false) {
            return true;
        }
        int msgCode = (int)params[0];
        String bitmapData =(String)params[1];

        readyToSend = false;
        try {
            sendingSocket.setKeepAlive(true);
            OutputStream os = sendingSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeInt(msgCode);
            switch (msgCode){
                case 9001:
                    //define token messages
                    //9001 = video frame
                    dos.writeUTF("+Frame+");
                    dos.writeInt(bitmapData.getBytes().length);
                    dos.writeUTF("-Frame-");
                    dos.flush();
                    dos.writeBytes(bitmapData);
                    dos.flush();
                    break;
            }
        }catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    @Override
    protected void onPostExecute(Boolean result) {

        super.onPostExecute(result);
        readyToSend = true;
    }
}
