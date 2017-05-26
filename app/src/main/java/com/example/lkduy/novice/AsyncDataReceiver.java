package com.example.lkduy.novice;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by lkduy on 4/9/2017.
 */
public class AsyncDataReceiver implements Runnable{
    private Socket recvSocket;
    boolean flagIsRunning = false;
    public AsyncDataReceiver(Socket s){
        recvSocket = s;
    }
    IDataReceivedEvent dataReceivedEventHandler = null;
    public  void setDataReceivedEventHandler(IDataReceivedEvent handler){
        dataReceivedEventHandler = handler;
    }
    @Override
    public void run() {
        try {
            recvSocket.setTcpNoDelay(true);
            recvSocket.setKeepAlive(true);
            flagIsRunning = true;
            InputStream inputStream = recvSocket.getInputStream();
            DataInputStream is = new DataInputStream(inputStream);
            while(flagIsRunning) {
                int messageCode = is.readInt();
                switch (messageCode){
                    case 9001:
                        receiveImageMessage(messageCode, is, "Frame");
                        break;
                    case 9002:
                        receivePointingMessage(messageCode, is);
                        break;
                    case 9003:
                        receiveImageMessage(messageCode, is, "Hand");
                        break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void receiveImageMessage(int messageCode, DataInputStream is, String tagName){
        try {
            if(is.readUTF().equals(String.format("+%s+", tagName))){
                int bmpByteLength = is.readInt();
                if(is.readUTF().equals(String.format("-%s-", tagName))) {
                    byte[] buffer = new byte[bmpByteLength];
                    int len = 0;
                    while(len < bmpByteLength){
                        len += is.read(buffer, len, bmpByteLength - len);
                    }
                    if(dataReceivedEventHandler != null){
                        Object[] eventParams = new Object[1];
                        eventParams[0] = buffer;
                        dataReceivedEventHandler.dataReceived(messageCode,eventParams);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void receivePointingMessage(int messageCode, DataInputStream is){
        try {
            if(is.readUTF().equals("+Pointing+")){
                int pathID = is.readInt();
                float x = is.readFloat();
                float y = is.readFloat();
                int eventType = is.readInt();
                if(dataReceivedEventHandler != null) {
                    Object[] eventParams = new Object[4];
                    eventParams[0] = pathID;
                    eventParams[1] = x;
                    eventParams[2] = y;
                    eventParams[3] = eventType;
                    dataReceivedEventHandler.dataReceived(messageCode,eventParams);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stop(){
        flagIsRunning = false;
        try {
            Thread.sleep(500);
            if(recvSocket != null){
                recvSocket.close();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
interface IDataReceivedEvent{
    void dataReceived(int messageCode, Object[] data);
}