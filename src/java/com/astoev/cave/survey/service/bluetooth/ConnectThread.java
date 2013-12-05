package com.astoev.cave.survey.service.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;
import com.astoev.cave.survey.Constants;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 11/22/13
 * Time: 11:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectThread extends Thread {

    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final int REC_MODE_PLUS = 9;
    private static final int REC_MODE_MINUS = 10;

    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;
    private InputStream mIn;
    private OutputStream mOut;
    private boolean running = true;

    public ConnectThread(BluetoothDevice device) throws IOException {
        mDevice = device;

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }

        Log.i(Constants.LOG_TAG_UI, "Prepare client");
        if (Build.VERSION.SDK_INT < 10) {
            mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
        } else {
            mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
        }
        mSocket.connect();
        mIn = mSocket.getInputStream();
        mOut = mSocket.getOutputStream();


    }

    public void sendMessage(byte[] aMessage) throws IOException {
        mOut.write(aMessage);
    }

    @Override
    public void run() {
        Log.i(Constants.LOG_TAG_UI, "Start client");

        byte[] buffer = new byte[1024];
        Log.i(Constants.LOG_TAG_UI, "Start reading ");
//            while (running) {
        try {
            int i = mIn.read(buffer);
//                    if (i == -1) {
//                        // nothing from the stream
//                        break;
//                    }

            Log.i(Constants.LOG_TAG_UI, "Got bytes " + i);
            Log.i(Constants.LOG_TAG_UI, "Got bytes " + new String(buffer, 0, i));

            Log.i(Constants.LOG_TAG_UI, "error code" + buffer[4]);
            Log.i(Constants.LOG_TAG_UI, "rec mode" + buffer[5]);
            Log.i(Constants.LOG_TAG_UI, "measure location" + buffer[6]);
            Log.i(Constants.LOG_TAG_UI, "units " + new String[]{" ", "m", "in", "in+", "ft", "ft&in"}[buffer[7]]);

            Log.i(Constants.LOG_TAG_UI, "real measure to go");

            for (int j = 7; j < i; j++) {
                Log.i(Constants.LOG_TAG_UI, "Byte " + j + " " + buffer[j]);
                Log.i(Constants.LOG_TAG_UI, "Byte " + j + " " + (256 + buffer[j]) % 256);
            }

            for (int j = 0; j < 4; j++) {
                Log.i(Constants.LOG_TAG_UI, "Measure " + j + " " +
                        (0xFF000000 & buffer[(8 + j * 4)] << 24
                                | 0xFF0000 & buffer[(9 + j * 4)] << 16
                                | 0xFF00 & buffer[(10 + j * 4)] << 8
                                | 0xFF & buffer[(11 + j * 4)]));

            }

           /* if (paramString.equals("°"))
            {
                double d7 = paramInt / 10.0D;
                Object[] arrayOfObject14 = new Object[1];
                arrayOfObject14[0] = Double.valueOf(d7);
                return String.format("%.1f", arrayOfObject14);
            }*/


            /*if ((paramString.equals("m")) || (paramString.equals("m²")) || (paramString.equals("m³")))
            {
                double d2 = paramInt / 1000.0D;
                Object[] arrayOfObject9 = new Object[1];
                arrayOfObject9[0] = Double.valueOf(d2);
                return String.format("%.3f", arrayOfObject9);
            }*/

            sleep(100);

        } catch (Exception connectException) {
            Log.e(Constants.LOG_TAG_UI, "Error client connect", connectException);
        }
//            }

        Log.i(Constants.LOG_TAG_UI, "End client");
    }

    public void cancel() {
        try {
            Log.i(Constants.LOG_TAG_UI, "Cancel client");
            if (mSocket != null) {
                if (mSocket.isConnected()) {
                    mSocket.close();
                }
            }
            IOUtils.closeQuietly(mIn);
            IOUtils.closeQuietly(mOut);
        } catch (IOException e) {
            Log.i(Constants.LOG_TAG_UI, "Error cancel client");
        }
    }
}