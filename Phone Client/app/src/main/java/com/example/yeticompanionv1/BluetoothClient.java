package com.example.yeticompanionv1;

import android.bluetooth.*;
import android.util.*;

import java.io.*;
import java.lang.String;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

public class BluetoothClient {
	private static final String TAG = "BluetoothClient";
	private static final UUID MY_UUID_INSECURE =
			UUID.fromString("b03e9d9c-ff1e-11e4-a322-1697f925ec7b");

	private BluetoothDevice mmDevice;
	private UUID deviceUUID;
	private ClientThread clientThread;
	private MainActivity activity;

	BluetoothClient(MainActivity activity){
		this.activity = activity;
	}

	private class ClientThread extends Thread {
		private BluetoothSocket socket;
		private InputStream in;
		private OutputStream out;

		ClientThread(BluetoothDevice device, UUID uuid) {
			Log.d(TAG, "ClientThread started.");
			mmDevice = device;
			deviceUUID = uuid;
			clientThread = null;
		}

		public void run() {
			/*try {
				Log.d(TAG, "ClientThread: Trying to create InsecureRfcommSocket using UUID: " + MY_UUID_INSECURE);
				socket = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
			}
			catch (Exception e) {
				Log.e(TAG, "ClientThread: Could not create InsecureRfcommSocket " + e.getMessage());
				return;
			}*/
			//-----------------------------------------------------------------------
			try {
				Method m = mmDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class});
				socket = (BluetoothSocket) m.invoke(mmDevice, 1);
			}
			catch (Exception e){
				Log.e(TAG, "ClientThread: Could not create InsecureRfcommSocket " + e.getMessage());
				return;
			}
			//-----------------------------------------------------------------------


			if(socket == null)
				return;
			
			try {
				socket.connect();
				Log.d(TAG, "run: ClientThread connected.");
			}
			catch (Exception e) {
				Log.d(TAG, "run: ClientThread: Failed with error " + e.getMessage());
				cancel();
				return;
			}

			try{
				in = socket.getInputStream();
				out = socket.getOutputStream();
			}
			catch(Exception e){
				Log.d(TAG, "run:ClientThread: Failed to create IOstreams " + e.getMessage());
				cancel();
				return;
			}

			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					activity.switchActivity(1);
				}
			});

			//read Loop
			byte[] buffer = new byte[9];
			int bytes;
			while (true) {
				try {
					bytes = in.read(buffer);
					String incomingMessage = new String(buffer, 0, bytes);
					Log.d(TAG, "InputStream: " + incomingMessage);

				} catch (IOException e) {
					Log.e(TAG, "read: Error reading input stream. " + e.getMessage());
					cancel();
					break;
				}
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

		private void cancel() {
			close();
		}

		public void close(){
			try {
				Log.d(TAG, "cancel: Closing Client Socket.");
				if(in != null)
					in.close();
				if(out != null)
					out.close();
				if(socket != null)
					socket.close();
			}
			catch(IOException e){
				Log.e(TAG, "cancel: close() of socket in clientThread failed. " + e.getMessage());
			}
			activity.runOnUiThread(new Runnable() {
				   @Override
				   public void run() {
					   activity.switchActivity(0);
				   }
			});
		}

	}

	public void startClient (BluetoothDevice device, UUID uuid) {
		Log.d(TAG, "startClient: Started.");
		if(clientThread != null)
			clientThread.close();
		clientThread = new ClientThread(device, uuid);
		clientThread.start();
	}

	public void write(byte[] out) throws IOException{
		if(clientThread != null){
			Log.d(TAG, "write: Write called.");
			if(clientThread.write(out) == -1)
				throw new IOException("Connection Failed");
		}
		else
			Log.d(TAG, "Error: ConnectedThread Not Initialized");
	}
}
