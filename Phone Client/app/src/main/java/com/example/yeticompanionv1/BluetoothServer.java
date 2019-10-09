package com.example.yeticompanionv1;

import android.bluetooth.*;
import android.util.*;

import java.io.*;
import java.lang.String;
import java.nio.charset.Charset;
import java.util.*;

public class BluetoothServer {
    private static final String TAG = "BluetoothClient";
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("b03e9d9c-ff1e-11e4-a322-1697f925ec7b");

    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    private ServerThread serverThread;
    private BluetoothAdapter bluetoothAdapter;

    private class ServerThread extends Thread {
        private BluetoothServerSocket serverSocket = null;
        private InputStream in;
        private OutputStream out;

        public ServerThread() {
            try {
                serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("AppName", MY_UUID_INSECURE);
                Log.d(TAG, "ServerThread: Setting up Server using: " + MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "ServerThread: IOException: " + e.getMessage());
            }
        }

        public void run() {
            Log.d(TAG, "run: ServerThread Running");

            BluetoothSocket socket = null;
            try{
                if(out != null )
                    out.close();
            }
            catch(Exception e){
                Log.e(TAG, e.getMessage());
            }

            try{
                if(in != null )
                    in.close();
            }
            catch(Exception e){
                Log.e(TAG, e.getMessage());
            }

            try {
                Log.d(TAG, "run RFCOMM server socket start....");
                socket = serverSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "ServerThread: IOException: " + e.getMessage());
                cancel();
                return;
            }

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (Exception e) {
                Log.d(TAG, "run:ServerThread: Failed to create IO-streams " + e.getMessage());
                cancel();
                return;
            }

            //Read Loop--add callback for dealing with data
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = in.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading inputstream. " + e.getMessage());
                    cancel();
                    break;
                }
            }
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling ServerThread.");
            try {
                if(in != null)
                    in.close();
                if(out != null)
                    out.close();
                if(serverSocket != null)
                    serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Cancel: Close of ServerThread ServerSocket failed. " + e.getMessage());
            }
        }

        public int write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                out.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to outputstream. " + e.getMessage());
                cancel();
                return -1;
            }
            return 1;
        }
    }

    public void startServer (BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: Started.");
        if(serverThread != null)
            serverThread.cancel();
        serverThread = new ServerThread();
        serverThread.start();
    }

    public void write(byte[] out) throws IOException{
        if(serverThread != null){
            Log.d(TAG, "write: Write called.");
            if(serverThread.write(out) == -1)
                throw new IOException("Connection Failed");
        }
        else
            Log.d(TAG, "Error: ServerThread Not Initialized");
    }
}
