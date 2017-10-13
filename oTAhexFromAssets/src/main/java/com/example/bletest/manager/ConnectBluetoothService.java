package com.example.bletest.manager;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Created by e.konobeeva on 05.10.2017.
 */

public class ConnectBluetoothService extends Service {
    private static final String TAG = "ConnectBluetoothService";

    private static final UUID NOTIY = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static String ACTION_GATT_CONNECTED = "com.light.ble.service.ACTION_GATT_CONNECTED";
//    public final static String ACTION_GATT_CONNECTING = "com.light.ble.service.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTED = "com.light.ble.service.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.light.ble.service.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.light.ble.service.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.light.ble.service.EXTRA_DATA";
    public final static String ON_DESCRIPTOR_WRITE = "ON_DESCRIPTOR_WRITE";

    public final static String RFSTAR_CHARACTERISTIC_ID = "com.light.ble.service.characteristic"; // 唯一标识

    private BluetoothGatt bluetoothGatt;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothDevice bluetoothDevice;


    private Context mContext;

    @Override
    public IBinder onBind(final Intent intent) {
        Log.d(TAG, "onBind: ");
        mContext = getBaseContext();


        return new LocalConnectionBinder();
    }

    public void initBluetoothGatt(String macAddress){
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        bluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddress);

        bluetoothGatt = bluetoothDevice.connectGatt(mContext, false, getBluetoothGattCallback());

    }

    private static int countDiscoverAttempts = 0;

    private BluetoothGattCallback getBluetoothGattCallback(){
        return new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                Log.d(TAG, "onConnectionStateChange: ");

                if (newState == BluetoothProfile.STATE_CONNECTED) {

                    gatt.discoverServices();
                    sendBroadcestAction(ACTION_GATT_CONNECTED,gatt.getDevice().getAddress());

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                    sendBroadcestAction(ACTION_GATT_DISCONNECTED,gatt.getDevice().getAddress());
                    disconnect();
                }
            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
                super.onServicesDiscovered(gatt, status);
                Log.d(TAG, "onServicesDiscovered: ");

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    sendBroadcestAction(ACTION_GATT_SERVICES_DISCOVERED, null);
                    countDiscoverAttempts=0;
                } else {
                    if(countDiscoverAttempts++<3){
                        gatt.discoverServices();
                    }
                }
            }

            @Override
            public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                Log.d(TAG, "onCharacteristicRead: ");

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastAction(ACTION_DATA_AVAILABLE, characteristic, gatt.getDevice().getAddress());
                }
            }

            @Override
            public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                Log.d(TAG, "onCharacteristicChanged: ");

                broadcastAction(ACTION_DATA_AVAILABLE, characteristic,
                        gatt.getDevice().getAddress());
            }

            @Override
            public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
                super.onDescriptorWrite(gatt, descriptor, status);

                Log.d(TAG, "onDescriptorWrite: ");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    sendBroadcestAction(ON_DESCRIPTOR_WRITE, null);
                }

            }
        };
    }

    public void readValue(BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null) {
            return;
        }

        try {
            Thread.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        bluetoothGatt.readCharacteristic(characteristic);
    }

    public  void writeValue(BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null) {
            return;
        }
        byte[]value=characteristic.getValue();
        if (value == null) {
            return;
        }

        bluetoothGatt.writeCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) {

        Log.d(TAG, "setCharacteristicNotification: ");

        if (bluetoothGatt == null) {
            return;
        }

        bluetoothGatt.setCharacteristicNotification(characteristic, enable);
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(NOTIY);
        if(descriptor==null){
            return;
        }
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        Log.d(TAG, "getSupportedGattServices: ");
        if (bluetoothGatt == null) {
            return null;
        }
        return bluetoothGatt.getServices();
    }

    public void disconnect() {
        Log.d(TAG, "disconnect: ");
        if(bluetoothGatt==null)return;
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
    }

    public void startScan(BluetoothAdapter.LeScanCallback callback){
        Log.d(TAG, "startScan: ");
        mBluetoothAdapter.startLeScan(callback);
    }

    public void stopScan(BluetoothAdapter.LeScanCallback callback){
        Log.d(TAG, "stopScan: ");
        mBluetoothAdapter.stopLeScan(callback);
    }

    private void sendBroadcestAction(String action, String address){
        Intent intent = new Intent(action);

        if(address != null && address.length() > 0){
            intent.putExtra("seraddress", address);
        }

        sendBroadcast(intent);

    }

    private void broadcastAction(String action, BluetoothGattCharacteristic characteristic, String mac) {
        Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
            intent.putExtra(RFSTAR_CHARACTERISTIC_ID, characteristic.getUuid()
                    .toString());
            intent.putExtra("BT-MAC", mac);
        }
        sendBroadcast(intent);
    }

    public class LocalConnectionBinder extends Binder {
        public ConnectBluetoothService getConnectBluetoothService(){
            return ConnectBluetoothService.this;
        }
    }
}
