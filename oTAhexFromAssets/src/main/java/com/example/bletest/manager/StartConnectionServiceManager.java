package com.example.bletest.manager;

import com.example.bletest.DddService;
import com.example.bletest.LightBLEService;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;

import no.nordicsemi.android.dfu.DfuBaseService;

import static com.example.bletest.manager.ConnectBluetoothService.ACTION_GATT_CONNECTED;
import static com.example.bletest.manager.ConnectBluetoothService.ACTION_GATT_DISCONNECTED;
import static com.example.bletest.manager.ConnectBluetoothService.ACTION_GATT_SERVICES_DISCOVERED;
import static com.example.bletest.manager.ConnectBluetoothService.ON_DESCRIPTOR_WRITE;
import static no.nordicsemi.android.dfu.DfuBaseService.BROADCAST_LOG;
import static no.nordicsemi.android.dfu.DfuBaseService.EXTRA_DATA;
import static no.nordicsemi.android.dfu.DfuBaseService.EXTRA_ERROR_TYPE;
import static no.nordicsemi.android.dfu.DfuBaseService.EXTRA_LOG_LEVEL;
import static no.nordicsemi.android.dfu.DfuBaseService.EXTRA_LOG_MESSAGE;

/**
 * Created by e.konobeeva on 05.10.2017.
 */

public class StartConnectionServiceManager {
    private static final String TAG = "StartServiceManager";

    private ConnectBluetoothService mService;
    private String macAddress;
    private String filePath;
    private Context mContext;
    private BluetoothGattCharacteristic shishiCharateristic;
    private BluetoothGattCharacteristic shujuCharateristic;
    private ServiceConnection serviceConnection;
    private BluetoothAdapter.LeScanCallback mScanCallback;
    private static boolean isFirstSendCmd = true;
    private static boolean isFirstSendHex = true;

    public StartConnectionServiceManager(String macAddress, Context context, String filePath){

        Log.d(TAG, "StartConnectionServiceManager: ");

        this.macAddress = macAddress;
        this.filePath = filePath;
        mContext = context;


        registerReceiver(context);
        registerReceiverForUpdateOta();

        Intent intent = new Intent(context, ConnectBluetoothService.class);
        boolean flag = context.bindService(intent, getServiceConnectionCallback(),
                Service.BIND_AUTO_CREATE);
        Log.d(TAG, "StartConnectionServiceManager: " + flag);


    }

    private void registerReceiverForUpdateOta() {
        Log.d(TAG, "registerReceiverForUpdateOta: ");
        IntentFilter filter = new IntentFilter();
        filter.addAction(DfuBaseService.BROADCAST_PROGRESS);
        filter.addAction(DfuBaseService.BROADCAST_ERROR);
        filter.addAction(ON_DESCRIPTOR_WRITE);
        filter.addAction(LightBLEService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(LightBLEService.ACTION_GATT_DISCONNECTED);
        filter.addAction(LightBLEService.ACTION_DATA_AVAILABLE);
        filter.addAction(BROADCAST_LOG);

        final BroadcastReceiver mDfuUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                final String action = intent.getAction();
                Log.d(TAG, "onReceive: 222 " + action);
                if (DfuBaseService.BROADCAST_PROGRESS.equals(action)) {

                    final int progress = intent.getIntExtra(EXTRA_DATA, 0);
                    final int currentPart = intent.getIntExtra(DfuBaseService.EXTRA_PART_CURRENT, 1);
                    final int totalParts = intent.getIntExtra(DfuBaseService.EXTRA_PARTS_TOTAL, 1);

                    Log.d(TAG, "onReceive: progress=" + progress);
                    Log.d(TAG, "onReceive: currentPart=" + currentPart);
                    Log.d(TAG, "onReceive: totalParts=" + totalParts);


                } else if (DfuBaseService.BROADCAST_ERROR.equals(action)) {

                    int data = intent.getIntExtra(EXTRA_DATA, -1);
                    int errType = intent.getIntExtra(EXTRA_ERROR_TYPE, -3);

                    Log.d(TAG, "onReceive: data=" + data);
                    Log.d(TAG, "onReceive: errType=" + errType);

                } else if (ON_DESCRIPTOR_WRITE.equals(action)) {

                } else if (LightBLEService.ACTION_GATT_DISCONNECTED.equals(action)) {

                } else if (LightBLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                } else if (LightBLEService.ACTION_DATA_AVAILABLE.equals(action)) {

                }else if(BROADCAST_LOG.equals(intent.getAction())){

                    String fullMessage = intent.getStringExtra(EXTRA_LOG_MESSAGE);
                    int level = intent.getIntExtra(EXTRA_LOG_LEVEL, -1);
                    Log.d(TAG, "onReceive: fullMessage=" + fullMessage);
                    Log.d(TAG, "onReceive:level= " + level);

                }
            }
        };

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mDfuUpdateReceiver, filter);
    }

    private ServiceConnection getServiceConnectionCallback(){
        Log.d(TAG, "getServiceConnectionCallback: ");

        if(serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
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

        return serviceConnection;
    }

    private void registerReceiver(Context context){
        Log.d(TAG, "registerReceiver: ");
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
                    sendUpdateCmd();

                }else if(intent.getAction().equals(ACTION_GATT_DISCONNECTED)){
                    Log.d(TAG, "onReceive: ACTION_GATT_DISCONNECTED");
                    mService.startScan(getScanCallback());
                }
            }
        };

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ON_DESCRIPTOR_WRITE);
        intentFilter.addAction("com.imagic.connected");

        context.registerReceiver(gattUpdateReceiver, intentFilter);
    }

    private void discoverCharacteristicsFromService() {
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

    private BluetoothAdapter.LeScanCallback getScanCallback(){
        if(mScanCallback == null){
            mScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int arg1, byte[] arg2) {
                    if(device.getName() != null && device.getName().toLowerCase().contains("dfu")){
                        ((TestManegerActivity)mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "run: ");
                                sendHex(device.getName(), device.getAddress(), filePath, mContext);
                            }
                        });
                    }

                }

            };
        }
        return mScanCallback;
    }

    private void sendUpdateCmd() {
        if(isFirstSendCmd) {
            isFirstSendCmd = false;
            Log.d(TAG, "sendUpdateCmd: ");
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
    }


    private void sendHex(String name,String address, String mFilePath, Context context) {
        Log.d(TAG, "sendHex: ");
        if(isFirstSendHex) {
            isFirstSendHex = false;

            mService.stopScan(getScanCallback());

            String type = filePath.substring(filePath.length() - 3);

            Intent intent = new Intent(context, DddService.class);
            intent.putExtra(DfuBaseService.EXTRA_DEVICE_NAME, name);
            intent.putExtra(DfuBaseService.EXTRA_DEVICE_ADDRESS, address);

            if (type.equals("zip")) {
                intent.putExtra(DfuBaseService.EXTRA_FILE_MIME_TYPE, DfuBaseService.MIME_TYPE_ZIP);
            } else {
                intent.putExtra(DfuBaseService.EXTRA_FILE_MIME_TYPE, DfuBaseService.MIME_TYPE_OCTET_STREAM);
            }

            intent.putExtra(DfuBaseService.EXTRA_FILE_PATH, mFilePath);
            intent.putExtra(DfuBaseService.EXTRA_FILE_TYPE, DfuBaseService.TYPE_APPLICATION);
            context.startService(intent);
        }

    }






}
