package com.example.yeticompanionv1;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class MainActivity extends AppCompatActivity{
    public BluetoothState btState;
    BluetoothClient bluetoothClient;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("b03e9d9c-ff1e-11e4-a322-1697f925ec7b");//in all honesty i have no idea how this works
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) { //check for file write permissions for writing config file
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        btState = new BluetoothState(this);
    }

    public void initClient(BluetoothDevice BTDevice){
        bluetoothClient = new BluetoothClient();
        bluetoothClient.startClient(BTDevice,MY_UUID_INSECURE);
    }
}
