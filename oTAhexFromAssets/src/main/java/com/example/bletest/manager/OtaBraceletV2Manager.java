package com.example.bletest.manager;

import com.example.bletest.DddService;
import com.example.bletest.LightBLEService;
import com.example.bletest.RFLampDevice;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import no.nordicsemi.android.dfu.DfuBaseService;

/**
 * Created by e.konobeeva on 04.10.2017.
 */

public class OtaBraceletV2Manager {
    private static final String TAG = "OtaBraceletV2Manager";
    public enum OtaManagerResponse{

    }
    public interface IResultListener {
        void onOtaManagerResponse(OtaManagerResponse response);
    }

    private IResultListener resultListener;
    private Context mContext;
    private RFLampDevice device;

    public void startUpdateOta(String otaFilePath, Context context, String deviceMacAddress){
        Log.d(TAG, "startUpdateOta: ");

        mContext = context;


        BluetoothDevice bluetoothDevice = getRemoteDeviceByMacAddress(deviceMacAddress);
        device = new RFLampDevice(context, bluetoothDevice);
//        registerReceiverForUpdateOta();
//        tryUpdate(device, otaFilePath, context);



    }

    private BluetoothDevice getRemoteDeviceByMacAddress(String macAddress){
        Log.d(TAG, "getRemoteDeviceByMacAddress: ");

        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddress);

        return mBluetoothAdapter.getRemoteDevice(macAddress);
    }

    private void tryUpdate(RFLampDevice device, String filePath, Context context){
        Log.d(TAG, "tryUpdate: ");
        if(!TextUtils.isEmpty(device.deviceName)&&device.deviceName.toLowerCase().contains("dfu")){
            sendHex(device.deviceName ,device.deviceMac, filePath, context);
        }else{
            if (device == null){
                return;
            }else{
                device.sendUpdate();
            }
        }
    }


    private void registerReceiverForUpdateOta(){
        Log.d(TAG, "registerReceiverForUpdateOta: ");

        IntentFilter filter = new IntentFilter();
        filter.addAction(DfuBaseService.BROADCAST_PROGRESS);
        filter.addAction(DfuBaseService.BROADCAST_ERROR);
        filter.addAction("onDescriptorWrite");
        filter.addAction(LightBLEService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(LightBLEService.ACTION_GATT_DISCONNECTED);
        filter.addAction(LightBLEService.ACTION_DATA_AVAILABLE);

        LocalBroadcastManager broadcastManager = android.support.v4.content.LocalBroadcastManager.getInstance(mContext);
        broadcastManager.registerReceiver(mDfuUpdateReceiver, filter);
        mContext.registerReceiver(mDfuUpdateReceiver, filter);
    }


    /**
     * нужен контекст для intent и service
     * @param name
     * @param address
     * @param mFilePath
     * @param context
     */
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
//        intent.putExtra(DfuBaseService.EXTRA_FILE_URI, );
        context.startService(intent);

    }

    public void stopWorking(){

        final LocalBroadcastManager manager= LocalBroadcastManager.getInstance(mContext);
        manager.unregisterReceiver(mDfuUpdateReceiver);
        mContext.unregisterReceiver(mDfuUpdateReceiver);

        if(device==null)return;
        device.disconnectedDevice();

    }


    private final BroadcastReceiver mDfuUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            // DFU is in progress or an error occurred
            final String action = intent.getAction();

            if (DfuBaseService.BROADCAST_PROGRESS.equals(action)) {

                final int progress = intent.getIntExtra(
                        DfuBaseService.EXTRA_DATA, 0);
                final int currentPart = intent.getIntExtra(
                        DfuBaseService.EXTRA_PART_CURRENT, 1);
                final int totalParts = intent.getIntExtra(
                        DfuBaseService.EXTRA_PARTS_TOTAL, 1);




            } else if (DfuBaseService.BROADCAST_ERROR.equals(action)) {
                final int error = intent.getIntExtra(DfuBaseService.EXTRA_DATA,
                        0);




                // We have to wait a bit before canceling notification. This is
                // called before DfuService creates the last notification.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // if this activity is still open and upload process was
                        // completed, cancel the notification
                        final NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.cancel(DfuBaseService.NOTIFICATION_ID);
                    }
                }, 200);
            } else if ("onDescriptorWrite".equals(action)) {
                boolean isOtaSuccess = true;
                if(isOtaSuccess){

                    isOtaSuccess=false;
                }else{
                    //					startUpdate();
                    //					dialog.show();
                    //					handler.postDelayed(new Runnable() {
                    //
                    //						@Override
                    //						public void run() {
                    //							// TODO Auto-generated method stub
                    //							dialog.cancel();
                    //						}
                    //					}, 20000);
                }
                //	checkUpDate();
            } else if (LightBLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                boolean isReset = false;
                if(!isReset){
//                    startScan(true);
                }

            }else if(LightBLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){

            }else if(LightBLEService.ACTION_DATA_AVAILABLE.equals(action)){

            }
        }
    };




}
