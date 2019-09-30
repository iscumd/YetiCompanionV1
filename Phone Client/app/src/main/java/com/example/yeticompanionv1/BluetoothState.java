package com.example.yeticompanionv1;

import android.Manifest;
import android.bluetooth.*;
import android.content.*;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;


class BluetoothState{
    private static final String TAG = "SetupActivity";
    private BroadcastReceiver broadcastr1,broadcastr2,broadcastr3,broadcastr4;
    private BluetoothAdapter mBluetoothAdapter;
    private DeviceListAdapter mDeviceListAdapter;
    private ArrayList<BluetoothDevice> mBTDevices;
    private BluetoothDevice btDevice;
    private MainActivity activity;
    private View layout;

    private Button btnDiscover,btnStartConnection,btnONOFF;
    private EditText etMACAddr;
    private ListView lvNewDevices;


    private BluetoothState(){}

    BluetoothState(MainActivity activity) {
        this.activity = activity;
        btnONOFF = (Button) activity.findViewById(R.id.btnONOFF);
        btnDiscover = (Button) activity.findViewById(R.id.btnDiscoverDevices);
        btnStartConnection = (Button) activity.findViewById(R.id.btnStartConnection);
        etMACAddr = (EditText) activity.findViewById(R.id.textMACAddress);
        lvNewDevices = (ListView) activity.findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        activity.registerReceiver( broadcastr4, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        layout = activity.findViewById(R.id.setup_layout);
        initOnClick();
        initBroadcastReceivers();
    }

    public void show(){
        layout.setVisibility(View.VISIBLE);
    }

    public void hide(){
        layout.setVisibility(View.INVISIBLE);
    }

    private void startBTConnection(){
        btDevice = mBluetoothAdapter.getRemoteDevice(etMACAddr.getText().toString());
        Log.d(TAG, "Trying to pair with " + btDevice.getName());
        btDevice.createBond();
        activity.initClient(btDevice);
    }

    private void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        else if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            activity.registerReceiver( broadcastr1, BTIntent);
        }
        else{
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            activity.registerReceiver( broadcastr1, BTIntent);
        }
    }

    private void Discover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            activity.registerReceiver( broadcastr3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){

            mBTDevices.clear();
            if(mDeviceListAdapter != null)
                mDeviceListAdapter.clear();
            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            activity.registerReceiver( broadcastr3, discoverDevicesIntent);
        }
    }

    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = activity.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += activity.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }
        else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    private void initOnClick(){

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                Discover(view);
            }
        });

        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                enableDisableBT();
            }
        });

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBTConnection();
            }
        });

        lvNewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mBluetoothAdapter.cancelDiscovery();
                Log.d(TAG, "onItemClick: You Clicked on a device.");
                Log.d(TAG, "onItemClick: deviceName = " + mBTDevices.get(i).getName());
                Log.d(TAG, "onItemClick: deviceAddress = " + mBTDevices.get(i).getAddress());
                etMACAddr.setText(mBTDevices.get(i).getAddress());
            }
        });
        /*btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = etMACAddr.getText().toString().getBytes(Charset.defaultCharset());
                if(bluetoothClient != null)
                {
                    try{
                        bluetoothClient.write(bytes);
                    }
                    catch(Exception e){
                        Log.d(TAG,"Failed to Write");
                    }
                }
                else
                    Log.d(TAG, "Bluetooth Connection not made yet.");
            }
        });*/
        //obsolete/moved to a different layout and manager
    }

    private void initBroadcastReceivers(){
        broadcastr1 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                    switch(state){
                        case BluetoothAdapter.STATE_OFF:
                            Log.d(TAG, "onReceive: STATE OFF");
                            break;

                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.d(TAG, " broadcastr1: STATE TURNING OFF");
                            break;

                        case BluetoothAdapter.STATE_ON:
                            Log.d(TAG, " broadcastr1: STATE ON");
                            break;

                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.d(TAG, " broadcastr1: STATE TURNING ON");
                            break;

                    }

                }
            }
        };

        broadcastr2 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                    int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                    switch (mode) {
                        //Device is in Discoverable Mode
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                            Log.d(TAG, " broadcastr2: Discoverability Enabled.");
                            break;

                        //Device not in discoverable mode
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                            Log.d(TAG, " broadcastr2: Discoverability Disabled. Able to receive connections.");
                            break;

                        case BluetoothAdapter.SCAN_MODE_NONE:
                            Log.d(TAG, " broadcastr2: Discoverability Disabled. Not able to receive connections.");
                            break;

                        case BluetoothAdapter.STATE_CONNECTING:
                            Log.d(TAG, " broadcastr2: Connecting....");
                            break;

                        case BluetoothAdapter.STATE_CONNECTED:
                            Log.d(TAG, " broadcastr2: Connected.");
                            break;

                    }

                }
            }
        };

        broadcastr3 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                Log.d(TAG, "onReceive: ACTION FOUND.");

                if (action.equals(BluetoothDevice.ACTION_FOUND)){
                    BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                    mBTDevices.add(device);
                    Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                    mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                    lvNewDevices.setAdapter(mDeviceListAdapter);

                }
            }
        };

        broadcastr4 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                    BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //3 cases:
                    //case1: bonded already
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                        Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                        //inside BroadcastReceiver4
                        btDevice = mDevice;

                    }
                    //case2: creating a bone
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                        Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");

                    }
                    //case3: breaking a bond
                    if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                        Log.d(TAG, "BroadcastReceiver: BOND_NONE.");

                    }

                }
            }
        };
    }
}
