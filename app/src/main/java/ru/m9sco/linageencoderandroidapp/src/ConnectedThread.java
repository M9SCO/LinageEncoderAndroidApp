package ru.m9sco.linageencoderandroidapp.src;


import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.m9sco.linageencoderandroidapp.enums.HandlerCodes;

//Class that given an open BT Socket will
//Open, manage and close the data Stream from the Arduino BT device
public class ConnectedThread extends Thread {

    private static final String TAG = "FrugalLogs";
    Handler handler;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private String valueRead;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        this.handler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }
        //Input and Output streams members of the class
        //We wont use the Output stream of this project
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public String getValueRead(){
        return valueRead;
    }

    public void run() {
        if(!mmSocket.isConnected()) {
            handler.obtainMessage(HandlerCodes.META.ordinal(), "Connected error").sendToTarget();

            return;
        }


        byte[] buffer = new byte[1024];
        int bytes = 0; // bytes returned from read()

        handler.obtainMessage(HandlerCodes.META.ordinal(), "Connection Success" ).sendToTarget();

        while (true) {
            try {

                buffer[bytes] = (byte) mmInStream.read();
                String readMessage;
                bytes++;
                readMessage = new String(buffer, 0, bytes);

                if(readMessage.endsWith("\r\n")){
                    Log.e(TAG, readMessage);
                    valueRead=readMessage;
                    handler.obtainMessage(HandlerCodes.DATA.ordinal(), readMessage).sendToTarget();
                    bytes = 0;

                }


            } catch (IOException e) {
                handler.obtainMessage(HandlerCodes.META.ordinal(), "Disconnected").sendToTarget();

                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }

    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}