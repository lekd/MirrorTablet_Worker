package com.example.lkduy.novice;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by lkduy on 4/8/2017.
 */
public class SyncDataSender {
    static boolean readyToSend = true;
    Socket sendingSocket;
    public SyncDataSender(Socket s){
        sendingSocket = s;
    }
    public void send(Object[] params){
        if (readyToSend == false) {
            return;
        }
        int msgCode = (int)params[0];
        String bitmapData = (String)params[1];

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
        readyToSend = true;
    }
}
