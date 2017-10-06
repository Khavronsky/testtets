package com.example.bletest;

	import java.util.Calendar;
	import java.util.List;





	import android.app.Activity;
	import android.app.Service;
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
import android.util.Log;
import android.widget.Toast;

	/*
	 * �����豸�Ļ���
	 *         ���ܣ�
	 *           1�������豸����
	 *           2����ȡ�豸����
	 *           3���������񣬶Ͽ���
	 *           4����ȡ����
	 *           5�����ӹ㲥������
	 *           6�����ݼ���
	 *           7������
	 *           
	 * 
	 * 
	 */

		public abstract class Bledevice {
		Intent serviceIntent;
		protected static final byte[] CRCPASSWORD = { 'C', 'h', 'e', 'c', 'k', 'A',
				'e', 's' };
		protected Context context = null;
		public String deviceName = null, deviceMac = null;
		protected LightBLEService bleService = null;
		public BluetoothDevice device = null;
		//public RFStarBLEBroadcastReceiver delegate = null;
		public int bleDeviceType; // ������������

		public Bledevice(Context context, BluetoothDevice device) {
			Log.d(TAG, "Bledevice: ");
			this.device = device;
			this.deviceName = this.device.getName();
			this.deviceMac = this.device.getAddress();
			this.context = context;
			this.registerReceiver();
			if (serviceIntent == null) {
				serviceIntent = new Intent(this.context, LightBLEService.class);
				this.context.bindService(serviceIntent, serviceConnection,
						Service.BIND_AUTO_CREATE);
			}
		}
		/**
		 * �Ƿ�����
		 * 
		 * @return
		 */
		public boolean isConnected() {
			// return gatt.connect();
			return true;
		}

		/**
		 * ���ӷ���
		 */
		private ServiceConnection serviceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected: ");
				// TODO Auto-generated method stub
				bleService = null;
				MyLog.e(" gatt is not init");
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected: ");
				bleService = ((LightBLEService.LocalBinder) service)
						.getService();
				
				bleService.initBluetoothDevice(device,context);
				//bleService.initBluetoothDevice(device);
				MyLog.i(" gatt is  init");
			}
		};
		public void reConnected(){
			bleService.initBluetoothDevice(device,context);
		}
		/**
		 * ��ȡ����ֵ
		 * 
		 * @param characteristic
		 */
		public void readValue(BluetoothGattCharacteristic characteristic) {
			if (characteristic == null) {
				MyLog.e(
						"readValue characteristic is null");
			} else {
				bleService.readValue(this.device, characteristic);
			}
		}

		/**
		 * ��������ֵд������
		 * 
		 * @param characteristic
		 */
		public void writeValue(BluetoothGattCharacteristic characteristic) {
			Log.d(TAG, "writeValue: ");
			if (characteristic == null) {
				MyLog.e(
						"writeValue characteristic is null");
			} else {
				if(bleService==null)return;
				bleService.writeValue(this.device, characteristic);
			}
		}

		/**
		 * ��Ϣʹ��
		 * 
		 * @param characteristic
		 * @param enable
		 */
		public void setCharacteristicNotification(
				BluetoothGattCharacteristic characteristic, boolean enable) {
			if (characteristic == null) {
				MyLog.e(
						"Notification characteristic is null");
			} else {
				MyLog.i(
						"set Notification ");
				bleService.setCharacteristicNotification(this.device,
						characteristic, enable);
			}
		}

		/**
		 * �Ͽ�����
		 */
		public void disconnectedDevice() {
			Log.d(TAG, "disconnectedDevice: ");
			this.ungisterReceiver();
			if(serviceConnection != null)
			this.context.unbindService(serviceConnection);
		}
		public void disconnectedDevice(String address) {
			//try {
				//if(gattUpdateRecevice.)
				//activity.unregisterReceiver(gattUpdateRecevice);
			
				this.bleService.disconnect(address);
				//this.context.unbindService(serviceConnection);
				 this.device=null;
				//bleService = null;
//			} catch (Exception e) {
//				System.out.println(e.getStackTrace());
//			}
			
		}
		public void disconnectedDevices() {
			Activity activity = (Activity) this.context;
			try {	
				this.bleService.disconnect();
				 this.device=null;			
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}

		public void closeDevice() {
			this.ungisterReceiver();
			this.context.unbindService(serviceConnection);
		}
		
	
		/**
		 * ��ȡ����
		 * 
		 * @return
		 */
//		public List<BluetoothGattService> getBLEGattServices() {
//			return this.bleService.getSupportedGattServices(this.device);
//		}

		/**
		 * ���ӹ㲥������
		 * 
		 * @return
		 */
		protected IntentFilter bleIntentFilter() {
			final IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(LightBLEService.ACTION_GATT_CONNECTED);
			intentFilter.addAction(LightBLEService.ACTION_GATT_DISCONNECTED);
			intentFilter
					.addAction(LightBLEService.ACTION_GATT_SERVICES_DISCOVERED);
			intentFilter.addAction(LightBLEService.ACTION_DATA_AVAILABLE);
			intentFilter.addAction(LightBLEService.ACTION_GAT_RSSI);
			intentFilter.addAction(LightBLEService.ACTION_GATT_CONNECTING);
			//�Զ�������
			intentFilter.addAction("com.imagic.connected");
			return intentFilter;
		}
		/**
		 * ע����������豸���������ݵģ��㲥
		 * 
		 * @param context
		 * @param delegate
		 * @param filter
		 */
		public void registerReceiver() {
			Log.d(TAG, "registerReceiver: ");
			Activity activity = (Activity) this.context;
			activity.registerReceiver(gattUpdateRecevice, this.bleIntentFilter());

		}

		/**
		 * ע�������������صĹ㲥
		 */
		public void ungisterReceiver() {
			Activity activity = (Activity) this.context;
			activity.unregisterReceiver(gattUpdateRecevice);
			bleService.disconnect();
			bleService = null;
		}

		/**
		 * ��ʼ�������е�����
		 */
		protected abstract void discoverCharacteristicsFromService();
		/**
		 * ���������㲥
		 */
		private static final String TAG = "Bledevice";
		private BroadcastReceiver gattUpdateRecevice = new BroadcastReceiver() {
			@Override
			public void onReceive(final Context context, Intent intent) {
				 if (LightBLEService.ACTION_GATT_SERVICES_DISCOVERED
						.equals(intent.getAction())) {
					 try {
						 Log.d(TAG, "onReceive: ");
						 discoverCharacteristicsFromService();
					} catch (Exception e) {
						 Log.d(TAG, "ON_RECEIVE: ERROR ERROR");
						// TODO: handle exception
					}
					
				} else {
					 Log.d(TAG, "ON_RECEIVE: NOT ACTION");
					 Log.d(TAG, "onReceive:intent.getAction() " + intent.getAction());


				 }
			}


		};
		
	
	}


