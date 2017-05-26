package com.example.lkduy.novice;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.net.Socket;

/**
 * Created by lkduy on 4/7/2017.
 */
public class NetworkCommunicator {
    private Context context;
    private final String SERVER_IP = "129.16.213.47";
    private final int PORT = 4034;
    private Socket comSocket;
    public Socket getComSocket(){
        return comSocket;
    }
    private ConnectionEstablisedNotifier connectionEstablisedNotifier;
    public void setConnectionEstablisedNotifier(ConnectionEstablisedNotifier notifier){
        connectionEstablisedNotifier = notifier;
    }
    public NetworkCommunicator(Context ctx){
        context = ctx;
    }
    public void Connect(){
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... unused) {
                // Background Code
                try {
                    comSocket = new Socket(SERVER_IP, PORT);

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            @Override
            protected void onPostExecute(Boolean result) {

                super.onPostExecute(result);
                if(connectionEstablisedNotifier != null){
                    connectionEstablisedNotifier.connectionEstablished(result);
                }
            }
        }.execute();
    }
    public void sendVideoFrame(final Bitmap frame){
        if(comSocket != null){
                AsyncDataSendingTask sendingTask = new AsyncDataSendingTask(comSocket);
                Object[] packetParams = new Object[2];
                packetParams[0] = 9001;
                packetParams[1] = Base64.encodeToString(Utilities.getBytesFromBitmap(frame),Base64.DEFAULT);
                sendingTask.execute(packetParams);
        }
    }
}
interface ConnectionEstablisedNotifier{
    void connectionEstablished(boolean result);
}
