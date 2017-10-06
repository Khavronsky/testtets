package com.example.bletest;

public class SendData {

	public static byte[] updateData(){
		byte[]value=new byte[4];
		value[0]=1;
		value[1]=1;
		value[2]=0;
		value[3]=0x10;
		return value;
	}
}
