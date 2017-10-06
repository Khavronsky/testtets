package com.example.bletest.manager;

import com.example.bletest.DddService;
import com.example.bletest.LightBLEService;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import no.nordicsemi.android.dfu.DfuBaseService;

import static com.example.bletest.manager.ConnectBluetoothService.ACTION_DATA_AVAILABLE;
import static com.example.bletest.manager.ConnectBluetoothService.ACTION_GATT_CONNECTED;
import static com.example.bletest.manager.ConnectBluetoothService.ACTION_GATT_DISCONNECTED;
import static com.example.bletest.manager.ConnectBluetoothService.ACTION_GATT_SERVICES_DISCOVERED;
import static com.example.bletest.manager.ConnectBluetoothService.ON_DESCRIPTOR_WRITE;

/**
 * Created by e.konobeeva on 05.10.2017.
 */

public class StartConnectionServiceManager {
    private static final String TAG = "StartServiceManager";
    private ConnectBluetoothService mService;
    private String macAddress;
    private String deviceName;
    private String filePath;
    private Context mContext;
    private BluetoothGattCharacteristic shishiCharateristic;
    private BluetoothGattCharacteristic shujuCharateristic;

    public StartConnectionServiceManager(String macAddress, Context context, String deviceName, String filePath){

        registerReceiver(context);

        Intent intent = new Intent(context, LightBLEService.class);
        context.bindService(intent, getServiceConnectionCallback(),
                Service.BIND_AUTO_CREATE);

        this.macAddress = macAddress;
        this.deviceName = deviceName;
        this.filePath = filePath;
        mContext = context;
    }

    private ServiceConnection getServiceConnectionCallback(){
        return new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected: ");
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected: ");
                mService = ((ConnectBluetoothService.LocalConnectionBinder) service).getConnectBluetoothService();

                mService.initBluetoothGatt(macAddress);
            }
        };
    }


    private void registerReceiver(Context context){

        BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                if (ACTION_GATT_SERVICES_DISCOVERED.equals(intent.getAction())) {
                    try {
                        Log.d(TAG, "onReceive: ACTION_GATT_SERVICES_DISCOVERED");
                        discoverCharacteristicsFromService();
                    } catch (Exception e) {
                        Log.d(TAG, "ON_RECEIVE: ERROR ERROR");
                    }

                }else if(intent.getAction().equals(ON_DESCRIPTOR_WRITE)){
                    Log.d(TAG, "onReceive: ON_DESCRIPTOR_WRITE");
                    updateOta();

                }else if(intent.getAction().equals(ACTION_GATT_DISCONNECTED)){
                    Log.d(TAG, "onReceive: ACTION_GATT_DISCONNECTED");
                    final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);

                    BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
                    BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddress);
                    updateOta();
                }
            }
        };

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        intentFilter.addAction("com.imagic.connected");

        context.registerReceiver(gattUpdateReceiver, intentFilter);
    }


    protected void discoverCharacteristicsFromService() {
        Log.d(TAG, "discoverCharacteristicsFromService: ");

        List<BluetoothGattService> services = mService.getSupportedGattServices();
        if (services == null) {
            Log.d(TAG, "discoverCharacteristicsFromService: services == null");
            return;
        }
        for (BluetoothGattService service : services) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("fff6")) {


                    shujuCharateristic = characteristic;

                } else if (characteristic.getUuid().toString().contains("fff7")) {

                    shishiCharateristic = characteristic;

                    mService.setCharacteristicNotification(characteristic, true);

                }

            }
        }

    }

    private void updateOta(){
        Log.d(TAG, "updateOta: ");
        if(!TextUtils.isEmpty(deviceName)&&deviceName.toLowerCase().contains("dfu")){
            sendHex(deviceName ,macAddress, filePath, mContext);
        }else{
            sendUpdate();
        }
    }

    public void sendUpdate() {
        Log.d(TAG, "sendUpdate: ");
        byte[] value = new byte[16];
        // value[0] = 0x03;
        // value[1] = 0x01;
        // value[2] = (byte) 0x00;
        // value[3] = 0x10;
        value[0] = (byte) 0x47;
        value[15] = (byte) 0x47;
        shujuCharateristic.setValue(value);
        mService.writeValue(shujuCharateristic);
    }


    private void sendHex(String name,String address, String mFilePath, Context context) {
        Log.d(TAG, "sendHex: ");
        String file="";
        String type="";
        file=mFilePath;

        type=file.substring(file.length()-3);
        //	mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "v0_6_4_b018_jstyle_limited.hex";
        Intent intent = new Intent(context, DddService.class);


        intent.putExtra(DfuBaseService.EXTRA_DEVICE_NAME,name);
        intent.putExtra(DfuBaseService.EXTRA_DEVICE_ADDRESS, address);
        if(type.equals("zip")){
            intent.putExtra(DfuBaseService.EXTRA_FILE_MIME_TYPE, DfuBaseService.MIME_TYPE_ZIP);
        }else{
            intent.putExtra(DfuBaseService.EXTRA_FILE_MIME_TYPE, DfuBaseService.MIME_TYPE_OCTET_STREAM);
        }

        intent.putExtra(DfuBaseService.EXTRA_FILE_PATH, mFilePath);
        intent.putExtra(DfuBaseService.EXTRA_FILE_TYPE, DfuBaseService.TYPE_APPLICATION);
        context.startService(intent);

    }






}
