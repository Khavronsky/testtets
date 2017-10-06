package com.example.bletest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.util.List;

public class RFLampDevice extends Bledevice {

	private BluetoothGattCharacteristic shishiCharateristic;
	private BluetoothGattCharacteristic shujuCharateristic;

	public RFLampDevice(Context context, BluetoothDevice device) {
		// TODO Auto-generated constructor stub
		super(context, device);
		this.device = device;
	}

	/*
	 * ��ʼ������ֵ �ӷ�����ɨ������ֵ
	 */

	private static final String TAG = "RFLampDevice";
	@Override
	protected void discoverCharacteristicsFromService() {
		Log.d(TAG, "discoverCharacteristicsFromService: ");
		// ���޸�
		if (bleService == null || device == null) {

			return;
		}
		List<BluetoothGattService> services = bleService
				.getSupportedGattServices(this.device);
		if (services == null) {
			return;
		}
		for (BluetoothGattService service : services) {
			for (BluetoothGattCharacteristic characteristic : service
					.getCharacteristics()) {
				if (characteristic.getUuid().toString().contains("fff6")) {
					shujuCharateristic = characteristic;
				} else if (characteristic.getUuid().toString().contains("fff7")) {
					// if(deviceName.toLowerCase().contains("dfu")){
					//
					// }else{

					shishiCharateristic = characteristic;// ����֪ͨ;
					this.setCharacteristicNotification(characteristic, true);
					// }
				}
//				 if (characteristic.getUuid().toString()
//				 .equals("6e400002-b5a3-f393-e0a9-e50e24dcca9e")) {
//				 shujuCharateristic = characteristic;
//				 } else if (characteristic.getUuid().toString()
//				 .contains("6e400003-b5a3-f393-e0a9-e50e24dcca9e")) {
//				 shishiCharateristic = characteristic;// 锟斤拷锟斤拷通知;
//				 this.setCharacteristicNotification(characteristic, true);
//				 }

			}
		}

	}

	public void checkUpdate() {
		byte[] value = new byte[16];
		value[0] = 0x27;
		for (int i = 0; i < 15; i++) {
			value[15] += value[i];
		}
		shujuCharateristic.setValue(value);
		this.writeValue(shujuCharateristic);
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
		System.out.println("��������" + Tools.byte2Hex(value));
		shujuCharateristic.setValue(value);
		this.writeValue(shujuCharateristic);
	}

	public void total(String day) {
		if (shujuCharateristic == null)
			return;
		byte[] value = new byte[16];
		value[0] = 0x07;
		value[1] = (byte) bcd(day);
		for (int i = 2; i < 15; i++) {
			value[i] = 0;
		}
		int s = 0;
		for (int i = 0; i < 15; i++) {
			s += value[i];
		}
		value[15] = (byte) s;

		shujuCharateristic.setValue(value);
		this.writeValue(shujuCharateristic);
	}

	public void goal(String day) {
		if (shujuCharateristic == null)
			return;
		byte[] value = new byte[16];
		value[0] = 0x08;
		value[1] = (byte) bcd(day);
		for (int i = 2; i < 15; i++) {
			value[i] = 0;
		}
		int s = 0;
		for (int i = 0; i < 15; i++) {
			s += value[i];
		}
		value[15] = (byte) s;
		shujuCharateristic.setValue(value);
		this.writeValue(shujuCharateristic);
	}

	public void SportDetali(String day) {
		if (shujuCharateristic == null) {
			System.out.println("shujuCharateristic is null");
			return;
		}

		byte[] value = new byte[16];
		value[0] = 0x43;
		value[1] = (byte) bcd(day);
		for (int i = 2; i < 15; i++) {
			value[i] = 0;
		}
		int s = 0;
		for (int i = 0; i < 15; i++) {
			s += value[i];
		}
		value[15] = (byte) s;
		shujuCharateristic.setValue(value);
		this.writeValue(shujuCharateristic);
	}

	public byte Crc(byte[] value) {
		byte c = 0;
		for (int i = 0; i < 15; i++) {
			c += value[i];
		}
		return c;
	}

	public int bcd(String s) {

		Integer i = Integer.parseInt(s, 16);
		return i;
	}

	public void jibu() {
		if (shujuCharateristic == null) {
			System.out.println("shujuCharateristic is null");
			return;
		}

		byte[] value = new byte[16];
		value[0] = 0x09;

		for (int i = 1; i < 15; i++) {
			value[i] = 0;
		}

		value[15] = 0x09;
		System.out.println("ʵʱ�ǲ�");
		shujuCharateristic.setValue(value);
		this.writeValue(shujuCharateristic);

	}
	public void McuReset(){
		byte[]value=new byte[16];
		value[0]=0x2e;
		value[15]=0x2e;
		shujuCharateristic.setValue(value);
		this.writeValue(shujuCharateristic);
	}
	public void FactoryReset(){
		byte[]value=new byte[16];
		value[0]=0x12;
		value[15]=0x12;
		shujuCharateristic.setValue(value);
		this.writeValue(shujuCharateristic);
	}
}
