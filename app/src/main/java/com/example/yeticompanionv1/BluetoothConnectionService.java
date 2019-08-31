package com.example.yeticompanionv1;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.String;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";

    private static final String appName = "MYAPP";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    /**
     * Listens for incoming connections
     */
    private class AcceptThread extends Thread {
        //Local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            //Creates a new listening server socket
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);

            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            mmServerSocket = tmp;

        }

        public void run(){
            Log.d(TAG, "run: cceptThread Running");

            BluetoothSocket socket = null;

            try{
                //Will only return on a successful connection or an exception
                Log.d(TAG, "run RFCOM server socket start....");

                socket = mmServerSocket.accept();

            }catch(IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());

            }

            //In third video
            if (socket != null) {
                connected(socket,mmDevice);

            }

            Log.i(TAG, "END mAcceptThread");

        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");

            try {
                mmServerSocket.close();

            }catch(IOException e){
                Log.e(TAG, "Cancel: CLose of AcceptThread ServerSocket failed. " + e.getMessage());

            }

        }

    }

    //Runs while attempting to make an outgoing connection to a device
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "COnnectThread started.");
            mmDevice = device;
            deviceUUID = uuid;

        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread");

            //get a Bluetooth Socket for a connection with the desired device
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: " + MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);

            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());

            }

            mmSocket = tmp;

            //Always cancel discovery after a connection is found
            mBluetoothAdapter.cancelDiscovery();

            try {
                //only returns on a successful connection or exception
                mmSocket.connect();

                Log.d(TAG, "run: ConnectThread connected.");

            } catch (IOException e) {
                //close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket");

                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket" + e1.getMessage());

                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE);

            }

            //3rd video
            connected(mmSocket, mmDevice);

        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing CLient Socket.");
                mmSocket.close();

            }catch(IOException e){
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());

            }

        }

    }

    //starts the communication service
    public synchronized void start() {
        Log.d(TAG, "start");

        //cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;

        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();

        }

    }

    //AcceptThread starts and waits for a connection, then ConnectThread attempts to connect with other devices
    public void startClient (BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: Started.");

        //initprogress dialog
        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please wait...", true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();

    }

    //Maintains the Bluetooth connection, and relays data
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss progressdialog when connection is made
            mProgressDialog.dismiss();

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

        public void run() {
            byte[] buffer = new byte[1024]; //buffer store for stream

            int bytes; //bytes returned from read()

            //keep listening to InputStream until an exception occurs
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading inputstream. " + e.getMessage());
                    break;
                }
            }

        }

        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);

            try {
                mmOutStream.write(bytes);

            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to outputstream. " + e.getMessage());

            }

        }

        //call this in the main activity to end the connection
        public void cancel() {
            try {
                mmSocket.close();

            } catch (IOException e) { }

        }

    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: starting.");

        //start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

    }

    //write to the ConnectedThread
    public void write(byte[] out) {
        //create temporary object
        ConnectedThread r;

        //synchronize a copy of ConnectedThread
        Log.d(TAG, "write: Write called.");
        mConnectedThread.write(out);

    }

}
